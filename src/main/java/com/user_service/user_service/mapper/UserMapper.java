package com.user_service.user_service.mapper;

import com.user_service.user_service.dto.*;
import com.user_service.user_service.entity.Role;
import com.user_service.user_service.entity.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    /**
     * User to RegisterResponseDto
     */

    public RegisterResponseDto toRegisterResponseDto(User user, boolean emailSent){
        return RegisterResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .emailSent(emailSent)
                .message(emailSent ? "Verification email sent" : "failed to send email")
                .build();
    }

    /**
     * User to Login ResponseDto
     */

    public LoginResponseDto toLoginResponseDto(User user ,String token){
        return LoginResponseDto.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles()
                        .stream()
                        .map(role -> role.getRole().name())
                        .collect(Collectors.toList())
                )
                .build();

    }

    /**
     * User to Dto {For admins roles and profiles}
     */
    public UserDto toUserDto(User user){
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles()
                        .stream()
                        .map(Role::getAuthority)
                        .collect(Collectors.toList())
                )
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * MapVerify EmailResponse
     */
    public VerifyEmailResponseDto toVerifyEmailResponseDto(boolean success, String message){
        return VerifyEmailResponseDto.builder()
                .verified(success)
                .message(message)
                .build();
    }

    /**
     * Map registerDto to User Entity for creating new users
     */

    public User toUser(RegisterRequestDto dto){
        return User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(dto.getPassword()) // hash before saving later
                .enabled(false)
                .build();
    }
}
