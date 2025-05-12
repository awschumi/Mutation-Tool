package core;

import storage.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Stream;

/*
 * Class containing custom options for the mutation
 * e.g: Strategy (fill-mask or LLM), number of threads, metrics, path where export mutants and results
 */
public class Mutator
{
    // How will be made the mutation?
    private Strategy strategy;

    // What languages will be processed (Java, Python, C++?)
    private ArrayList<Language> languages;

    // The number of threads for the mutation
    private int threadsNumber;

    // Where will be exported all the results, such as the mutated files
    private Path exportPath;

    private Mutator(MutatorBuilder b)
    {
        this.strategy = b.strategy;
        this.languages = b.languages;
        this.threadsNumber = b.threadsNumber;
        this.exportPath = b.exportPath;
    }

    /*
     * Mutates only the file
     */
    public FileInfo mutate(File file)
    {
        try
        {
            System.out.println("FILE NAME: " + file.getAbsolutePath());
            FileInfo fileInfo = this.strategy.mutate(file);
            //System.out.println("For the file " + file.getPath() + ", " + mutants.size() + " mutants have been generated");
            return fileInfo;
        } catch (Exception e) {
            return null;
            //throw new RuntimeException(e);
        }
    }

    /*
     * Mutates all the possible files included in the directory
     */
    public ArrayList<FileInfo> mutateAll(Path path)
    {
        ArrayList<FileInfo> fileInfos = new ArrayList<>();
        try {
            Stream<Path> paths = Files.walk(path);

            paths.filter(Files::isRegularFile)
            .forEach(p ->
            {
                try {
                    FileInfo f = mutate(p.toFile());
                    if (f != null) fileInfos.add(f);
                    System.out.println("/** FILE MUTATED **/");
                } catch (Exception e) {
                    System.out.println("(/(/(/(/(/(/");
                }
            });
            return fileInfos;
        } catch (Exception e) {
            return fileInfos;
            //throw new RuntimeException(e);
        }
    }

    public ArrayList<FileInfo> mutateAll(String path)
    {
        return mutateAll(Path.of(path));
    }

    /*
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
        for (FileInfo f : fileInfos) {
            File file = Path.of(f.pathName).toFile();
            try {
                String content = Files.readString(Path.of(f.pathName));
                Path folderName = Path.of(String.valueOf(exportPath.getFileName()), f.fileName.replace(".", "-"));
                System.out.println(folderName);
                int predictionNumber = 0;
                for (ClassInfo cl : f.classes) {
                    try {
                        // Try to create directory
                        Files.createDirectories(folderName);
                        for (MethodInfo me : cl.methods) {
                            for (StatementInfo st : me.statements) {
                                for (MaskingInfo ma : st.maskingInfos) {
                                    try {
                                        for (PredictionInfo pr : ma.predictions) {
                                            //Creation of folder number 0, 1, ...
                                            Path folderNumber = Path.of(String.valueOf(folderName), String.valueOf(predictionNumber));
                                            Files.createDirectories(folderNumber);
                                            System.out.println("(" + predictionNumber + ") Prediction info: " + pr.tokenPredicted);
                                            // Creation of the file
                                            File newFile = new File(folderNumber.toFile(), f.fileName);
                                            //System.out.println(newFile.getAbsolutePath());
                                            String newFileName = newFile.getAbsolutePath();
                                            FileWriter newFile1 = new FileWriter(newFileName);
                                            newFile1.write(
                                                    content.substring(0, st.position.beginIndex)
                                                            + pr.statementAfter
                                                            + content.substring(st.position.endIndex + 1)
                                            );
                                            newFile1.close();
                                            predictionNumber++;
                                        }
                                    } catch (Exception e) {
                                        predictionNumber++;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Do nothing
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    /** BUILDER PART **/

    public static class MutatorBuilder
    {
        private Strategy strategy;
        private ArrayList<Language> languages = new ArrayList<Language>();
        private int threadsNumber;
        private Path exportPath;

        public MutatorBuilder setStrategy(Strategy s)
        {
            this.strategy = s;
            return this;
        }

        public MutatorBuilder addLanguage(Language l)
        {
            this.languages.add(l);
            return this;
        }

        public MutatorBuilder setThreadsNumber(int n)
        {
            this.threadsNumber = n;
            return this;
        }

        public MutatorBuilder setExportPath(Path p)
        {
            this.exportPath = p;
            return this;
        }

        public Mutator build()
        {
            return new Mutator(this);
        }
    }
}


