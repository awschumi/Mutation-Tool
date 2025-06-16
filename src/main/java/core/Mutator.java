package core;

import compilation.Compiler;
import parser.parsinghandle.ParsingHandler;
import storage.*;
import parser.parsinghandle.*;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class containing custom options for the mutation
 * e.g: Strategy (fill-mask or LLM), number of threads, metrics, path where export mutants and results
 */
public class Mutator
{
    static
    {
        Mutator.getInstance();
    }

    //
    private static Mutator instance = null;

    // How will be made the mutation?
    private Strategy strategy;

    // What parsers will be used (Java, Python, C++?)
    private ArrayList<MaskParser> parsers = new ArrayList<MaskParser>();

    // Used for the compilation of the mutants
    private ArrayList<Compiler> compilers = new ArrayList<Compiler>();

    // The number of threads for the mutation
    private int threadsNumber = 4;

    // Shared pool of maximum 4 threads
    private ExecutorService sharedPool = Executors.newFixedThreadPool(4);

    // Where will be exported all the results, such as the mutated files
    private Path exportPath = Path.of("output").toAbsolutePath();

    // The path containing the files to be mutated
    private Path projectPath = Path.of("examples").toAbsolutePath();

    // The path containing the test files
    private Path testsPath = Path.of("").toAbsolutePath();

    private ParsingHandler fileHandler = ParsingHandler.link(
                new JavaHandler(),
                new CppHandler(),
                new PythonHandler(),
                new CHandler()
        );

    public static Mutator getInstance()
    {
        if(instance == null)
            Mutator.instance = new Mutator();
        return instance;
    }

    private Mutator(){}

    public Mutator setStrategy(Strategy s)
    {
        this.strategy = s;
        return this;
    }

    public Mutator addParser(MaskParser p)
    {
        this.parsers.add(p);
        return this;
    }

    public Mutator addCompiler(Compiler c)
    {
        this.compilers.add(c);
        return this;
    }

    public Mutator setThreadsNumber(int n)
    {
        this.threadsNumber = n;
        this.sharedPool = Executors.newFixedThreadPool(n);
        return this;
    }

    public Mutator setExportPath(Path p)
    {
        this.exportPath = p.toAbsolutePath();
        return this;
    }

    public Mutator setProjectPath(Path p)
    {
        this.projectPath = p.toAbsolutePath();
        return this;
    }

    public Mutator setTestsPath(Path p) {
        this.testsPath = p.toAbsolutePath();
        return this;
    }

    public ArrayList<MaskParser> getParsers()
    {
        return this.parsers;
    }

    public ArrayList<Compiler> getCompilers()
    {
        return this.compilers;
    }

    public ParsingHandler getFileHandler()
    {
        return this.fileHandler;
    }

    public ExecutorService getSharedPool()
    {
        return sharedPool;
    }

    public Path getExportPath()
    {
        return exportPath;
    }

    public Path getProjectPath()
    {
        return projectPath;
    }

    public Path getTestsPath()
    {
        return testsPath;
    }

    /**
     * Mutates only the file
     */
    public FileInfo mutate(File file)
    {
        try
        {
            System.out.println("FILE NAME: " + file.getAbsolutePath());
            FileInfo fileInfo = this.strategy.mutate(file);
            //System.out.println(fileInfo.visit(new JsonExport()));
            return fileInfo;
        } catch (Exception e) {
            System.out.println("Exception 1 here");
            return null;
        }
    }

    /**
     * Mutates all the possible files included in the directory
     * However, the tests classes must not be mutated
     */
    public ArrayList<FileInfo> mutateAll(Path path)
    {
        ArrayList<FileInfo> fileInfos = new ArrayList<>();
        try {
            // List of all java test classes
            // TODO must be refactored somewhere else and not hard coded
            List<Path> allPaths = Files.walk(this.testsPath)
                    .filter(p -> p.toString().endsWith(".java"))
                    .collect(Collectors.toList());

            Stream<Path> paths = Files.walk(path);

            paths.filter(Files::isRegularFile)
            .forEach(p ->
            {
                boolean canContinue = true;
                for(Path testFile: allPaths)
                {
                    if (testFile.toAbsolutePath().toFile().getAbsolutePath().equals(p.toAbsolutePath().toFile().getAbsolutePath()))
                    {
                        canContinue = false;
                        System.out.println("ON N'INCLUS PAS " + p.getFileName());
                        break;
                    }
                }
                if(canContinue)
                {
                    try
                    {
                        FileInfo f = mutate(p.toFile());
                        if (f != null) {
                            fileInfos.add(f);
                            System.out.println("/** FILE MUTATED **/");
                        }
                    } catch (Exception e) {
                        System.out.println("(/(/(/(/(/(/");
                    }
                }
            });
            return fileInfos;
        } catch (Exception e) {
            System.out.println("Exception? " + e);
            return fileInfos;
        }
    }

    public ArrayList<FileInfo> mutateAll(String path)
    {
        return mutateAll(Path.of(path));
    }

    public ArrayList<FileInfo> mutateAll()
    {
        return mutateAll(this.projectPath);
    }

    /**
     * Generate mutant files in the export path
     * @param fileInfos A list of file infos
     */
    public void createMutatedFiles(ArrayList<FileInfo> fileInfos) {
        /*
         outputdir
            File1 // Class1
                0
                    Class1.java
                1
                    Class1.java
                2
                    Class1.java
                ...
            File2 // Class2
                0
                    Class2.py
                1
                    Class2.py
            File3 // Class3
                0
                    Class3.cpp
                1
                    Class3.cpp
         */
        System.out.println("** List of all files **");
        for (FileInfo f : fileInfos)
            System.out.println("> " + f.pathName);
        for (FileInfo f : fileInfos) {
            File file = Path.of(f.pathName).toFile();
            try {
                System.out.println("Pathname: " + f.pathName);
                String content = Files.readString(Path.of(f.pathName));
                Path folderName = Path.of(String.valueOf(exportPath.getFileName()), f.fileName.replace(".", "-"));
                System.out.println(folderName);
                int predictionNumber = 0;

                try {
                    // Try to create directory
                    Files.createDirectories(folderName);

                    // Get every prediction
                    for (AbstractInfo ab : f.getSpecificChildren(AbstractInfo.Info.PREDICTION_INFO)) {
                        PredictionInfo pr = (PredictionInfo) ab;

                        try {
                            // If the mutation is equivalent, don't consider it
                            if (pr.metrics.get("Equivalent") != null)
                                if (pr.metrics.get("Equivalent").equals("true")) {
                                    predictionNumber++;
                                    continue;
                                }
                            ;

                            String codeToCompile = content.substring(0, ((MutationInfo) pr.parent).position.beginIndex)
                                    + pr.afterCode
                                    + content.substring(((MutationInfo) pr.parent).position.endIndex + 1);

                            //Creation of folder number 0, 1, ...
                            Path folderNumber = Path.of(String.valueOf(folderName), String.valueOf(predictionNumber));

                            if (Mutator.getInstance().getFileHandler().tryCompile(
                                    f,
                                    this.projectPath.toString(),
                                    Path.of(f.pathName),
                                    codeToCompile,
                                    folderNumber,
                                    compilers)) {

                                Files.createDirectories(folderNumber);
                                // Creation of the file
                                // We want to create the file <=> the file can be compiled
                                // --> Compile the file: determine the type (Java, C++, C, etc.)
                                File newFile = new File(folderNumber.toFile(), f.fileName);
                                //System.out.println(newFile.getAbsolutePath());
                                String newFileName = newFile.getAbsolutePath();
                                FileWriter newFile1 = new FileWriter(newFileName);
                                newFile1.write(codeToCompile);
                                newFile1.close();

                                // Mutant is alive
                                pr.metrics.put("Stillborn", "false");
                                pr.pathToOutput = folderNumber.toAbsolutePath().toString();
                            } else {
                                // Mutant is dead
                                pr.metrics.put("Stillborn", "true");
                            }
                            predictionNumber++;
                        } catch (Exception e) {
                            predictionNumber++;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Strange exception... " + e);
                    //throw new RuntimeException(e);
                }


            } catch (Exception e) {
                // Problem with reading the file
            }
        }
    }
}


