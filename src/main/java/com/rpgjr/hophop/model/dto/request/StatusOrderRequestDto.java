package com.rpgjr.hophop.model.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StatusOrderRequestDto(

        @NotEmpty List<Long> ids

) {
}
