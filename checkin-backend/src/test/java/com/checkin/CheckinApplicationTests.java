package com.checkin;

import com.checkin.entity.User;
import com.checkin.repository.UserRepository;
import com.checkin.service.CheckinService;
import com.checkin.service.WeChatService;
import com.checkin.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CheckinApplicationTests {

    @Autowired
    private WeChatService weChatService;

    @Autowired
    private CheckinService checkinService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void contextLoads() {
        // 测试Spring Boot上下文是否正常加载
        assertNotNull(weChatService);
        assertNotNull(checkinService);
        assertNotNull(userRepository);
        assertNotNull(jwtUtil);
    }

    @Test
    void testJwtGeneration() {
        // 测试JWT token生成和解析
        Long userId = 1L;
        String token = jwtUtil.generateToken(userId);
        assertNotNull(token);
        Long parsedUserId = jwtUtil.getUserIdFromToken(token);
        assertEquals(userId, parsedUserId);
    }

    @Test
    void testUserRepository() {
        // 测试用户 repository
        User user = new User();
        user.setOpenid("test_openid");
        user.setNickname("测试用户");
        user.setAvatarUrl("https://example.com/avatar.jpg");
        
        User savedUser = userRepository.save(user);
        assertNotNull(savedUser.getId());
        
        User foundUser = userRepository.findByOpenid("test_openid");
        assertNotNull(foundUser);
        assertEquals("测试用户", foundUser.getNickname());
    }

    // 更多测试方法...

}
