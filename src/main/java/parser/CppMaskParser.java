package parser;

import ch.usi.si.seart.treesitter.*;
import com.github.javaparser.Range;
import core.MaskParser;
import storage.*;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class CppMaskParser extends MaskParser
{
    /// IMPORTANT TO LOAD THE SHARED OBJECT !!!
    static {
        LibraryLoader.load();
    }

    public CppMaskParser() { this.language = core.Language.CPP; }

    @Override
    public ArrayList<ClassInfo> generateVariants(File fileCode, boolean toMask) {
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
    public static PositionInfo rangeToPosition(Point begin, Point end, String[] codeInLines)
    {
        PositionInfo positionInfo = new PositionInfo();
        positionInfo.setBeginLine(begin.getRow()+1);
        positionInfo.setBeginColumn(begin.getColumn()+1);
        positionInfo.setEndLine(end.getRow()+1);
        positionInfo.setEndColumn(end.getColumn()+1);
        positionInfo.setBeginIndex(lineColToIndex(codeInLines,positionInfo.beginLine,positionInfo.beginColumn));
        positionInfo.setEndIndex(lineColToIndex(codeInLines,positionInfo.endLine,positionInfo.endColumn));
        return positionInfo;
    }

    @Override
    public ArrayList<ClassInfo> generateVariants(String sourceCode, boolean toMask) {
        ArrayList<ClassInfo> classes = new ArrayList<ClassInfo>();
        String[] codeInLines = sourceCode.split("\\r?\\n", -1);

        Parser parser = Parser.getFor(Language.CPP);
        Tree tree = parser.parse(sourceCode);

        // Get every class node
        List<Node> listOfClassNodes = new ArrayList<>();
        getNodes(tree.getRootNode(), "class_specifier", listOfClassNodes);
        for(Node classNode: listOfClassNodes)
        {
            // Stores the current class infos
            ClassInfo classInfo = new ClassInfo();
            classInfo.className = classNode.getChild(1).getContent();
            classInfo.position = rangeToPosition(classNode.getStartPoint(), classNode.getEndPoint(), codeInLines);
            //System.out.println("The class: " + classInfo.className);

            // Get every method/function node
            List<Node> listOfMethodNodes = new ArrayList<>();
            getNodes(classNode, "function_definition", listOfMethodNodes);
            for(Node methodNode: listOfMethodNodes)
            {
                // Stores the current method info
                MethodInfo methodInfo = new MethodInfo();
                methodInfo.methodName = methodNode.getChild(1).getChild(0).getContent();
                methodInfo.declaration = methodNode.getChild(0).getContent() + " " + methodNode.getChild(1).getContent();
                methodInfo.position = rangeToPosition(methodNode.getStartPoint(), methodNode.getEndPoint(), codeInLines);
                //System.out.println("\tThe method/function: [" + methodNode.getChild(0).getContent() + " " + methodNode.getChild(1).getContent() + "]");

                // Get every statement
                List<Node> listOfStatementNodes = new ArrayList<>();
                getNodes(methodNode, "statement", listOfStatementNodes);
                for(Node statementNode: listOfStatementNodes)
                {
                    // Stores the current statement info
                    StatementInfo statementInfo = new StatementInfo();

                    statementInfo.statement = statementNode.getContent();
                    statementInfo.position = rangeToPosition(statementNode.getStartPoint(), statementNode.getEndPoint(), codeInLines);
                    //System.out.println("\t\tThe statement: " + statementNode.getContent());
                    if(toMask)
                    {
                        // Get every sub nodes
                        List<Node> listOfSubNodes = new ArrayList<>();
                        getNodes(statementNode, "", listOfSubNodes);

                        // Parsing every expression in every statement

                        for(Node expr: listOfSubNodes)
                        {
                            // Binary
                            if(expr.getType().equals("binary_expression"))
                            {
                                statementInfo.addMaskingInfos(binaryMaskOperand(expr, true, codeInLines));
                                statementInfo.addMaskingInfos(binaryMaskOperand(expr, false, codeInLines));
                                statementInfo.addMaskingInfos(binaryMaskOperator(expr, codeInLines));
                            }

                            // Unary
                        }
                    }
                    methodInfo.addStatement(statementInfo);
                }
                classInfo.addMethodInfo(methodInfo);
            }
            classes.add(classInfo);
        }

        return classes;
    }

    /**
     */
    private void getNodes(Node n, String type, List<Node> listOfNodes)
    {
        ArrayList<String> typesOfStatements = new ArrayList<>();
        typesOfStatements.add("expression_statement");
        typesOfStatements.add("declaration");
        typesOfStatements.add("return_statement");
        typesOfStatements.add("if_statement");
        typesOfStatements.add("for_statement");
        /*
         A compound statement = all statements that are btw { and }
         Don't add it because we only want a single statement!!!
         */
        if(listOfNodes != null)
        {
            // Case statement: add all the statements (except compound_statement)
            if(type.equals("statement"))
            {
                boolean wasAdded = false;
                for(String possibleStatement: typesOfStatements)
                {
                    if(possibleStatement.equals(n.getType()))
                    {
                        listOfNodes.add(n);
                        wasAdded = true;
                        break;
                    }
                }

                if(!wasAdded)
                    for (Node child : n.getChildren()) {
                        getNodes(child, type, listOfNodes);
                    }
            }
            // Case where we want every sub nodes
            else if(type.isEmpty())
            {
                listOfNodes.add(n);
                for (Node child : n.getChildren()) {
                    getNodes(child, type, listOfNodes);
                }
            }

            // Classic behavior
            else {
                if (n.getType().contains(type)) listOfNodes.add(n);
                for (Node child : n.getChildren())
                    getNodes(child, type, listOfNodes);
            }
        }
    }

    @Override
    public MaskingInfo binaryMaskOperand(Object expr, boolean left, String[] codeInLines) {
        return binaryMaskOperand((Node)expr, left, codeInLines);
    }

    public MaskingInfo binaryMaskOperand(Node expr, boolean left, String[] codeInLines) {
        MaskingInfo maskingInfo = new MaskingInfo();
        if(left)
        {
            maskingInfo.position = rangeToPosition(expr.getChild(0).getStartPoint(), expr.getChild(0).getEndPoint(), codeInLines);
            maskingInfo.setMaskingType("BinaryExpressionLeft");
        }
        else
        {
            maskingInfo.position = rangeToPosition(expr.getChild(2).getStartPoint(), expr.getChild(2).getEndPoint(), codeInLines);
            maskingInfo.setMaskingType("BinaryExpressionRight");
        }
        return maskingInfo;
    }

    @Override
    public MaskingInfo binaryMaskOperator(Object expr, String[] codeInLines) {
        return binaryMaskOperator((Node)expr, codeInLines);
    }

    public MaskingInfo binaryMaskOperator(Node expr, String[] codeInLines){
        MaskingInfo maskingInfo = new MaskingInfo();

        maskingInfo.maskingType = "BinaryExpressionToken";
        maskingInfo.position = rangeToPosition(expr.getChild(1).getStartPoint(), expr.getChild(1).getEndPoint(), codeInLines);

        return maskingInfo;
    }
}
