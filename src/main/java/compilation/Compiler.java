package compilation;

import core.Language;

import java.io.IOException;
import java.nio.file.Path;

public abstract class Compiler
{
    /// The language to be compiled
    protected Language language;

    public Language getLanguage() {
        return language;
    }

    /**
     * In Java, seeks for maven jars
     * @param projectRoot The root of the project, e.g: "/usr/home/My-Project"
     * @param originalFile The original file of the code, e.g: "/usr/home/My-Project/src/java/A.java"
     * @param codeToCompile The code to be compiled, e.g: "public class A{...}"
     * @param pathToExport The path to export our mutated code, e.g: "/usr/home/output/A-java/0"
     * @return true if the code compiles, false otherwise
     */
    public abstract boolean tryCompile(String projectRoot, Path originalFile, String codeToCompile, Path pathToExport) throws IOException, InterruptedException;
}
