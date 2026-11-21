package com.rpgjr.hophop.repository;

import com.rpgjr.hophop.model.entity.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {

    List<StatusHistory> findByApplicationIdOrderByChangedAtAscIdAsc(Long applicationId);

}
