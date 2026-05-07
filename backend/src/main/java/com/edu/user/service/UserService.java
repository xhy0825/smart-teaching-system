package com.edu.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.common.exception.BusinessException;
import com.edu.common.util.JwtUtil;
import com.edu.common.util.TenantContextHolder;
import com.edu.user.dto.LoginRequest;
import com.edu.user.dto.LoginResponse;
import com.edu.user.dto.RegisterRequest;
import com.edu.user.entity.User;
import com.edu.user.entity.UserRole;
import com.edu.user.mapper.UserMapper;
import com.edu.user.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

    private final UserRoleMapper userRoleMapper;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public User register(RegisterRequest request) {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("租户上下文缺失");
        }

        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getTenantId, tenantId)
                .eq(User::getUsername, request.getUsername());
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setTenantId(tenantId);
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(1);
        baseMapper.insert(user);

        // 分配默认角色（TEACHER或STUDENT）
        String defaultRole = request.getRoleCode() != null ? request.getRoleCode() : "TEACHER";
        roleService.assignRoleToUser(user.getId(), defaultRole);

        log.info("用户注册成功: userId={}, username={}", user.getId(), user.getUsername());
        return user;
    }

    public LoginResponse login(LoginRequest request) {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("租户上下文缺失");
        }

        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getTenantId, tenantId)
                .eq(User::getUsername, request.getUsername());
        User user = baseMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException("用户已禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 生成JWT
        String token = jwtUtil.generateToken(user.getId(), tenantId, user.getUsername());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());

        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());
        return response;
    }

    public User getUserById(Long userId) {
        return baseMapper.selectById(userId);
    }

    public User getUserByUsername(String username) {
        Long tenantId = TenantContextHolder.getTenantId();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getTenantId, tenantId)
                .eq(User::getUsername, username);
        return baseMapper.selectOne(wrapper);
    }

    @Transactional
    public void updateUser(User user) {
        baseMapper.updateById(user);
        log.info("更新用户信息: userId={}", user.getId());
    }

    @Transactional
    public void assignRole(Long userId, String roleCode) {
        roleService.assignRoleToUser(userId, roleCode);
    }

    public List<String> getUserRoles(Long userId) {
        return roleService.getUserRoleCodes(userId);
    }
}
