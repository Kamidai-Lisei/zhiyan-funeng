package com.jay.aigcoj.judge.codesandbox.impl;

import com.google.gson.*;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.jay.aigcoj.bot.ds.DSApi;
import com.jay.aigcoj.judge.codesandbox.CodeSandbox;
import com.jay.aigcoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.jay.aigcoj.judge.codesandbox.model.ExecuteCodeResponse;
import com.jay.aigcoj.judge.codesandbox.model.JudgeInfo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AICodeSandbox implements CodeSandbox {

//    @Resource
//    DeepSeekApi deepSeekApi;

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {

        // 提取请求中的代码、语言和输入用例
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();
        List<String> inputList = executeCodeRequest.getInputList();

        // 初始化DeepSeek API对象
        DSApi deepSeekApi = new DSApi();

        // 构造向DeepSeek API发送的问题字符串，包含代码、语言和输入用例等信息
        String myQuestion = "你是一个+"+ language+"+代码沙箱，负责运行"+ language+"代码，"+
                "现在有一段代码是"+code+"输入用例是"+inputList.toString()+"请你运行它，并返回一个Json对象，格式如下"+
                "{\n" +
                "  \"outputList\": [\"1\"],\n" +
                "  \"message\": \"\",\n" +
                "  \"status\": ,\n" +
                "  \"judgeInfo\": {\n" +
                "    \"message\": \"\",\n" +
                "    \"memory\": xxx,\n" +
                "    \"time\": xxx\n" +
                "  }\n" +
                "}outputlist是输出结果，message为空，status是状态码，" +
                "judgeInfo中message为空,time单位是ms，memory单位是kb，严格判断你所在的语言沙箱环境与代码的语言是否匹配" +
                "，java沙箱只能运行java代码，python沙箱只能运行python代码，以此类推，当不符的时候是编译失败，" +
                "如果代码编译失败无法运行的时候，judgeInfo的message写为Compile Error。" +
                "同时time和memory的值为0，我会问你多次，因为每次运行的time和memory都不一定一样，不要固定这两个值" +
                "请你返回这样一个JSON对象，只返回JSON，其他不要输出" +
                "一定不要输出```json开头和```结尾，返回正常文本格式";
        String answer = null;
        try {
            answer = deepSeekApi.sendDeepseekChat(myQuestion);
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(answer);

        //String转换成JsonObject
        JsonObject answerJson = JsonParser.parseString(answer).getAsJsonObject();

        // 提取 outputList 属性
        JsonArray outputListJson = answerJson.getAsJsonArray("outputList");
        List<String> outputList = new ArrayList<>();
        for (int i = 0; i < outputListJson.size(); i++) {
            outputList.add(outputListJson.get(i).getAsString());
        }
        // 提取 message 属性
        String message = String.valueOf(answerJson.get("message"));
        // 提取 status 属性
        int status = answerJson.get("status").getAsInt();
        // 提取 judgeInfo 属性
        JsonObject judgeInfo = answerJson.getAsJsonObject("judgeInfo");
        // 提取 infoMessage 属性
        String infoMessage = String.valueOf(judgeInfo.get("message"));
        // 对字符串两端的引号进行处理删除
        infoMessage = infoMessage.trim().substring(1, infoMessage.length() - 1);
        // 提取 memory 属性
        int memory = judgeInfo.get("memory").getAsInt();
        // 提取 time 属性
        int time = judgeInfo.get("time").getAsInt();
        // 提取 judgeInfo 属性
        JudgeInfo info = new JudgeInfo(infoMessage, (long)memory, (long)time);

        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse(outputList, message, status, info);

        System.out.println(executeCodeResponse);
        return executeCodeResponse;

    }
}
