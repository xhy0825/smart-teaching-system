package com.edu.user.controller;

import com.edu.common.entity.Result;
import com.edu.user.dto.LoginRequest;
import com.edu.user.dto.LoginResponse;
import com.edu.user.dto.RegisterRequest;
import com.edu.user.entity.User;
import com.edu.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.success(response);
    }

    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        return Result.success(user);
    }
}
