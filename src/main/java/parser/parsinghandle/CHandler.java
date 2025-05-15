package parser.parsinghandle;

import core.Language;

import java.util.ArrayList;
import java.util.List;

/**
 * The handler for the C language
 */
public class CHandler extends ParsingHandler
{
    public CHandler(){
        super();
        setSettings();
    }

    public CHandler(ParsingHandler nextHandler) {
        super(nextHandler);
        setSettings();
    }

    @Override
    protected void setSettings() {
        this.lang = Language.C;
        this.fileExtensions = new ArrayList<>(List.of(".c", ".h"));
    }
}
