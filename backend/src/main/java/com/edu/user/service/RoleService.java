package com.edu.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.common.exception.BusinessException;
import com.edu.common.util.TenantContextHolder;
import com.edu.user.entity.Role;
import com.edu.user.entity.UserRole;
import com.edu.user.mapper.RoleMapper;
import com.edu.user.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService extends ServiceImpl<RoleMapper, Role> {

    private final UserRoleMapper userRoleMapper;

    public Role getRoleByCode(String code) {
        Long tenantId = TenantContextHolder.getTenantId();
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getTenantId, tenantId)
                .eq(Role::getCode, code);
        return baseMapper.selectOne(wrapper);
    }

    @Transactional
    public void assignRoleToUser(Long userId, String roleCode) {
        Role role = getRoleByCode(roleCode);
        if (role == null) {
            throw new BusinessException("角色不存在: " + roleCode);
        }

        // 检查是否已分配
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId)
                .eq(UserRole::getRoleId, role.getId());
        if (userRoleMapper.selectCount(wrapper) > 0) {
            return; // 已分配
        }

        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(role.getId());
        userRoleMapper.insert(userRole);

        log.info("分配角色: userId={}, roleCode={}", userId, roleCode);
    }

    public List<String> getUserRoleCodes(Long userId) {
        LambdaQueryWrapper<UserRole> urWrapper = new LambdaQueryWrapper<>();
        urWrapper.eq(UserRole::getUserId, userId);
        List<UserRole> userRoles = userRoleMapper.selectList(urWrapper);

        if (userRoles.isEmpty()) {
            return List.of();
        }

        List<Long> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());

        List<Role> roles = baseMapper.selectBatchIds(roleIds);
        return roles.stream()
                .map(Role::getCode)
                .collect(Collectors.toList());
    }

    @Transactional
    public Role createRole(Role role) {
        baseMapper.insert(role);
        log.info("创建角色: code={}", role.getCode());
        return role;
    }
}
