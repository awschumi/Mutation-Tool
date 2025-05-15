package core;

import java.io.File;
import java.util.HashMap;

/**
 * This class represents a mutation with some information,
 * like the original file, a list of metrics etc.
 */
public class Mutant
{
    // The file containing the original code
    private File originalFile;

    // The line where the mutation has occurred
    private int lineNumber;

    // The code from the mutated line
    private String lineCode;

    // The mutation
    private String mutation;

    // A map to store the different metrics:
    // key (String): metric name (e.g: "confident score")
    // value (String): metric value (e.g: "0.5")
    private HashMap<String, String> metrics;

    public Mutant(File originalFile,
                  int lineNumber,
                  String lineCode,
                  String mutation)
    {
        this.originalFile = originalFile;
        this.lineNumber = lineNumber;
        this.lineCode = lineCode;
        this.mutation = mutation;
        this.metrics = new HashMap<String, String>();
    }

    public void addMetric(String key, String value)
    {
        this.metrics.put(key, value);
    }

    public String getMutation() {
        return mutation;
    }

    public File getOriginalFile() {
        return originalFile;
    }

    public HashMap<String, String> getMetrics() {
        return metrics;
    }

    public String getLineCode() {
        return lineCode;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String toString()
    {
        String result = "Mutant[" + "\n";
        result += "file: " + this.originalFile.getAbsolutePath() + ",\n";
        result += "mutation: " + this.mutation + ",\n";
        result += "line number: " + this.lineNumber + ",\n";
        result += "line code: " + this.lineCode + ",\n";
        for(HashMap.Entry<String, String> entry : this.metrics.entrySet())
        {
            String key = entry.getKey();
            String value = entry.getValue();
            result += key + ": " + value + ",\n";
        }
        result += "]";
        return result;
    }
}
