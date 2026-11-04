package com.rpgjr.hophop.application;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

	List<Application> findByAppliedDateBetweenOrderByAppliedDateAscIdAsc(LocalDate from, LocalDate to);
}
