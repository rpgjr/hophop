package com.rpgjr.hophop.model.dto.request;

import com.rpgjr.hophop.model.dto.MappingEntryDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateMappingsRequestDto(

        @NotBlank
        String host,

        @NotNull
        @Valid
        List<MappingEntryDto> mappings

) {
}
