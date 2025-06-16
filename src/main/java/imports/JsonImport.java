package imports;

import org.json.JSONException;
import org.json.JSONObject;
import storage.*;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JsonImport extends ImportHandler
{
    public JsonImport()
    {
        fileExtensions = new ArrayList<>(List.of(".json"));
    }

    @Override
    public AbstractInfo protectedGetInfo(File file)
    {
        try
        {
            return this.protectedGetInfo(Files.readString(file.toPath()));
        }
        catch (Exception e)
        {
            System.out.println("Exception..... " + e);
            return null;
        }
    }

    public PositionInfo protectedGetPositionInfo(String content)
    {
        JSONObject jsonObject = new JSONObject(content);
        try
        {
            String type = jsonObject.get("type").toString();
            if(type.equals("PositionInfo"))
            {
                PositionInfo positionInfo = new PositionInfo();
                positionInfo.beginLine = Integer.parseInt(jsonObject.get("beginline").toString());
                positionInfo.beginColumn = Integer.parseInt(jsonObject.get("begincolumn").toString());
                positionInfo.endLine = Integer.parseInt(jsonObject.get("endline").toString());
                positionInfo.endColumn = Integer.parseInt(jsonObject.get("endcolumn").toString());
                positionInfo.beginIndex = Integer.parseInt(jsonObject.get("beginindex").toString());
                positionInfo.endIndex = Integer.parseInt(jsonObject.get("endindex").toString());
                return positionInfo;
            }
            else return null;
        }
        catch (JSONException e)
        {
            return null;
        }
    }

    public AbstractInfo protectedGetInfo(String content)
    {
        JSONObject jsonObject = new JSONObject(content);
        try
        {
            String type = jsonObject.get("type").toString();
            if(type.equals("FileInfo"))
            {
                FileInfo fileInfo = new FileInfo();
                fileInfo.language = jsonObject.get("language").toString();
                fileInfo.fileName = jsonObject.get("filename").toString();
                fileInfo.pathName = jsonObject.get("pathname").toString();

                JSONObject classes = jsonObject.getJSONObject("children");
                Iterator<?> classesIterator = classes.keys();
                while(classesIterator.hasNext())
                {
                    AbstractInfo child = protectedGetInfo(classes.get(classesIterator.next().toString()).toString());
                    if(child != null)
                    {
                        fileInfo.children.add(child);
                        child.parent = fileInfo;
                    }
                }
                return fileInfo;
            }
            else if(type.equals("ClassInfo"))
            {
                ClassInfo classInfo = new ClassInfo();
                classInfo.className = jsonObject.get("classname").toString();
                JSONObject methods = jsonObject.getJSONObject("children");
                PositionInfo positionInfo = this.protectedGetPositionInfo(jsonObject.get("position").toString());
                if(positionInfo != null) classInfo.position = positionInfo;

                Iterator<?> methodsIterator = methods.keys();
                while(methodsIterator.hasNext())
                {
                    AbstractInfo child = protectedGetInfo(methods.get(methodsIterator.next().toString()).toString());
                    if(child != null)
                    {
                        classInfo.children.add(child);
                        child.parent = classInfo;
                    }
                }
                return classInfo;
            }
            else if(type.equals("MethodInfo"))
            {
                MethodInfo methodInfo = new MethodInfo();
                methodInfo.methodName = jsonObject.get("methodname").toString();
                methodInfo.signature = jsonObject.get("signature").toString();
                JSONObject statements = jsonObject.getJSONObject("children");
                PositionInfo positionInfo = this.protectedGetPositionInfo(jsonObject.get("position").toString());
                if(positionInfo != null) methodInfo.position = positionInfo;

                Iterator<?> statementsIterator = statements.keys();
                while(statementsIterator.hasNext())
                {
                    AbstractInfo child = protectedGetInfo(statements.get(statementsIterator.next().toString()).toString());
                    if(child != null)
                    {
                        methodInfo.children.add(child);
                        child.parent = methodInfo;
                    }
                }
                return methodInfo;
            }
            else if(type.equals("FunctionInfo"))
            {
                FunctionInfo functionInfo = new FunctionInfo();
                functionInfo.functionName = jsonObject.get("functionname").toString();
                functionInfo.signature = jsonObject.get("signature").toString();
                JSONObject statements = jsonObject.getJSONObject("children");
                PositionInfo positionInfo = this.protectedGetPositionInfo(jsonObject.get("position").toString());
                if(positionInfo != null) functionInfo.position = positionInfo;

                Iterator<?> statementsIterator = statements.keys();
                while(statementsIterator.hasNext())
                {
                    AbstractInfo child = protectedGetInfo(statements.get(statementsIterator.next().toString()).toString());
                    if(child != null)
                    {
                        functionInfo.children.add(child);
                        child.parent = functionInfo;
                    }
                }
                return functionInfo;
            }
            else if(type.equals("MutationInfo"))
            {
                MutationInfo mutationInfo = new MutationInfo();
                mutationInfo.maskingType = jsonObject.get("maskingtype").toString();
                JSONObject predictions = jsonObject.getJSONObject("children");
                PositionInfo positionInfo = this.protectedGetPositionInfo(jsonObject.get("position").toString());
                if(positionInfo != null) mutationInfo.position = positionInfo;

                Iterator<?> predictionIterator = predictions.keys();
                while(predictionIterator.hasNext())
                {
                    AbstractInfo child = protectedGetInfo(predictions.get(predictionIterator.next().toString()).toString());
                    if(child != null)
                    {
                        mutationInfo.children.add(child);
                        child.parent = mutationInfo;
                    }
                }
                return mutationInfo;
            }
            else if(type.equals("PredictionInfo"))
            {
                PredictionInfo predictionInfo = new PredictionInfo();
                predictionInfo.tokenPredicted = jsonObject.get("tokenpredicted").toString();
                predictionInfo.preCode = jsonObject.get("precode").toString();
                predictionInfo.afterCode = jsonObject.get("aftercode").toString();
                predictionInfo.pathToOutput = jsonObject.get("pathtooutput").toString();

                // Add metrics
                JSONObject metrics = jsonObject.getJSONObject("metrics");
                Iterator<?> metricsIterator = metrics.keys();
                while(metricsIterator.hasNext())
                {
                    String key = metricsIterator.next().toString();
                    String value = metrics.getString(key);
                    predictionInfo.metrics.put(key,value);
                }

                return predictionInfo;
            }
            else
            {
                return null;
            }
        }
        catch(JSONException e)
        {
            System.out.println("Json Exception... " + e);
        }

        return null;
    }
}
