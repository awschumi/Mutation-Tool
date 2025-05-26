package storage;

import export.ExportVisitor;

import java.util.ArrayList;

/*
 * This class provides infos about masking for the mutation
 * e.g: masking for "<mask> = 3"
 */
public class MaskingInfo extends AbstractInfo
{
    // e.g: binaryMaskOperand, unaryMaskOperand, etc
    public String maskingType = "";

    public PositionInfo position = new PositionInfo();

    public ArrayList<PredictionInfo> predictions = new ArrayList<PredictionInfo>();

    public void setMaskingType(String maskingType)
    {
        this.maskingType = maskingType;
    }

    @Override
    public String toString() {
        return "MaskingInfo{" +
                "maskingType='" + maskingType + '\'' +
                ", position=" + position +
                ", prediction=" + predictions +
                '}';
    }

    @Override
    public String visit(ExportVisitor visitor) {
        return visitor.visitMaskingInfo(this);
    }
}
