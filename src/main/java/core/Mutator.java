package core;

import java.io.File;
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
    public void mutate(File file)
    {
        try
        {
            System.out.println("FILE NAME: " + file.getAbsolutePath());
            ArrayList<Mutant> mutants = this.strategy.mutate(file);
            System.out.println("For the file " + file.getPath() + ", " + mutants.size() + " mutants have been generated");
            for(Mutant m: mutants) System.out.println(m);
            System.out.println("*******#######*******");
        }
        catch (Exception e){
            System.out.println(e);
        }
    }

    /*
     * Mutates all the possible files included in the directory
     */
    public void mutateAll(Path path)
    {
        try {
            Stream<Path> paths = Files.walk(path);

            paths.filter(Files::isRegularFile)
                    .forEach(p ->
                    {
                        mutate(p.toFile());
                    });
            System.out.println("/** FILE MUTATED **/");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void mutateAll(String path)
    {
        mutateAll(Path.of(path));
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


