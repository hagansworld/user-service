package com.user_service.user_service.controller;

import com.user_service.user_service.dto.AssignRolesRequestDto;
import com.user_service.user_service.dto.ResponseDto;
import com.user_service.user_service.dto.UserRequestDto;
import com.user_service.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/v1")
@Tag(name = "User Management", description = "User Management")
public class UserController {

    private final UserService userService;

    /**
     * Create/register a new user
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ROLE_ADMIN')")  // Ensure only users with the 'ADMIN' role can create users
    @Operation(summary = "Register a new user", description = "Creates a new user with the provided information.")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    public ResponseEntity<ResponseDto> createUser(@RequestBody UserRequestDto userRequestDto, HttpServletRequest request) {
        ResponseDto responseDto = userService.createUser(userRequestDto, request);
        return ResponseEntity.status(responseDto.getStatusCode()).body(responseDto);
    }

    /**
     * Get all users
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")  // Ensure only users with the 'ADMIN' role can get all users
    @Operation(summary = "Get all users", description = "Fetches all registered users.")
    public ResponseEntity<ResponseDto> getAllUsers(HttpServletRequest request) {
        ResponseDto responseDto = userService.getAllUsers(request);
        return ResponseEntity.status(responseDto.getStatusCode()).body(responseDto);
    }




    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")  // Ensure only users with the 'ADMIN' role can get user by ID
    @Operation(summary = "Get user by ID", description = "Fetches a user by their UUID.")
    public ResponseEntity<ResponseDto> getUserById(@PathVariable UUID id, HttpServletRequest request) {
        ResponseDto responseDto = userService.getUserByID(id, request);
        return ResponseEntity.status(responseDto.getStatusCode()).body(responseDto);
    }


    /**
     * Update entire user (PUT)
     */
    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")  // Ensure only users with the 'ADMIN' role can update users
    @Operation(summary = "Update user", description = "Updates all user details for the given ID.")
    public ResponseEntity<ResponseDto> updateUser(@PathVariable UUID id, @RequestBody UserRequestDto userRequestDto, HttpServletRequest request) {
        ResponseDto responseDto = userService.updateUser(id, userRequestDto, request);
        return ResponseEntity.status(responseDto.getStatusCode()).body(responseDto);
    }


    /**
     * Delete user by ID
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")  // Ensure only users with the 'ADMIN' role can delete users
    @Operation(summary = "Delete user", description = "Deletes the user associated with the given UUID.")
    public ResponseEntity<ResponseDto> deleteUser(@PathVariable UUID id, HttpServletRequest request) {
        ResponseDto responseDto = userService.deleteUser(id, request);
        return ResponseEntity.status(responseDto.getStatusCode()).body(responseDto);
    }


    /**
     * Partially update user (PATCH)
     */
    @PatchMapping("/patch/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")  // Ensure only users with the 'ADMIN' role can patch users
    @Operation(summary = "Partially update user", description = "Applies partial updates to the user.")
    public ResponseEntity<ResponseDto> patchUser(@PathVariable UUID id, @RequestBody UserRequestDto userRequestDto, HttpServletRequest request) {
        ResponseDto responseDto = userService.patchUser(id, userRequestDto, request);
        return ResponseEntity.status(responseDto.getStatusCode()).body(responseDto);
    }


    /**
     * Search users by username and/or email
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ROLE_ADMIN')")  // Ensure only users with the 'ADMIN' role can search users
    @Operation(summary = "Search users", description = "Search users by username or email.")
    public ResponseEntity<ResponseDto> searchUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            HttpServletRequest request
    ) {
        ResponseDto responseDto = userService.searchUsers(username, email, request);
        return ResponseEntity.status(responseDto.getStatusCode()).body(responseDto);
    }

    /**
     * Assign roles to users
     */
    @PutMapping("assign-roles/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")  // Ensure only users with the 'ADMIN' role can assign roles
    @Operation(
            summary = "Assign roles to a user",
            description = "Assign one or more roles to the user based on their ID. " +
                    "The roles should be provided as a list of role names in the request body."
    )
    @ApiResponse(responseCode = "200", description = "Roles assigned successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "400", description = "Invalid roles provided")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public ResponseEntity<ResponseDto> assignRolesToUser(@PathVariable UUID id,
                                                         @RequestBody AssignRolesRequestDto assignRolesRequestDto,
                                                         HttpServletRequest request) {

        ResponseDto responseDto = userService.assignRolesToUser(id, assignRolesRequestDto, request);
        return ResponseEntity.status(responseDto.getStatusCode()).body(responseDto);
    }
}
