package storage;

import export.ExportVisitor;
import java.util.ArrayList;

public abstract class AbstractInfo
{
    public AbstractInfo parent = null;

    public ArrayList<AbstractInfo> children = new ArrayList<>();

    public Info info;

    public AbstractInfo(){}

    public AbstractInfo(AbstractInfo parent)
    {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return "AbstractInfo{" +
                "children=" + children +
                '}';
    }

    public abstract String visit(ExportVisitor visitor);

    /**
     *
     * @param info The parent type we are looking for
     * @return The 1st encountered parent if it exists
     */
    public AbstractInfo getSpecificParent(Info info)
    {
        AbstractInfo currentParent = this.parent;
        if(currentParent == null) return null;
        if(currentParent.info.equals(info)) return currentParent;
        else return currentParent.getSpecificParent(info);
    }

    /**
     * Generate the list of every node and sub node having the type "info" from this node
     * @param info The child type we are looking for
     * @return The list of the children
     */
    public ArrayList<AbstractInfo> getSpecificChildren(Info info)
    {
        ArrayList<AbstractInfo> infos = new ArrayList<AbstractInfo>();
        protectedGetSpecificChildren(this, info, infos);
        return infos;
    }

    protected void protectedGetSpecificChildren(AbstractInfo node, Info info, ArrayList<AbstractInfo> infos)
    {
        if(infos == null) return;

        if(node.info.equals(info)) infos.add(node);
        for(AbstractInfo child: node.children)
        {
            protectedGetSpecificChildren(child, info, infos);
        }
    }

    public static enum Info
    {
        STRATEGY_INFO("StrategyInfo"),
        FILE_INFO("FileInfo"),
        CLASS_INFO("ClassInfo"),
        METHOD_INFO("MethodInfo"),
        FUNCTION_INFO("FunctionInfo"),
        MUTATION_INFO("MutationInfo"),
        PREDICTION_INFO("PredictionInfo");

        public final String info;

        Info(String info)
        {
            this.info = info;
        }

        @Override
        public String toString() {
            return this.info;
        }
    }
}

