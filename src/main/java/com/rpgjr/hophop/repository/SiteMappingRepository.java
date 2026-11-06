package com.rpgjr.hophop.repository;

import com.rpgjr.hophop.model.entity.SiteMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SiteMappingRepository extends JpaRepository<SiteMapping, String> {

    List<SiteMapping> findBySiteHost(String siteHost);

    List<SiteMapping> findAllByOrderBySiteHostAscIdAsc();

    boolean existsBySiteHost(String siteHost);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from SiteMapping m where m.siteHost = :host")
    void deleteBySiteHost(@Param("host") String host);

}
