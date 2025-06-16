package storage;

import export.ExportVisitor;


public class MethodInfo extends AbstractInfo
{
    // The method name, e.g: "add"
    public String methodName = "";

    // The method signature, e.g: "double add(double,double)"
    public String signature = "";

    public PositionInfo position = new PositionInfo();

    public MethodInfo()
    {
        this.info = Info.METHOD_INFO;
    }

    public MethodInfo(AbstractInfo parent)
    {
        super(parent);
        this.info = Info.METHOD_INFO;
    }

    @Override
    public String toString() {
        return "MethodInfo{" +
                "children=" + children +
                ", position=" + position +
                ", signature='" + signature + '\'' +
                ", methodName='" + methodName + '\'' +
                '}';
    }

    @Override
    public String visit(ExportVisitor visitor) {
        return visitor.visitMethodInfo(this);
    }
}
