package parser.parsinghandle;

import core.Language;
import core.MaskParser;
import parser.JavaMaskParser;
import storage.ClassInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * The handler for the Java language
 */
public class JavaHandler extends ParsingHandler
{
    public JavaHandler(){
        super();
        setSettings();
    }

    public JavaHandler(ParsingHandler nextHandler) {
        super(nextHandler);
        setSettings();
    }

    @Override
    protected void setSettings() {
        this.lang = Language.JAVA;
        this.fileExtensions = new ArrayList<>(List.of(".java"));
    }
}
