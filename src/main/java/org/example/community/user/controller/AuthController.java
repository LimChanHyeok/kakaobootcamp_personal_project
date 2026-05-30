package org.example.community.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.community.global.response.ApiResponse;
import org.example.community.user.dto.request.LoginRequest;
import org.example.community.user.dto.response.LoginResponse;
import org.example.community.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = userService.login(
                request.getEmail(),
                request.getPassword()
        );

        return ResponseEntity
                .ok(ApiResponse.success("로그인에 성공했습니다.", response));
    }
}
