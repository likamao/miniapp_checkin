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

    
    /**
     * 微信登录
     * 
     * @param code 微信登录码
     * @return 用户信息
     * @throws RuntimeException 登录失败时抛出
     */
    public User login(String code) {
        // 调用微信 API 获取 openid 和 session_key
        String url = String.format("https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appId, appSecret, code);

        try {
            // 先获取字符串响应
            String response = restTemplate.getForObject(url, String.class);
            // 手动解析 JSON
            Map<String, Object> result = objectMapper.readValue(response, Map.class);
            
            // 检查是否有错误
            if (result.containsKey("errcode")) {
                throw new RuntimeException("微信 API 错误：" + result.get("errmsg"));
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
                user.setProfileSetupCompleted(false);
            }

            user.setUpdatedAt(new Date());
            
            // 保存用户信息（事务性更新）
            userRepository.save(user);

            return user;
        } catch (Exception e) {
            // 处理异常，返回错误信息
            e.printStackTrace();
            throw new RuntimeException("微信登录失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户信息
     * 
     * @param user 用户信息
     * @param nickname 用户昵称
     * @return 更新后的用户信息
     */
    public User updateUserInfo(User user, String nickname) {
        user.setNickname(nickname);
        user.setUpdatedAt(new Date());
        return userRepository.save(user);
    }

    /**
     * 根据用户ID获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    /**
     * 根据微信登录码获取用户信息
     * 
     * @param code 微信登录码
     * @return 用户信息（如果存在）
     */
    public User getUserByOpenidCode(String code) {
        try {
            String url = String.format("https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    appId, appSecret, code);

            String response = restTemplate.getForObject(url, String.class);
            Map<String, Object> result = objectMapper.readValue(response, Map.class);
            
            if (result.containsKey("errcode")) {
                return null;
            }
            
            String openid = (String) result.get("openid");
            return userRepository.findByOpenid(openid);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}