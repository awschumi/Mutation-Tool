package strategy;

import core.Mutator;
import core.ObjectForThread;
import core.Strategy;
import storage.*;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.Future;

public class StrategyLLM extends Strategy
{
    private CustomClient client;

    public StrategyLLM(CustomClient client)
    {
        // TODO
    }

    public StrategyLLM()
    {
        // TODO
    }


    @Override
    public FileInfo mutate(File fileToMutate)
    {
        FileInfo fileInfo = new FileInfo();
        fileInfo.fileName = fileToMutate.getName();
        fileInfo.pathName = fileToMutate.getAbsolutePath();
        fileInfo.language = "Java"; // To change to be dynamically determined
        fileInfo.strategy = this.getClass().getName();

        try
        {
            String codeToMutate = Files.readString(fileToMutate.toPath());

            // Generate every variant of the code with masks
            ArrayList<ClassInfo> classes = Mutator.getInstance().getFileHandler().generateVariants(fileToMutate, Mutator.getInstance().getParsers());
            if(classes == null) return null;

            ArrayList<ObjectForThread> threads = new ArrayList<ObjectForThread>();
            ArrayList<Future<?>> futures = new ArrayList<Future<?>>();

            // Browse every mask info to determine what to mutate
            // Browse every mask info to generate the whole code masked
            // 1. Generate pre-threads
            for(ClassInfo cl: classes) {
                for (MethodInfo me : cl.methods) {
                    for (StatementInfo st : me.statements) {
                        for (MaskingInfo ma : st.maskingInfos) {
                            ObjectForThread o = new ObjectForThread(this) {
                                @Override
                                public void run()
                                {

                                }
                            };
                            threads.add(o);
                        }
                    }
                }
            }

            // 2. Start the threads
            for(ObjectForThread o: threads) futures.add(Mutator.getInstance().getSharedPool().submit(o));

            // 3. Join all the threads
            for (Future<?> f : futures) f.get();

            fileInfo.classes = classes;
        }
        catch (Exception e)
        {
            return null;
        }

        return fileInfo;
    }
}
