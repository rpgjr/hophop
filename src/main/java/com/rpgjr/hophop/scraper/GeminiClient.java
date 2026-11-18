package com.rpgjr.hophop.scraper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * The one place that talks to Gemini. Every AI feature calls this.
 */
@Slf4j
@Component
public class GeminiClient {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private final RestClient restClient;
    private final List<String> models;

    public GeminiClient(@Value("${gemini.api-key}") String apiKey,
                        @Value("${gemini.model}") String model,
                        @Value("${gemini.fallback-models:}") List<String> fallbackModels) {
        this.models = Stream.concat(Stream.of(model), fallbackModels.stream())
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .distinct()
                .toList();
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .defaultHeader("x-goog-api-key", apiKey)
                .build();
    }

    /**
     * Sends the prompt and returns the JSON text Gemini answered with.
     * When a model is busy or over its limit, the next model in the list answers instead.
     */
    public String generateJson(String prompt, Map<String, Object> schema) {
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of(
                        "responseMimeType", "application/json",
                        "responseSchema", schema));
        RestClientException failure = null;
        for (String model : models) {
            try {
                return answer(model, body);
            } catch (HttpStatusCodeException e) {
                failure = e;
                if (!e.getStatusCode().is5xxServerError() && e.getStatusCode().value() != 429) break;
                log.warn("Gemini model {} answered {}, trying the next one", model, e.getStatusCode().value());
            } catch (RestClientException e) {
                failure = e;
            } catch (JacksonException e) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "The AI request failed");
            }
        }
        log.warn("Gemini request failed: {}", failure == null ? "no model set" : failure.getMessage());
        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "The AI request failed");
    }

    private String answer(String model, Map<String, Object> body) {
        String response = restClient.post()
                .uri("/models/{model}:generateContent", model)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        return MAPPER.readTree(response)
                .at("/candidates/0/content/parts/0/text")
                .asString();
    }
}