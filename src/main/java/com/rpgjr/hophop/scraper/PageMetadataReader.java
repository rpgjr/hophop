package com.rpgjr.hophop.scraper;

import com.rpgjr.hophop.enums.MappingSourceType;
import com.rpgjr.hophop.model.dto.CandidateDto;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Lists the tags on a page, and reads one tag by its key.
 */
@Component
public class PageMetadataReader {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final int MAX_CANDIDATES = 150;
    private static final int MAX_VALUE_CHARS = 12000;

    private record LinePattern(Pattern regex, int maxChars) {
    }

    // Lines found by their look, so they work on any site. Keep in step with the Mac app's BrowserPageReader.
    private static final Map<String, LinePattern> LINE_PATTERNS = new LinkedHashMap<>();

    static {
        LINE_PATTERNS.put("salary", new LinePattern(Pattern.compile("(₱|PHP|USD|\\$)\\s?\\d[\\d,]*|\\d[\\d,.]*\\s?k?\\s?(₱|PHP|USD)", Pattern.CASE_INSENSITIVE), 120));
        LINE_PATTERNS.put("setup", new LinePattern(Pattern.compile("\\b(hybrid|remote|work from home|wfh|on-?site)\\b", Pattern.CASE_INSENSITIVE), 120));
        LINE_PATTERNS.put("employment_type", new LinePattern(Pattern.compile("\\b(full[- ]?time|part[- ]?time|contract(or)?|temporary|freelance|internship)\\b", Pattern.CASE_INSENSITIVE), 60));
    }

    // Page elements worth offering as tags. Keep in step with the list in the Mac app's BrowserPageReader.
    public static final List<String> CSS_SELECTORS = List.of(
            "h1",
            ".top-card-layout__title",
            ".topcard__org-name-link",
            ".topcard__flavor--bullet",
            ".description__job-criteria-item:nth-child(2) .description__job-criteria-text",
            ".show-more-less-html__markup",
            "[data-testid=\"jobsearch-JobInfoHeader-title\"]",
            "[data-testid=\"inlineHeader-companyName\"]",
            "[data-testid=\"inlineHeader-companyLocation\"]",
            "#salaryInfoAndJobType",
            "#jobDescriptionText",
            "[data-automation=\"jobAdDetails\"]",
            ".job-details-fit-level-preferences",
            ".job-details-jobs-unified-top-card__job-title",
            ".job-details-jobs-unified-top-card__company-name",
            ".job-details-jobs-unified-top-card__primary-description-container",
            "#job-details");

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
        addCssValues(page, found);
        addLineValues(page, found);
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

        if (type == MappingSourceType.PATTERN) {
            return findLine(page, key);
        }

        if (type == MappingSourceType.CSS) {
            try {
                Element element = page.selectFirst(key);
                String text = element == null ? "" : readableText(element);
                return text.isEmpty() ? null : text;
            } catch (IllegalArgumentException e) {
                return null; // the key is not a valid CSS selector
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

    // records the text of each known page element that exists and is not empty
    private void addCssValues(Document page, List<CandidateDto> out) {
        for (String selector : CSS_SELECTORS) {
            Element element = page.selectFirst(selector);
            String text = element == null ? "" : readableText(element);
            if (!text.isEmpty()) out.add(new CandidateDto(MappingSourceType.CSS, selector, cut(text)));
        }

        // sites like JobStreet and Indeed label each detail, for example data-automation="job-detail-location"
        Set<String> seen = new HashSet<>();
        for (Element element : page.select("[data-automation], [data-testid]")) {
            String attribute = element.hasAttr("data-automation") ? "data-automation" : "data-testid";
            String text = readableText(element);
            String selector = "[" + attribute + "=\"" + element.attr(attribute) + "\"]";
            if (!text.isEmpty() && text.length() <= 120 && seen.add(selector) && seen.size() <= 40) {
                out.add(new CandidateDto(MappingSourceType.CSS, selector, cut(text)));
            }
        }
    }

    // records the first visible line that looks like a salary, a work setup or an employment type
    private void addLineValues(Document page, List<CandidateDto> out) {
        for (String key : LINE_PATTERNS.keySet()) {
            String line = findLine(page, key);
            if (line != null) out.add(new CandidateDto(MappingSourceType.PATTERN, key, cut(line)));
        }
    }

    // the first text that matches the pattern, or null. An element counts by its own text, so a chip
    // like <span><svg/>On-site</span> is found even though it has an icon inside.
    private String findLine(Document page, String key) {
        LinePattern pattern = LINE_PATTERNS.get(key);
        if (pattern == null || page.body() == null) return null;

        for (Element element : page.body().select("*")) {
            if (Set.of("script", "style", "noscript").contains(element.tagName())) continue;
            String text = element.ownText().trim();
            if (!text.isEmpty() && text.length() <= pattern.maxChars() && pattern.regex().matcher(text).find()) {
                return text;
            }
        }
        return null;
    }

    /**
     * The text of an element with its paragraphs, line breaks and list items kept,
     * so a long job description stays readable instead of running together.
     */
    private String readableText(Element element) {
        StringBuilder out = new StringBuilder();
        NodeTraversor.traverse(new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                if (node instanceof TextNode text) {
                    out.append(text.text());
                } else if (node instanceof Element child) {
                    if (child.tagName().equals("br")) out.append('\n');
                    else if (child.tagName().equals("li")) {
                        if (!out.isEmpty() && out.charAt(out.length() - 1) != '\n') out.append('\n');
                        out.append("• ");
                    } else if (child.isBlock()) out.append('\n');
                }
            }

            @Override
            public void tail(Node node, int depth) {
                if (node instanceof Element child && child.isBlock() && !child.tagName().equals("li")) out.append('\n');
            }
        }, element);
        return out.toString()
                .replaceAll("[ \\t]+\n", "\n")
                .replaceAll("\n[ \\t]+", "\n")
                .replaceAll("\n{3,}", "\n\n")
                .trim();
    }

    private String cut(String value) {
        return value.length() > MAX_VALUE_CHARS ? value.substring(0, MAX_VALUE_CHARS) : value;
    }
}