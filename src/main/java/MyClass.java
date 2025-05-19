import core.Mutator;
import export.JsonExport;
import parser.CppMaskParser;
import parser.JavaMaskParser;
import storage.*;
import strategy.StrategyFillMask;

import java.io.FileWriter;
import java.nio.file.Path;
import java.util.ArrayList;

public class MyClass
{
    public static void main(String[] args)
    {
        // 1) Fill-mask strategy. The model used is CodeBERT
        StrategyFillMask strategy =
                new StrategyFillMask.Builder()
                .setPathToModel("/Users/schumi/eclipse-workspace/Test-Mutation-Testing2/onnx")
                .build();

        // 2) Only want to mutate Java files, included in the 'example' folder
        Mutator mutator = Mutator.getInstance()
                .addParser(new JavaMaskParser())
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

        mutator.getSharedPool().shutdown();
    }
}
