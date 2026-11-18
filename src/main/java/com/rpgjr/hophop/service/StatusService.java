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

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.rpgjr.hophop.utils.Constants.STATUS_ERROR_DUPLICATE;
import static com.rpgjr.hophop.utils.Constants.STATUS_ERROR_IN_USE;
import static com.rpgjr.hophop.utils.Constants.STATUS_ERROR_LAST;
import static com.rpgjr.hophop.utils.Constants.STATUS_ERROR_ORDER;
import static com.rpgjr.hophop.utils.Constants.STATUS_ERROR_UNKNOWN;

@Service
@RequiredArgsConstructor
public class StatusService {

    private final StatusRepository statusRepository;

    private final ApplicationRepository applicationRepository;

    public List<Status> getAllStatuses() {
        return statusRepository.findAllByOrderBySortOrderAscIdAsc();
    }

    public Status getStatusById(Long statusId) {
        return statusRepository.findById(statusId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, STATUS_ERROR_UNKNOWN + statusId));
    }

    public Status createStatus(StatusRequestDto request) {
        Status status = new Status();
        status.setSortOrder(statusRepository.findFirstByOrderBySortOrderDesc().map(last -> last.getSortOrder() + 1).orElse(0));
        return save(status, request);
    }

    /**
     * Puts the statuses in the given order. The list must name every status once.
     */
    public List<Status> reorder(List<Long> ids) {
        List<Status> all = statusRepository.findAll();
        if (ids.size() != all.size() || !new HashSet<>(ids).equals(all.stream().map(Status::getId).collect(Collectors.toSet()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, STATUS_ERROR_ORDER);
        }
        for (Status status : all) {
            status.setSortOrder(ids.indexOf(status.getId()));
        }
        statusRepository.saveAll(all);
        return getAllStatuses();
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
