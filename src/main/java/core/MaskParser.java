package core;

import com.github.javaparser.ast.expr.BinaryExpr;
import storage.ClassInfo;
import storage.MaskingInfo;
import strategy.StrategyFillMask;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides methods in order to parse the code
 * e.g: generating mask variants such as for the binary expressions
 * Abstract because the parsing depends on the language and/or dependencies
 */
public abstract class MaskParser
{
    // A strategy object may be required because it contains infos like the mask, etc.
    protected StrategyFillMask strategy;

    // The language that can be parsed
    protected Language language;

    protected MaskParser(){}

    public MaskParser(StrategyFillMask strat)
    {
        this.strategy = strat;
    }

    public void setStrategy(StrategyFillMask strategy)
    {
        this.strategy = strategy;
    }

    public Language getLanguage()
    {
        return this.language;
    }

    /*
     * For a same code, return the list of the code with different masks (unary, binary, boolean)
     * @param sourceCode The code to parse
     * @param toMask
     */
    public abstract ArrayList<ClassInfo> generateVariants(String sourceCode, boolean toMask);

    /**
     * Masks the left or right operand
     * e.g: "i = 1" --> "<mask> = 1"
     * @param expr The expression to mask
     * @param left If left = true, masks only the left operand, if = false, it's the right operand
     * @param codeInLines The code split in lines
     */
    public abstract MaskingInfo binaryMaskOperand(Object expr, boolean left, String[] codeInLines);

    /**
     * Masks the operator
     * e.g: "i = 1" --> "<mask> = 1"
     * @param expr The expression to mask
     * @param codeInLines The code split in lines
     */
    public abstract MaskingInfo binaryMaskOperator(Object expr, String[] codeInLines);

    /**
     * Converts a line-column position to an index position
     */
    public static int lineColToIndex(String[] codeInLines, int line, int column)
    {
        int idx = 0;
        for (int i = 1; i < line; i++)
        {
            idx += codeInLines[i - 1].length();
            idx += 1;
        }

        idx += (column - 1);
        return idx;
    }
}
