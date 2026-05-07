package com.edu.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.user.entity.Permission;
import com.edu.user.entity.RolePermission;
import com.edu.user.mapper.PermissionMapper;
import com.edu.user.mapper.RolePermissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService extends ServiceImpl<PermissionMapper, Permission> {

    private final RolePermissionMapper rolePermissionMapper;

    public Permission getPermissionByCode(String code) {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getCode, code);
        return baseMapper.selectOne(wrapper);
    }

    public List<String> getRolePermissions(Long roleId) {
        LambdaQueryWrapper<RolePermission> rpWrapper = new LambdaQueryWrapper<>();
        rpWrapper.eq(RolePermission::getRoleId, roleId);
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(rpWrapper);

        if (rolePermissions.isEmpty()) {
            return List.of();
        }

        List<Long> permIds = rolePermissions.stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toList());

        List<Permission> permissions = baseMapper.selectBatchIds(permIds);
        return permissions.stream()
                .map(Permission::getCode)
                .collect(Collectors.toList());
    }

    @Transactional
    public void assignPermissionToRole(Long roleId, Long permissionId) {
        RolePermission rp = new RolePermission();
        rp.setRoleId(roleId);
        rp.setPermissionId(permissionId);
        rolePermissionMapper.insert(rp);
        log.info("分配权限: roleId={}, permissionId={}", roleId, permissionId);
    }

    public List<String> getUserPermissions(Long userId) {
        // 获取用户所有角色的权限
        return List.of(); // 需要RoleService配合，后续实现
    }
}
