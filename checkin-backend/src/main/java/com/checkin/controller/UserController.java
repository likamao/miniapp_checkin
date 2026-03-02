package com.checkin.controller;

import com.checkin.entity.User;
import com.checkin.repository.UserRepository;
import com.checkin.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PermissionService permissionService;

    /**
     * 获取所有用户信息（需要管理员权限）
     */
    @GetMapping
    public Map<String, Object> getAllUsers(@RequestAttribute("user") User currentUser) {
        // 检查当前用户是否有管理员权限
        if (!permissionService.hasRole(currentUser, "ADMIN")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Forbidden: Insufficient permissions");
            return errorResponse;
        }

        List<User> users = userRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("users", users);
        return response;
    }

    /**
     * 获取指定用户信息（需要管理员或发布者权限）
     */
    @GetMapping("/{id}")
    public Map<String, Object> getUserById(@PathVariable Long id, @RequestAttribute("user") User currentUser) {
        // 检查当前用户是否有管理员或发布者权限
        if (!permissionService.hasRole(currentUser, "ADMIN") && !permissionService.hasRole(currentUser, "PUBLISHER")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Forbidden: Insufficient permissions");
            return errorResponse;
        }

        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "User not found");
            return errorResponse;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        return response;
    }

    /**
     * 获取当前用户信息（所有登录用户都可以访问）
     */
    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(@RequestAttribute("user") User currentUser) {
        Map<String, Object> response = new HashMap<>();
        response.put("user", currentUser);
        return response;
    }
}
