package com.rpgjr.hophop.service;

import com.rpgjr.hophop.enums.Currency;
import com.rpgjr.hophop.enums.Setup;
import com.rpgjr.hophop.model.dto.request.CreateApplicationRequestDto;
import com.rpgjr.hophop.model.entity.Application;
import com.rpgjr.hophop.model.entity.Status;
import com.rpgjr.hophop.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static com.rpgjr.hophop.utils.Constants.APPLICATION_ERROR_DUPLICATE_LINK;
import static com.rpgjr.hophop.utils.Constants.APPLICATION_ERROR_SALARY_RANGE;
import static com.rpgjr.hophop.utils.Constants.APPLICATION_ERROR_UNKNOWN;
import static java.util.Optional.ofNullable;

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

    public Application createApplication(CreateApplicationRequestDto request) {
        checkSalaryRange(request);

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
                .description(request.description())
                .link(request.link().trim())
                .appliedDate(request.appliedDate())
                .build();

        try {
            return applicationRepository.save(application);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, APPLICATION_ERROR_DUPLICATE_LINK);
        }
    }

    public Application updateApplication(Long applicationId, CreateApplicationRequestDto request) {
        checkSalaryRange(request);

        Application application = getApplicationById(applicationId);
        application.setStatus(statusService.getStatusById(request.statusId()));
        application.setTitle(request.title().trim());
        application.setCompany(request.company().trim());
        application.setSalaryMin(request.salaryMin());
        application.setSalaryMax(request.salaryMax());
        application.setSalaryCurrency(ofNullable(request.salaryCurrency()).orElse(Currency.PHP));
        application.setEmploymentType(request.employmentType());
        application.setSetup(ofNullable(request.setup()).orElse(Setup.Onsite));
        application.setRemarks(request.remarks());
        application.setDescription(request.description());
        application.setLink(request.link().trim());
        application.setAppliedDate(request.appliedDate());

        try {
            return applicationRepository.saveAndFlush(application);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, APPLICATION_ERROR_DUPLICATE_LINK);
        }
    }

    private void checkSalaryRange(CreateApplicationRequestDto request) {
        if (request.salaryMax() != null
                && (request.salaryMin() == null || request.salaryMax().compareTo(request.salaryMin()) < 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, APPLICATION_ERROR_SALARY_RANGE);
        }
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
