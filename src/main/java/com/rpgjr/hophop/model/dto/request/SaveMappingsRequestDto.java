package com.rpgjr.hophop.model.dto.request;

import com.rpgjr.hophop.model.dto.MappingEntryDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SaveMappingsRequestDto(

        @NotNull
        @Valid
        List<MappingEntryDto> mappings

) {
}
