package com.checkin.service;

import com.checkin.entity.Permission;
import com.checkin.entity.Role;
import com.checkin.entity.User;
import com.checkin.repository.RolePermissionRepository;
import com.checkin.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionService {

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    /**
     * 检查用户是否拥有指定权限
     * 
     * @param user 用户信息
     * @param permissionCode 权限代码
     * @return 是否拥有权限
     */
    public boolean hasPermission(User user, String permissionCode) {
        // 获取用户的所有角色
        List<Role> roles = userRoleRepository.findByUser(user).stream()
                .map(userRole -> userRole.getRole())
                .collect(Collectors.toList());

        // 如果用户没有任何角色，默认作为普通用户，允许基本的打卡操作
        if (roles.isEmpty()) {
            // 普通用户默认拥有的权限
            List<String> defaultPermissions = List.of(
                    "checkin:create",
                    "checkin:read",
                    "checkin:statistics"
            );
            return defaultPermissions.contains(permissionCode);
        }

        // 检查每个角色是否拥有指定权限
        for (Role role : roles) {
            List<Permission> permissions = rolePermissionRepository.findByRole(role).stream()
                    .map(rolePermission -> rolePermission.getPermission())
                    .collect(Collectors.toList());

            for (Permission permission : permissions) {
                if (permissionCode.equals(permission.getCode())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 检查用户是否拥有指定角色
     * 
     * @param user 用户信息
     * @param roleName 角色名称
     * @return 是否拥有角色
     */
    public boolean hasRole(User user, String roleName) {
        List<Role> roles = userRoleRepository.findByUser(user).stream()
                .map(userRole -> userRole.getRole())
                .collect(Collectors.toList());

        for (Role role : roles) {
            if (roleName.equals(role.getName())) {
                return true;
            }
        }

        return false;
    }
}