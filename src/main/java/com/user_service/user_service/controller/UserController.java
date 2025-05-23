package com.user_service.user_service.controller;

import com.user_service.user_service.dto.ResponseDto;
import com.user_service.user_service.dto.UserRequestDto;
import com.user_service.user_service.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/v1")
public class UserController {

    private final UserService userService;

    /**
     * Create/register a new user
     */
    @PostMapping("/create")
    public ResponseEntity<ResponseDto> createUser(@RequestBody UserRequestDto userRequestDto, HttpServletRequest request) {
        ResponseDto responseDto = userService.createUser(userRequestDto, request);
        return ResponseEntity.status(responseDto.getStatusCode()).body(responseDto);
    }

    /**
     * Get all users
     */
    @GetMapping
    public ResponseEntity<ResponseDto> getAllUsers(HttpServletRequest request) {
        ResponseDto responseDto = userService.getAllUsers(request);
        return ResponseEntity.status(responseDto.getStatusCode()).body(responseDto);
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto> getUserById(@PathVariable UUID id, HttpServletRequest request) {
        ResponseDto responseDto = userService.getUserByID(id, request);
        return ResponseEntity.status(responseDto.getStatusCode()).body(responseDto);
    }

    /**
     * Update entire user (PUT)
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseDto> updateUser(@PathVariable UUID id, @RequestBody UserRequestDto userRequestDto, HttpServletRequest request) {
        ResponseDto responseDto = userService.updateUser(id, userRequestDto, request);
        return ResponseEntity.status(responseDto.getStatusCode()).body(responseDto);
    }

    /**
     * Delete user by ID
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseDto> deleteUser(@PathVariable UUID id, HttpServletRequest request) {
        ResponseDto responseDto = userService.deleteUser(id, request);
        return ResponseEntity.status(responseDto.getStatusCode()).body(responseDto);
    }

    /**
     * Partially update user (PATCH)
     */
    @PatchMapping("/patch/{id}")
    public ResponseEntity<ResponseDto> patchUser(@PathVariable UUID id, @RequestBody UserRequestDto userRequestDto, HttpServletRequest request) {
        ResponseDto responseDto = userService.patchUser(id, userRequestDto, request);
        return ResponseEntity.status(responseDto.getStatusCode()).body(responseDto);
    }

    /**
     * Search users by username and/or email
     */
    @GetMapping("/search")
    public ResponseEntity<ResponseDto> searchUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            HttpServletRequest request
    ) {
        ResponseDto responseDto = userService.searchUsers(username, email, request);
        return ResponseEntity.status(responseDto.getStatusCode()).body(responseDto);
    }
}
