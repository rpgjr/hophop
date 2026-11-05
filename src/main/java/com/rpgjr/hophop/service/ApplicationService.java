package com.rpgjr.hophop.service;

import com.rpgjr.hophop.model.entity.Application;
import com.rpgjr.hophop.model.entity.Status;
import com.rpgjr.hophop.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static com.rpgjr.hophop.utils.Constants.APPLICATION_ERROR_UNKNOWN;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    private final StatusService statusService;

    public List<Application> getAllApplicationsByDateRange(LocalDate from, LocalDate to) {
        return applicationRepository.findByAppliedDateBetweenOrderByAppliedDateAscIdAsc(from, to);
    }

    public Application getApplicationById(Long applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, APPLICATION_ERROR_UNKNOWN + applicationId));
    }

    public Application updateApplicationByStatus(Long applicationId, Long statusId) {
        Application application = getApplicationById(applicationId);
        Status status = statusService.getStatusById(statusId);

        application.setStatus(status);
        return applicationRepository.save(application);
    }

    public void deleteApplicationById(Long applicationId) {
        Application application = getApplicationById(applicationId);
        applicationRepository.delete(application);
    }

}
