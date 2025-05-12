package storage;

import export.ExportVisitor;

import java.util.ArrayList;

/*
 * This class provides infos about classes for the mutation
 */
public class ClassInfo extends AbstractInfo
{
    // The class name e.g: "Class1"
    public String className = "";

    // The info about the methods of the class
    public ArrayList<MethodInfo> methods = new ArrayList<MethodInfo>();

    public void setClassName(String className)
    {
        this.className = className;
    }

    public void addMethodInfo(MethodInfo m)
    {
        methods.add(m);
    }

    @Override
    public String toString() {
        return "ClassInfo{" +
                "position=" + position +
                ", methods=" + methods +
                ", className='" + className + '\'' +
                '}';
    }

    @Override
    public String visit(ExportVisitor visitor) {
        return visitor.visitClassInfo(this);
    }
}
