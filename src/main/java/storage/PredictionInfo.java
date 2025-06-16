package storage;

import export.ExportVisitor;

import java.util.HashMap;

public class PredictionInfo extends AbstractInfo
{
    public String tokenPredicted = "";

    public String preCode = "";

    public String afterCode = "";

    public String pathToOutput = "";

    public HashMap<String, String> metrics = new HashMap<String, String>();

    public PredictionInfo()
    {
        this.info = Info.PREDICTION_INFO;
    }

    public PredictionInfo(AbstractInfo parent)
    {
        super(parent);
        this.info = Info.PREDICTION_INFO;
    }

    @Override
    public String toString() {
        return "PredictionInfo{" +
                "tokenPredicted='" + tokenPredicted + '\'' +
                ", preCode='" + preCode + '\'' +
                ", afterCode='" + afterCode + '\'' +
                ", pathToOutput='" + pathToOutput + '\'' +
                ", metrics=" + metrics +
                ", children=" + children +
                '}';
    }

    @Override
    public String visit(ExportVisitor visitor) {
        return visitor.visitPredictionInfo(this);
    }
}
