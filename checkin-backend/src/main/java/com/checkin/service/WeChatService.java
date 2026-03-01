package com.checkin.service;

import com.checkin.entity.User;
import com.checkin.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.Map;

@Service
public class WeChatService {

    @Value("${wechat.appid}")
    private String appId;

    @Value("${wechat.secret}")
    private String appSecret;

    @Autowired
    private UserRepository userRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public User login(String code) {
        // 调用微信API获取openid和session_key
        String url = String.format("https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appId, appSecret, code);

        try {
            // 先获取字符串响应
            String response = restTemplate.getForObject(url, String.class);
            // 手动解析JSON
            Map<String, Object> result = objectMapper.readValue(response, Map.class);
            
            // 检查是否有错误
            if (result.containsKey("errcode")) {
                throw new RuntimeException("微信API错误: " + result.get("errmsg"));
            }
            
            String openid = (String) result.get("openid");
            String unionid = (String) result.get("unionid");

            // 查找或创建用户
            User user = userRepository.findByOpenid(openid);
            if (user == null) {
                user = new User();
                user.setOpenid(openid);
                user.setUnionid(unionid);
                user.setCreatedAt(new Date());
                user.setUpdatedAt(new Date());
                userRepository.save(user);
            }

            return user;
        } catch (Exception e) {
            // 处理异常，返回错误信息
            e.printStackTrace();
            throw new RuntimeException("微信登录失败: " + e.getMessage());
        }
    }

    public User updateUserInfo(User user, String nickname, String avatarUrl) {
        user.setNickname(nickname);
        user.setAvatarUrl(avatarUrl);
        user.setUpdatedAt(new Date());
        return userRepository.save(user);
    }
}