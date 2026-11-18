package com.rpgjr.hophop.scraper;

import com.rpgjr.hophop.enums.MappingSourceType;
import com.rpgjr.hophop.model.dto.CandidateDto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageMetadataReaderTest {

    private final PageMetadataReader reader = new PageMetadataReader();

    private static final String INDEED_LIKE = """
            <html><body>
              <h1 data-testid="jobsearch-JobInfoHeader-title">Full Stack and Ai Developer</h1>
              <div data-testid="inlineHeader-companyName"><a>Solve Outsource</a></div>
              <div data-testid="inlineHeader-companyLocation">Manila</div>
              <div id="jobDescriptionText">
                <p><b>Solve Outsource Inc.</b></p>
                <p>Build Real Applications.</p>
                <ul><li>React</li><li>Spring Boot</li></ul>
              </div>
            </body></html>""";

    private String value(List<CandidateDto> found, String key) {
        return found.stream()
                .filter(c -> c.type() == MappingSourceType.CSS && c.key().equals(key))
                .map(CandidateDto::value)
                .findFirst()
                .orElse(null);
    }

    @Test
    void offersTheTitleCompanyAndDescriptionOfAnIndeedStylePage() {
        Document page = Jsoup.parse(INDEED_LIKE);
        List<CandidateDto> found = reader.candidates(page);

        assertThat(value(found, "[data-testid=\"jobsearch-JobInfoHeader-title\"]")).isEqualTo("Full Stack and Ai Developer");
        assertThat(value(found, "[data-testid=\"inlineHeader-companyName\"]")).isEqualTo("Solve Outsource");
        assertThat(value(found, "#jobDescriptionText")).contains("Build Real Applications.");
    }

    @Test
    void keepsParagraphsAndListItemsInALongDescription() {
        List<CandidateDto> found = reader.candidates(Jsoup.parse(INDEED_LIKE));

        assertThat(value(found, "#jobDescriptionText")).isEqualTo(
                "Solve Outsource Inc.\n\nBuild Real Applications.\n\n• React\n• Spring Boot");
    }

    @Test
    void aSavedRuleReadsTheSameElementBack() {
        Document page = Jsoup.parse(INDEED_LIKE);

        assertThat(reader.resolve(page, MappingSourceType.CSS, "[data-testid=\"inlineHeader-companyName\"]"))
                .isEqualTo("Solve Outsource");
    }

    private static final String LINKEDIN_CHIPS = """
            <html><body><h1>Jr. BackEnd Developer</h1>
              <button><span><svg width="8" height="8"></svg><strong>On-site</strong></span></button>
              <button><span><svg width="8" height="8"></svg>Full-time</span></button>
              <button>30K PHP/month - 45K PHP/month</button>
            </body></html>""";

    private String pattern(List<CandidateDto> found, String key) {
        return found.stream()
                .filter(c -> c.type() == MappingSourceType.PATTERN && c.key().equals(key))
                .map(CandidateDto::value)
                .findFirst()
                .orElse(null);
    }

    @Test
    void findsSetupAndEmploymentInChipsThatHoldAnIcon() {
        List<CandidateDto> found = reader.candidates(Jsoup.parse(LINKEDIN_CHIPS));

        assertThat(pattern(found, "setup")).isEqualTo("On-site");
        assertThat(pattern(found, "employment_type")).isEqualTo("Full-time");
    }

    @Test
    void findsASalaryWrittenWithTheCurrencyAfterTheAmount() {
        List<CandidateDto> found = reader.candidates(Jsoup.parse(LINKEDIN_CHIPS));

        assertThat(pattern(found, "salary")).isEqualTo("30K PHP/month - 45K PHP/month");
    }

    private static final String HEADING_ONLY_PAGE = """
            <html><body>
              <h2>Job details</h2><p>PHP 50,000 a month</p>
              <section>
                <h2 data-testid="vj-job-description-heading">Full job description</h2>
                <div><p><b>Solve Outsource Inc.</b></p>
                <p>Build Real Applications. Work Smarter with AI. Grow Your Development Career. Are you a junior developer who loves
                experimenting with AI coding tools and turning ideas into working applications? Join a team that ships.</p></div>
              </section>
            </body></html>""";

    @Test
    void findsTheDescriptionUnderItsHeadingWhenThePageHasNoUsefulClass() {
        List<CandidateDto> found = reader.candidates(Jsoup.parse(HEADING_ONLY_PAGE));

        assertThat(pattern(found, "description"))
                .startsWith("Solve Outsource Inc.")
                .contains("Join a team that ships.")
                .doesNotContain("Full job description");
    }

    @Test
    void aSavedDescriptionRuleReadsTheSameTextBack() {
        Document page = Jsoup.parse(HEADING_ONLY_PAGE);

        assertThat(reader.resolve(page, MappingSourceType.PATTERN, "description")).contains("Build Real Applications.");
    }
}
