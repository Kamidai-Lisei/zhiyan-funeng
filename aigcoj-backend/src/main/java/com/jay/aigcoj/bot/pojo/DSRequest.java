package com.jay.aigcoj.bot.pojo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DSRequest {
    private String model;
    private List<Message> messages;

    @Data
    @Builder
    public static class Message {
        private String role;
        private String content;
    }
}