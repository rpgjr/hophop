package com.rpgjr.hophop.service;

import com.rpgjr.hophop.model.entity.Status;
import com.rpgjr.hophop.repository.StatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.rpgjr.hophop.utils.Constants.STATUS_ERROR_UNKNOWN;

@Service
@RequiredArgsConstructor
public class StatusService {

    private final StatusRepository statusRepository;

    public List<Status> getAllStatuses() {
        return statusRepository.findAll();
    }

    public Status getStatusById(Long statusId) {
        return statusRepository.findById(statusId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, STATUS_ERROR_UNKNOWN + statusId));
    }

}
