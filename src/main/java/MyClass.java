import compilation.Compiler;
import compilation.JavaTryCompiler;
import core.Mutator;
import export.HtmlExport;
import export.JsonExport;
import imports.JsonImport;
import org.json.JSONObject;
import parser.CppMaskParser;
import parser.JavaMaskParser;
import storage.*;
import strategy.StrategyFillMask;
import testing.JavaTesting;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Stream;

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
        boolean compile = false;

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

        mutator.getSharedPool().shutdown();
    }
}
