package com.aiclassroom.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class BytezAiService {

    @Value("${groq.api-key}")
    private String groqApiKey;

    @Value("${groq.model:llama3-70b-8192}")
    private String groqModel;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getAiResponse(String prompt) {
        try {
            String url = "https://api.groq.com/openai/v1/chat/completions";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqApiKey);

            Map<String, Object> message = Map.of("role", "user", "content", prompt);
            Map<String, Object> body = new HashMap<>();
            body.put("model", groqModel);
            body.put("messages", List.of(message));
            body.put("max_tokens", 2048);
            body.put("temperature", 1.0);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate
                    .postForEntity(url, request, Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null)
                return mockFallback(prompt);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (choices == null || choices.isEmpty())
                return mockFallback(prompt);

            @SuppressWarnings("unchecked")
            Map<String, Object> msg = (Map<String, Object>) choices.get(0).get("message");
            return msg != null ? msg.get("content").toString() : mockFallback(prompt);

        } catch (Exception e) {
            System.err.println("[Groq] Error: " + e.getMessage());
            return mockFallback(prompt);
        }
    }

    private String mockFallback(String question) {
        String q = question.toLowerCase();
        if (q.contains("quiz") || q.contains("json")) {
            return "{\"question\": \"[OFFLINE] What does API stand for?\", \"options\": [\"Application Programming Interface\", \"Applied Protocol Integration\", \"Automated Process Interface\", \"Application Process Integration\"], \"correctAnswer\": 0, \"explanation\": \"API stands for Application Programming Interface — a contract between software components.\"}";
        }
        return "[Groq AI unavailable] Please check your API key or internet connection.";
    }
}
