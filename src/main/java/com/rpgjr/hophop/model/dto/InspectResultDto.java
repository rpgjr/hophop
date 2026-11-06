package com.rpgjr.hophop.model.dto;

import java.util.List;

public record InspectResultDto(

        String host,

        List<CandidateDto> candidates,

        List<MappingEntryDto> mappings

) {
}
