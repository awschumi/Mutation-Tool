package imports;

import parser.parsinghandle.ParsingHandler;
import storage.AbstractInfo;

import java.io.File;
import java.util.ArrayList;

/**
 * An ImportHandler is an object used to import an info like ClassInfo, FileInfo
 * from a file. This file can have an XML, JSON, or custom txt format.
 * It is in fact a "Chain Of Responsibility"
 */
public abstract class ImportHandler
{
    protected ImportHandler nextHandler = null;

    // The list of file extensions (e.g: ".json", ".xml", ".txt", etc.)
    protected ArrayList<String> fileExtensions;

    public ImportHandler(){}

    public ImportHandler(ImportHandler nextHandler)
    {
        setNextHandler(nextHandler);
    }

    /**
     * Links every handler
     * @param first The first handler (e.g: JavaHandler)
     * @param chain A list of handler (e.g: CppHandler, PythonHandler, etc.)
     * @return The first handler
     */
    public static ImportHandler link(ImportHandler first, ImportHandler... chain)
    {
        ImportHandler head = first;
        for(ImportHandler next: chain)
        {
            head.nextHandler = next;
            head = next;
        }
        return first;
    }

    public void setNextHandler(ImportHandler nextHandler)
    {
        this.nextHandler = nextHandler;
    }

    public AbstractInfo getInfo(File file)
    {
        if(file == null) return null;
        if(!file.exists()) return null;
        try
        {
            for(String extension: fileExtensions) {
                if (file.getName().endsWith(extension))
                {
                    return this.protectedGetInfo(file);
                }
            }
            return null;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Once we are here, we know the file is not null, exists and has the right extension
     * @param file
     * @return
     */
    protected abstract AbstractInfo protectedGetInfo(File file);
}
