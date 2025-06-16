package export;

import org.json.JSONObject;
import storage.*;

import java.util.HashMap;
import java.util.Map;

/*
 * The visitor for exporting in Json
 */
public class JsonExport implements ExportVisitor
{
    @Override
    public String visitFileInfo(FileInfo fi) {
        return visitFileInfoJson(fi).toString();
    }

    public JSONObject visitFileInfoJson(FileInfo fi) {
        if(fi == null) return new JSONObject();
        JSONObject export = new JSONObject();

        export.put("type", fi.info.toString());
        export.put("filename", fi.fileName);
        export.put("pathname", fi.pathName);
        export.put("language", fi.language);

        JSONObject children = new JSONObject();
        int i = 0;
        for(AbstractInfo ab: fi.children)
        {
            children.put(String.valueOf(i), new JSONObject(ab.visit(this)));
            i++;
        }

        export.put("children", children);
        return export;
    }

    @Override
    public String visitClassInfo(ClassInfo cl) {
        return visitClassInfoJson(cl).toString();
    }

    public JSONObject visitClassInfoJson(ClassInfo cl) {
        if(cl == null) return new JSONObject();
        JSONObject export = new JSONObject();

        export.put("type", cl.info.toString());
        export.put("classname", cl.className);
        export.put("position", new JSONObject(cl.position.visit(this)));

        JSONObject children = new JSONObject();
        int i = 0;
        for(AbstractInfo ab: cl.children)
        {
            children.put(String.valueOf(i), new JSONObject(ab.visit(this)));
            i++;
        }

        export.put("children", children);
        return export;
    }

    @Override
    public String visitMethodInfo(MethodInfo me) {
        return visitMethodInfoJson(me).toString();
    }

    public JSONObject visitMethodInfoJson(MethodInfo me) {
        if(me == null) return new JSONObject();
        JSONObject export = new JSONObject();

        export.put("type", me.info.toString());
        export.put("methodname", me.methodName);
        export.put("signature", me.signature);
        export.put("position", new JSONObject(me.position.visit(this)));

        JSONObject children = new JSONObject();
        int i = 0;
        for(AbstractInfo ab: me.children)
        {
            children.put(String.valueOf(i), new JSONObject(ab.visit(this)));
            i++;
        }

        export.put("children", children);
        return export;
    }

    @Override
    public String visitFunctionInfo(FunctionInfo fu) {
        return visitFunctionInfoJson(fu).toString();
    }

    public JSONObject visitFunctionInfoJson(FunctionInfo fu) {
        if(fu == null) return new JSONObject();
        JSONObject export = new JSONObject();

        export.put("type", fu.info.toString());
        export.put("functionname", fu.functionName);
        export.put("signature", fu.signature);
        export.put("position", new JSONObject(fu.position.visit(this)));

        JSONObject children = new JSONObject();
        int i = 0;
        for(AbstractInfo ab: fu.children)
        {
            children.put(String.valueOf(i), new JSONObject(ab.visit(this)));
            i++;
        }

        export.put("children", children);
        return export;
    }

    @Override
    public String visitMutationInfo(MutationInfo mu) {
        return visitMutationInfoJson(mu).toString();
    }

    public JSONObject visitMutationInfoJson(MutationInfo mu) {
        if(mu == null) return new JSONObject();
        JSONObject export = new JSONObject();

        export.put("type", mu.info.toString());
        export.put("maskingtype", mu.maskingType);
        export.put("position", new JSONObject(mu.position.visit(this)));

        JSONObject children = new JSONObject();
        int i = 0;
        for(AbstractInfo ab: mu.children)
        {
            children.put(String.valueOf(i), new JSONObject(ab.visit(this)));
            i++;
        }

        export.put("children", children);
        return export;
    }

    @Override
    public String visitPredictionInfo(PredictionInfo pr) {
        return this.visitPredictionInfoJson(pr).toString();
    }

    public JSONObject visitPredictionInfoJson(PredictionInfo pr)
    {
        if(pr == null) return new JSONObject();

        JSONObject export = new JSONObject();

        export.put("type", "PredictionInfo");
        export.put("tokenpredicted", pr.tokenPredicted);
        export.put("precode", pr.preCode);
        export.put("aftercode", pr.afterCode);
        export.put("pathtooutput", pr.pathToOutput);

        JSONObject metrics = new JSONObject();
        for(Map.Entry<String, String> entry: pr.metrics.entrySet())
            metrics.put(entry.getKey(), entry.getValue());
        export.put("metrics", metrics);

        return export;
    }

    @Override
    public String visitPositionInfo(PositionInfo po)
    {
        return this.visitPositionInfoJson(po).toString();
    }

    public JSONObject visitPositionInfoJson(PositionInfo po)
    {
        if (po == null) return new JSONObject();

        HashMap<String, String> position = new HashMap<>();
        position.put("type", "PositionInfo");
        position.put("beginindex", String.valueOf(po.beginIndex));
        position.put("endindex", String.valueOf(po.endIndex));
        position.put("beginline", String.valueOf(po.beginLine));
        position.put("begincolumn", String.valueOf(po.beginColumn));
        position.put("endline", String.valueOf(po.endLine));
        position.put("endcolumn", String.valueOf(po.endColumn));

        return new JSONObject(position);
    }
}
