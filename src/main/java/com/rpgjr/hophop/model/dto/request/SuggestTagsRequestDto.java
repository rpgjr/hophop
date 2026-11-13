package com.rpgjr.hophop.model.dto.request;

import com.rpgjr.hophop.model.dto.CandidateDto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SuggestTagsRequestDto(

        @NotEmpty
        @Size(max = 150)
        List<CandidateDto> candidates

) {
}
