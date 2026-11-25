package com.rpgjr.hophop.service;

import com.rpgjr.hophop.model.dto.CandidateDto;
import com.rpgjr.hophop.model.dto.InspectResultDto;
import com.rpgjr.hophop.model.dto.MappingEntryDto;
import com.rpgjr.hophop.model.dto.SiteMappingGroupDto;
import com.rpgjr.hophop.model.dto.request.CreateMappingsRequestDto;
import com.rpgjr.hophop.model.dto.request.SaveMappingsRequestDto;
import com.rpgjr.hophop.model.entity.MappingGroup;
import com.rpgjr.hophop.model.entity.SiteMapping;
import com.rpgjr.hophop.repository.ApplicationRepository;
import com.rpgjr.hophop.repository.MappingGroupRepository;
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

import static com.rpgjr.hophop.utils.Constants.MAPPING_ERROR_DUPLICATE_NAME;
import static com.rpgjr.hophop.utils.Constants.MAPPING_ERROR_UNKNOWN;

@Service
@RequiredArgsConstructor
public class SiteMappingService {

    private final SiteMappingRepository siteMappingRepository;
    private final MappingGroupRepository mappingGroupRepository;
    private final ApplicationRepository applicationRepository;
    private final JobPageFetcher jobPageFetcher;
    private final PageMetadataReader pageMetadataReader;
    private final GeminiMappingSuggester geminiMappingSuggester;

    // GET /api/site-mappings: every saved mapping with its rules
    public List<SiteMappingGroupDto> list() {
        Map<Long, List<MappingEntryDto>> rules = rulesByGroup();
        return mappingGroupRepository.findAllByOrderByNameAsc().stream()
                .map(group -> SiteMappingGroupDto.from(group, rules.getOrDefault(group.getId(), List.of())))
                .toList();
    }

    // POST /api/site-mappings/inspect: list the tags on a sample page, no AI
    public InspectResultDto inspect(String url) {
        String host = HostUtils.hostOf(url);
        Document page = jobPageFetcher.fetch(url);
        Map<Long, List<MappingEntryDto>> rules = rulesByGroup();
        List<SiteMappingGroupDto> groups = mappingGroupRepository.findBySiteHostOrderByNameAsc(host).stream()
                .map(group -> SiteMappingGroupDto.from(group, rules.getOrDefault(group.getId(), List.of())))
                .toList();
        return new InspectResultDto(host, pageMetadataReader.candidates(page), groups);
    }

    private Map<Long, List<MappingEntryDto>> rulesByGroup() {
        Map<Long, List<MappingEntryDto>> byGroup = new LinkedHashMap<>();
        for (SiteMapping row : siteMappingRepository.findAllByOrderByGroupIdAscIdAsc()) {
            byGroup.computeIfAbsent(row.getGroupId(), id -> new ArrayList<>()).add(MappingEntryDto.from(row));
        }
        return byGroup;
    }

    // POST /api/site-mappings/suggest: AI picks a tag for each field, nothing saved
    public List<MappingEntryDto> suggest(String url) {
        Document page = jobPageFetcher.fetch(url);
        return geminiMappingSuggester.suggest(pageMetadataReader.candidates(page));
    }

    // POST /api/site-mappings/suggest-tags: same as suggest, for tags the app already read in its own browser
    public List<MappingEntryDto> suggest(List<CandidateDto> candidates) {
        return geminiMappingSuggester.suggest(candidates);
    }

    // POST /api/site-mappings: add a named mapping for a website
    @Transactional
    public SiteMappingGroupDto create(CreateMappingsRequestDto request) {
        String name = request.name().trim();
        if (mappingGroupRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, MAPPING_ERROR_DUPLICATE_NAME + name);
        }
        MappingGroup group = mappingGroupRepository.save(MappingGroup.builder()
                .name(name)
                .siteHost(HostUtils.normalizeHost(request.host()))
                .country(blankToNull(request.country()))
                .build());
        return replaceRules(group, request.mappings());
    }

    // PUT /api/site-mappings/{id}: rename a mapping, change its country and replace its rules
    @Transactional
    public SiteMappingGroupDto update(Long id, SaveMappingsRequestDto request) {
        MappingGroup group = mappingGroupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MAPPING_ERROR_UNKNOWN + id));
        String name = request.name().trim();
        if (mappingGroupRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, MAPPING_ERROR_DUPLICATE_NAME + name);
        }
        if (!group.getName().equals(name)) {
            applicationRepository.renameWebsite(group.getName(), name);
            group.setName(name);
        }
        group.setCountry(blankToNull(request.country()));
        mappingGroupRepository.save(group);
        return replaceRules(group, request.mappings());
    }

    // shared by create and update: delete the old rows, insert the new ones
    private SiteMappingGroupDto replaceRules(MappingGroup group, List<MappingEntryDto> entries) {
        if (entries.stream().map(MappingEntryDto::field).distinct().count() != entries.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each field can be mapped only once");
        }

        siteMappingRepository.deleteByGroupId(group.getId());
        siteMappingRepository.saveAll(entries.stream()
                .map(entry -> SiteMapping.builder()
                        .groupId(group.getId())
                        .fieldName(entry.field())
                        .sourceType(entry.sourceType())
                        .sourceKey(entry.sourceKey().trim())
                        .build())
                .toList());
        return SiteMappingGroupDto.from(group, entries);
    }

    private String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }

    // DELETE /api/site-mappings/{id}
    @Transactional
    public void delete(Long id) {
        siteMappingRepository.deleteByGroupId(id);
        mappingGroupRepository.deleteById(id);
    }
}
