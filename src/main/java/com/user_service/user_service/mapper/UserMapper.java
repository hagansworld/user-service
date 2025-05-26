package com.user_service.user_service.mapper;

import com.user_service.user_service.dto.*;
import com.user_service.user_service.entity.Role;
import com.user_service.user_service.entity.User;
import com.user_service.user_service.enums.UserRole;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
                .id(user.getId())
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
//    public UserDto toUserDto(User user){
//        return UserDto.builder()
//                .id(user.getId())
//                .username(user.getUsername())
//                .email(user.getEmail())
//                .roles(user.getRoles()
//                        .stream()
//                        .map(Role::getAuthority)
//                        .collect(Collectors.toList())
//                )
//                .enabled(user.isEnabled())
//                .createdAt(user.getCreatedAt())
//                .updatedAt(user.getUpdatedAt())
//                .build();
//    }

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


    /**
     * MapVerificationCodeResponseDto
     */
    public ResendVerificationCodeResponseDto toResendVerificationCodeResponseDto(boolean sent, String message){
        return ResendVerificationCodeResponseDto.builder()
                .sent(sent)
                .message(message)
                .build();
    }

    /**
     *
     * Map User to UserResponse
     */
    public UserResponseDto touserResponseDto(User user){
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(
                        user.getRoles()
                                .stream()
                                .map(Role::getAuthority)
                                .collect(Collectors.toList())
                )
                .enabled(user.isEnabled())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

    }

    /**
     * Map UserRequest toUser ( for creating and saving users to the database)
     */

    public User toUser(UserRequestDto dto){
       return User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .enabled(false)
                .build();
    }

    /**
     * The RoleMapper class helps convert a list of role names
     * (in the form of strings like "admin", "user", etc.)
     * into a list of UserRole enum values
     * (which are values defined in your UserRole enum, such as ADMIN, USER, etc.)
     *
     * @param roles - this holes list of roles in string
     * @return returns userRoles in enum
     */

    public static List<UserRole> mapRoles(List<String>roles){
        List<UserRole> userRoles = new ArrayList<>();

        for(String role : roles){
            try {
                userRoles.add(UserRole.valueOf(role.toUpperCase()));
            }catch (IllegalArgumentException e){
                throw  new IllegalArgumentException("Invalid role: " + role);
            }
        }

        return userRoles;
    }





}
