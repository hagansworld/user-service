package com.user_service.user_service.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Component
/**
 * This method is used for generating and validating tokens
 */
@RequiredArgsConstructor
public class JwtUtils {
    private final UserDetailsService userDetailsService;

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Getter
    @Value("${security.jwt.expiration-time}")
    private long expirationTime;

    @Getter
    @Value("${security.jwt.refresh-token}")
    private long refreshTokenExpirationTime;

    /**
     * Extracts the username (subject) from the JWT token.
     * @param token the JWT token
     * @return the username (subject) extracted from the token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject); // Extract the subject (username) from the token
    }

    /**
     * Extracts a specific claim from the JWT token.
     * @param token the JWT token
     * @param claimsResolver a function that extracts a claim (e.g., expiration, subject, etc.)
     * @param <T> the type of the claim
     * @return the value of the claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token); // Extract all claims from the token
        return claimsResolver.apply(claims); // Apply the claims resolver function to extract the desired claim
    }

    /**
     * Generates a JWT token for the given user.
     * @param userDetails the details of the user
     * @return the generated JWT token
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails); // Generate a token without additional claims
    }

    /**
     * Generates a JWT token with additional claims for the given user.
     * @param extraClaims additional claims to include in the token
     * @param userDetails the details of the user
     * @return the generated JWT token
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, expirationTime); // Build and return the JWT token with additional claims
    }

    /**
     * Builds the JWT token using the provided claims, user details, and expiration time.
     * @param extraClaims additional claims to include in the token
     * @param userDetails the details of the user
     * @param expiration the expiration time of the token
     * @return the JWT token
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
//                .claims(extraClaims) // Set additional claims
//                .claim("roles", userDetails.getAuthorities())
                .claims(extraClaims)
                .subject(userDetails.getUsername()) // Set the subject as the username
                .issuedAt(new Date(System.currentTimeMillis())) // Set the issued time to the current time
                .expiration(new Date(System.currentTimeMillis() + expiration)) // Set the expiration time
                .signWith(getSignInKey()) // Sign the token using the secret key (algorithm is inferred)
                .compact(); // Return the compact (encoded) token
    }

    /**
     * Validates the JWT token by checking if the username in the token matches the userDetails
     * and whether the token is expired.
     * @param token the JWT token
     * @param userDetails the user details to validate the token against
     * @return true if the token is valid, false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token); // Extract the username from the token
        return (username.equals(userDetails.getUsername())) && isTokenExpired(token); // Check if the token is valid
    }

    /**
     * Checks whether the token has expired.
     * @param token the JWT token
     * @return true if the token has expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        return !extractExpiration(token).before(new Date()); // Compare the expiration date with the current date
    }

    /**
     * Extracts the expiration date from the JWT token.
     * @param token the JWT token
     * @return the expiration date of the token
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration); // Extract the expiration date claim from the token
    }

    /**
     * Extracts all claims from the JWT token.
     * @param token the JWT token
     * @return the claims extracted from the token
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey()) // Set the signing key to validate the token
                .build()
                .parseSignedClaims(token) // Parse the token and extract the claims
                .getPayload(); // Return the payload (claims) of the token
    }



    /**
     * Retrieves the signing key used for signing and validating the JWT token.
     * @return the signing key
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey); // Decode the secret key from Base64 encoding
        return Keys.hmacShaKeyFor(keyBytes); // Return the HMAC signing key
    }


    /**
     * *******************************************
     * Generate refresh token starts from here
     * ********************************************
     */


    /**
     * Generates a refresh JWT token for the given user.
     * @param userDetails the details of the user
     * @return the generated JWT token
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return generateRefreshToken(new HashMap<>(), userDetails); // Generate a refresh token without additional claims
    }

    /**
     * Generates a refresh JWT token with additional claims for the given user.
     * @param extraClaims additional claims to include in the token
     * @param userDetails the details of the user
     * @return the generated JWT token
     */
    public String generateRefreshToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return generateRefreshToken(extraClaims, userDetails, refreshTokenExpirationTime); // Build and return the JWT refresh token with additional claims
    }


    /**
     * Generates a refresh token for the given user with additional claims and expiration time.
     * @param extraClaims additional claims to include in the token
     * @param userDetails the details of the user
     * @param refreshTokenExpirationTime the expiration time for the refresh token
     * @return the generated refresh token
     */

    public String generateRefreshToken(Map<String, Object> extraClaims,
                                  UserDetails userDetails,
                                  long  refreshTokenExpirationTime){
        return  Jwts
                .builder()
//                .claim("roles", userDetails.getAuthorities())
                .claims(extraClaims)
                .subject(userDetails.getUsername()) // Set the subject as the username
                .issuedAt(new Date(System.currentTimeMillis())) // Set the issued time to the current time
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpirationTime)) // Set the expiration time
                .signWith(getSignInKey()) // Sign the token using the secret key (algorithm is inferred)
                .compact(); // Return the compact (encoded) token

    }


    /**
     * Extracts the username from the given token.
     * @param token the JWT token
     * @return the username (subject) extracted from the token
     */
    public String  getUserFromToken(String  token){
        return  extractUsername(token);
    }


    /**
     * Validates the given token by checking its expiration and matching username.
     * @param token the JWT token
     * @return true if the token is valid, false otherwise
     */

    public  Boolean validateToken(String token){
        String username = extractUsername(token);
        if (StringUtils.isNotEmpty(username) && isTokenExpired(token)){
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            return isTokenValid(token,userDetails);  // Validate the token against user details
        }
        return false; // Return false if username is empty or token is expired
    }



}