package compilation;

import core.Language;
import org.apache.commons.io.FilenameUtils;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    public boolean tryCompile(String projectRoot, Path originalFile, String codeToCompile, Path pathToExport) throws IOException, InterruptedException {
        // 1. Collect every .java files in the project
        List<Path> allPaths = Files.walk(Paths.get(projectRoot))
                .filter(p -> p.toString().endsWith(".java"))
                .collect(Collectors.toList());

        // 2. Only discard the file to be mutated
        List<File> files = new ArrayList<>();
        for (Path p : allPaths) {
            if (!p.equals(originalFile)) files.add(p.toFile());
        }

        // 3. Prepare the filemanager
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager stdFM = compiler.getStandardFileManager(diagnostics, null, null);

        // 4. Disk units
        Iterable<? extends JavaFileObject> diskUnits =
                stdFM.getJavaFileObjectsFromFiles(files);
        System.out.println(diskUnits);
        System.out.println("File: " + "string:///" + originalFile.toFile().getName());

        // 5. Mutation unit
        JavaFileObject memUnit = new JavaStringObject(FilenameUtils.removeExtension(originalFile.toFile().getName()), codeToCompile);
        JavaByteObject byteCode = new JavaByteObject(FilenameUtils.removeExtension(originalFile.toFile().getName()));
        System.out.println(memUnit);
        System.out.println(byteCode);

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

        if(success)
        {
            if(!Path.of(pathToExport.toString(), "compiled-classes").toFile().exists())
            {
                Files.createDirectories(Path.of(pathToExport.toString(), "compiled-classes"));
            }
            stdFM.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(Path.of(pathToExport.toString(), "compiled-classes").toFile()));
            task = compiler.getTask(
                    null, stdFM, diagnostics, options, null, allUnits
            );
            task.call();
        }

        stdFM.close();

        return success;
    }

    public static byte[] compile(String className, String sourceCode) throws CompilerException
    {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        JavaByteObject javaByteObject = new JavaByteObject(className);
        CompilerFileManager compilerFileManager = new CompilerFileManager(compiler.getStandardFileManager(diagnostics, null, null), javaByteObject);

        List<String> options = Collections.emptyList();
        JavaCompiler.CompilationTask compilationTask = compiler.getTask(
                null, compilerFileManager, diagnostics,
                options, null, () -> {
                    JavaFileObject javaFileObject = new JavaStringObject(className, sourceCode);
                    return Collections.singletonList(javaFileObject).iterator();
                });

        boolean compilationSuccessful = compilationTask.call();
        if (!compilationSuccessful){
            String message = diagnostics.getDiagnostics().stream().map(Object::toString).collect(Collectors.joining());
            throw new CompilerException(String.format("Failed to compile class '%s':\n%s", className, message));
        }
        return javaByteObject.getBytes();
    }
}

/// Source: https://marvin-haagen.de/tutorials/software/java/how-to-compile-java-code-at-runtime/

/**
 * Class used during the compilation operation
 */
class CompilerFileManager extends ForwardingJavaFileManager<StandardJavaFileManager>
{
    private final JavaFileObject javaFileObject;

    public CompilerFileManager(StandardJavaFileManager fileManager, JavaFileObject javaFileObject)
    {
        super(fileManager);
        this.javaFileObject = javaFileObject;
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException
    {
        return javaFileObject;
    }
}

/**
 * Class used to store the bytecode of the code
 */
class JavaByteObject extends SimpleJavaFileObject
{
    private ByteArrayOutputStream outputStream;

    public JavaByteObject(String name)
    {
        super(URI.create(String.format("bytes:///%s%s", name, name.replaceAll("\\.", "/"))), Kind.CLASS);
    }

    @Override
    public OutputStream openOutputStream() throws IOException
    {
        this.outputStream = new ByteArrayOutputStream();
        return outputStream;
    }

    public byte[] getBytes()
    {
        return outputStream.toByteArray();
    }
}

/**
 * Class used to store the string of the class
 */
class JavaStringObject extends SimpleJavaFileObject
{
    private final String code;

    public JavaStringObject(String className, String code)
    {
        super(URI.create(String.format(
                "string:///%s%s",
                className.replace('.','/'),
                Kind.SOURCE.extension
        )), Kind.SOURCE);
        this.code = code;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors)
    {
        return code;
    }
}

/**
 * Class used as an exception for the compilation
 */
class CompilerException extends Exception{
    public CompilerException(String message) {
        super(message);
    }
}
