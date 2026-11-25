package com.rpgjr.hophop.model.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rpgjr.hophop.enums.Currency;
import com.rpgjr.hophop.enums.EmploymentType;
import com.rpgjr.hophop.enums.Setup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateApplicationRequestDto(

        @NotNull Long statusId,

        @NotBlank String title,

        @NotBlank String company,

        BigDecimal salaryMin,

        BigDecimal salaryMax,

        Currency salaryCurrency,

        EmploymentType employmentType,

        Setup setup,

        String remarks,

        @Size(max = 255) String location,

        @Size(max = 20000) String description,

        @NotBlank String link,

        @NotNull LocalDate appliedDate

) {
}
