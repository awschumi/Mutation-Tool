import core.Language;
import core.Mutator;
import parser.JavaMaskParser;
import strategy.StrategyFillMask;

import java.nio.file.Path;

public class MyClass
{
    public static void main(String[] args)
    {
        // 1) Fill-mask strategy on Java classes (Java Parser). The model used is CodeBERT
        StrategyFillMask strategy =
                new StrategyFillMask.Builder()
                .setParser(new JavaMaskParser())
                .setPathToModel("path/to/codebert-onnx")
                .build();

        // 2) Only want to mutate Java files, included in the 'example' folder
        Mutator mutator = new Mutator.MutatorBuilder()
                .addLanguage(Language.JAVA) // @TODO
                .setExportPath(Path.of("examples"))
                .setStrategy(strategy)
                .setThreadsNumber(4)        // @TODO
                .build();

        mutator.mutateAll("examples");
    }
}
