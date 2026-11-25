package com.rpgjr.hophop.repository;

import com.rpgjr.hophop.model.entity.MappingGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MappingGroupRepository extends JpaRepository<MappingGroup, Long> {

    List<MappingGroup> findAllByOrderByNameAsc();

    List<MappingGroup> findBySiteHostOrderByNameAsc(String siteHost);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

}
