package com.user_service.user_service.service;

import com.user_service.user_service.dto.*;
import com.user_service.user_service.entity.Role;
import com.user_service.user_service.entity.User;
import com.user_service.user_service.enums.UserRole;
import com.user_service.user_service.exception.EmailAlreadyExistsException;
import com.user_service.user_service.exception.NotFoundException;
import com.user_service.user_service.exception.UserAlreadyExitsException;
import com.user_service.user_service.exception.UserNotFoundException;
import com.user_service.user_service.mapper.UserMapper;
import com.user_service.user_service.repository.RoleRepository;
import com.user_service.user_service.repository.UserRepository;
import com.user_service.user_service.response.ApiResponse;
import com.user_service.user_service.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;

//comment
    /**
     *
     * @param requestRequestDto
     * @param request
     * @return
     */
    public ResponseDto registerUser(RegisterRequestDto requestRequestDto,HttpServletRequest request){
        try {
            if (userRepository.existsByEmail(requestRequestDto.getEmail())) {
                log.info("Attempting to register email with {}", requestRequestDto.getEmail());
                throw new EmailAlreadyExistsException("Email '" + requestRequestDto.getEmail() + " ' already exist");
            }

            if (userRepository.existsByUsername(requestRequestDto.getUsername())) {
                log.info("Attempting to register user with {}", requestRequestDto.getUsername());
                throw new UserAlreadyExitsException("User '" + requestRequestDto.getUsername() + " 'already exist");
            }

            User user = userMapper.toUser(requestRequestDto);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
            user.setCreatedAt(LocalDateTime.now());

            Role userRole = roleRepository.findByRole(UserRole.USER)
                    .orElseThrow(() -> {
                        log.error("Default user not found");
                        return new NotFoundException("Default user not found");
                    });

            user.setRoles(Collections.singletonList(userRole));

            User savedUser = userRepository.save(user);

             sendVerification(savedUser);

             return ApiResponse.buildResponse(
                     userMapper.toRegisterResponseDto(savedUser,true),
                     201,
                     "User registered successfully",
//                     ServletUriComponentsBuilder.fromCurrentContextPath().toUriString()
                     request.getRequestURI()
             );
        } catch (EmailAlreadyExistsException | NotFoundException e){
            throw  e;
        } catch (Exception e){
            log.error("An error occurred during registration");
            throw new RuntimeException("An error occurred during registration");
        }

    }


    /**
     *
     * @param loginRequestDto
     * @param request
     * @return
     */
    public ResponseDto loginUser (LoginRequestDto loginRequestDto, HttpServletRequest request){
       try {
           if (userRepository.existsByEmail(loginRequestDto.getEmail())){
               log.info("Attempting to login email with : {}", loginRequestDto.getEmail());
               throw new EmailAlreadyExistsException("Email '" + loginRequestDto.getEmail() + "' already exist");

           }

           User user = new User();
           if (!user.isEnabled()){
               throw new RuntimeException("Account not verified. Please verify your account ");
           }

           authenticationManager.authenticate(
                   new UsernamePasswordAuthenticationToken(
                   loginRequestDto.getEmail(),
                           loginRequestDto.getPassword()
                   )
           );

           String token = jwtUtils.generateToken(user);

           LoginResponseDto loginResponseDto = userMapper.toLoginResponseDto(user, token);

           return ApiResponse.buildResponse(
                    loginResponseDto,
                   200,
                   "Login successfully",
                   request.getRequestURI()
           );

       }catch (NotFoundException e){
           throw  e;
       } catch (Exception e){
           log.error("An error occurred during sign in ");
           throw new RuntimeException("An error occurred during login");
       }


    }

    /**
     *
     * @param verifyEmailRequestDto
     * @param request
     * @return
     */
    public ResponseDto verifyUser(VerifyEmailRequestDto verifyEmailRequestDto, HttpServletRequest request){
        try{
            Optional<User>optionalUser= userRepository.findByVerificationCode(verifyEmailRequestDto.getVerificationCode());

            if (optionalUser.isEmpty()){
                throw  new UserNotFoundException("Invalid or expired verification code");
            }

            User user = optionalUser.get();

            if (user.getVerificationCodeExpiresAt() == null || user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())){
                throw new RuntimeException("Verification code has expired");
            }
            user.setEnabled(true);
            user.setVerificationCode(null);
            user.setVerificationCodeExpiresAt(null);
            userRepository.save(user);

            VerifyEmailResponseDto responseDto = userMapper.toVerifyEmailResponseDto(true, "Email verified successfully");

            return ApiResponse.buildResponse(
                    responseDto,
                    200,
                    "verification successful",
                    request.getRequestURI()
            );

        }catch (UserNotFoundException e){
            throw  e;
        }catch (RuntimeException e){
            /**
             *  for expired or invalid code
             */
            VerifyEmailResponseDto responseDto= userMapper.toVerifyEmailResponseDto(false, e.getMessage());

            return ApiResponse.buildResponse(
                   responseDto,
                    400,
                    e.getMessage(),
                    request.getRequestURI()
            );
        }catch (Exception e){
            throw  new RuntimeException("An expected error during email verification");
        }

    }

    /**
     *
     * @param requestDto
     * @param request
     * @return
     */
    public ResponseDto ResendVerificationCode(ResendVerificationCodeRequestDto requestDto, HttpServletRequest request){
       try {
           Optional<User> optionalUser = userRepository.findByEmail(requestDto.getEmail());
           if (optionalUser.isEmpty()) {
            throw new UserNotFoundException("Email not found");
//               return userMapper.toResendVerificationCodeResponseDto(false, "Email not found");
           }

           User user = optionalUser.get();
           if (user.isEnabled()) {
//               return userMapper.toResendVerificationCodeResponseDto(false, "Account already verified");
               throw new UserNotFoundException("User not found");
           }

           user.setVerificationCode(generateVerificationCode());
           user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(2));

           if (user.getVerificationCodeExpiresAt() == null || user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
               throw new RuntimeException("Verification code has expired");
           }
           sendVerification(user);
           userRepository.save(user);

           ResendVerificationCodeResponseDto responseDto = userMapper.toResendVerificationCodeResponseDto(true, "verification code sent");

           return ApiResponse.buildResponse(
                   responseDto,
                   200,
                   "verification sent successfully",
                   request.getRequestURI()

           );


       }catch (Exception e){
           ResendVerificationCodeResponseDto responseDto = userMapper.toResendVerificationCodeResponseDto(false, "Failed to resend verification code: " + e.getMessage());
          return ApiResponse.buildResponse(
                   responseDto,
                   400,
                   "Failed to send code",
                   request.getRequestURI()
           );

       }
    }

    /**
     *
     * @param user
     */
    public void sendVerification(User user) {
        String subject = "Account verification";
        String verificationCode = user.getVerificationCode();

        String htmlMessage = """
        <html>
        <head>
            <style>
                @media only screen and (max-width: 600px) {
                    .container {
                        padding: 15px !important;
                    }
                }
            </style>
        </head>
        <body style="margin: 0; padding: 0; font-family: 'Segoe UI', sans-serif; background-color: #f0f2f5;">
            <div class="container" style="max-width: 600px; margin: 30px auto; background-color: #ffffff; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); padding: 30px;">
                <div style="text-align: center;">
                    <h2 style="color: #333333;">👋 Welcome to Our App!</h2>
                    <p style="font-size: 16px; color: #666666;">
                        We're excited to have you on board. To complete your registration, please use the verification code below:
                    </p>

                    <div style="margin: 20px 0; background-color: #f1f1f1; padding: 20px; border-radius: 8px;">
                        <h3 style="margin: 0; color: #333333;">Your Verification Code:</h3>
                        <p style="font-size: 24px; font-weight: bold; color: #007bff; margin: 10px 0;">
                            %s
                        </p>
                    </div>

                    <p style="font-size: 14px; color: #999999;">
                        If you did not sign up for this account, please ignore this email.
                    </p>

                    <hr style="margin: 30px 0; border: none; border-top: 1px solid #eeeeee;" />

                    <p style="font-size: 13px; color: #aaaaaa;">
                        This is an automated message, please do not reply. <br>
                        &copy; %d Your App Name. All rights reserved.
                    </p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(verificationCode, LocalDateTime.now().getYear());
        emailService.sendVerificationEmail(user.getEmail(), subject , htmlMessage);

    }

    /**
     *
     * @return
     */
    private String generateVerificationCode() {
        Random random = new Random();
        // to generate 6-digit number
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
