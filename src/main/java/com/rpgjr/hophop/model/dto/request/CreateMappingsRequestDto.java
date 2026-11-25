package com.rpgjr.hophop.model.dto.request;

import com.rpgjr.hophop.model.dto.MappingEntryDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateMappingsRequestDto(

        @NotBlank
        @Size(max = 100)
        String name,

        @NotBlank
        String host,

        @Size(max = 100)
        String country,

        @NotNull
        @Valid
        List<MappingEntryDto> mappings

) {
}
