package com.rpgjr.hophop.service;

import com.rpgjr.hophop.enums.Currency;
import com.rpgjr.hophop.enums.Setup;
import com.rpgjr.hophop.model.dto.request.CreateApplicationRequestDto;
import com.rpgjr.hophop.model.entity.Application;
import com.rpgjr.hophop.model.entity.Status;
import com.rpgjr.hophop.model.entity.StatusHistory;
import com.rpgjr.hophop.repository.ApplicationRepository;
import com.rpgjr.hophop.repository.StatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.rpgjr.hophop.utils.Constants.APPLICATION_ERROR_DUPLICATE_LINK;
import static com.rpgjr.hophop.utils.Constants.APPLICATION_ERROR_FUTURE_DATE;
import static com.rpgjr.hophop.utils.Constants.APPLICATION_ERROR_SALARY_RANGE;
import static com.rpgjr.hophop.utils.Constants.APPLICATION_ERROR_UNKNOWN;
import static java.util.Optional.ofNullable;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    private final StatusHistoryRepository statusHistoryRepository;

    private final StatusService statusService;

    public List<Application> getAllApplicationsByDateRange(LocalDate from, LocalDate to) {
        return applicationRepository.findByAppliedDateBetweenOrderByAppliedDateAscIdAsc(from, to);
    }

    public Application getApplicationById(Long applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, APPLICATION_ERROR_UNKNOWN + applicationId));
    }

    @Transactional
    public Application createApplication(CreateApplicationRequestDto request) {
        checkSalaryRange(request);
        checkNotFuture(request);

        Application application = Application.builder()
                .status(statusService.getStatusById(request.statusId()))
                .title(request.title().trim())
                .company(request.company().trim())
                .salaryMin(request.salaryMin())
                .salaryMax(request.salaryMax())
                .salaryCurrency(ofNullable(request.salaryCurrency()).orElse(Currency.PHP))
                .employmentType(request.employmentType())
                .setup(ofNullable(request.setup()).orElse(Setup.Onsite))
                .remarks(request.remarks())
                .location(blankToNull(request.location()))
                .description(request.description())
                .link(request.link().trim())
                .appliedDate(request.appliedDate())
                .build();

        try {
            Application saved = applicationRepository.save(application);
            // the first entry is dated on the day applied, so the history starts with the application
            recordStatusChange(saved.getId(), null, saved.getStatus(), saved.getAppliedDate().atStartOfDay());
            return saved;
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, APPLICATION_ERROR_DUPLICATE_LINK);
        }
    }

    public List<StatusHistory> getStatusHistory(Long applicationId) {
        getApplicationById(applicationId);
        return statusHistoryRepository.findByApplicationIdOrderByChangedAtAscIdAsc(applicationId);
    }

    private void recordStatusChange(Long applicationId, Status from, Status to, LocalDateTime at) {
        statusHistoryRepository.save(StatusHistory.builder()
                .applicationId(applicationId)
                .fromStatus(from == null ? null : from.getName())
                .fromColor(from == null ? null : from.getColor())
                .toStatus(to.getName())
                .toColor(to.getColor())
                .changedAt(at)
                .build());
    }

    @Transactional
    public Application updateApplication(Long applicationId, CreateApplicationRequestDto request) {
        checkSalaryRange(request);
        checkNotFuture(request);

        Application application = getApplicationById(applicationId);
        Status previous = application.getStatus();
        Status next = statusService.getStatusById(request.statusId());
        application.setStatus(next);
        application.setTitle(request.title().trim());
        application.setCompany(request.company().trim());
        application.setSalaryMin(request.salaryMin());
        application.setSalaryMax(request.salaryMax());
        application.setSalaryCurrency(ofNullable(request.salaryCurrency()).orElse(Currency.PHP));
        application.setEmploymentType(request.employmentType());
        application.setSetup(ofNullable(request.setup()).orElse(Setup.Onsite));
        application.setRemarks(request.remarks());
        application.setLocation(blankToNull(request.location()));
        application.setDescription(request.description());
        application.setLink(request.link().trim());
        application.setAppliedDate(request.appliedDate());

        try {
            Application saved = applicationRepository.saveAndFlush(application);
            if (!previous.getId().equals(next.getId())) {
                recordStatusChange(applicationId, previous, next, LocalDateTime.now());
            }
            return saved;
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, APPLICATION_ERROR_DUPLICATE_LINK);
        }
    }

    private String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }

    // one day of slack, so a user ahead of the server's clock is not refused on their own today
    private void checkNotFuture(CreateApplicationRequestDto request) {
        if (request.appliedDate().isAfter(LocalDate.now().plusDays(1))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, APPLICATION_ERROR_FUTURE_DATE);
        }
    }

    private void checkSalaryRange(CreateApplicationRequestDto request) {
        if (request.salaryMax() != null
                && (request.salaryMin() == null || request.salaryMax().compareTo(request.salaryMin()) < 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, APPLICATION_ERROR_SALARY_RANGE);
        }
    }

    @Transactional
    public Application updateApplicationByStatus(Long applicationId, Long statusId) {
        Application application = getApplicationById(applicationId);
        Status previous = application.getStatus();
        Status status = statusService.getStatusById(statusId);

        application.setStatus(status);
        Application saved = applicationRepository.save(application);
        if (!previous.getId().equals(status.getId())) {
            recordStatusChange(applicationId, previous, status, LocalDateTime.now());
        }
        return saved;
    }

    public void deleteApplicationById(Long applicationId) {
        Application application = getApplicationById(applicationId);
        applicationRepository.delete(application);
    }

}
