package strategy;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import okhttp3.*;
import com.fasterxml.jackson.core.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomClient
{
    public static final String TASK = "Above is the original code. your task is to generate $numberOfMutants mutants in original code(notice:mutant refers to mutant in software engineering, i.e. making subtle alterations to the original code) in :";
    public static final String EXAMPLES = """
            as follows are some examples of mutants which you can refer to:
            {
            "precode": "n = (n & (n - 1));",
            "aftercode": " n = (n ^ (n - 1));"
            },
            {
            "precode": "  while (!queue.isEmpty()) {",
            "aftercode": " while (true) { "
            },
            {
            "precode": "return depth==0;",
            "aftercode": "return true;"
            },
            {
            "precode": "ArrayList r = new
            ArrayList();r.add(first).addll(subset);to_add(r)",
            "aftercode": "to_add.addAll(subset);"
            },
            {
            "precode": "c = bin_op.apply(b,a);",
            "aftercode": "c = bin_op.apply(a,b);",
            },
            {
            "precode":"while (Math.abs(x-approx*approx) > epsilon) { "
            "aftercode": " while (Math.abs(x-approx) > epsilon) {"
            },
            """;
    public static final String REQUIREMENTS = """
            #Requirement:
            1.Provide generated mutants directly
            2.A mutation can only occur on one line
            3.Your output must be like:
            [
                {
                    "id":,
                    "line":,
                    "precode":"",
                    "filepath":"kk",
                    "aftercode":"",
                    "mutation":""
                }
            ]
            Where "id" stand for mutant serial number,"Line" represent the line number of the mutated,"precode" represent the line of code before mutation and it can't be empty,"aftercode" represent the line of code after mutation,"mutation" represents the mutation AND ONLY the mutation processed LOCATED IN THE AFTERCODE, NO EXPLANATION
            4.Prohibit generating the exact same mutants
            5.all write in a json file
            """;

    private String apiKey;
    private String url;
    private String model;
    private String message;

    OpenAIClient client;
    ChatCompletionCreateParams params;

    private CustomClient(Builder builder)
    {
        this.apiKey = builder.apiKey;
        this.url = builder.url;
        this.model = builder.model;
        this.message = builder.message;

        this.client = OpenAIOkHttpClient.builder()
                .apiKey(this.apiKey)
                .baseUrl(this.url)
                .build();

        this.params = ChatCompletionCreateParams.builder()
                .addUserMessage(this.message)
                .model(this.model)
                .build();
    }

    public String sendRequestAndGetResponse()
    {
        try
        {
            ChatCompletion chatCompletion = client.chat().completions().create(params);
            return chatCompletion.choices().get(0).message().content().get();
        }
        catch (Exception e)
        {
            System.err.println(e);
            return null;
        }
    }

    /**
     * If the request contains:
     * ```json
     * [
     *  ...
     * ]
     * ```
     * , then it returns only [ ... ]
     * @return
     */
    public String getJson()
    {
        Pattern pattern = Pattern.compile("```json(.*?)```", Pattern.DOTALL);
        String resp = sendRequestAndGetResponse();
        System.out.println(resp);
        Matcher matcher = pattern.matcher(resp);
        if(matcher.find())
        {
            return matcher.group(1);
        }
        return null;
    }





    /** BUILDER PART **/
    public static class Builder
    {
        String apiKey;
        String url;
        String model;
        String message = "";

        public Builder setApiKey(String apiKey)
        {
            this.apiKey = apiKey;
            return this;
        }

        public Builder setUrl(String url)
        {
            this.url = url;
            return this;
        }

        public Builder setModel(String model)
        {
            this.model = model;
            return this;
        }

        public Builder setMessage(String message)
        {
            this.message = message;
            return this;
        }

        public CustomClient build()
        {
            return new CustomClient(this);
        }
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        String content = """
                def add(x : float, y : float) -> float:
                    return x+y
                
                Above is the original code. your task is to generate 1 mutants in original code(notice:mutant refers to mutant in software engineering, i.e. making subtle alterations to the original code) in :
                
                    return x+y
                
                as follows are some examples of mutants which you can refer to:
                    {
                    "precode": "n = (n & (n - 1));",
                    "aftercode": " n = (n ^ (n - 1));"
                    },
                    {
                    "precode": "  while (!queue.isEmpty()) {",
                    "aftercode": " while (true) { "
                    },
                    {
                    "precode": "return depth==0;",
                    "aftercode": "return true;"
                    },
                    {
                    "precode": "ArrayList r = new
                    ArrayList();r.add(first).addll(subset);to_add(r)",
                    "aftercode": "to_add.addAll(subset);"
                    },
                    {
                    "precode": "c = bin_op.apply(b,a);",
                    "aftercode": "c = bin_op.apply(a,b);",
                    },
                    {
                    "precode":"while (Math.abs(x-approx*approx) > epsilon) { "
                    "aftercode": " while (Math.abs(x-approx) > epsilon) {"
                    },
                #Requirement:
                1.Provide generated mutants directly
                2.A mutation can only occur on one line
                3.Your output must be like:
                [
                    {
                        "id":,
                        "line":,
                        "precode":"",
                        "filepath":"kk",
                        "aftercode":"",
                	    "mutation":""
                    }
                ]
                Where "id" stand for mutant serial number,"Line" represent the line number of the mutated,"precode" represent the line of code before mutation and it can't be empty,"aftercode" represent the line of code after mutation,"mutation" represents the mutation AND ONLY the mutation processed LOCATED IN THE AFTERCODE, NO EXPLANATION
                4.Prohibit generating the exact same mutants
                5.all write in a json file
                """;

        CustomClient client = new CustomClient.Builder()
                .setApiKey("api-key-xxxxxxxxxxxxxxxxxxxxxx")
                .setModel("deepseek/deepseek-chat")
                .setUrl("https://openrouter.ai/api/v1/")
                .setMessage(content)
                .build();

        System.out.println(client.getJson());
    }
}