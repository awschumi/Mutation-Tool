package storage;

import export.ExportVisitor;

public class FunctionInfo extends AbstractInfo
{
    // The method name, e.g: "add"
    public String functionName = "";

    // The method declaration, e.g: "double add(double,double)"
    public String signature = "";

    public PositionInfo position = new PositionInfo();

    public FunctionInfo()
    {
        this.info = Info.FUNCTION_INFO;
    }

    public FunctionInfo(AbstractInfo parent)
    {
        super(parent);
        this.info = Info.FUNCTION_INFO;
    }

    @Override
    public String toString() {
        return "FunctionInfo{" +
                "functionName='" + functionName + '\'' +
                ", signature='" + signature + '\'' +
                ", position=" + position +
                ", children=" + children +
                '}';
    }

    @Override
    public String visit(ExportVisitor visitor) {
        return visitor.visitFunctionInfo(this);
    }
}
