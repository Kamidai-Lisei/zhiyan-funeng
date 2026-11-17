package com.jay.aigcoj.bot.ds;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jay.aigcoj.bot.pojo.DSRequest;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * DeepSeek 客户端
 */
@Component
public class DSApi {

    private final Gson gson = new Gson();

    @Value("${ai.deepseek-key:}")
    private String key;

    @Value("${ai.deepseek-url:https://api.deepseek.com/chat/completions}")
    private String apiUrl;

    private static final String DEFAULT_SYSTEM_PROMPT = "你是一个考研学习平台的智能助教，擅长计算机 408、算法与求职辅导。";

    public String sendDeepseekChat(String question) throws UnirestException {
        return sendDeepseekChat(DEFAULT_SYSTEM_PROMPT, question);
    }

    public String sendDeepseekChat(String systemPrompt, String question) throws UnirestException {
        Unirest.setTimeouts(60000, 120000);
        List<DSRequest.Message> messages = new ArrayList<>();
        messages.add(DSRequest.Message.builder().role("system").content(systemPrompt).build());
        messages.add(DSRequest.Message.builder().role("user").content(question).build());
        DSRequest requestBody = DSRequest.builder()
                .model("deepseek-chat")
                .messages(messages)
                .build();
        HttpResponse<String> res = Unirest.post(apiUrl)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + key)
                .body(gson.toJson(requestBody))
                .asString();
        if (res.getStatus() == 200) {
            JsonObject jsonResponse = JsonParser.parseString(res.getBody()).getAsJsonObject();
            return jsonResponse.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
        }
        throw new RuntimeException("AI 服务请求失败，状态码: " + res.getStatus());
    }
}
