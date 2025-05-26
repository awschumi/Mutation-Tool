package strategy;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.*;
import core.*;
import export.JsonExport;
import parser.parsinghandle.ParsingHandler;
import storage.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Future;
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
                    .optTruncation(false)   // No limitation to get the tokens and ids
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
        opts.setInterOpNumThreads(1);
        //opts.setIntraOpNumThreads(1);
        //opts.setExecutionMode(OrtSession.SessionOptions.ExecutionMode.PARALLEL);
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
     * Requires a lot of computation and resources, so compute it only once for the same file
     * @param toMutate
     * @return
     */
    private TemporaryStorage codeToTokens(String toMutate)
    {
        TemporaryStorage result = new TemporaryStorage();

        // I) Extract every token
        String[] tokens = this.tokenizerForCenteringMask.encode(toMutate).getTokens();
        tokens = Arrays.copyOfRange(tokens, 1, tokens.length-1);
        long[] ids = this.tokenizerForCenteringMask.encode(toMutate).getIds();
        ids = Arrays.copyOfRange(ids, 1, ids.length-1);

        result.tokens = tokens;
        result.ids = ids;
        return result;
    }

    /**
     * Returns the code to be mutated corresponding to a maximum of maxTokens tokens centered around the mask
     * In toMutate, there must be the mask (e.g: "... <mask> ...")
     */
    public String centerTheMask(String toMutate)
    {
        //System.out.println("CENTER THE MASK");
        // I)
        TemporaryStorage tmpResult = codeToTokens(toMutate);
        String[] _tokens = tmpResult.tokens;
        long[] _ids = tmpResult.ids;

        // II) Find the position of the mask
        int maskIndex = 0;
        for(String s: _tokens)
        {
            if(s.trim().equals(this.mask)) break;
            maskIndex++;
        }
        //System.out.println("POSITION: " + maskIndex + "/" + _tokens.length);

        int start_index = Math.max(0, maskIndex - this.maxTokens/2);
        int stop_index = Math.min(_ids.length, maskIndex + this.maxTokens/2) - 1;
        //System.out.println("MIN-MAX: " + start_index + "-" + stop_index);

        String res = "";
        long[] res1 = new long[stop_index-start_index+1];

        //System.out.println("long[] created");
        for(int i = start_index; i <= stop_index; i++) {
            res1[i - start_index] = _ids[i];
            //System.out.println();
        }
        //System.out.println("After init on long");
        //System.out.println();

        return this.tokenizerForCenteringMask.decode(res1);
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
            //System.out.println("MUTATING " + fileToMutate.getName() + ": " + classes);
            if(classes == null) return null;

            // Only computed once for the same file
            // After generating variants to be sure that it's a valid file
            TemporaryStorage tmpResult = codeToTokens(codeToMutate);

            ArrayList<ObjectForThread> threads = new ArrayList<ObjectForThread>();
            ArrayList<Future<?>> futures = new ArrayList<Future<?>>();

            // Browse every mask info to generate the whole code masked
            // 1. Generate pre-threads
            for(ClassInfo cl: classes)
            {
                for(MethodInfo me: cl.methods)
                {
                    for(StatementInfo st: me.statements)
                    {
                        for(MaskingInfo ma: st.maskingInfos)
                        {
                            ObjectForThread o = new ObjectForThread(this)
                            {
                                @Override
                                public void run()
                                {
                                    //System.out.println("    <Thread number " + this.getName() + " running>");
                                    String toMutate =
                                            codeToMutate.substring(0,ma.position.beginIndex)
                                                    + mask
                                                    + codeToMutate.substring(ma.position.endIndex+1);
                                    //System.out.println("    <break-4>");
                                    // Center the mask
                                    String toMutateCenter = centerTheMask(toMutate);

                                    //System.out.println("    <break-3>");
                                    Encoding encoding = tokenizer.encode(toMutateCenter);
                                    //System.out.println("    <break-2>");
                                    long[] inputIds = encoding.getIds();
                                    long[] attentionMask = encoding.getAttentionMask();
                                    long[] typeIds = encoding.getTypeIds(); // Add for Bert
//                                    System.out.println("inputIds size: " + inputIds.length);
//                                    System.out.println("attentionMask size: " + attentionMask.length);
//                                    System.out.println("typeIds size: " + typeIds.length);
                                    //System.out.println("    <break-1>");
                                    // Determining the mask index
                                    int maskIndex = 0;
                                    for (String s : encoding.getTokens()) {
                                        if (s.trim().equals(((StrategyFillMask)object).mask)) break;
                                        maskIndex++;
                                    }
                                    //System.out.println("    <break0>");
                                    try {
                                        // 2) Prepare inputs (batch size = 1)
                                        OnnxTensor idsTensor = OnnxTensor.createTensor(env, new long[][]{inputIds});
                                        OnnxTensor maskTensor = OnnxTensor.createTensor(env, new long[][]{attentionMask});
                                        OnnxTensor typeTensor = OnnxTensor.createTensor(env, new long[][]{typeIds});
                                        Map<String, OnnxTensor> inputs = Map.of(
                                                "input_ids", idsTensor,
                                                "attention_mask", maskTensor
                                                //"token_type_ids", typeTensor // Add for Bert
                                        );

                                        long start = System.currentTimeMillis();
                                        OrtSession.Result result = session.run(inputs);
                                        long end = System.currentTimeMillis();
                                        System.out.println("    Time to run: " + (end-start) + "ms");
                                        // The first (and only) output is [batch, seq_len, vocab_size]
                                        //System.out.println("WE ARE AFTER THE RUN");
                                        float[][][] logits = (float[][][]) result.get(0).getValue();
                                        float[] maskLogits = logits[0][maskIndex];

                                        // 2. Map containing mutation:score, e.g: ["+":0.6, " +":0.2, "-":0.1, ...]
                                        LinkedHashMap<String, Double> preds = getTopKMutations(maskLogits, 5);
                                        //System.out.println("preds: " + preds);

                                        int k = 5;
                                        PriorityQueue<Integer> pq = new PriorityQueue<>(
                                                Comparator.comparingDouble(i -> -maskLogits[i])
                                        );
                                        for (int i = 0; i < maskLogits.length; i++) pq.add(i);
                                        List<Integer> topIds = IntStream.range(0, k)
                                                .mapToObj(_i -> pq.poll())
                                                .collect(Collectors.toList());
                                        //System.out.println("topIds: " + topIds);

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

                                            // Equivalent mutation metric
                                            if(mutation.equals(codeToMutate.substring(ma.position.beginIndex, ma.position.endIndex+1))
                                            || mutation.trim().equals(codeToMutate.substring(ma.position.beginIndex, ma.position.endIndex+1)))
                                                prediction.metrics.put("Equivalent", "true");
                                            else
                                                prediction.metrics.put("Equivalent", "false");

                                            ma.predictions.add(prediction);
                                            //System.gc();
                                        }
                                        //System.out.println("    <End of thread" + this.getName() + " >\n");
                                    }
                                    catch (OrtException e)
                                    {
                                        System.out.println("Here is an interesting except... " + e);
                                        throw new RuntimeException(e);
                                    }
                                }
                            };
                            threads.add(o);
                        }
                    }
                }
            }
            //System.out.println("*** Number of threads generated: " + threads.size() + " ***");

            // 2. Start the threads
            for(ObjectForThread o: threads) futures.add(Mutator.getInstance().getSharedPool().submit(o));

            // 3. Join all the threads
            for (Future<?> f : futures) f.get();

            fileInfo.classes = classes;
        }
        catch (Exception e) {
            System.out.println("An error: " + e);
            return null;
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


    public static void main(String[] args) {
        StrategyFillMask strat =
                new StrategyFillMask.Builder()
                .setPathToModel("/Users/schumi/eclipse-workspace/Test-Mutation-Testing2/onnx")
                .build();

        String codeToMutate = """
package cryptography;

import exception.CryptographyException;

public class Vigenere extends AbstractCryptography
{
    public static final char[] LATIN_ALPHABET_LOWERCASE = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
    public static final char[] LATIN_ALPHABET_UPPERCASE = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
    public static final char[] LATIN_ALPHABET_WITH_NUMBERS = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','0','1','2','3','4','5','6','7','8','9'};
    public static final char[] LATIN_ALPHABET_EXTENDED = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','0','1','2','3','4','5','6','7','8','9',',',';','.',':','-','_','+','"','*','#','%','&','/','|','\\\\','(',')','\\'','?','!',' ','[',']','{','}'};

    /// The alphabet used, e.g. ['a','b','c', ..., 'y','z']
    private char[] alphabet;

    /// The key used for encryption, e.g. "mysecretkey"
    private String key;

    public Vigenere()
    {
        this.setName("Vigenère");
        this.setDescription("The Vigenère cipher is an encryption method similar to the Caesar cipher, except that every letter has a different shifting. It uses an alphabet, usually the latin alphabet, and a key consisting of a word of the alphabet.");
    }

    public void setAlphabet(char[] alphabet) {
        if(alphabet != null)
            if(alphabet.length > 0)
                this.alphabet = alphabet;
    }

    public void setKey(String key) throws CryptographyException
    {
        if(key != null)
        {
            if (!key.isEmpty())
            {
                // Check if the key is valid
                for (char c : key.toCharArray()) {
                    if (getCharIndexInAlphabet(c) == -1) // A character of the key is not in the alphabet
                        throw new CryptographyException("The key contains a forbidden character: " + c);
                }
                this.key = key;
            }
            else
            {
                throw new CryptographyException("The key is empty");
            }
        }
        else
        {
            throw new CryptographyException("The key is null");
        }
    }

    public char[] getAlphabet() {
        return alphabet;
    }

    public String getKey() {
        return key;
    }

    /**
     *
     * @param c The character to find, e.g. 'b'
     * @return The position of the character in the alphabet, if not found then returns -1
     */
    public int getCharIndexInAlphabet(char c)
    {
        for(int i = 0; i < this.alphabet.length; i++)
        {
            if(this.alphabet[i] == c) return i;
        }
        return -1;
    }

    public String encrypt(String textToEncrypt)
    {
        String encrypted = "";
        int keyPosition = 0;    // The position of the key

        /*
         * Browse every character of the text to encrypt
         * If the character is a valid letter, then we will shift
         * the letter with the key
         */
        for(char c: textToEncrypt.toCharArray())
        {
            int indexOfChar = getCharIndexInAlphabet(c);
            if(indexOfChar != -1) // Valid character
            {
                int indexOfKeyChar = getCharIndexInAlphabet(key.toCharArray()[keyPosition]);
                if(indexOfKeyChar != -1) // This case must always happen
                {
                    int newPosition = (indexOfChar + indexOfKeyChar) % alphabet.length;
                    char newChar = alphabet[newPosition];
                    encrypted += newChar;
                    keyPosition = (keyPosition + 1) % key.length(); // If the end is reached, go back to the start of the key
                }
            }
            else    // add the char without doing anything
            {
                encrypted <mask> c;
            }
        }

        return encrypted;
    }

    public String decrypt(String textToDecrypt)
    {
        String decrypted = "";
        int keyPosition = 0;    // The position of the key

        /*
         * Browse every character of the text to encrypt
         * If the character is a valid letter, then we will shift
         * the letter with the key
         */
        for(char c: textToDecrypt.toCharArray())
        {
            int indexOfChar = getCharIndexInAlphabet(c);
            if(indexOfChar != -1) // Valid character
            {
                int indexOfKeyChar = getCharIndexInAlphabet(key.toCharArray()[keyPosition]);
                if(indexOfKeyChar != -1) // This case must always happen
                {

                    int newPosition = (indexOfChar - indexOfKeyChar) % alphabet.length;
                    if(newPosition < 0) newPosition += alphabet.length; // The remainder can be negative, and we want it between 0 and alphabet.length-1
                    char newChar = alphabet[newPosition];
                    decrypted += newChar;
                    keyPosition = (keyPosition + 1) % key.length(); // If the end is reached, go back to the start of the key
                }
            }
            else    // add the char without doing anything
            {
                decrypted += c;
            }
        }

        return decrypted;
    }
}
                """;

        //System.out.println(strat.centerTheMask(codeToMutate));
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




    /**
     * A static class for storing info about the mask centering
     * As it is a very greedy and long computation, this will be only computed one
     */
    private static class TemporaryStorage
    {
        public String[] tokens;
        public long[] ids;
    }
}