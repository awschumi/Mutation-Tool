package parser.parsinghandle;

import core.Language;
import core.MaskParser;
import storage.ClassInfo;
import strategy.StrategyFillMask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The handler for the Python language
 */
public class PythonHandler extends ParsingHandler
{
    public PythonHandler(){
        super();
        setSettings();
    }

    public PythonHandler(ParsingHandler nextHandler) {
        super(nextHandler);
        setSettings();
    }

    @Override
    protected void setSettings() {
        this.lang = Language.PYTHON;
        this.fileExtensions = new ArrayList<>(List.of(".py"));
    }
}
