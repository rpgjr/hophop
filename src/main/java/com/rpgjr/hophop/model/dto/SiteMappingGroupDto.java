package com.rpgjr.hophop.model.dto;

import java.util.List;

public record SiteMappingGroupDto(

        String host,

        List<MappingEntryDto> mappings
        
) {
}
