package com.example.weeklymealplannergpt.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OpenAIRequest {
    private String model;
    private List<Message> messages;
    private double temperature;
    private int max_tokens;

    @Getter
    @Setter
    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
