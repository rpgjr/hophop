package com.rpgjr.hophop.scraper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

/**
 * The one place that talks to Gemini. Every AI feature calls this.
 */
@Component
public class GeminiClient {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private final RestClient restClient;
    private final String model;

    public GeminiClient(@Value("${gemini.api-key}") String apiKey,
                        @Value("${gemini.model}") String model) {
        this.model = model;
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .defaultHeader("x-goog-api-key", apiKey)
                .build();
    }

    /**
     * Sends the prompt and returns the JSON text Gemini answered with.
     */
    public String generateJson(String prompt, Map<String, Object> schema) {
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of(
                        "responseMimeType", "application/json",
                        "responseSchema", schema));
        try {
            String response = restClient.post()
                    .uri("/models/{model}:generateContent", model)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            return MAPPER.readTree(response)
                    .at("/candidates/0/content/parts/0/text")
                    .asString();
        } catch (RestClientException | JacksonException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "The AI request failed");
        }
    }
}