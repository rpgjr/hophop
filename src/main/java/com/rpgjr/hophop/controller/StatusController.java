package com.rpgjr.hophop.controller;

import com.rpgjr.hophop.model.dto.request.StatusRequestDto;
import com.rpgjr.hophop.model.entity.Status;
import com.rpgjr.hophop.service.StatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/statuses")
@RequiredArgsConstructor
public class StatusController {

    private final StatusService statusService;

    @GetMapping
    public List<Status> getAllStatuses() {
        return statusService.getAllStatuses();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Status createStatus(@Valid @RequestBody StatusRequestDto request) {
        return statusService.createStatus(request);
    }

    @PutMapping("/{statusId}")
    public Status updateStatus(@PathVariable Long statusId, @Valid @RequestBody StatusRequestDto request) {
        return statusService.updateStatus(statusId, request);
    }

    @DeleteMapping("/{statusId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStatus(@PathVariable Long statusId) {
        statusService.deleteStatus(statusId);
    }

}
