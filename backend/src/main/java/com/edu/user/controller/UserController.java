package com.edu.user.controller;

import com.edu.common.entity.Result;
import com.edu.user.dto.CreateUserRequest;
import com.edu.user.entity.User;
import com.edu.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/list")
    public Result<List<User>> listUsers() {
        List<User> users = userService.listUsers();
        return Result.success(users);
    }

    @PostMapping
    public Result<User> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(request);
        return Result.success(user);
    }

    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }

    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody User user) {
        User existingUser = userService.getUserById(id);
        if (existingUser == null) {
            return Result.error("用户不存在");
        }
        user.setId(id);
        user.setTenantId(existingUser.getTenantId());
        userService.updateUser(user);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @GetMapping("/me")
    public Result<User> getCurrentUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        User user = userService.getUserById(userId);
        return Result.success(user);
    }

    @PutMapping("/{id}/role")
    public Result<Void> assignRole(@PathVariable Long id, @RequestParam String roleCode) {
        userService.assignRole(id, roleCode);
        return Result.success();
    }

    @GetMapping("/{id}/roles")
    public Result<List<String>> getUserRoles(@PathVariable Long id) {
        List<String> roles = userService.getUserRoles(id);
        return Result.success(roles);
    }
}
