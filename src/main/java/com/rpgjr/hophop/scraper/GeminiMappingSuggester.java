package com.rpgjr.hophop.scraper;

import com.rpgjr.hophop.enums.MappingFormField;
import com.rpgjr.hophop.model.dto.CandidateDto;
import com.rpgjr.hophop.model.dto.MappingEntryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Asks Gemini which tag on a page holds each form field. Only runs when the user clicks Suggest with AI.
 */
@Component
@RequiredArgsConstructor
public class GeminiMappingSuggester {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private static final String PROMPT = """
            Below are the tags found on a job posting page. Each line is: TYPE | KEY | SAMPLE VALUE.
            For each form field, choose the one line whose sample value holds that information.
            Leave a field out when no line fits. Copy TYPE and KEY exactly as listed. Do not invent keys.
            Fields: %s
            
            TAGS:
            %s
            """;

    private final GeminiClient geminiClient;

    record Suggestion(List<MappingEntryDto> mappings) {
    }

    public List<MappingEntryDto> suggest(List<CandidateDto> candidates) {
        String fields = Arrays.stream(MappingFormField.values()).map(Enum::name).collect(Collectors.joining(", "));
        String tags = candidates.stream()
                .map(c -> c.type() + " | " + c.key() + " | " + c.value())
                .collect(Collectors.joining("\n"));

        String json = geminiClient.generateJson(PROMPT.formatted(fields, tags), schema());
        return parse(json, candidates);
    }

    /**
     * Keeps only rules that point at a tag that really exists, one rule per field.
     */
    List<MappingEntryDto> parse(String json, List<CandidateDto> candidates) {
        try {
            Suggestion suggestion = MAPPER.readValue(json, Suggestion.class);
            if (suggestion.mappings() == null) return List.of();

            Set<String> known = candidates.stream()
                    .map(c -> c.type() + "|" + c.key())
                    .collect(Collectors.toSet());
            Map<MappingFormField, MappingEntryDto> byField = new LinkedHashMap<>();
            for (MappingEntryDto m : suggestion.mappings()) {
                if (known.contains(m.sourceType() + "|" + m.sourceKey())) byField.putIfAbsent(m.field(), m);
            }
            return List.copyOf(byField.values());
        } catch (JacksonException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI could not suggest a mapping");
        }
    }

    private Map<String, Object> schema() {
        Map<String, Object> entry = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "field", Map.of("type", "STRING", "enum", Arrays.stream(MappingFormField.values()).map(Enum::name).toList()),
                        "sourceType", Map.of("type", "STRING", "enum", List.of("META", "JSON_LD")),
                        "sourceKey", Map.of("type", "STRING")),
                "required", List.of("field", "sourceType", "sourceKey"));
        return Map.of(
                "type", "OBJECT",
                "properties", Map.of("mappings", Map.of("type", "ARRAY", "items", entry)));
    }
}