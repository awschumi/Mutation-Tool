package storage;

import export.ExportVisitor;

import java.util.ArrayList;

/*
 * This class provides infos about statements for the mutation
 */
public class StatementInfo extends AbstractInfo
{
    // The statement, e.g: "int i = 1;" or
    // "MyObject obj = new MyObject.Builder().build();"
    public String statement = "";

    public ArrayList<MaskingInfo> maskingInfos = new ArrayList<>();

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public void addMaskingInfos(MaskingInfo maskingInfo)
    {
        this.maskingInfos.add(maskingInfo);
    }

    @Override
    public String toString() {
        return "StatementInfo{" +
                "statement='" + statement + '\'' +
                ", maskingInfos=" + maskingInfos +
                ", position=" + position +
                '}';
    }

    @Override
    public String visit(ExportVisitor visitor) {
        return visitor.visitStatementInfo(this);
    }
}
