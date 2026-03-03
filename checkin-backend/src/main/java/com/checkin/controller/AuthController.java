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
     * 微信登录
     * 
     * @param request 请求参数，包含code（微信登录码）、nickname（昵称）和avatarUrl（头像URL）
     * @return 包含token和用户信息的响应
     * @throws RuntimeException 登录失败时抛出
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        String nickname = request.get("nickname");
        String avatarUrl = request.get("avatarUrl");
        
        User user = weChatService.login(code, nickname, avatarUrl);

        String token = jwtUtil.generateToken(user.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", user);

        return response;
    }

    /**
     * 更新用户信息
     * 
     * @param request 请求参数，包含userId（用户ID）、nickname（昵称）和avatarUrl（头像URL）
     * @return 包含更新后用户信息的响应
     * @throws RuntimeException 更新失败时抛出
     */
    @PostMapping("/updateInfo")
    public Map<String, Object> updateUserInfo(@RequestBody Map<String, String> request) {
        Long userId = Long.parseLong(request.get("userId"));
        String nickname = request.get("nickname");
        String avatarUrl = request.get("avatarUrl");

        // 这里需要根据userId获取用户信息，然后更新
        // 简化处理，实际项目中需要添加权限验证
        User user = new User();
        user.setId(userId);
        user = weChatService.updateUserInfo(user, nickname, avatarUrl);

        Map<String, Object> response = new HashMap<>();
        response.put("user", user);

        return response;
    }
}