package com.user_service.user_service.service.Impl;

import com.user_service.user_service.dto.RegisterRequestDto;
import com.user_service.user_service.dto.RegisterResponseDto;
import com.user_service.user_service.entity.User;
import com.user_service.user_service.exception.EmailAlreadyExistsException;
import com.user_service.user_service.exception.NotFoundException;
import com.user_service.user_service.mapper.UserMapper;
import com.user_service.user_service.repository.UserRepository;
import com.user_service.user_service.service.AuthInterface;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthInterface {
    private final UserRepository userRepository;

    @Override
    public RegisterResponseDto register(RegisterRequestDto request) {
        try {
            if (userRepository.existsByEmail(request.getEmail())){
                throw  new EmailAlreadyExistsException("Email '" + request.getEmail() + "' already exist");
            }


        }catch (EmailAlreadyExistsException | NotFoundException e){
            throw e;
        }catch (Exception e){
            log.error("");
            throw  new RuntimeException("An error occurred during registration")
        }


    }
}
