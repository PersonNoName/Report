package com.report.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class AIService {

    @Value("${minimax.api-key}")
    private String apiKey;

    @Value("${minimax.group-id}")
    private String groupId;

    @Value("${minimax.url}")
    private String apiUrl;

    @Value("${minimax.model}")
    private String model;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AIService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public String polishContent(String originalContent) {
        try {
            // Construct the Minimax request body - Using OpenAI Compatible format
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.put("stream", false);
            requestBody.put("temperature", 0.7);

            ArrayNode messages = requestBody.putArray("messages");

            ObjectNode systemMessage = messages.addObject();
            systemMessage.put("role", "system");
            systemMessage.put("content",
                    "You are a professional report editor. Your task is to polish the provided text to make it more professional, concise, and clear, suitable for a business report. Return ONLY the polished text, without any explanations or markdown formatting unless the original had it.");

            ObjectNode userMessage = messages.addObject();
            userMessage.put("role", "user");
            userMessage.put("content", originalContent);

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            // Allow groupId injection if needed for URL
            String finalUrl = apiUrl;
            if (groupId != null && !groupId.isEmpty()) {
                finalUrl = finalUrl.replace("{GroupId}", groupId);
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(finalUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Log for debugging
            if (response.statusCode() != 200) {
                System.out.println("AI Service Error Body: " + response.body());
                throw new RuntimeException(
                        "API Call failed with status: " + response.statusCode() + ", body: " + response.body());
            }

            JsonNode responseNode = objectMapper.readTree(response.body());

            // Try standard OpenAI format access first
            if (responseNode.has("choices") && responseNode.get("choices").isArray()
                    && responseNode.get("choices").size() > 0) {
                JsonNode choice = responseNode.get("choices").get(0);
                if (choice.has("message")) {
                    return choice.get("message").get("content").asText();
                } else if (choice.has("messages")) {
                    // Fallback for native v2 if it works with role
                    return choice.get("messages").get(0).get("text").asText();
                }
            } else if (responseNode.has("reply")) {
                return responseNode.get("reply").asText();
            } else if (responseNode.has("base_resp") && responseNode.get("base_resp").has("status_msg")) {
                throw new RuntimeException(
                        "API returned error: " + responseNode.get("base_resp").get("status_msg").asText());
            }

            throw new RuntimeException("Could not parse AI response: " + response.body());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to polish content: " + e.getMessage(), e);
        }
    }
}
