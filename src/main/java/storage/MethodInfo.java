package storage;

import export.ExportVisitor;

import java.util.ArrayList;

/*
 * This class provides infos about methods for the mutation
 */
public class MethodInfo extends AbstractInfo
{
    // The method name, e.g: "add"
    public String methodName = "";

    // The method declaration, e.g: "double add(double,double)"
    public String declaration = "";

    // The info about the statements of the class
    public ArrayList<StatementInfo> statements = new ArrayList<StatementInfo>();

    public void setMethodName(String methodName)
    {
        this.methodName = methodName;
    }

    public void setDeclaration(String declaration)
    {
        this.declaration = declaration;
    }

    public void addStatement(StatementInfo statementInfo)
    {
        statements.add(statementInfo);
    }

    @Override
    public String toString() {
        return "MethodInfo{" +
                "methodName='" + methodName + '\'' +
                ", declaration='" + declaration + '\'' +
                ", statements=" + statements +
                ", position=" + position +
                '}';
    }

    @Override
    public String visit(ExportVisitor visitor) {
        return visitor.visitMethodInfo(this);
    }
}
