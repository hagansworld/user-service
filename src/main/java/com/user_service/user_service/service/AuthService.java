package com.user_service.user_service.service;

import com.user_service.user_service.dto.*;
import com.user_service.user_service.entity.Role;
import com.user_service.user_service.entity.User;
import com.user_service.user_service.enums.UserRole;
import com.user_service.user_service.exception.*;
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

import java.security.SecureRandom;
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
     *  Registers a new user by performing the following steps:
     *   Checks if the email or username already exists
     *   Encodes the password and generates a verification code
     *   Assigns a default user role
     *   Saves the user to the database
     *   Sends an email verification code
     *
     *
     * @param requestRequestDto Contains the registration details (email, username, password, etc.)
     * @param request The HTTP request object (used to get request URI)
     * @return A ResponseDto containing registration status and user data
     * @throws EmailAlreadyExistsException if the email is already registered
     * @throws UserAlreadyExitsException if the username is already taken
     * @throws RuntimeException if any unexpected error occurs during registration
     */
    public ResponseDto registerUser(RegisterRequestDto requestRequestDto,HttpServletRequest request){
        try {
            // Check if email already exists
            if (userRepository.existsByEmail(requestRequestDto.getEmail())) {
                log.info("Attempting to register email with {}", requestRequestDto.getEmail());
                throw new EmailAlreadyExistsException("Email '" + requestRequestDto.getEmail() + " ' already exist");
            }

            // Check if username already exists
            if (userRepository.existsByUsername(requestRequestDto.getUsername())) {
                log.info("Attempting to register user with {}", requestRequestDto.getUsername());
                throw new UserAlreadyExitsException("User '" + requestRequestDto.getUsername() + " 'already exist");
            }


            // Convert registration DTO to User entity
            User user = userMapper.toUser(requestRequestDto);


            // Encode password
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // Generate and set verification code + expiry + time generated
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
            user.setCreatedAt(LocalDateTime.now());

            // Assign default USER role
            Role userRole = roleRepository.findByRole(UserRole.USER)
                    .orElseThrow(() -> {
                        log.error("Default role not found");
                        return new NotFoundException("Default role not found");
                    });
            // List all Roles that user has (Ex, ADMIN, MANAGER Etc)
            user.setRoles(Collections.singletonList(userRole));

            // Save user to the database
            User savedUser = userRepository.save(user);

            // Send verification email
            sendVerification(savedUser);

            // Build and return success response
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
            throw new RuntimeException("An error occurred during registration",e);
        }

    }


    /**
     * Logs in a user by authenticating credentials and generating a JWT token.
     *  Validates that the user exists and has verified their account</li>
     *  Authenticates email and password</li>
     *  Generates and returns a JWT token if successful</li>
     *
     *
     * @param loginRequestDto The login request containing email and password
     * @param request The HTTP request object
     * @return A response containing user data and JWT token if login is successful
     * @throws RuntimeException if login fails due to unverified account or invalid credentials
     */
    public ResponseDto loginUser (LoginRequestDto loginRequestDto, HttpServletRequest request){
       try {
           // Find user by email
           User user = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(()-> new NotFoundException("User not found with email "+ loginRequestDto.getEmail()));
           log.info("Attempting login for user: {}", user.getEmail());

           // Check if the user's email is verified
           if (!user.isEnabled()){
               throw new NotFoundException("Account not verified. Please verify your account ");
           }

           if (!passwordEncoder.matches(loginRequestDto.getPassword(),user.getPassword() )){
               log.info("Password >>>> {} ", loginRequestDto.getPassword());
               throw new InvalidCredentialsException("Incorrect password. Please try again");
           }

           // Authenticate user credentials
           authenticationManager.authenticate(
                   new UsernamePasswordAuthenticationToken(
                   loginRequestDto.getEmail(),
                           loginRequestDto.getPassword()
                   )
           );

           // Generate JWT token
           String token = jwtUtils.generateToken(user);

           // Build response DTO
           LoginResponseDto loginResponseDto = userMapper.toLoginResponseDto(user, token);

           return ApiResponse.buildResponse(
                    loginResponseDto,
                   200,
                   "Login successfully",
                   request.getRequestURI()
           );

       }
       catch (NotFoundException | InvalidCredentialsException e){
           throw  e;
       } catch (Exception e){
           log.error("An error occurred during sign in ");
           throw new RuntimeException("An error occurred during login",e);
       }


    }

    /**
     * Verifies a user's email using a verification code.
     * <ul>
     *   <li>Checks if the verification code is valid and not expired</li>
     *   <li>Enables the user account if the code is valid</li>
     *   <li>Removes the verification code from the user record</li>
     *   <li>Returns a success or failure response based on the result</li>
     * </ul>
     *
     * @param verifyEmailRequestDto Contains the verification code sent to the user's email
     * @param request The HTTP request object (used to get request URI)
     * @return A ResponseDto containing the result of the verification process
     * @throws UserNotFoundException if the code is invalid or doesn't match any user
     * @throws RuntimeException if other errors occur during verification
     */
    public ResponseDto verifyUser(VerifyEmailRequestDto verifyEmailRequestDto, HttpServletRequest request){
        try{
            // Look for a user with the provided verification code
            Optional<User>optionalUser= userRepository.findByVerificationCode(verifyEmailRequestDto.getVerificationCode());

            // If no user is found, the code is invalid or expired
            if (optionalUser.isEmpty()){
                throw  new UserNotFoundException("Invalid or expired verification code");
            }

            User user = optionalUser.get();

            // Check if the verification code has expired
            if (user.getVerificationCodeExpiresAt() == null || user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Verification code has expired");
            }


            // Mark the user's account as verified
            user.setEnabled(true);

            // Clear the verification code and expiration
            user.setVerificationCode(null);
            user.setVerificationCodeExpiresAt(null);

            // Save the updated user info
            userRepository.save(user);

            // Create a success response
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
            // For any other known errors (e.g. invalid/expired code), return a 400 response
            VerifyEmailResponseDto responseDto= userMapper.toVerifyEmailResponseDto(false, e.getMessage());

            return ApiResponse.buildResponse(
                   responseDto,
                    400,
                    e.getMessage(),
                    request.getRequestURI()
            );
        }catch (Exception e){
            // Catch any unexpected errors
            throw  new RuntimeException("An expected error during email verification");
        }

    }

    /**
     *  Resends a new email verification code to the user.
     *   Checks if the email exists and the account is not already verified
     *   Generates a new verification code and sets its expiration time
     *   Sends the code to the user's email
     *   Returns a success or failure response
     *
     *
     * @param requestDto Contains the email address to resend the verification code to
     * @param request The HTTP request object (used to retrieve the request URI)
     * @return A ResponseDto indicating whether the code was resent successfully
     * @throws UserNotFoundException if the user does not exist or is already verified
     */
    public ResponseDto ResendVerificationCode(ResendVerificationCodeRequestDto requestDto, HttpServletRequest request){
       try {
           // Check if user with given email exists
           Optional<User> optionalUser = userRepository.findByEmail(requestDto.getEmail());
           if (optionalUser.isEmpty()) {
            throw new UserNotFoundException("Email not found");
           }

           // If user is already verified, do not resend the code
           User user = optionalUser.get();
           if (user.isEnabled()) {
               throw new UserNotFoundException("User already verified");
           }

           // Generate a new verification code and set its expiration
           user.setVerificationCode(generateVerificationCode());
           user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(2));

           // Send the verification code via email
           sendVerification(user);

           // Save the updated user record
           userRepository.save(user);

           // Create a success response DTO
           ResendVerificationCodeResponseDto responseDto = userMapper.toResendVerificationCodeResponseDto(true, "verification code sent");

           return ApiResponse.buildResponse(
                   responseDto,
                   200,
                   "verification sent successfully",
                   request.getRequestURI()

           );


       }catch (Exception e){
           // Return a failure response if something goes wrong
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
     * Sends a styled HTML email to the user containing their account verification code.
     *   Builds a mobile-friendly HTML email with the code
     *   Includes basic branding and messaging
     *   Uses the current year dynamically
     *   Delegates actual email sending to the email service
     *
     * @param user The user object containing the email address and verification code
     */
    public void sendVerification(User user) {

        if (user == null || user.getEmail() == null || user.getVerificationCode() == null) {
            throw new IllegalArgumentException("User or verification details are missing.");
        }

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
                    <h2 style="color: #333333;"> Welcome to Our App!</h2>
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
                        &copy; %d hagansworld. All rights reserved.
                    </p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(verificationCode, LocalDateTime.now().getYear());
        emailService.sendVerificationEmail(user.getEmail(), subject , htmlMessage);
    }

    /**
     * Generates a random 6-digit verification code.
     * Ensures the code is always between 100000 and 999999 (inclusive),
     * making it suitable for email or SMS verification.
     *
     * @return A 6-digit verification code as a string
     */
    private String generateVerificationCode() {
        SecureRandom  secureRandom = new SecureRandom();
        // to generate 6-digit number
        int code = secureRandom.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
