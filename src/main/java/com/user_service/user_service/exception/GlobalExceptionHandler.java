package com.user_service.user_service.exception;

import com.user_service.user_service.dto.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto> handleAllExceptions(Exception ex, WebRequest request){
        ResponseDto errorResponse = ResponseDto.builder()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(ex.getMessage())
                .timeRequested(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ResponseDto>handleNotFoundException(NotFoundException ex, WebRequest request){
        ResponseDto errorResponse = ResponseDto.builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .timeRequested(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ResponseDto>handleEmailAlreadyExistsException(EmailAlreadyExistsException ex, WebRequest request){
        ResponseDto errorResponse = ResponseDto.builder()
                .statusCode(HttpStatus.CONFLICT.value())
                .message(ex.getMessage())
                .timeRequested(LocalDateTime.now())
                .build();

        return  new ResponseEntity<>(errorResponse,HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserAlreadyExitsException.class)
    public ResponseEntity<ResponseDto>handleUserAlreadyExistsException(UserAlreadyExitsException ex, WebRequest request){
        ResponseDto errorResponse = ResponseDto.builder()
                .statusCode(HttpStatus.NO_CONTENT.value())
                .message(ex.getMessage())
                .message(ex.getMessage())
                .timeRequested(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorResponse,HttpStatus.CONFLICT);
    }


    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ResponseDto>handleUserNotFoundException(UserNotFoundException ex, WebRequest request){
        ResponseDto errorResponse = ResponseDto.builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .timeRequested(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorResponse,HttpStatus.NOT_FOUND);
    }

@ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ResponseDto>handleInvalidCredentialsException(InvalidCredentialsException ex, WebRequest request){
        ResponseDto errorResponse = ResponseDto.builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .timeRequested(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);

    }







}
