package com.user_service.user_service.controller;


import com.user_service.user_service.dto.*;
import com.user_service.user_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/v1/users/authenticate")
@RequiredArgsConstructor
@Tag(name = "Authentication",
        description = "Endpoints for user registration, login and email verification")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user",
            description = "Creates a new user account and sends a verification email.")
    public ResponseEntity<ResponseDto>registerUser(@RequestBody @Valid RegisterRequestDto registerRequestDto,
                                                   HttpServletRequest request
                                                   ){
        ResponseDto responseDto = authService.registerUser(registerRequestDto,request);

        return ResponseEntity.status(responseDto.getStatusCode()).body(responseDto);
    }



    @PostMapping("/login")
    @Operation(summary = "Login user",
            description = "Authenticates a user and returns a JWT token.")
    public ResponseEntity<ResponseDto>loginUser(@RequestBody @Valid LoginRequestDto loginRequestDto,
                                                HttpServletRequest request){
        ResponseDto responseDto = authService.loginUser(loginRequestDto,request);

        return ResponseEntity.status(responseDto.getStatusCode()).body(responseDto);
    }


    @PostMapping("/verify")
    @Operation(summary = "Verify email",
            description = "Verifies the user's email using the OTP code sent.")
    public ResponseEntity<ResponseDto>verifyUser(@RequestBody @Valid VerifyEmailRequestDto verifyEmailRequestDto,
                                                 HttpServletRequest request){
        ResponseDto responseDto = authService.verifyUser(verifyEmailRequestDto, request);

        return ResponseEntity.status(responseDto.getStatusCode()).body(responseDto);

    }

    @PostMapping("/resend")
    @Operation(summary = "Resend verification code",
            description = "Resends a new OTP to the user's email address.")

    public ResponseEntity<ResponseDto>resendVerificationCode(@RequestBody @Valid ResendVerificationCodeRequestDto resendVerificationCodeRequestDto,
                                                             HttpServletRequest request){
        ResponseDto responseDto = authService.ResendVerificationCode(resendVerificationCodeRequestDto , request);

        return ResponseEntity.status(responseDto.getStatusCode()).body(responseDto);
    }


}



