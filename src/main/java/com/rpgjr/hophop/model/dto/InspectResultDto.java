package com.rpgjr.hophop.model.dto;

import java.util.List;

public record InspectResultDto(

        String host,

        List<CandidateDto> candidates,

        // every saved mapping for this website, one per country
        List<SiteMappingGroupDto> groups

) {
}
