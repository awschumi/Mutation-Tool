package storage;

import export.ExportVisitor;

import java.util.ArrayList;
import java.util.HashMap;

public class FileInfo extends AbstractInfo
{
    // e.g: Class1.java
    public String fileName = "";

    // e.g: /usr/home/Class1.java
    public String pathName = "";

    // e.g: Java
    public String language = "";

    // e.g: Fill-mask
    public String strategy = "";

    /* e.g:
            <"mask":"<mask>",
             "sep":"<s>",
             "cls":"</s>",
             "LLM":"GPT-4o-mini">
     */
    public HashMap<String, String> otherInfo = new HashMap<String, String>();

    public ArrayList<ClassInfo> classes = new ArrayList<ClassInfo>();

    @Override
    public String toString() {
        return "FileInfo{" +
                "\nfileName='" + fileName + '\'' +
                ", \npathName='" + pathName + '\'' +
                ", \nlanguage='" + language + '\'' +
                ", \nstrategy='" + strategy + '\'' +
                ", \notherInfo=" + otherInfo +
                ", \nclasses=" + classes +
                "\n}";
    }

    @Override
    public String visit(ExportVisitor visitor) {
        return visitor.visitFileInfo(this);
    }
}
