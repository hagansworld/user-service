package com.user_service.user_service.response;

import com.user_service.user_service.dto.ResponseDto;

import java.time.LocalDateTime;

public class ApiResponse {
    public static ResponseDto buildResponse(Object data, int statusCode, String message, String url){
        return ResponseDto.builder()
                .data(data)
                .statusCode(statusCode)
                .message(message)
                .timeRequested(LocalDateTime.now())
                .url(url)
                .build();
    }
}
