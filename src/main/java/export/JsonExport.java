package export;

import storage.*;

import java.util.Map;

/*
 * The visitor for exporting in Json
 */
public class JsonExport implements ExportVisitor
{
    @Override
    public String visitFileInfo(FileInfo fi) {
        if(fi == null) return "";
        String res = "";

        res += "{" + "\n"
                + "\t\"filename\": \"" + fi.fileName + "\",\n"
                + "\t\"pathname\": \"" + fi.pathName + "\",\n"
                + "\t\"strategy\": \"" + fi.strategy + "\",\n"
                + "\t\"language\": \"" + fi.language + "\",\n";

        if(fi.position != null) res += "\t\"position\": " + fi.position.visit(this).replace("\t","\t\t");

        if(!fi.classes.isEmpty())
        {
            res += ",\t\"classinfos\": \n" + "\t{\n";
            int i = 0;
            for(ClassInfo cl: fi.classes)
            {
                res += "\t\t\""+i+"\": \n";
                res += "\t\t\t" + cl.visit(this);
                if(i < fi.classes.size()-1) res += ",\n";
                else res += "\n";
                i++;
            }
            res += "}";
        }

        res += "\n}";

        return res;
    }

    @Override
    public String visitClassInfo(ClassInfo cl) {
        if(cl == null) return "";
        String res = "";

        res += "{" + "\n"
                + "\t\"classname\": \"" + cl.className + "\",\n";

        if(cl.position != null) res += "\t\"position\": " + cl.position.visit(this).replace("\t","\t\t");

        if(!cl.methods.isEmpty())
        {
            res += ",\t\"classinfos\": \n" + "\t{\n";
            int i = 0;
            for(MethodInfo me: cl.methods)
            {
                res += "\t\t\""+i+"\": \n";
                res += "\t\t\t" + me.visit(this);
                if(i < cl.methods.size()-1) res += ",\n";
                else res += "\n";
                i++;
            }
            res += "}";
        }

        res += "\n}";

        return res;
    }

    @Override
    public String visitMethodInfo(MethodInfo me) {
        if(me == null) return "";
        String res = "";

        res += "{" + "\n"
                + "\t\"methodname\": \"" + me.methodName + "\",\n"
                + "\t\"declaration\": \"" + me.declaration + "\",\n";

        if(me.position != null) res += "\t\"position\": " + me.position.visit(this).replace("\t","\t\t");

        if(!me.statements.isEmpty())
        {
            res += ",\t\"statementinfos\": \n" + "\t{\n";
            int i = 0;
            for(StatementInfo st: me.statements)
            {
                res += "\t\t\""+i+"\": \n";
                res += "\t\t\t" + st.visit(this);
                if(i < me.statements.size()-1) res += ",\n";
                else res += "\n";
                i++;
            }
            res += "}";
        }

        res += "\n}";
        return res;
    }

    @Override
    public String visitStatementInfo(StatementInfo st) {
        if(st == null) return "";
        String res = "";

        res += "{" + "\n"
                + "\t\"statement\": \"" + st.statement.replace("\n", "\\n").replace("\"", "\\\"") + "\",\n";

        if(st.position != null) res += "\t\"position\": " + st.position.visit(this).replace("\t","\t\t");

        if(!st.maskingInfos.isEmpty())
        {
            res += ",\t\"maskinginfos\": \n" + "\t{\n";
            int i = 0;
            for(MaskingInfo mask: st.maskingInfos)
            {
                res += "\t\t\""+i+"\": \n";
                res += "\t\t\t" + mask.visit(this);
                if(i < st.maskingInfos.size()-1) res += ",\n";
                else res += "\n";
                i++;
            }
            res += "}";
        }

        res += "\n}";

        return res;
    }

    @Override
    public String visitMaskingInfo(MaskingInfo ma) {
        if(ma == null) return "";
        String res = "";

        res += "{" + "\n"
                + "\t\"maskingtype\": \"" + ma.maskingType + "\",\n";

        if(ma.position != null) res += "\t\"position\": " + ma.position.visit(this).replace("\t","\t\t");

        if(!ma.predictions.isEmpty())
        {
            res += ",\t\"predictions\": \n" + "\t{\n";
            int i = 0;
            for(PredictionInfo pred: ma.predictions)
            {
                res += "\t\t\""+i+"\": \n";
                res += "\t\t\t" + pred.visit(this);
                if(i < ma.predictions.size()-1) res += ",\n";
                else res += "\n";
                i++;
            }
            res += "}";
        }

        res += "\n}";

        return res;
    }

    @Override
    public String visitPredictionInfo(PredictionInfo pr) {
        if(pr == null) return "";
        String res = "";

        res += "{" + "\n"
            + "\t\"tokenpredicted\": \"" + pr.tokenPredicted.replace("\"", "\\\"") + "\",\n"
            + "\t\"statementbefore\": \"" + pr.statementBefore.replace("\n", "\\n").replace("\"", "\\\"") + "\",\n"
            + "\t\"statementafter\": \"" + pr.statementAfter.replace("\n", "\\n").replace("\"", "\\\"") + "\",\n";

        if(!pr.metrics.isEmpty())
        {
            res += "\t\"metrics\": \n" + "\t[\n";
            int i = 0;
            for(Map.Entry<String, String> entry: pr.metrics.entrySet())
            {
                res += "\t\t{\n";
                res += "\t\t\t\"metric\": \"" + entry.getKey() + "\",\n";
                res += "\t\t\t\"value\": \"" + entry.getValue() + "\"\n";
                if(i < pr.metrics.size()-1) res += "\t\t},\n";
                else res += "\t\t}\n";
                i++;
            }
            res += "\t],\n";
        }

        if(pr.position != null) res += "\t\"position\": " + pr.position.visit(this).replace("\t","\t\t");

        res += "\n}";

        return res;
    }

    @Override
    public String visitPositionInfo(PositionInfo po)
    {
        if(po == null) return "";
        return "{" + "\n"
            + "\t\"beginindex\": \"" + po.beginIndex + "\",\n"
            + "\t\"endindex\": \"" + po.endIndex + "\",\n"
            + "\t\"beginline\": \"" + po.beginLine + "\",\n"
            + "\t\"begincolumn\": \"" + po.beginColumn + "\",\n"
            + "\t\"endline\": \"" + po.endLine + "\",\n"
            + "\t\"endcolumn\": \"" + po.endColumn + "\"\n"
            + "}";
    }
}
