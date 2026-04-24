package com.sadeem.multitenantsaas.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GeminiClient {
    @Value("${gemini.api.key}")
    private String apiKey;
    @Value("${gemini.api.url}")
    private String apiUrl;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateResponse(String prompt) {
        try {
            GeminiRequest requestObj = new GeminiRequest(
                    List.of(new Content("user", List.of(new Part(prompt))))
            );

            String requestBody = objectMapper.writeValueAsString(requestObj);

            Request request = new Request.Builder()
                    .url(apiUrl + "?key=" + apiKey)
                    .post(RequestBody.create(
                            requestBody,
                            MediaType.parse("application/json")
                    ))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No response body";
                    System.err.println("Agent error: " + errorBody);
                    return "Agent failed: HTTP " + response.code();
                }

                String responseBody = response.body().string();
                JsonNode root = objectMapper.readTree(responseBody);

                return root
                        .path("candidates")
                        .get(0)
                        .path("content")
                        .path("parts")
                        .get(0)
                        .path("text")
                        .asText("No response generated");
            }
        } catch (Exception e) {
            return "Agent error: " + e.getMessage();
        }
    }

    record GeminiRequest(List<Content> contents) {}
    record Content(String role, List<Part> parts) {}
    record Part(String text) {}
}
