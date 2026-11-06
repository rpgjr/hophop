package com.rpgjr.hophop.model.entity;

import com.rpgjr.hophop.enums.MappingFormField;
import com.rpgjr.hophop.enums.MappingSourceType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "site_mappings")
public class SiteMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String siteHost;

    @Enumerated(EnumType.STRING)
    private MappingFormField fieldName;

    @Enumerated(EnumType.STRING)
    private MappingSourceType sourceType;

    private String sourceKey;
}