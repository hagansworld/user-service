package com.user_service.user_service.service;

import com.user_service.user_service.dto.RegisterRequestDto;
import com.user_service.user_service.dto.RegisterResponseDto;



public interface AuthInterface {
    RegisterResponseDto register(RegisterRequestDto request);
}
