package storage;

import export.ExportVisitor;

import java.util.HashMap;

public class PredictionInfo extends AbstractInfo
{
    public String tokenPredicted = "";

    public String statementBefore = "";

    public String statementAfter = "";

    public String pathToOutput = "";

    public HashMap<String, String> metrics = new HashMap<String, String>();

    @Override
    public String toString() {
        return "PredictionInfo{" +
                "tokenPredicted='" + tokenPredicted + '\'' +
                ", statementBefore='" + statementBefore + '\'' +
                ", statementAfter='" + statementAfter + '\'' +
                ", pathToOutput='" + pathToOutput + '\'' +
                ", metrics=" + metrics +
                ", position=" + position +
                '}';
    }

    @Override
    public String visit(ExportVisitor visitor) {
        return visitor.visitPredictionInfo(this);
    }
}
