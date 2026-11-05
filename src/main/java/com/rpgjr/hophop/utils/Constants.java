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

}
