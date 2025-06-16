package storage;

import export.ExportVisitor;

public class ClassInfo extends AbstractInfo
{
    // The class name e.g: "Class1"
    public String className = "";

    public PositionInfo position = new PositionInfo();

    public ClassInfo()
    {
        super();
        this.info = Info.CLASS_INFO;
    }

    public ClassInfo(AbstractInfo parent)
    {
        super(parent);
        this.info = Info.CLASS_INFO;
    }

    @Override
    public String toString() {
        return "ClassInfo{" +
                "position=" + position +
                ", children=" + children +
                '}';
    }

    @Override
    public String visit(ExportVisitor visitor) {
        return visitor.visitClassInfo(this);
    }
}
