package com.user_service.user_service.service;


import com.user_service.user_service.dto.*;
import com.user_service.user_service.entity.Role;
import com.user_service.user_service.entity.User;
import com.user_service.user_service.enums.UserRole;
import com.user_service.user_service.exception.EmailAlreadyExistsException;
import com.user_service.user_service.exception.UserAlreadyExitsException;
import com.user_service.user_service.exception.UserNotFoundException;
import com.user_service.user_service.mapper.UserMapper;
import com.user_service.user_service.repository.RoleRepository;
import com.user_service.user_service.repository.UserRepository;
import com.user_service.user_service.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


    /**
     *
     * @param userRequestDto
     * @param request
     * @return
     */
    public ResponseDto createUser(UserRequestDto userRequestDto, HttpServletRequest request){
        try{
            // Check if email or username already exists
            if (userRepository.existsByEmail(userRequestDto.getEmail()) ||
                    userRepository.existsByUsername(userRequestDto.getUsername())){
                return ApiResponse.buildResponse(
                        null,
                        400,
                        "User with this email and username already exist",
                        request.getRequestURI()
                );
            }

            // Map DTO to Entity
            User newUser = userMapper.toUser(userRequestDto);
            newUser.setEnabled(true); // This will enable true by default
            newUser.setPassword(passwordEncoder.encode(userRequestDto.getPassword())); // Hash the password


            // Assigned a roles if provided , else assign Default role(ROLE_USER)
            List<Role> roles = new ArrayList<>();
            if (userRequestDto.getRoles() != null && !userRequestDto.getRoles().isEmpty()){
                roles = roleRepository.findByRoleIn(userRequestDto.getRoles());
            }else{
                roles.add(roleRepository.findByRole(UserRole.USER)
                        .orElseThrow( ()-> new RuntimeException("Default role not found")));
            }
            newUser.setRoles(roles);


            // Save user
            User savedUser = userRepository.save(newUser);

            // Map the Response
            UserResponseDto userResponseDto = userMapper.touserResponseDto(savedUser);


            return ApiResponse.buildResponse(
                    userResponseDto,
                    201,
                    "User Created successfully",
                    request.getRequestURI()
            );

        }catch (UserAlreadyExitsException  | EmailAlreadyExistsException e){
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("An expected error occurred ", e);
        }
    }

    /**
     *
     * @param request
     * @return
     */
    public ResponseDto getAllUsers(HttpServletRequest request){
        try{
            List<User> users = userRepository.findAll();

            List<UserResponseDto> responseDtoList = users.stream()
                    .map(userMapper::touserResponseDto)
                    .toList();

            return ApiResponse.buildResponse(
                    responseDtoList,
                    200,
                    "All users retrieved successfully",
                    request.getRequestURI()
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve users", e);
        }
    }

    /**
     *
     * @param id
     * @param request
     * @return
     */

    public ResponseDto getUserByID(UUID id,HttpServletRequest request){
        try{

            // Fetch user from DB using ID
            Optional<User> optionalUser = userRepository.findById(id);

            // If user not found, return 404 response
            if (optionalUser.isEmpty()){
                throw new UserNotFoundException("User with " + id + " not found");
            }

            //  Convert User entity to UserResponseDto
            UserResponseDto userResponseDto = userMapper.touserResponseDto(optionalUser.get());

            // Return 200 OK with user data
            return ApiResponse.buildResponse(
                    userResponseDto,
                    200,
                    "user retrieved successfully",
                    request.getRequestURI()
            );

        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e){
            throw  new RuntimeException("Failed to retrieve user by Id", e);
        }
    }

    /**
     *
     * @param id
     * @param userRequestDto
     * @param request
     * @return
     */
    public ResponseDto updateUser(UUID id, UserRequestDto userRequestDto, HttpServletRequest request){

        try{
            // Check if user exists
            Optional<User> optionalUser = userRepository.findById(id);

            if (optionalUser.isEmpty()) {
                throw new UserNotFoundException("User with " + id + " not found");
            }

                // Get existing user
                User user = optionalUser.get();

                // use the existing user to update it
                user.setUsername(userRequestDto.getUsername());
                user.setEmail(userRequestDto.getEmail());
                user.setPassword(userRequestDto.getPassword());
                user.setEnabled(userRequestDto.getEnabled());


                // Update roles if provided
                if (userRequestDto.getRoles() != null && !userRequestDto.getRoles().isEmpty()){
                    List<Role> roles = roleRepository.findByRoleIn(userRequestDto.getRoles());
                    user.setRoles(roles);
                }

                // Step 5: Save updated user
                User savedUser = userRepository.save(user);

                // Convert User entity to UserResponseDto
            UserResponseDto userResponseDto  = userMapper.touserResponseDto(savedUser);

                return ApiResponse.buildResponse(
                        userResponseDto,
                        200,
                        "user updated successfully",
                        request.getRequestURI()
                );

        }catch (UserNotFoundException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException("Failed to update user by id " ,e);
        }


    }

    /**
     *
     * @param id
     * @param userRequestDto
     * @param request
     * @return
     */
    public ResponseDto patchUser(UUID id, UserRequestDto userRequestDto, HttpServletRequest request){
        try{
                 //  Check if user exists
            Optional<User> optionalUser = userRepository.findById(id);

            if (optionalUser.isEmpty()){
                throw new UserNotFoundException("User with " + id + " not found");
            }

            //Get existing user
            User user = optionalUser.get();

            // Update fields only if provided (not null)

            // Update username if present
            if (userRequestDto.getUsername() != null){
                user.setUsername(userRequestDto.getUsername());
            }

            // Update email if present
            if (userRequestDto.getEmail() !=null){
                user.setEmail(userRequestDto.getEmail());
            }

            // Update password if present
            if (userRequestDto.getPassword() !=null){
                user.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
            }

            // Update roles if present
            if (userRequestDto.getRoles() !=null && !userRequestDto.getRoles().isEmpty()){
                user.setRoles(roleRepository.findByRoleIn(userRequestDto.getRoles()));
            }


            // Update enabled only if explicitly sent (true or false)
            if (userRequestDto.getEnabled() !=null) {
                user.setEnabled(userRequestDto.getEnabled());
            }


            //  Save updated user
            User saveduser = userRepository.save(user);

            // Convert User entity to UserResponseDto
            UserResponseDto userResponseDto = userMapper.touserResponseDto(saveduser);

            return ApiResponse.buildResponse(
                    userResponseDto,
                    200,
                    "User partially updated successfully",
                    request.getRequestURI()
            );

    }catch(UserNotFoundException e){
            throw  e;
        }catch (Exception e){
            throw  new RuntimeException("Failed to patch user by id ", e);
        }

    }

    /**
     *
     * @param id
     * @param request
     * @return
     */
    public ResponseDto deleteUser(UUID id, HttpServletRequest request){
        try{
            //Check if the user exists.
            Optional<User> optionalUser = userRepository.findById(id);

            if ( optionalUser.isEmpty()){
                throw  new UserNotFoundException("User not found with id " + id);
            }

            // Delete the user from the repository.
            userRepository.delete(optionalUser.get());

            return ApiResponse.buildResponse(
                    null,
                    200,
                    "user deleted succesfully",
                    request.getRequestURI()
            );

        }catch (UserNotFoundException e){
            throw  e;
        }catch (Exception e){
            throw new RuntimeException("Failed to delete user by id", e);
        }

    }

    /**
     *
     * @param username
     * @param email
     * @param request
     * @return
     */
    public ResponseDto searchUsers(String username, String email, HttpServletRequest request){
        try{
        List<User>users;

        if (username != null && email !=null){
            users =userRepository.findUserByUsernameContainingIgnoreCaseAndEmailContainingIgnoreCase(username,email);
        }else if (username !=null ){
            users = userRepository.findByUsernameContainingIgnoreCase(username);
        }else  if (email != null){
            users = userRepository.findByEmailContainingIgnoreCase(email);
        }else {
            users = userRepository.findAll();
        }
        List<UserResponseDto> result = users.stream()
                .map(userMapper::touserResponseDto)
                .toList();

        return ApiResponse.buildResponse(
                result,
                200,
                "Search result",
                request.getRequestURI()
        );
        }catch (UserNotFoundException e){
            throw e;
        }catch (Exception e){
            throw  new RuntimeException("Failed to search users", e);
        }
    }



//    public ResponseDto assignRoles(AssignRolesRequestDto assignRolesRequestDto, UUID id, HttpServletRequest request){
//
//        try{
//            // find user by id
//            User user = userRepository.findById(id).orElseThrow(()->  new UsernameNotFoundException("User not found" + id));
//
//
//
//           return null;
//
//
//        } catch (UserNotFoundException e) {
//            throw e;
//        } catch (Exception e){
//            throw new RuntimeException("Failed to assign user with ", e);
//        }
//    }
//



}
