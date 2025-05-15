package parser.parsinghandle;

import core.Language;
import core.MaskParser;
import storage.ClassInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * A ParsingHandler is an object used to generate the different variants of a file
 * depending on its file extension (".java", ".py", etc.)
 * It is in fact a "Chain Of Responsibility"
 */
public abstract class ParsingHandler
{
    protected ParsingHandler nextHandler = null;

    // The language to be parsed
    protected Language lang;

    // The list of file extensions (e.g: ".cpp", ".c++", ".hpp", etc. for C++)
    protected ArrayList<String> fileExtensions;

    public ParsingHandler(){}

    public ParsingHandler(ParsingHandler nextHandler)
    {
        setNextHandler(nextHandler);
    }

    /**
     * Links every handler
     * @param first The first handler (e.g: JavaHandler)
     * @param chain A list of handler (e.g: CppHandler, PythonHandler, etc.)
     * @return The first handler
     */
    public static ParsingHandler link(ParsingHandler first, ParsingHandler... chain)
    {
        ParsingHandler head = first;
        for(ParsingHandler next: chain)
        {
            head.nextHandler = next;
            head = next;
        }
        return first;
    }

    public void setNextHandler(ParsingHandler nextHandler)
    {
        this.nextHandler = nextHandler;
    }

    /**
     * Sets the language and the file extensions list
     */
    protected abstract void setSettings();

    public ArrayList<ClassInfo> generateVariants(File file, ArrayList<MaskParser> parsers)
    {
        ArrayList<ClassInfo> classes = protectedGenerateVariants(file, parsers);

        if(classes != null) return classes;
        else if(nextHandler != null) return nextHandler.generateVariants(file, parsers);
        else return null;
    }

    /**
     * 1st step: check and identify if the file matches a language (e.g: "Class1.java" is a Java file)
     * 2nd step: generate variants by choosing the right parser
     * @param file
     * @return
     */
    protected ArrayList<ClassInfo> protectedGenerateVariants(File file, ArrayList<MaskParser> parsers)
    {
        String fileName = file.getName();
        for(String extension: fileExtensions)
        {
            if(fileName.endsWith(extension))
            {
                for(MaskParser parser: parsers) {
                    if (parser.getLanguage().equals(lang)) {
                        try {
                            return parser.generateVariants(Files.readString(file.toPath()), true);
                        } catch (IOException e) {
                            return null;
                        }
                    }
                }
            }
        }
        return null;
    }
}