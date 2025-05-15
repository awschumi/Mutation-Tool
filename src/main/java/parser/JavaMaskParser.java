package parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import core.Language;
import core.MaskParser;
import storage.*;
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

    public JavaMaskParser(){ this.language = Language.JAVA; }

    public JavaMaskParser(StrategyFillMask strat)
    {
        super(strat);
    }

    /*
     * Extracts the position from an expression
     */
    public static PositionInfo rangeToPosition(Range range, String[] codeInLines)
    {
        PositionInfo positionInfo = new PositionInfo();
        positionInfo.setBeginLine(range.begin.line);
        positionInfo.setBeginColumn(range.begin.column);
        positionInfo.setEndLine(range.end.line);
        positionInfo.setEndColumn(range.end.column);
        positionInfo.setBeginIndex(lineColToIndex(codeInLines,positionInfo.beginLine,positionInfo.beginColumn));
        positionInfo.setEndIndex(lineColToIndex(codeInLines,positionInfo.endLine,positionInfo.endColumn));
        return positionInfo;
    }

    /*
     * @param toMask If true, will generate mask code, nothing otherwise
     */
    @Override
    public ArrayList<ClassInfo> generateVariants(String sourceCode, boolean toMask)
    {
        ArrayList<ClassInfo> classes = new ArrayList<ClassInfo>();
        String[] codeInLines = sourceCode.split("\\r?\\n", -1);

        // Configuration of the parser
        ParserConfiguration config =
                new ParserConfiguration()
                        .setLexicalPreservationEnabled(true);
        JavaParser parser = new JavaParser(config);
        CompilationUnit cu = parser
                .parse(sourceCode)
                .getResult()
                .orElseThrow();
        LexicalPreservingPrinter.setup(cu);

        // Parsing every class
        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(
                cls ->
                {
                    ClassInfo classInfo = new ClassInfo();

                    // Stores the current class infos
                    classInfo.setClassName(cls.getNameAsString());
                    classInfo.setPosition(rangeToPosition(cls.getRange().get(), codeInLines));

                    ArrayList<MethodInfo> methods = new ArrayList<MethodInfo>();

                    // Parsing every method
                    for(MethodDeclaration meth: cls.getMethods())
                    {
                        // Stores the current method info
                        MethodInfo methodInfo = new MethodInfo();

                        // 1) Browse every method + get some methods infos
                        methodInfo.setMethodName(meth.getNameAsString());
                        methodInfo.setPosition(rangeToPosition(meth.getRange().get(), codeInLines));
                        methodInfo.setDeclaration(meth.getDeclarationAsString(false,false,false));

                        // 2) Browse every statement (≈ lines),
                        // get infos & check for binary / unary operators

                        // Parsing every statement
                        for(Statement n: meth.getBody().get().getStatements())
                        {
                            // Stores the current statement info
                            StatementInfo statementInfo = new StatementInfo();

                            statementInfo.setStatement(n.toString());
                            statementInfo.setPosition(rangeToPosition(n.getRange().get(), codeInLines));

                            methodInfo.addStatement(statementInfo);

                            if(toMask)
                            {
                                // Parsing every expression
                                for (BinaryExpr expr : n.findAll(BinaryExpr.class))
                                {
                                    statementInfo.addMaskingInfos(binaryMaskOperand(expr, true, codeInLines));
                                    statementInfo.addMaskingInfos(binaryMaskOperand(expr, false, codeInLines));
                                    statementInfo.addMaskingInfos(binaryMaskOperator(expr, codeInLines));
                                }

//                                for(UnaryExpr expr: n.findAll(UnaryExpr.class))
//                                {
//                                    // TODO
//                                }
                            }
                            methodInfo.addStatement(statementInfo);
                        }
                        classInfo.addMethodInfo(methodInfo);
                    }
                    classes.add(classInfo);
                });

        return classes;
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

    public MaskingInfo binaryMaskOperand(BinaryExpr expr, boolean left, String[] codeInLines)
    {
        MaskingInfo maskingInfo = new MaskingInfo();
        if(left)
        {
            maskingInfo.position = (rangeToPosition(expr.getLeft().getRange().get(), codeInLines));
            maskingInfo.setMaskingType("BinaryExpressionLeft");
        }
        else
        {
            maskingInfo.position = (rangeToPosition(expr.getRight().getRange().get(), codeInLines));
            maskingInfo.setMaskingType("BinaryExpressionRight");
        }
        return maskingInfo;
    }

    @Override
    public MaskingInfo binaryMaskOperand(Object expr, boolean left, String[] codeInLines)
    {
        return binaryMaskOperand((BinaryExpr) expr, left, codeInLines);
    }

    public MaskingInfo binaryMaskOperator(BinaryExpr expr, String[] codeInLines)
    {
        MaskingInfo maskingInfo = new MaskingInfo();

        // Operator replacement Function
        expr.getTokenRange().ifPresent(tokenRange -> {
            // Look for token, e.g: '+'
            tokenRange.forEach(token -> {
                if (token.getText().equals(expr.getOperator().asString())) {
                    maskingInfo.position = (rangeToPosition(token.getRange().get(), codeInLines));
                    maskingInfo.setMaskingType("BinaryExpressionToken");
                }
            });
        });

        return maskingInfo;
    }

    @Override
    public MaskingInfo binaryMaskOperator(Object expr, String[] codeInLines)
    {
        return binaryMaskOperator((BinaryExpr) expr, codeInLines);
    }
}
