package core;

import strategy.StrategyFillMask;

import java.util.List;

/*
 * This class provides methods in order to parse the code
 * e.g: generating mask variants such as for the binary expressions
 * Abstract because the parsing depends on the language and/or dependencies
 */
public abstract class MaskParser
{
    // A strategy object may be required because it contains infos like the mask, etc.
    protected StrategyFillMask strategy;

    public MaskParser(){}

    public MaskParser(StrategyFillMask strat)
    {
        this.strategy = strat;
    }

    public void setStrategy(StrategyFillMask strategy)
    {
        this.strategy = strategy;
    }

    /*
     * For a same code, return the list of the code with different masks (unary, binary, boolean)
     * @param sourceCode The code to parse
     */
    public abstract List<String> generateMaskVariants(String sourceCode);

    /*
     * Masks the left or right operand by adding it the variant list
     * e.g: "i = 1" --> "<mask> = 1"
     * @param expr The expression to mask
     * @param variants The list of existing variants
     */
    public abstract void binaryMaskOperand(Object expr, boolean left, List<String> variants);

    /*
     * Masks the operator by adding it to the variant list
     * e.g: "i = 1" --> "<mask> = 1"
     * @param expr The expression to mask
     * @param left If left = true, masking the left operand, right otherwise
     * @param variants The list of existing variants
     */
    public abstract void binaryMaskOperator(Object expr, List<String> variants);

    /*
     * Converts a line-column position to an index position
     */
    public static int lineColToIndex(String code, int line, int column)
    {
        // Splitting without removing empty lines
        String[] lines = code.split("\\r?\\n", -1);

        int idx = 0;
        for (int i = 1; i < line; i++)
        {
            idx += lines[i - 1].length();
            idx += 1;
        }

        idx += (column - 1);
        return idx;
    }
}
