package com.user_service.user_service.config;

import com.user_service.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


/**
 * Configuration class for setting up Spring Security beans.
 * This class is responsible for setting up the user authentication and password encoding configuration.
 * It provides beans for:
 * - UserDetailsService: To load user details from the database based on email.
 * - PasswordEncoder: To securely hash and check passwords (using BCrypt).
 * - AuthenticationManager: To authenticate users based on credentials.
 * - AuthenticationProvider: To validate user credentials with UserDetailsService and PasswordEncoder.
 */


@Configuration
@RequiredArgsConstructor
public class ApplicationConfiguration {

    private final UserRepository userRepository;

    /**
     * Provides a custom implementation of the UserDetailsService interface.
     * It retrieves user details (username, password, etc.) based on the email address.
     * If the user is not found, a UsernameNotFoundException is thrown.
     * @return a UserDetailsService implementation to load user details by email.
     */
    @Bean
    UserDetailsService userDetailsService() {
        // Return a UserDetailsService that fetches user data from the repository by email
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Provides a BCryptPasswordEncoder bean that is used to securely encode passwords.
     * BCrypt is a strong hash function that provides an extra layer of security when handling passwords.
     * @return a BCryptPasswordEncoder instance for encoding and matching passwords.
     */
    @Bean
    BCryptPasswordEncoder bCryptPasswordEncoder() {
        // Create and return a new BCryptPasswordEncoder instance for password encryption
        return new BCryptPasswordEncoder();
    }

    /**
     * Provides an AuthenticationManager bean, which is responsible for managing user authentication.
     * It uses the AuthenticationConfiguration to retrieve the default authentication manager.
     * @param config AuthenticationConfiguration instance containing configuration for the authentication manager.
     * @return the AuthenticationManager instance.
     * @throws Exception if there is an issue configuring the authentication manager.
     */
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        // Return the default AuthenticationManager from the AuthenticationConfiguration
        return config.getAuthenticationManager();
    }

    /**
     * Provides an AuthenticationProvider bean to authenticate the user.
     * The DaoAuthenticationProvider validates user credentials (username/password) by calling the custom
     * UserDetailsService and using the BCryptPasswordEncoder for password matching.
     * @return the configured AuthenticationProvider that uses DaoAuthenticationProvider.
     */
    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // Set the custom UserDetailsService to load user details based on email
        authProvider.setUserDetailsService(userDetailsService());
        // Set the password encoder to BCryptPasswordEncoder to securely match passwords
        authProvider.setPasswordEncoder(bCryptPasswordEncoder());
        // Return the fully configured AuthenticationProvider instance
        return authProvider;
    }
}
