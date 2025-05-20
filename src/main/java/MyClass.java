import compilation.Compiler;
import compilation.JavaTryCompiler;
import core.Mutator;
import export.JsonExport;
import parser.CppMaskParser;
import parser.JavaMaskParser;
import storage.*;
import strategy.StrategyFillMask;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Stream;

public class MyClass
{
    public static void main(String[] args) throws IOException, InterruptedException {
        // 1) Fill-mask strategy. The model used is CodeBERT
        StrategyFillMask strategy =
                new StrategyFillMask.Builder()
                .setPathToModel("/Users/schumi/eclipse-workspace/Test-Mutation-Testing2/onnx")
                .build();

        // 2) Only want to mutate Java files, included in the 'example' folder
        Mutator mutator = Mutator.getInstance()
                .addParser(new JavaMaskParser())
                .addCompiler(new JavaTryCompiler().setUseMaven(false))
                //.addParser(new CppMaskParser()) // Uncomment if you want to mutate C++ files
                .setExportPath(Path.of("output"))
                .setStrategy(strategy)
                .setThreadsNumber(16);

        // Mutate every file in our directory
        ArrayList<FileInfo> results = mutator.mutateAll("examples");

        // Generate every mutant into a file for each mutation
        mutator.createMutatedFiles(results);

        // Save the results into a json file
        for(FileInfo f: results)
        {
            String toSave = f.visit(new JsonExport());
            try
            {
                FileWriter fw = new FileWriter("results-"+f.fileName+".json");
                fw.write(toSave);
                fw.close();
                System.out.println("******************\n");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

//        ArrayList<Compiler> compilers = new ArrayList<>();
//        compilers.add(new JavaTryCompiler());
//
//        FileInfo f = new FileInfo();
//        f.language = "Java";
//
//        try {
//            Stream<Path> paths = Files.walk(Path.of("/Users/schumi/java-crypto-utils/"));
//
//            paths.filter(Files::isRegularFile)
//                    .forEach(p ->
//                    {
//                        if(p.toFile().getName().endsWith(".java")) {
//                            try {
//                                String code = Files.readString(p);
//
//                                System.out.println("Will " + p.toFile().getName() + "compile? " + Mutator.getInstance().getFileHandler().tryCompile(f,
//                                        "/Users/schumi/java-crypto-utils",
//                                        p,
//                                        code,
//                                        compilers));
//                            } catch (Exception e) {
//                                //throw new RuntimeException(e);
//                            }
//                        }
//                    });
//        } catch (Exception e) {
//        }


        //System.out.println(javaTryCompiler.tryCompile("/Users/schumi/java-crypto-utils",
//                Path.of("/Users/schumi/java-crypto-utils/src/main/java/co/tunjos/crypto/crypto/CryptoUtils.java"),
//                code));


        mutator.getSharedPool().shutdown();
    }
}
