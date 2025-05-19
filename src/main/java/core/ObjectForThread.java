package core;

/**
 * A class for handling threads
 */
public class ObjectForThread extends Thread
{
    public Object object; // e.g: StrategyFillMask
    public Object returnObject; // e.g: what is returned

    public ObjectForThread(Object obj)
    {
        this.object = obj;
    }
}
