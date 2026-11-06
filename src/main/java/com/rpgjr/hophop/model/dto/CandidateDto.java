package com.rpgjr.hophop.model.dto;

import com.rpgjr.hophop.enums.MappingSourceType;

// One tag found on a page, with its value, so the user can pick from it.
public record CandidateDto(

        MappingSourceType type,

        String key,

        String value

) {
}
