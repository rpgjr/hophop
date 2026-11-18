package com.rpgjr.hophop.service;

import com.rpgjr.hophop.model.dto.request.StatusRequestDto;
import com.rpgjr.hophop.model.entity.Status;
import com.rpgjr.hophop.repository.ApplicationRepository;
import com.rpgjr.hophop.repository.StatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.rpgjr.hophop.utils.Constants.STATUS_ERROR_DUPLICATE;
import static com.rpgjr.hophop.utils.Constants.STATUS_ERROR_IN_USE;
import static com.rpgjr.hophop.utils.Constants.STATUS_ERROR_LAST;
import static com.rpgjr.hophop.utils.Constants.STATUS_ERROR_UNKNOWN;

@Service
@RequiredArgsConstructor
public class StatusService {

    private final StatusRepository statusRepository;

    private final ApplicationRepository applicationRepository;

    public List<Status> getAllStatuses() {
        return statusRepository.findAllByOrderByIdAsc();
    }

    public Status getStatusById(Long statusId) {
        return statusRepository.findById(statusId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, STATUS_ERROR_UNKNOWN + statusId));
    }

    public Status createStatus(StatusRequestDto request) {
        return save(new Status(), request);
    }

    public Status updateStatus(Long statusId, StatusRequestDto request) {
        return save(getStatusById(statusId), request);
    }

    public void deleteStatus(Long statusId) {
        Status status = getStatusById(statusId);
        if (applicationRepository.countByStatusId(statusId) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, STATUS_ERROR_IN_USE);
        }
        if (statusRepository.count() <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, STATUS_ERROR_LAST);
        }
        statusRepository.delete(status);
    }

    private Status save(Status status, StatusRequestDto request) {
        status.setName(request.name().trim());
        status.setColor(request.color().toLowerCase());
        try {
            return statusRepository.saveAndFlush(status);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, STATUS_ERROR_DUPLICATE);
        }
    }

}
