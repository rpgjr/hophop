package com.rpgjr.hophop.controller;

import com.rpgjr.hophop.model.dto.request.StatusUpdateResquestDto;
import com.rpgjr.hophop.model.entity.Application;
import com.rpgjr.hophop.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static com.rpgjr.hophop.utils.Constants.*;
import static java.util.Optional.ofNullable;

@Slf4j
@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @GetMapping
    public List<Application> getApplicationsByDateRange(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from, @RequestParam LocalDate to) throws Exception {
        try {
            return applicationService.getAllApplicationsByDateRange(from, to);
        } catch (Exception ex) {
            log.info(APPLICATION_ERROR_INTERNAL_SERVER_ERROR, ex);
            throw new Exception(INTERNAL_SERVER_ERROR, ex);
        }
    }

    @PatchMapping("/{applicationId}/status")
    public Application updateApplicationStatus(@PathVariable Long applicationId, @RequestBody StatusUpdateResquestDto status) throws Exception {
        try {
            Long statusId = ofNullable(status.statusId())
                    .orElseThrow(() -> new Exception(MISSING_REQUEST_DATA + "statusId"));

            return applicationService.updateApplicationByStatus(applicationId, statusId);
        } catch (Exception ex) {
            log.info(APPLICATION_ERROR_INTERNAL_SERVER_ERROR, ex);
            throw new Exception(INTERNAL_SERVER_ERROR, ex);
        }
    }

    @DeleteMapping("/{applicationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public String deleteApplicationById(@PathVariable Long applicationId) throws Exception {
        try {
            applicationService.deleteApplicationById(applicationId);
            return APPLICATION + DELETE_SUCCESS;
        } catch (Exception ex) {
            log.info(APPLICATION_ERROR_DELETE, applicationId, ex.getMessage());
            return APPLICATION + DELETE_FAILURE;
        }
    }

}
