package com.rpgjr.hophop.scraper;

import com.rpgjr.hophop.enums.MappingSourceType;
import com.rpgjr.hophop.model.dto.CandidateDto;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.*;

/**
 * Lists the tags on a page, and reads one tag by its key.
 */
@Component
public class PageMetadataReader {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final int MAX_CANDIDATES = 150;
    private static final int MAX_VALUE_CHARS = 200;

    /**
     * Every meta tag and every JobPosting value on the page.
     */
    public List<CandidateDto> candidates(Document page) {
        List<CandidateDto> found = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (Element meta : page.select("meta[content]")) {
            String key = !meta.attr("property").isBlank() ? meta.attr("property") : meta.attr("name");
            String value = meta.attr("content").trim();
            if (!key.isBlank() && !value.isEmpty() && seen.add(key)) {
                found.add(new CandidateDto(MappingSourceType.META, key, cut(value)));
            }
        }
        addJsonValues(findJobPosting(page), "", found);

        return found.size() > MAX_CANDIDATES ? found.subList(0, MAX_CANDIDATES) : found;
    }

    /**
     * The text a saved rule points at, or null when the tag is missing or empty.
     */
    public String resolve(Document page, MappingSourceType type, String key) {
        if (type == MappingSourceType.META) {
            try {
                String value = page.select("meta[property=\"" + key + "\"], meta[name=\"" + key + "\"]")
                        .attr("content");
                return value.isBlank() ? null : value.trim();
            } catch (IllegalArgumentException e) {
                return null; // the key broke the CSS selector
            }
        }

        JsonNode node = findJobPosting(page);
        for (String part : key.split("\\.")) {
            node = part.matches("\\d+") ? node.path(Integer.parseInt(part)) : node.path(part);
        }
        return node.isValueNode() && !node.isNull() && !node.asString().isBlank() ? node.asString() : null;
    }

    /**
     * The schema.org JobPosting block, or a missing node when the page has none.
     */
    public JsonNode findJobPosting(Document page) {
        for (Element script : page.select("script[type=application/ld+json]")) {
            try {
                JsonNode found = searchJobPosting(MAPPER.readTree(script.data()));
                if (found != null) return found;
            } catch (JacksonException e) {
                // ignore a broken JSON block and try the next one
            }
        }
        return MAPPER.missingNode();
    }

    private JsonNode searchJobPosting(JsonNode node) {
        if (node.isArray()) {
            for (JsonNode child : node) {
                JsonNode found = searchJobPosting(child);
                if (found != null) return found;
            }
        } else if (node.isObject()) {
            JsonNode type = node.path("@type");
            boolean isJob = type.isArray()
                    ? type.toString().contains("JobPosting") : "JobPosting".equals(type.asString());
            if (isJob) return node;
            return searchJobPosting(node.path("@graph"));
        }
        return null;
    }

    // walks the JSON and records each plain value under a dotted path like hiringOrganization.name
    private void addJsonValues(JsonNode node, String path, List<CandidateDto> out) {
        if (node.isObject()) {
            for (Map.Entry<String, JsonNode> field : node.properties()) {
                if (field.getKey().startsWith("@")) continue;
                addJsonValues(field.getValue(), path.isEmpty() ? field.getKey() : path + "." + field.getKey(), out);
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                addJsonValues(node.get(i), path + "." + i, out);
            }
        } else if (node.isValueNode() && !node.isNull() && !path.isEmpty() && !node.asString().isBlank()) {
            out.add(new CandidateDto(MappingSourceType.JSON_LD, path, cut(node.asString())));
        }
    }

    private String cut(String value) {
        return value.length() > MAX_VALUE_CHARS ? value.substring(0, MAX_VALUE_CHARS) : value;
    }
}