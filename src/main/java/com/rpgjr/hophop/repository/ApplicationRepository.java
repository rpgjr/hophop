package com.rpgjr.hophop.repository;

import com.rpgjr.hophop.model.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByAppliedDateBetweenOrderByAppliedDateAscIdAsc(LocalDate from, LocalDate to);

}
