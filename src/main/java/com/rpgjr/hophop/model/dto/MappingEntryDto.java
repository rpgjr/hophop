package com.rpgjr.hophop.model.dto;

import com.rpgjr.hophop.enums.MappingFormField;
import com.rpgjr.hophop.enums.MappingSourceType;
import com.rpgjr.hophop.model.entity.SiteMapping;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MappingEntryDto(

        @NotNull
        MappingFormField field,

        @NotNull
        MappingSourceType sourceType,

        @NotBlank String sourceKey) {

    public static MappingEntryDto from(SiteMapping sm) {
        return new MappingEntryDto(sm.getFieldName(), sm.getSourceType(), sm.getSourceKey());
    }
}
