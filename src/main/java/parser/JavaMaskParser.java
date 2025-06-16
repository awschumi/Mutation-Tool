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
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import core.Language;
import core.MaskParser;
import storage.ClassInfo;
import storage.MethodInfo;
import storage.MutationInfo;
import storage.PositionInfo;
import strategy.StrategyFillMask;

import java.io.File;
import java.nio.file.Files;
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

    @Override
    public ArrayList<ClassInfo> generateVariants(File fileCode, boolean toMask)
    {
        try
        {
            return generateVariants(Files.readString(fileCode.toPath().toAbsolutePath()), toMask);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /*
     * Extracts the position from an expression
     */
    public static PositionInfo rangeToPosition(Range range, String[] codeInLines)
    {
        PositionInfo positionInfo = new PositionInfo();
        positionInfo.beginLine = range.begin.line;
        positionInfo.beginColumn = range.begin.column;
        positionInfo.endLine = range.end.line;
        positionInfo.endColumn = range.end.column;
        positionInfo.beginIndex = lineColToIndex(codeInLines,positionInfo.beginLine,positionInfo.beginColumn);
        positionInfo.endIndex = lineColToIndex(codeInLines,positionInfo.endLine,positionInfo.endColumn);
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
                    ClassInfo classInfo = new ClassInfo(null);

                    // Stores the current class infos
                    classInfo.className = cls.getNameAsString();
                    classInfo.position = rangeToPosition(cls.getRange().get(), codeInLines);

                    ArrayList<MethodInfo> methods = new ArrayList<MethodInfo>();

                    // Parsing every method
                    for(MethodDeclaration meth: cls.getMethods())
                    {
                        // Stores the current method info
                        MethodInfo methodInfo = new MethodInfo(classInfo);

                        // 1) Browse every method + get some methods infos
                        methodInfo.methodName = meth.getNameAsString();
                        methodInfo.position = rangeToPosition(meth.getRange().get(), codeInLines);
                        methodInfo.signature = meth.getDeclarationAsString(false,false,false);

                        // 2) Get every mutation info
                        // get infos & check for binary / unary operators

                        if(toMask)
                        {
                            // Parsing every expression
                            for (BinaryExpr expr : meth.findAll(BinaryExpr.class))
                            {
                                MutationInfo mutationInfo = new MutationInfo(methodInfo);
                                mutationInfo.children.add(binaryMaskOperand(expr, true, codeInLines));
                                mutationInfo.children.add(binaryMaskOperand(expr, false, codeInLines));
                                mutationInfo.children.add(binaryMaskOperator(expr, codeInLines));
                                methodInfo.children.add(mutationInfo);
                            }

//                            for(UnaryExpr expr: n.findAll(UnaryExpr.class))
//                            {
//                                // TODO
//                            }
                        }

                        classInfo.children.add(methodInfo);
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

    public MutationInfo binaryMaskOperand(BinaryExpr expr, boolean left, String[] codeInLines)
    {
        MutationInfo maskingInfo = new MutationInfo();
        if(left)
        {
            maskingInfo.position = (rangeToPosition(expr.getLeft().getRange().get(), codeInLines));
            maskingInfo.maskingType = "BinaryExpressionLeft";
        }
        else
        {
            maskingInfo.position = (rangeToPosition(expr.getRight().getRange().get(), codeInLines));
            maskingInfo.maskingType = "BinaryExpressionRight";
        }
        return maskingInfo;
    }

    @Override
    public MutationInfo binaryMaskOperand(Object expr, boolean left, String[] codeInLines)
    {
        return binaryMaskOperand((BinaryExpr) expr, left, codeInLines);
    }

    public MutationInfo binaryMaskOperator(BinaryExpr expr, String[] codeInLines)
    {
        MutationInfo maskingInfo = new MutationInfo();

        // Operator replacement Function
        expr.getTokenRange().ifPresent(tokenRange -> {
            // Look for token, e.g: '+'
            tokenRange.forEach(token -> {
                if (token.getText().equals(expr.getOperator().asString())) {
                    maskingInfo.position = (rangeToPosition(token.getRange().get(), codeInLines));
                    maskingInfo.maskingType = "BinaryExpressionToken";
                }
            });
        });

        return maskingInfo;
    }

    @Override
    public MutationInfo binaryMaskOperator(Object expr, String[] codeInLines)
    {
        return binaryMaskOperator((BinaryExpr) expr, codeInLines);
    }
}
