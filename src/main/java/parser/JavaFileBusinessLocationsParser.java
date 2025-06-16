package parser;

import core.Language;
import core.MaskParser;
import storage.ClassInfo;
import storage.MutationInfo;
import strategy.StrategyFillMask;

import java.io.File;
import java.util.ArrayList;

public class JavaFileBusinessLocationsParser extends MaskParser
{
    public JavaFileBusinessLocationsParser(){ this.language = Language.JAVA; }

    public JavaFileBusinessLocationsParser(StrategyFillMask strat)
    {
        super(strat);
    }

    @Override
    public ArrayList<ClassInfo> generateVariants(File fileCode, boolean toMask)
    {
//        try {
//            String[] codeInLines = Files.readString(fileCode.toPath()).split("\\r?\\n", -1);;
//            ArrayList<ClassInfo> classes = new ArrayList<ClassInfo>();
//            String[] arguments =
//            {
//                "-in=" + fileCode.getAbsolutePath()
//            };
//
//            CliRequest request = CliRequest.parseArgs(arguments);
//            LocationsCollector collector = request.start();
//
//            Gson gson = GsonHolder.getGson();
//
//            for (FileLocations f : collector.getItems()) {
//                String json = gson.toJson(f);
//                JSONObject jsonObject = new JSONObject(json);
//
//                // Get classes
//                JSONArray classPredictions = jsonObject.getJSONArray("classPredictions");
//                Iterator<?> classesIterator = classPredictions.iterator();
//                while (classesIterator.hasNext()) {
//                    ClassInfo classInfo = new ClassInfo();
//                    JSONObject nextClass = (JSONObject) classesIterator.next();
//                    classInfo.className = nextClass.getString("qualifiedName");
//
//                    // Get methods
//                    JSONArray methodPredictions = nextClass.getJSONArray("methodPredictions");
//                    Iterator<?> methodIterator = methodPredictions.iterator();
//                    while (methodIterator.hasNext()) {
//                        MethodInfo methodInfo = new MethodInfo();
//                        JSONObject nextMethod = (JSONObject) methodIterator.next();
//                        methodInfo.declaration = nextMethod.getString("methodSignature");
//                        methodInfo.methodName = methodInfo.declaration;
//                        methodInfo.position.beginLine = (Integer)nextMethod.get("startLineNumber");
//                        methodInfo.position.endLine = (Integer)nextMethod.get("endLineNumber");
//                        methodInfo.position.beginColumn = MaskParser.firstNoneEmptyIndex(codeInLines[methodInfo.position.beginLine])+1;
//                        methodInfo.position.endColumn = MaskParser.lastNoneEmptyIndex(codeInLines[methodInfo.position.endLine])+1;
//
//                        // Get statements ≈ masking position
//                        JSONArray linePredictions = nextMethod.getJSONArray("line_predictions");
//                        Iterator<?> lineIterator = linePredictions.iterator();
//                        while(lineIterator.hasNext())
//                        {
//                            StatementInfo statementInfo = new StatementInfo();
//                            JSONObject nextLine = (JSONObject) lineIterator.next();
//                            statementInfo.position.beginLine = (Integer)nextLine.get("line_number");
//
//                            // Get masking info = locations
//                            JSONArray locations = nextLine.getJSONArray("locations");
//                            Iterator<?> locationIterator = locations.iterator();
//                            while (locationIterator.hasNext())
//                            {
//                                MaskingInfo maskingInfo = new MaskingInfo();
//                                JSONObject nextMasking = (JSONObject) locationIterator.next();
//                                maskingInfo.maskingType = nextMasking.getString("operator");
//                                maskingInfo.position.beginIndex = (Integer)((JSONObject)nextMasking.get("codePosition")).get("startPosition");
//                                maskingInfo.position.endIndex = (Integer)((JSONObject)nextMasking.get("codePosition")).get("startPosition");
//                                statementInfo.maskingInfos.add(maskingInfo);
//                            }
//                            methodInfo.statements.add(statementInfo);
//                        }
//                        classInfo.methods.add(methodInfo);
//                    }
//                    classes.add(classInfo);
//                }
//                break;
//            }
//            return classes;
//        }
//        catch (Exception e)
//        {
//            System.err.println(e);
//            return null;
//        }
        return null;
    }

    @Override
    public ArrayList<ClassInfo> generateVariants(String sourceCode, boolean toMask) {
        return null;
    }

    @Override
    public MutationInfo binaryMaskOperand(Object expr, boolean left, String[] codeInLines) {
        return null;
    }

    @Override
    public MutationInfo binaryMaskOperator(Object expr, String[] codeInLines) {
        return null;
    }
}
