package com.rpgjr.hophop.enums;

public enum MappingSourceType {
    META,     // <meta property="og:title" content="...">  -> key "og:title"
    JSON_LD   // the JobPosting data block              -> key "hiringOrganization.name"
}