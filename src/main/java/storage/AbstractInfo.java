package storage;

import export.ExportVisitor;

/*
 * An abstract class to provide multiple information
 */
public abstract class AbstractInfo
{
    public PositionInfo position = new PositionInfo();

    public void setPosition(PositionInfo position) {
        this.position = position;
    }

    public abstract String visit(ExportVisitor visitor);
}
