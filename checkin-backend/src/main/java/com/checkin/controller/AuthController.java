package com.checkin.controller;

import com.checkin.entity.User;
import com.checkin.service.WeChatService;
import com.checkin.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")

public class AuthController {

    @Autowired
    private WeChatService weChatService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 验证 token 有效性
     * 
     * @param token 请求头中的 token
     * @return 包含验证结果和用户信息的响应
     */
    @GetMapping("/verify")
    public Map<String, Object> verifyToken(@RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            if (jwtUtil.isTokenExpired(token)) {
                response.put("valid", false);
                response.put("message", "Token 已过期");
                return response;
            }
            
            Long userId = jwtUtil.getUserIdFromToken(token);
            User user = weChatService.getUserById(userId);
            
            if (user != null) {
                response.put("valid", true);
                response.put("user", user);
            } else {
                response.put("valid", false);
                response.put("message", "用户不存在");
            }
        } catch (Exception e) {
            response.put("valid", false);
            response.put("message", "Token 验证失败");
        }
        
        return response;
    }

    /**
     * 检查用户信息是否存在
     * 
     * @param request 请求参数，包含code（微信登录码）
     * @return 包含用户信息存在状态的响应
     */
    @PostMapping("/checkUser")
    public Map<String, Object> checkUser(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = weChatService.getUserByOpenidCode(code);
            
            if (user != null) {
                response.put("exists", true);
                response.put("profileSetupCompleted", user.getProfileSetupCompleted());
                response.put("nickname", user.getNickname());
            } else {
                response.put("exists", false);
                response.put("profileSetupCompleted", false);
            }
        } catch (Exception e) {
            response.put("exists", false);
            response.put("profileSetupCompleted", false);
        }
        
        return response;
    }

    /**
     * 微信登录
     * 
     * @param request 请求参数，包含code（微信登录码）和nickname（昵称）
     * @return 包含token和用户信息的响应
     * @throws RuntimeException 登录失败时抛出
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        
        User user = weChatService.login(code);

        String token = jwtUtil.generateToken(user.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        
        // 构建用户信息响应，不包含 nickname 字段
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("openid", user.getOpenid());
        userResponse.put("unionid", user.getUnionid());
        userResponse.put("createdAt", user.getCreatedAt());
        userResponse.put("updatedAt", user.getUpdatedAt());
        userResponse.put("allowStatsDisplay", user.getAllowStatsDisplay());
        userResponse.put("profileSetupCompleted", user.getProfileSetupCompleted());
        
        response.put("user", userResponse);

        return response;
    }

    /**
     * 更新用户信息
     * 
     * @param request 请求参数，包含userId（用户ID）和nickname（昵称）
     * @return 包含更新后用户信息的响应
     * @throws RuntimeException 更新失败时抛出
     */
    @PostMapping("/updateInfo")
    public Map<String, Object> updateUserInfo(@RequestBody Map<String, String> request) {
        Long userId = Long.parseLong(request.get("userId"));
        String nickname = request.get("nickname");

        // 这里需要根据userId获取用户信息，然后更新
        // 简化处理，实际项目中需要添加权限验证
        User user = new User();
        user.setId(userId);
        user = weChatService.updateUserInfo(user, nickname);

        Map<String, Object> response = new HashMap<>();
        response.put("user", user);

        return response;
    }
}