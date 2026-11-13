package com.rpgjr.hophop.enums;

public enum MappingSourceType {
    META,     // <meta property="og:title" content="...">  -> key "og:title"
    JSON_LD,  // the JobPosting data block              -> key "hiringOrganization.name"
    CSS,      // text of a page element                 -> key ".top-card-layout__title"
    PATTERN   // a visible line recognised by its look  -> key "salary", "setup" or "employment_type"
}