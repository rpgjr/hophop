package com.rpgjr.hophop.model.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UrlRequestDto(

        @NotBlank
        String url

) {
}
