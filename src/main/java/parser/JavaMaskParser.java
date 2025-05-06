package parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import core.MaskParser;
import strategy.StrategyFillMask;

import java.util.ArrayList;
import java.util.List;

/*
 * QUESTION: If multiple threads are running and only one JavaMaskParser
 * is running with toMutate, will it cause side effects (shared file???)
 */
public class JavaMaskParser extends MaskParser
{
    private CompilationUnit cu;

    public JavaMaskParser(){}

    public JavaMaskParser(StrategyFillMask strat)
    {
        super(strat);
    }

    @Override
    public List<String> generateMaskVariants(String sourceCode)
    {
        // Configuration of the parser
        ParserConfiguration config = new ParserConfiguration()
                .setLexicalPreservationEnabled(true);
        JavaParser parser = new JavaParser(config);
        this.cu = parser.parse(sourceCode)
                .getResult().orElseThrow();
        LexicalPreservingPrinter.setup(cu);

        List<String> variants = new ArrayList<>();

        for (BinaryExpr expr : cu.findAll(BinaryExpr.class))
        {
            binaryMaskOperand(expr, true, variants);   // a -> <mask>
            binaryMaskOperand(expr, false, variants);  // b -> <mask>
            binaryMaskOperator(expr, variants);             // + -> <mask>
        }
        return variants;
    }

    public void binaryMaskOperand(BinaryExpr expr, boolean left, List<String> variants)
    {
        BinaryExpr copy = expr.clone();             // Clone the existing expression
        Expression mask = new NameExpr(strategy.getMask());   // Create the mask expression
        if (left) {                                 // left replacement
            expr.setLeft(mask);
        } else {                                    // right replacement
            expr.setRight(mask);
        }
        variants.add(LexicalPreservingPrinter.print(cu));
        expr.setLeft(copy.getLeft());
        expr.setRight(copy.getRight());
    }

    @Override
    public void binaryMaskOperand(Object expr, boolean left, List<String> variants)
    {
        this.binaryMaskOperand((BinaryExpr) expr, left, variants);
    }

    public void binaryMaskOperator(BinaryExpr expr, List<String> variants)
    {
        expr.getTokenRange().ifPresent(tokenRange -> {
            // Look for token, e.g: '+'
            tokenRange.forEach(token -> {
                if (token.getText().equals(expr.getOperator().asString())) {
                    // Get range (line/column, begin - end)
                    Range opRange = token.getRange()
                            .orElseThrow(() -> new IllegalStateException("Token without range"));

                    String code = LexicalPreservingPrinter.print(cu);
                    int begin = lineColToIndex(code, opRange.begin.line, opRange.begin.column);
                    int end = lineColToIndex(code, opRange.end.line, opRange.end.column);
                    String mutated = code.substring(0, begin) + strategy.getMask() + code.substring(end+1);
                    variants.add(mutated);
                }
            });
        });
    }

    @Override
    public void binaryMaskOperator(Object expr, List<String> variants)
    {
        binaryMaskOperator((BinaryExpr) expr, variants);
    }
}
