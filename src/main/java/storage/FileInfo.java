package storage;

import export.ExportVisitor;

public class FileInfo extends AbstractInfo
{
    // e.g: Class1.java
    public String fileName = "";

    // e.g: /usr/home/Class1.java
    public String pathName = "";

    // e.g: Java
    public String language = "";

    public FileInfo()
    {
        super();
        this.info = Info.FILE_INFO;
    }

    public FileInfo(AbstractInfo parent)
    {
        super(parent);
        this.info = Info.FILE_INFO;
    }


    @Override
    public String toString() {
        return "FileInfo{" +
                "fileName='" + fileName + '\'' +
                ", pathName='" + pathName + '\'' +
                ", language='" + language + '\'' +
                ", children=" + children +
                '}';
    }

    @Override
    public String visit(ExportVisitor visitor) {
        return visitor.visitFileInfo(this);
    }
}
