package storage;

import export.ExportVisitor;

public class PositionInfo
{
    // The line where the statement begins, e.g: line 2
    public int beginLine = 0;

    // The column where the statement begins, e.g: column 8
    public int beginColumn = 0;

    // The line where the statement ends, e.g: line 2
    public int endLine = 0;

    // The column where the statement begins, e.g: column 17
    public int endColumn = 0;

    // The index in the code where the statement begins, e.g: 15
    public int beginIndex = 0;

    // The index in the code where the statement ends, e.g: 28
    public int endIndex = 0;

//    public FileInfo(AbstractInfo parent)
//    {
//
//    }

    @Override
    public String toString() {
        return "PositionInfo{" +
                "beginLine=" + beginLine +
                ", beginColumn=" + beginColumn +
                ", endLine=" + endLine +
                ", endColumn=" + endColumn +
                ", beginIndex=" + beginIndex +
                ", endIndex=" + endIndex +
                '}';
    }

    public String visit(ExportVisitor visitor) {
        return visitor.visitPositionInfo(this);
    }
}
