package com.digiview.lms.model;

public class ApiResponse<T> {
    public boolean success;
    public String message;
    public T data;
}
