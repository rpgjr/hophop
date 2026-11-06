package com.rpgjr.hophop.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    public static String APPLICATION = "[APPLICATION] => ";

    public static String STATUS = "[STATUS] => ";

    public static String STATUS_ERROR_UNKNOWN = STATUS + "Unknown Status with Status ID: ";

    public static String APPLICATION_ERROR_UNKNOWN = APPLICATION + "Unknown Application with Application ID: ";

    public static String APPLICATION_ERROR_DELETE = APPLICATION + "Error deleting Application with Application ID: {} Error: {}";

    public static String INTERNAL_SERVER_ERROR = "Something went wrong. Please try again later.";

    public static String MISSING_REQUEST_DATA = "Missing Request Data: ";

    public static String APPLICATION_ERROR_INTERNAL_SERVER_ERROR = "[APPLICATION] => " + INTERNAL_SERVER_ERROR;

    public static String DELETE_SUCCESS = "Deleted Successfully";

    public static String DELETE_FAILURE = "Delete Failure";

    public static String SCRAPER_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 Chrome/124.0 Safari/537.36";

    public static String SCRAPER_DOCUMENT_ERROR = "Could not open the job page";

    public static String SCRAPER_URL_NOT_VALID = "Enter a public http(s) job link";

    public static String HTTPS_NAME = "https";

    public static String HTTP_NAME = "http";

    public static String WWW_DOT = "www.";

    public static String SCRAPER_CSS_QUERY = "script[type=application/ld+json]";

    public static String SCRAPER_INVALID_SITE_URL = "Enter a valid job link";
}
