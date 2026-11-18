package com.rpgjr.hophop.repository;

import com.rpgjr.hophop.model.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StatusRepository extends JpaRepository<Status, Long> {

    List<Status> findAllByOrderBySortOrderAscIdAsc();

    Optional<Status> findFirstByOrderBySortOrderDesc();

}
