package com.rpgjr.hophop.model.dto;

import com.rpgjr.hophop.model.entity.MappingGroup;

import java.util.List;

public record SiteMappingGroupDto(

        Long id,

        String name,

        String host,

        String country,

        List<MappingEntryDto> mappings

) {

    public static SiteMappingGroupDto from(MappingGroup group, List<MappingEntryDto> mappings) {
        return new SiteMappingGroupDto(group.getId(), group.getName(), group.getSiteHost(), group.getCountry(), mappings);
    }
}
