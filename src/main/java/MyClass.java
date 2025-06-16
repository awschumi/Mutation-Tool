import compilation.JavaTryCompiler;
import core.Mutator;
import export.HtmlExport;
import export.JsonExport;
import imports.JsonImport;
import parser.JavaMaskParser;
import storage.ClassInfo;
import storage.FileInfo;
import strategy.StrategyFillMask;
import testing.JavaTesting;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class MyClass
{
    public static void main(String[] args) throws IOException, InterruptedException {
        // Fill-mask strategy. The model used is CodeBERT
        StrategyFillMask strategy =
                new StrategyFillMask.Builder()
                .setPathToModel("/Users/schumi/eclipse-workspace/Test-Mutation-Testing2/onnx")
                .build();

        // Only want to mutate Java files, included in the 'example' folder
        Mutator mutator = Mutator.getInstance()
                .addParser(new JavaMaskParser())
                .addCompiler(new JavaTryCompiler().setUseMaven(true))
                //.addParser(new CppMaskParser()) // Uncomment if you want to mutate C++ files
                .setExportPath(Path.of("output"))
                .setProjectPath(Path.of("Sample-Project"))
                .setTestsPath(Path.of("Sample-Project/src/test"))
                .setStrategy(strategy)
                .setThreadsNumber(8);

        ArrayList<FileInfo> results;
        boolean loadFiles = true;
        boolean compile = true;

        if(!loadFiles)
        {
            // 1. Mutate every file in our directory
            results = mutator.mutateAll();

            // Save the results into a json file
            for(FileInfo f: results)
            {
                String toSave = f.visit(new JsonExport());
                try
                {
                    FileWriter fw = new FileWriter("results-"+f.fileName+".json");
                    fw.write(toSave);
                    fw.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            // 2. Generate every mutant into a file for each mutation
            // 3. Try to compile the mutated files
            mutator.createMutatedFiles(results);
        }
        else
        {
            results = new ArrayList<>();
            JsonImport jsonImport = new JsonImport();
            FileInfo f = (FileInfo) jsonImport.getInfo(Path.of("results-Caesar.java.json").toFile());
            if(f != null) results.add(f);
            f = (FileInfo) jsonImport.getInfo(Path.of("results-Vigenere.java.json").toFile());
            if(f != null) results.add(f);
            f = (FileInfo) jsonImport.getInfo(Path.of("results-Calculator.java.json").toFile());
            if(f != null) results.add(f);

            if(compile)
            {
                mutator.createMutatedFiles(results);
            }
        }

        // 5. Testing part with the mutated files
        JavaTesting javaTesting = new JavaTesting();
        javaTesting.test(results);

        // 6. Export the results
        HtmlExport htmlExport = new HtmlExport();
        htmlExport.setPathToExport(Path.of("html-report"));
        htmlExport.export(results);

        // Save the results into a json file
        for(FileInfo f2: results)
        {
            String toSave = f2.visit(new JsonExport());
            try
            {
                FileWriter fw = new FileWriter("results-"+f2.fileName+".json");
                fw.write(toSave);
                fw.close();
                System.out.println("******************\n");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
//
//        String[] arguments = {
//                "-in=Sample-Project/src/main/java/calculator/Calculator.java"
//        };
//
//        CliRequest request = CliRequest.parseArgs(arguments);
//        System.out.println(request);
//        LocationsCollector collector = request.start();
//
//        Gson gson = GsonHolder.getGson();
//
//        JavaFileBusinessLocationsParser javaFileBusinessLocationsParser = new JavaFileBusinessLocationsParser();
//        ArrayList<ClassInfo> classes = javaFileBusinessLocationsParser.generateVariants(Path.of("Sample-Project/src/main/java/calculator/Calculator.java").toFile(), true);
//        System.out.println(classes);
//        FileInfo f = new FileInfo();
//        f.language = "Java";
//        f.classes = classes;
//
//        JsonExport jsonExport = new JsonExport();
//        System.out.println(f.visit(jsonExport));
//
//        mutator.getSharedPool().shutdown();

//        FileInfo fileInfo = new FileInfo();
//        fileInfo.fileName = "truc.java";
//        fileInfo.pathName = "/path/to/truc.java";
//        fileInfo.language = "Java";
//
//            ClassInfo classInfo1 = new ClassInfo(fileInfo);
//            fileInfo.children.add(classInfo1);
//            classInfo1.className = "Class1";
//
//                MethodInfo methodInfo1 = new MethodInfo(classInfo1);
//                classInfo1.children.add(methodInfo1);
//                methodInfo1.signature = "add(double,double)";
//
//                    MutationInfo mutationInfo1 = new MutationInfo(methodInfo1);
//                    methodInfo1.children.add(mutationInfo1);
//                    mutationInfo1.maskingType = "Type1";
//
//                        PredictionInfo predictionInfo1 = new PredictionInfo(mutationInfo1);
//                        mutationInfo1.children.add(predictionInfo1);
//                        predictionInfo1.preCode = "before";
//                        predictionInfo1.afterCode = "after;";
//
//                    MutationInfo mutationInfo2 = new MutationInfo(methodInfo1);
//                    methodInfo1.children.add(mutationInfo2);
//                    mutationInfo2.maskingType = "Type2";
//
//                        PredictionInfo predictionInfo2 = new PredictionInfo(mutationInfo2);
//                        mutationInfo2.children.add(predictionInfo2);
//                        predictionInfo1.preCode = "before2";
//                        predictionInfo1.afterCode = "after2";
//                        predictionInfo1.metrics.put("SoftMax", "0.97998");
//
//
//            FunctionInfo functionInfo1 = new FunctionInfo(fileInfo);
//            fileInfo.children.add(functionInfo1);
//            functionInfo1.signature = "fun(boolean b)";
//
//                MutationInfo mutationInfo3 = new MutationInfo(functionInfo1);
//                functionInfo1.children.add(mutationInfo3);
//                mutationInfo3.maskingType = "Type3";
//
//                    PredictionInfo predictionInfo3 = new PredictionInfo(mutationInfo3);
//                    mutationInfo3.children.add(predictionInfo3);
//                    predictionInfo3.preCode = "Code....";
//                    predictionInfo3.afterCode = "cODE::::";
//                    predictionInfo3.metrics.put("Metric1", "eeeee");
//
//        System.out.println(fileInfo);
//
//        System.out.println(fileInfo.visit(new JsonExport()));
//
//        JsonImport jsonImport = new JsonImport();
//        System.out.println(jsonImport.protectedGetInfo(fileInfo.visit(new JsonExport())));
//        System.out.println();
//        ArrayList<AbstractInfo> infos = fileInfo.getSpecificChildren(AbstractInfo.Info.MUTATION_INFO);
//        for(AbstractInfo ab: infos)
//        {
//            System.out.println(ab);
//        }

//        FileInfo fileInfo = new FileInfo();
//        ArrayList<ClassInfo> classes = new JavaMaskParser().generateVariants(Files.readString(Path.of("/Users/schumi/eclipse-workspace/Mutation Tool/Sample-Project/src/main/java/calculator/Calculator.java")), true);
//        fileInfo.children.addAll(classes);
//        System.out.println(fileInfo.visit(new JsonExport()));
    }
}
