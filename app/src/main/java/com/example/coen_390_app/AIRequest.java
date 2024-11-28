package com.example.coen_390_app;

import java.util.List;

public class AIRequest {
    private String model;
    private List<Message> messages;
    private int max_tokens;
    private double temperature;

    public AIRequest(String model, List<Message> messages, int maxTokens, double temperature) {
        this.model = model;
        this.messages = messages;
        this.max_tokens = maxTokens;
        this.temperature = temperature;
    }

    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }
    }
}


