package com.user_service.user_service.controller;


import com.user_service.user_service.dto.*;
import com.user_service.user_service.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth/v1")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ResponseDto>registerUser(@RequestBody @Valid RegisterRequestDto registerRequestDto,
                                                   HttpServletRequest request
                                                   ){
        ResponseDto responseDto = authService.registerUser(registerRequestDto,request);

        return new ResponseEntity<>(responseDto, HttpStatus.valueOf(responseDto.getStatusCode()));
    }



    @PostMapping("/login")
    public ResponseEntity<ResponseDto>loginUser(@RequestBody @Valid LoginRequestDto loginRequestDto,
                                                HttpServletRequest request){
        ResponseDto responseDto = authService.loginUser(loginRequestDto,request);

        return new ResponseEntity<>(responseDto,HttpStatus.valueOf(responseDto.getStatusCode()));
    }


    @PostMapping("/verify")
    public ResponseEntity<ResponseDto>verifyUser(@RequestBody @Valid VerifyEmailRequestDto verifyEmailRequestDto,
                                                 HttpServletRequest request){
        ResponseDto responseDto = authService.verifyUser(verifyEmailRequestDto, request);

        return  new ResponseEntity<>(responseDto, HttpStatus.valueOf(responseDto.getStatusCode()));

    }

    @PostMapping("/resend")
    public ResponseEntity<ResponseDto>resendVerificationCode(@RequestBody @Valid ResendVerificationCodeRequestDto resendVerificationCodeRequestDto,
                                                             HttpServletRequest request){
        ResponseDto responseDto = authService.ResendVerificationCode(resendVerificationCodeRequestDto , request);

        return new ResponseEntity<>(responseDto, HttpStatus.valueOf(responseDto.getStatusCode()));
    }


}



