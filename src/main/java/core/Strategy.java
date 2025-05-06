package core;

import java.io.File;
import java.util.ArrayList;

/*
 * This abstract class manages how the mutation will be done (fill-mask, LLMs)
 */
public abstract class Strategy
{
    public abstract ArrayList<Mutant> mutate(File fileToMutate);
}
