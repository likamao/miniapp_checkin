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