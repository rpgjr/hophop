package com.rpgjr.hophop.repository;

import com.rpgjr.hophop.model.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    long countByStatusId(Long statusId);

    // a renamed mapping renames the website on the applications made with it
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Application a set a.website = :to where a.website = :from")
    void renameWebsite(@Param("from") String from, @Param("to") String to);

    List<Application> findByAppliedDateBetweenOrderByAppliedDateAscIdAsc(LocalDate from, LocalDate to);

}
