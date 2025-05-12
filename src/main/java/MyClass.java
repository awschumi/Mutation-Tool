import core.Language;
import core.Mutator;
import export.JsonExport;
import parser.JavaMaskParser;
import storage.*;
import strategy.StrategyFillMask;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.ArrayList;

public class MyClass
{
    public static void main(String[] args)
    {
        // 1) Fill-mask strategy on Java classes (Java Parser). The model used is CodeBERT
        StrategyFillMask strategy =
                new StrategyFillMask.Builder()
                .setParser(new JavaMaskParser())
                .setPathToModel("/Users/schumi/eclipse-workspace/Test-Mutation-Testing2/onnx")
                .build();

        // 2) Only want to mutate Java files, included in the 'example' folder
        Mutator mutator = new Mutator.MutatorBuilder()
                .addLanguage(Language.JAVA) // @TODO
                .setExportPath(Path.of("output"))
                .setStrategy(strategy)
                .setThreadsNumber(4)        // @TODO
                .build();

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

    }
}
