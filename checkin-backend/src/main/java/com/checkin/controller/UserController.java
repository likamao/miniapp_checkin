package com.checkin.controller;

import com.checkin.entity.User;
import com.checkin.entity.UserWithPermissions;
import com.checkin.repository.UserRepository;
import com.checkin.repository.UserRoleRepository;
import com.checkin.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PermissionService permissionService;

    /**
     * 获取所有用户信息
     * 
     * @param currentUser 当前登录用户信息
     * @return 包含用户列表的响应
     * @throws RuntimeException 权限不足时抛出
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
     * 获取指定用户信息
     * 
     * @param id 用户ID
     * @param currentUser 当前登录用户信息
     * @return 包含用户信息的响应
     * @throws RuntimeException 权限不足或用户不存在时抛出
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
     * 获取当前用户信息
     * 
     * @param currentUser 当前登录用户信息
     * @return 包含当前用户信息和权限信息的响应
     */
    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(@RequestAttribute("user") User currentUser) {
        // 获取用户的角色列表
        List<String> roles = userRoleRepository.findByUser(currentUser).stream()
                .map(userRole -> userRole.getRole().getName())
                .collect(Collectors.toList());
        
        // 构建权限列表（这里简化处理，实际项目中可能需要从数据库获取）
        List<String> permissions = List.of("checkin:read", "checkin:create", "checkin:statistics");
        
        // 创建带权限的用户信息
        UserWithPermissions userWithPermissions = new UserWithPermissions(currentUser, roles, permissions);
        
        Map<String, Object> response = new HashMap<>();
        response.put("user", userWithPermissions);
        return response;
    }

    /**
     * 更新当前用户隐私设置
     * 
     * @param request 请求参数，包含allowStatsDisplay（是否允许在统计中显示）
     * @param currentUser 当前登录用户信息
     * @return 包含更新后用户信息的响应
     */
    @PutMapping("/me/settings")
    public Map<String, Object> updateUserSettings(
            @RequestBody Map<String, Object> request,
            @RequestAttribute("user") User currentUser) {
        if (request.containsKey("allowStatsDisplay")) {
            Boolean allowStatsDisplay = Boolean.valueOf(request.get("allowStatsDisplay").toString());
            currentUser.setAllowStatsDisplay(allowStatsDisplay);
            userRepository.save(currentUser);
        }

        // 获取用户的角色列表
        List<String> roles = userRoleRepository.findByUser(currentUser).stream()
                .map(userRole -> userRole.getRole().getName())
                .collect(Collectors.toList());
        
        // 构建权限列表
        List<String> permissions = List.of("checkin:read", "checkin:create", "checkin:statistics");
        
        // 创建带权限的用户信息
        UserWithPermissions userWithPermissions = new UserWithPermissions(currentUser, roles, permissions);

        Map<String, Object> response = new HashMap<>();
        response.put("user", userWithPermissions);
        return response;
    }

    /**
     * 更新当前用户个人信息
     * 
     * @param request 请求参数，包含nickname（昵称）
     * @param currentUser 当前登录用户信息
     * @return 包含更新后用户信息的响应
     */
    @PutMapping("/me/profile")
    public Map<String, Object> updateUserProfile(
            @RequestBody Map<String, Object> request,
            @RequestAttribute("user") User currentUser) {
        if (request.containsKey("nickname")) {
            String nickname = request.get("nickname").toString();
            if (nickname != null && !nickname.isEmpty()) {
                currentUser.setNickname(nickname);
            }
        }
        userRepository.save(currentUser);

        // 获取用户的角色列表
        List<String> roles = userRoleRepository.findByUser(currentUser).stream()
                .map(userRole -> userRole.getRole().getName())
                .collect(Collectors.toList());
        
        // 构建权限列表
        List<String> permissions = List.of("checkin:read", "checkin:create", "checkin:statistics");
        
        // 创建带权限的用户信息
        UserWithPermissions userWithPermissions = new UserWithPermissions(currentUser, roles, permissions);

        Map<String, Object> response = new HashMap<>();
        response.put("user", userWithPermissions);
        return response;
    }

    /**
     * 获取数据设置说明（隐私说明）
     * 
     * @return 包含数据设置说明列表的响应
     */
    @GetMapping("/data-settings")
    public Map<String, Object> getDataSettings() {
        Map<String, Object> response = new HashMap<>();
        
        // 数据设置说明列表
        List<Map<String, String>> dataSettings = List.of(
            Map.of("icon", "🌊", "text", "当您登录时，你只能查看你自己的打卡数据"),
            Map.of("icon", "🌊", "text", "只有管理员，对应主题发布者才能查看主题内所有人的周报和月报"),
            Map.of("icon", "🌊", "text", "未开启此开关时，您的信息将被匿名化处理，显示为\"微信用户\""),
            Map.of("icon", "🌊", "text", "您可以随时在设置中开启或关闭此功能")
        );
        
        response.put("title", "数据设置");
        response.put("items", dataSettings);
        return response;
    }
}
