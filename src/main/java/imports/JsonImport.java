package imports;

import core.Language;
import org.json.JSONException;
import org.json.JSONObject;
import storage.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLOutput;
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
                fileInfo.strategy = jsonObject.get("strategy").toString();
                PositionInfo positionInfo = this.protectedGetPositionInfo(jsonObject.get("position").toString());
                if(positionInfo != null) fileInfo.position = positionInfo;

                JSONObject classes = jsonObject.getJSONObject("classinfos");
                Iterator<?> classesIterator = classes.keys();
                while(classesIterator.hasNext())
                {
                    ClassInfo classInfo = (ClassInfo) protectedGetInfo(classes.get(classesIterator.next().toString()).toString());
                    if(classInfo != null) fileInfo.classes.add(classInfo);
                }
                return fileInfo;
            }
            else if(type.equals("ClassInfo"))
            {
                ClassInfo classInfo = new ClassInfo();
                classInfo.className = jsonObject.get("classname").toString();
                JSONObject methods = jsonObject.getJSONObject("methodinfos");
                PositionInfo positionInfo = this.protectedGetPositionInfo(jsonObject.get("position").toString());
                if(positionInfo != null) classInfo.position = positionInfo;

                Iterator<?> methodsIterator = methods.keys();
                while(methodsIterator.hasNext())
                {
                    MethodInfo methodInfo = (MethodInfo) protectedGetInfo(methods.get(methodsIterator.next().toString()).toString());
                    if(methodInfo != null) classInfo.methods.add(methodInfo);
                }
                return classInfo;
            }
            else if(type.equals("MethodInfo"))
            {
                MethodInfo methodInfo = new MethodInfo();
                methodInfo.methodName = jsonObject.get("methodname").toString();
                methodInfo.declaration = jsonObject.get("declaration").toString();
                JSONObject statements = jsonObject.getJSONObject("statementinfos");
                PositionInfo positionInfo = this.protectedGetPositionInfo(jsonObject.get("position").toString());
                if(positionInfo != null) methodInfo.position = positionInfo;

                Iterator<?> statementsIterator = statements.keys();
                while(statementsIterator.hasNext())
                {
                    StatementInfo statementInfo = (StatementInfo) protectedGetInfo(statements.get(statementsIterator.next().toString()).toString());
                    if(statementInfo != null) methodInfo.statements.add(statementInfo);
                }
                return methodInfo;
            }
            else if(type.equals("StatementInfo"))
            {
                StatementInfo statementInfo = new StatementInfo();
                statementInfo.statement = jsonObject.get("statement").toString();
                JSONObject maskinginfos = jsonObject.getJSONObject("maskinginfos");
                PositionInfo positionInfo = this.protectedGetPositionInfo(jsonObject.get("position").toString());
                if(positionInfo != null) statementInfo.position = positionInfo;

                Iterator<?> maskingIterator = maskinginfos.keys();
                while(maskingIterator.hasNext())
                {
                    MaskingInfo maskingInfo = (MaskingInfo) protectedGetInfo(maskinginfos.get(maskingIterator.next().toString()).toString());
                    if(maskingInfo != null) statementInfo.maskingInfos.add(maskingInfo);
                }
                return statementInfo;
            }
            else if(type.equals("MaskingInfo"))
            {
                MaskingInfo maskingInfo = new MaskingInfo();
                maskingInfo.maskingType = jsonObject.get("maskingtype").toString();
                JSONObject predictions = jsonObject.getJSONObject("predictions");
                PositionInfo positionInfo = this.protectedGetPositionInfo(jsonObject.get("position").toString());
                if(positionInfo != null) maskingInfo.position = positionInfo;

                Iterator<?> predictionIterator = predictions.keys();
                while(predictionIterator.hasNext())
                {
                    PredictionInfo predictionInfo = (PredictionInfo) protectedGetInfo(predictions.get(predictionIterator.next().toString()).toString());
                    if(positionInfo != null) maskingInfo.predictions.add(predictionInfo);
                }
                return maskingInfo;
            }
            else if(type.equals("PredictionInfo"))
            {
                PredictionInfo predictionInfo = new PredictionInfo();
                predictionInfo.tokenPredicted = jsonObject.get("tokenpredicted").toString();
                predictionInfo.statementBefore = jsonObject.get("statementbefore").toString();
                predictionInfo.statementAfter = jsonObject.get("statementafter").toString();
                predictionInfo.pathToOutput = jsonObject.get("pathtooutput").toString();
                PositionInfo positionInfo = this.protectedGetPositionInfo(jsonObject.get("position").toString());
                if(positionInfo != null) predictionInfo.position = positionInfo;

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
