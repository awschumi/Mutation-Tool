package storage;

import export.ExportVisitor;

public class MutationInfo extends AbstractInfo
{
    // e.g: binaryMaskOperand, unaryMaskOperand, fullLine, etc
    public String maskingType = "";

    public PositionInfo position = new PositionInfo();

    public MutationInfo()
    {
        this.info = Info.MUTATION_INFO;
    }

    public MutationInfo(AbstractInfo parent)
    {
        super(parent);
        this.info = Info.MUTATION_INFO;
    }

    @Override
    public String toString() {
        return "MutationInfo{" +
                "children=" + children +
                ", position=" + position +
                ", maskingType='" + maskingType + '\'' +
                '}';
    }

    @Override
    public String visit(ExportVisitor visitor) {
        return visitor.visitMutationInfo(this);
    }
}
