package strategy;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.*;
import core.MaskParser;
import core.Mutant;
import core.Mutator;
import core.Strategy;
import parser.parsinghandle.ParsingHandler;
import storage.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/*
 * Fill-mask strategy:
 * This strategy will mutate everything as possible by using masks
 * e.g: ...int i = 1;...
 *      > ...int <mask> = 1;...
 *      > ...int i <mask> 1;...
 *      > ...int i = <mask>;...
 * It requires a fill-mask model, such as CodeBERT
 * This model must be converted to ONNX format before using it
 * This can be done with the command optimum-cli
 */
public class StrategyFillMask extends Strategy
{
    // The token for the mask, e.g: <mask>
    private String mask;

    // The token for the class, e.g: <s>
    private String cls;

    // The token for the separator, e.g: </s>
    private String sep;

    // The maximum number of tokens that can be provided to the model, e.g: 512
    private int maxTokens;

    // The path name to the model, e.g: "~/codebert-base-onnx"
    private String pathToModel;

    // The file name of the model, e.g: "model.onnx"
    private String modelName;

    // The file name of the tokenizer json, e.g: "tokenizer.json"
    private String tokenizerName;

    // Tokenizer only used for centering the mask
    private HuggingFaceTokenizer tokenizerForCenteringMask;

    // Tokenizer for tokenizing the code
    private HuggingFaceTokenizer tokenizer;

    // For the environment initialization of ONNX
    OrtEnvironment env;
    OrtSession.SessionOptions opts;
    OrtSession session;

    public StrategyFillMask(Builder b)
    {
        this.mask = b.mask;
        this.cls = b.cls;
        this.sep = b.sep;
        this.maxTokens = b.maxTokens;
        this.pathToModel = b.pathToModel;
        this.modelName = b.modelName;
        this.tokenizerName = b.tokenizerName;

        try {
            this.tokenizerForCenteringMask = HuggingFaceTokenizer.builder()
                    .optTokenizerPath(Paths.get(pathToModel, tokenizerName))
                    .optMaxLength(3)
                    .build();

            this.tokenizer = HuggingFaceTokenizer.builder()
                    .optTokenizerPath(Paths.get(pathToModel, tokenizerName))
                    .optMaxLength(this.maxTokens)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try { setEnvironment(); }
        catch (OrtException e) { throw new RuntimeException(e); }

        for(MaskParser parser: Mutator.getInstance().getParsers())
            parser.setStrategy(this);
    }

    /**
     * Initializes once the environment
     * (otherwise the memory of the program will get bigger and bigger and your computer will crash :( )
     */
    public void setEnvironment() throws OrtException
    {
        env = OrtEnvironment.getEnvironment();
        opts = new OrtSession.SessionOptions();
        session = env.createSession(Paths.get(this.pathToModel, this.modelName).toString(), opts);
    }

    public String getMask() {
        return mask;
    }

    public String getSep() {
        return sep;
    }

    public String getCls() {
        return cls;
    }

    /**
     * Returns the code to be mutated corresponding to a maximum of maxTokens tokens centered around the mask
     * In toMutate, there must be the mask (e.g: "... <mask> ...")
     */
    public String centerTheMask(String toMutate)
    {
        // I) Extract every token
        List<String> _tokens = new ArrayList<>();           // Will contain the tokens of the code
        List<String> _translation = new ArrayList<>();      // Translation of the token to string
        int kk = 0;

        while(!toMutate.isEmpty())
        {
            // 1) Get the first token of the code
            String[] tokens = tokenizerForCenteringMask.encode(toMutate).getTokens();

            // 2) Remove CLS and SEP from the token list
            tokens = Arrays.copyOfRange(tokens, 1, tokens.length - 1);

            // 3) Add the token to the list
            _tokens.add(tokens[0]);

            // 4) Decode it to only have the string
            String tmp = tokenizerForCenteringMask.decode(tokenizerForCenteringMask.encode(toMutate).getIds());

            if(tokens[0].trim().equals(this.mask))
            {
                _translation.add(tokens[0]);
                toMutate = toMutate.substring(tokens[0].length());
            }
            else
            {
                // 5) Remove the CLS and the SEP
                tmp = tmp.substring(this.cls.length(), tmp.length() - this.sep.length());

                // 6) Add it to the array
                _translation.add(tmp);
                toMutate = toMutate.substring(tmp.length());
            }
        }

        // II) Find the position of the mask
        int maskIndex = 0;
        for(String s: _translation)
        {
            if(s.trim().equals(this.mask)) break;
            maskIndex++;
        }

        int start_index = Math.max(0, maskIndex - this.maxTokens/2);
        int stop_index = Math.min(_translation.size(), maskIndex + this.maxTokens/2) - 1;

        String res = "";

        for(int i = start_index; i <= stop_index; i++)
            res += _translation.get(i);

        return res;
    }

    @Override
    public FileInfo mutate(File fileToMutate)
    {
        FileInfo fileInfo = new FileInfo();
        fileInfo.fileName = fileToMutate.getName();
        fileInfo.pathName = fileToMutate.getAbsolutePath();
        fileInfo.language = "Java";
        fileInfo.strategy = this.getClass().getName();

        try
        {
            String codeToMutate = Files.readString(fileToMutate.toPath());

            // 1) Initialize
            /** The environment has already been initialized in the constructor **/

            // Generate every variant of the code with masks
            ArrayList<ClassInfo> classes = Mutator.getInstance().getFileHandler().generateVariants(fileToMutate, Mutator.getInstance().getParsers());
            if(classes == null) return null;

            // Browse every mask info to generate the whole code masked
            for(ClassInfo cl: classes)
            {
                for(MethodInfo me: cl.methods)
                {
                    for(StatementInfo st: me.statements)
                    {
                        for(MaskingInfo ma: st.maskingInfos)
                        {
                            String toMutate =
                                    codeToMutate.substring(0,ma.position.beginIndex)
                                    + mask
                                    + codeToMutate.substring(ma.position.endIndex+1);

                            // Center the mask
                            String toMutateCenter = centerTheMask(toMutate);
                            Encoding encoding = tokenizer.encode(toMutateCenter);

                            long[] inputIds = encoding.getIds();
                            long[] attentionMask = encoding.getAttentionMask();
                            long[] typeIds = encoding.getTypeIds(); // Add for Bert

                            // Determining the mask index
                            int maskIndex = 0;
                            for (String s : encoding.getTokens()) {
                                if (s.trim().equals(this.mask)) break;
                                maskIndex++;
                            }

                            // 2) Prepare inputs (batch size = 1)
                            OnnxTensor idsTensor = OnnxTensor.createTensor(env, new long[][]{inputIds});
                            OnnxTensor maskTensor = OnnxTensor.createTensor(env, new long[][]{attentionMask});
                            OnnxTensor typeTensor = OnnxTensor.createTensor(env, new long[][]{typeIds});
                            Map<String, OnnxTensor> inputs = Map.of(
                                    "input_ids", idsTensor,
                                    "attention_mask", maskTensor
                                    //"token_type_ids", typeTensor // Add for Bert
                            );

                            try {
                                OrtSession.Result result = session.run(inputs);
                                // The first (and only) output is [batch, seq_len, vocab_size]

                                float[][][] logits = (float[][][]) result.get(0).getValue();
                                float[] maskLogits = logits[0][maskIndex];

                                // 2. Map containing mutation:score, e.g: ["+":0.6, " +":0.2, "-":0.1, ...]
                                LinkedHashMap<String, Double> preds = getTopKMutations(maskLogits, 5);

                                int k = 5;
                                PriorityQueue<Integer> pq = new PriorityQueue<>(
                                        Comparator.comparingDouble(i -> -maskLogits[i])
                                );
                                for (int i = 0; i < maskLogits.length; i++) pq.add(i);
                                List<Integer> topIds = IntStream.range(0, k)
                                        .mapToObj(_i -> pq.poll())
                                        .collect(Collectors.toList());

                                for (int id : topIds) {
                                    String mutation = "";
                                    String score = "";

                                    long[] newIds = inputIds.clone();
                                    newIds[maskIndex] = id;
                                    String decoded = tokenizer.decode(newIds);

                                    for (HashMap.Entry<String, Double> entry : preds.entrySet()) {
                                        String key = entry.getKey();
                                        double value = entry.getValue();

                                        mutation = key;
                                        score = String.valueOf(value);

                                        preds.remove(key);
                                        break;
                                    }
                                    PredictionInfo prediction = new PredictionInfo();
                                    prediction.statementBefore = codeToMutate.substring(st.position.beginIndex, st.position.endIndex+1);
                                    prediction.statementAfter = codeToMutate.substring(st.position.beginIndex, ma.position.beginIndex)
                                            + mutation
                                            + codeToMutate.substring(ma.position.endIndex+1, st.position.endIndex+1);
                                    prediction.tokenPredicted = mutation;
                                    prediction.metrics.put("SoftMax", score);
                                    ma.predictions.add(prediction);
                                }
                            }
                            catch (OrtException e)
                            {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
            fileInfo.classes = classes;
        }
        catch (Exception e) {
            return fileInfo;
            //throw new RuntimeException(e);
        }
        return fileInfo;
    }

    /**
     * ==> PUT IT AS A METRIC
     * Computes the softmax for the generated mutant
     * Reminder: for (x1,...,xn) as input, softmax(x1,...,xn) = (y1,...,yn)
     * where yi = exp(xi)/[exp(x1) + ... + exp(xn)], for 1 <= i <= n
     */
    public static double[] softmax(float[] logits)
    {
        double sumExp = 0.0;
        double[] exps = new double[logits.length];
        for (int i = 0; i < logits.length; i++) {
            exps[i] = Math.exp(logits[i]);
            sumExp += exps[i];
        }
        double[] probs = new double[logits.length];
        for (int i = 0; i < logits.length; i++) {
            probs[i] = exps[i] / sumExp;
        }
        return probs;
    }

    /**
     * Returns the top k mutations (format: ["mutation1":softmax, "mutation2":softmax, ...])
     */
    public LinkedHashMap<String, Double> getTopKMutations(float[] maskLogits, int topK)
    {
        double[] probs = softmax(maskLogits);
        // PriorityQueue on probabilities
        PriorityQueue<Integer> pq = new PriorityQueue<>(
                Comparator.comparingDouble(i -> -probs[i])
        );
        for (int i = 0; i < probs.length; i++)
        {
            pq.add(i);
        }

        LinkedHashMap<String, Double> result = new LinkedHashMap<String, Double>();
        for (int i = 0; i < topK; i++)
        {
            int id = pq.poll();
            // Decoding only one token
            String tok = tokenizer.decode(new long[]{id});
            result.put(tok, probs[id]);
        }

        for (HashMap.Entry<String, Double> entry : result.entrySet())
        {
            String key = entry.getKey();
            double value = entry.getValue();
        }
        return result;
    }





    /** BUILDER PART **/
    public static class Builder {
        private String mask = "<mask>";
        private String cls = "<s>";
        private String sep = "</s>";
        private int maxTokens = 512;

        private String pathToModel = "";
        private String modelName = "model.onnx";
        private String tokenizerName = "tokenizer.json";

        private MaskParser parser;

        private HuggingFaceTokenizer tokenizerForCenteringMask;
        private HuggingFaceTokenizer tokenizer;
        OrtEnvironment env;
        OrtSession.SessionOptions opts;
        OrtSession session;

        public Builder setMask(String mask) {
            this.mask = mask;
            return this;
        }

        public Builder setCls(String cls) {
            this.cls = cls;
            return this;
        }

        public Builder setSep(String sep) {
            this.sep = sep;
            return this;
        }

        public Builder setMaxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder setPathToModel(String pathToModel) {
            this.pathToModel = pathToModel;
            return this;
        }

        public Builder setModelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        public Builder setTokenizerName(String tokenizerName) {
            this.tokenizerName = tokenizerName;
            return this;
        }

        public Builder setParser(MaskParser parser)
        {
            this.parser = parser;
            return this;
        }

        public StrategyFillMask build()
        {
            return new StrategyFillMask(this);
        }
    }
}