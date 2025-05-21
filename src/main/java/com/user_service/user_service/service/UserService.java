package com.user_service.user_service.service;

import com.user_service.user_service.dto.ResponseDto;
import com.user_service.user_service.dto.UserDto;
import com.user_service.user_service.entity.User;
import com.user_service.user_service.mapper.UserMapper;
import com.user_service.user_service.repository.UserRepository;
import com.user_service.user_service.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;


    /**
     *
     * @param email
     * @param request
     * @return
     */
    public ResponseDto getCurrentUserProfile(String email, HttpServletRequest request){

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Email not found"));

        UserDto userDto = userMapper.toUserDto(user);

        return ApiResponse.buildResponse(
                userDto,
                HttpStatus.OK.value(),
                "User profile fetched successfully",
                request.getRequestURI()
                );
    }

//
//    public ResponseDto updateCurrentUserProfile(String email, UserDto userDto){
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new UsernameNotFoundException("Email not found"));
//
//        if (userDto.getUsername()!=null ){
//            user.setUsername(userDto.getUsername());
//        }
//    }



}
