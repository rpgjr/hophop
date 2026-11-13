package com.rpgjr.hophop.model.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rpgjr.hophop.enums.Currency;
import com.rpgjr.hophop.enums.EmploymentType;
import com.rpgjr.hophop.enums.Setup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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

        @NotBlank String link,

        @NotNull LocalDate appliedDate

) {
}
