package com.rpgjr.hophop.controller;

import com.rpgjr.hophop.model.dto.InspectResultDto;
import com.rpgjr.hophop.model.dto.MappingEntryDto;
import com.rpgjr.hophop.model.dto.SiteMappingGroupDto;
import com.rpgjr.hophop.model.dto.request.CreateMappingsRequestDto;
import com.rpgjr.hophop.model.dto.request.SaveMappingsRequestDto;
import com.rpgjr.hophop.model.dto.request.UrlRequestDto;
import com.rpgjr.hophop.service.SiteMappingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/site-mappings")
@RequiredArgsConstructor
public class SiteMappingController {

    private final SiteMappingService siteMappingService;

    @GetMapping
    public List<SiteMappingGroupDto> list() {
        return siteMappingService.list();
    }

    @PostMapping("/inspect")
    public InspectResultDto inspect(@Valid @RequestBody UrlRequestDto body) {
        return siteMappingService.inspect(body.url());
    }

    @PostMapping("/suggest")
    public List<MappingEntryDto> suggest(@Valid @RequestBody UrlRequestDto body) {
        return siteMappingService.suggest(body.url());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SiteMappingGroupDto create(@Valid @RequestBody CreateMappingsRequestDto body) {
        return siteMappingService.create(body.host(), body.mappings());
    }

    @PutMapping("/{host}")
    public SiteMappingGroupDto update(@PathVariable String host, @Valid @RequestBody SaveMappingsRequestDto body) {
        return siteMappingService.update(host, body.mappings());
    }

    @DeleteMapping("/{host}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String host) {
        siteMappingService.delete(host);
    }
}