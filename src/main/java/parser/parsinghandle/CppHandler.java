package parser.parsinghandle;

import core.Language;
import core.MaskParser;
import storage.ClassInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The handler for the C++ language
 */
public class CppHandler extends ParsingHandler
{
    public CppHandler(){
        super();
        setSettings();
    }

    public CppHandler(ParsingHandler nextHandler) {
        super(nextHandler);
        setSettings();
    }

    @Override
    protected void setSettings() {
        this.lang = Language.CPP;
        this.fileExtensions = new ArrayList<>(List.of(".cpp", ".c++", ".cxx", ".hpp", ".h++", ".hxx"));
    }
}
