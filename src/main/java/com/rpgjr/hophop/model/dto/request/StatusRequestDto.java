package com.rpgjr.hophop.model.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StatusRequestDto(

        @NotBlank @Size(max = 50) String name,

        @NotBlank @Pattern(regexp = "^#[0-9a-fA-F]{6}$") String color

) {
}
