package com.user_service.user_service.exception;


public class UserAlreadyExitsException extends  RuntimeException {
    public UserAlreadyExitsException(String message){
        super(message);
    }
}
