package com.rpgjr.hophop.service;

import com.rpgjr.hophop.model.dto.InspectResultDto;
import com.rpgjr.hophop.model.dto.MappingEntryDto;
import com.rpgjr.hophop.model.dto.SiteMappingGroupDto;
import com.rpgjr.hophop.model.entity.SiteMapping;
import com.rpgjr.hophop.repository.SiteMappingRepository;
import com.rpgjr.hophop.scraper.GeminiMappingSuggester;
import com.rpgjr.hophop.scraper.JobPageFetcher;
import com.rpgjr.hophop.scraper.PageMetadataReader;
import com.rpgjr.hophop.utils.HostUtils;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SiteMappingService {

    private final SiteMappingRepository siteMappingRepository;
    private final JobPageFetcher jobPageFetcher;
    private final PageMetadataReader pageMetadataReader;
    private final GeminiMappingSuggester geminiMappingSuggester;

    // GET /api/site-mappings: every saved website with its rules
    public List<SiteMappingGroupDto> list() {
        Map<String, List<MappingEntryDto>> byHost = new LinkedHashMap<>();
        for (SiteMapping row : siteMappingRepository.findAllByOrderBySiteHostAscIdAsc()) {
            byHost.computeIfAbsent(row.getSiteHost(), host -> new ArrayList<>()).add(MappingEntryDto.from(row));
        }
        return byHost.entrySet().stream()
                .map(entry -> new SiteMappingGroupDto(entry.getKey(), entry.getValue()))
                .toList();
    }

    // POST /api/site-mappings/inspect: list the tags on a sample page, no AI
    public InspectResultDto inspect(String url) {
        String host = HostUtils.hostOf(url);
        Document page = jobPageFetcher.fetch(url);
        List<MappingEntryDto> saved = siteMappingRepository.findBySiteHost(host).stream()
                .map(MappingEntryDto::from)
                .toList();
        return new InspectResultDto(host, pageMetadataReader.candidates(page), saved);
    }

    // POST /api/site-mappings/suggest: AI picks a tag for each field, nothing saved
    public List<MappingEntryDto> suggest(String url) {
        Document page = jobPageFetcher.fetch(url);
        return geminiMappingSuggester.suggest(pageMetadataReader.candidates(page));
    }

    // POST /api/site-mappings: add a website that has no rules yet
    @Transactional
    public SiteMappingGroupDto create(String host, List<MappingEntryDto> entries) {
        String siteHost = HostUtils.normalizeHost(host);
        if (siteMappingRepository.existsBySiteHost(siteHost)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, siteHost + " already has rules. Edit it instead.");
        }
        return replaceRules(siteHost, entries);
    }

    // PUT /api/site-mappings/{host}: replace all rules of a website that already has rules
    @Transactional
    public SiteMappingGroupDto update(String host, List<MappingEntryDto> entries) {
        String siteHost = HostUtils.normalizeHost(host);
        if (!siteMappingRepository.existsBySiteHost(siteHost)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, siteHost + " has no rules yet. Add it first.");
        }
        return replaceRules(siteHost, entries);
    }

    // shared by create and update: delete the old rows, insert the new ones
    private SiteMappingGroupDto replaceRules(String siteHost, List<MappingEntryDto> entries) {
        if (entries.stream().map(MappingEntryDto::field).distinct().count() != entries.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each field can be mapped only once");
        }

        siteMappingRepository.deleteBySiteHost(siteHost);
        siteMappingRepository.saveAll(entries.stream()
                .map(entry -> SiteMapping.builder()
                        .siteHost(siteHost)
                        .fieldName(entry.field())
                        .sourceType(entry.sourceType())
                        .sourceKey(entry.sourceKey().trim())
                        .build())
                .toList());
        return new SiteMappingGroupDto(siteHost, entries);
    }

    // DELETE /api/site-mappings/{host}
    @Transactional
    public void delete(String host) {
        siteMappingRepository.deleteBySiteHost(HostUtils.normalizeHost(host));
    }
}