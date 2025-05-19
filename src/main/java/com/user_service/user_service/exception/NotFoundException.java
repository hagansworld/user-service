package com.user_service.user_service.exception;


public class NotFoundException extends RuntimeException {
    public NotFoundException(String message){
        super(message);
    }
}
