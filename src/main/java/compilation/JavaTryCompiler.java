package compilation;

import core.Language;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JavaTryCompiler extends Compiler
{
    private boolean useMaven = true;

    public JavaTryCompiler()
    {
        this.language = Language.JAVA;
    }

    public boolean isUseMaven() {
        return useMaven;
    }

    public JavaTryCompiler setUseMaven(boolean b)
    {
        this.useMaven = b;
        return this;
    }

    @Override
    public boolean tryCompile(String projectRoot, Path originalFile, String codeToCompile) throws IOException, InterruptedException {
        // 1. Collect every .java files in the project
        List<Path> allPaths = Files.walk(Paths.get(projectRoot))
                .filter(p -> p.toString().endsWith(".java"))
                .collect(Collectors.toList());

        // 2. Only discard the file to be mutated
        List<File> files = new ArrayList<>();
        for (Path p : allPaths) {
            if (!p.equals(originalFile)) files.add(p.toFile());
            else System.out.println("CACACACACA");
        }

        // 3. Prepare the filemanager
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager stdFM = compiler.getStandardFileManager(diagnostics, null, null);

        // 4. Disk units
        Iterable<? extends JavaFileObject> diskUnits =
                stdFM.getJavaFileObjectsFromFiles(files);
        System.out.println("File: " + "string:///" + originalFile.toFile().getName());

        // 5. Mutation unit
        JavaFileObject memUnit = new SimpleJavaFileObject(
                URI.create("string:///" + originalFile.toFile().getName()), JavaFileObject.Kind.SOURCE) {
            @Override public CharSequence getCharContent(boolean ignore) {
                return codeToCompile;
            }
        };

        // 6. The list of .jars (requires a pom.xml file in the project)
        // if cp.txt exists: no call to maven
        if(!Files.exists(Paths.get(projectRoot, "cp.txt")) && useMaven) {
            Process proc = new ProcessBuilder("mvn",
                    "-f", projectRoot,
                    "dependency:build-classpath", "-Dmdep.outputFile=cp.txt")
                    .start();
            proc.waitFor();
        }

        String mavenClasspath = "";
        if(useMaven) mavenClasspath  = Files.readString(Paths.get(projectRoot, "cp.txt")).trim();
        //System.out.println("Maven Class Path: " + mavenClasspath);
        List<String> options;
        if(useMaven) options = Arrays.asList("-classpath", mavenClasspath);
        else options = null;
        //System.out.println(options);

        // 7. Compilation
        List<JavaFileObject> allUnits = new ArrayList<>();
        diskUnits.forEach(allUnits::add);
        allUnits.add(memUnit);

        JavaCompiler.CompilationTask task = compiler.getTask(
                null, stdFM, diagnostics, options, null, allUnits
        );
        boolean success = task.call();
        System.out.println("Success for " + originalFile.toFile().getName() + "? " + success);
        // 8. Diagnostics
        diagnostics.getDiagnostics().forEach(d ->
                System.err.printf("%s:%d: %s%n",
                        d.getSource().getName(), d.getLineNumber(), d.getMessage(null))
        );

        stdFM.close();

        return success;
    }
}