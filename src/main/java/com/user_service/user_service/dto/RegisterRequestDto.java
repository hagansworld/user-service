package com.user_service.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequestDto {

    @NotBlank(message = "Username is required")
    private String username;

    @Email(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
