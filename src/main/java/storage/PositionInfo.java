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

    public void setBeginLine(int beginLine) {
        this.beginLine = beginLine;
    }

    public void setBeginColumn(int beginColumn) {
        this.beginColumn = beginColumn;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public void setEndColumn(int endColumn) {
        this.endColumn = endColumn;
    }

    public void setBeginIndex(int beginIndex) {
        this.beginIndex = beginIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    @Override
    public String toString() {
        String res = "[\nbegin_lin-col: [" + beginLine + "," + beginColumn + "],\n";
        res += "end_lin-col: [" + endLine + "," + endColumn + "],\n";
        res += "begin_end-index: [" + beginLine + "," + endIndex + "],\n";
        res += "]\n";
        return res;
    }

    public String visit(ExportVisitor visitor) {
        return visitor.visitPositionInfo(this);
    }
}
