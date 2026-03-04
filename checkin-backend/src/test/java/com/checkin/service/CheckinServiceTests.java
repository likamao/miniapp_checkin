package com.checkin.service;

import com.checkin.entity.CheckinTopic;
import com.checkin.entity.User;
import com.checkin.repository.CheckinTopicRecordRepository;
import com.checkin.repository.CheckinTopicRepository;
import com.checkin.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CheckinServiceTests {

    @Autowired
    private CheckinService checkinService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CheckinTopicRepository checkinTopicRepository;

    @Autowired
    private CheckinTopicRecordRepository checkinTopicRecordRepository;

    private User testUser;
    private CheckinTopic testTopic;

    @BeforeEach
    void setUp() {
        // 检查是否已存在测试用户
        User existingUser = userRepository.findByOpenid("test_openid_123");
        if (existingUser == null) {
            // 创建测试用户
            testUser = new User();
            testUser.setOpenid("test_openid_123");
            testUser.setNickname("测试用户");
            testUser.setCreatedAt(new Date());
            testUser.setUpdatedAt(new Date());
            testUser = userRepository.save(testUser);
        } else {
            testUser = existingUser;
        }

        // 尝试获取第一个主题，如果没有则创建
        testTopic = checkinService.getActiveTopics().stream().findFirst().orElse(null);
        if (testTopic == null) {
            testTopic = checkinService.createTopic("测试主题", "测试主题描述", 7, testUser);
        }
    }

    @Test
    void testGetWeeklyReport() {
        // 测试获取周报数据
        Map<String, Object> weeklyReport = checkinService.getWeeklyReport(testTopic.getId());
        assertNotNull(weeklyReport);
        
        // 检查报告中是否包含必要的字段
        assertTrue(weeklyReport.containsKey("metrics"));
        assertTrue(weeklyReport.containsKey("userDetails"));
        assertTrue(weeklyReport.containsKey("heatmapData"));
        assertTrue(weeklyReport.containsKey("trendData"));
        assertTrue(weeklyReport.containsKey("startDate"));
        assertTrue(weeklyReport.containsKey("endDate"));
        assertTrue(weeklyReport.containsKey("topicName"));

        // 检查metrics字段
        Map<String, Object> metrics = (Map<String, Object>) weeklyReport.get("metrics");
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("totalCheckinCount"));
        assertTrue(metrics.containsKey("participantCount"));
        assertTrue(metrics.containsKey("averageCheckinCount"));
        assertTrue(metrics.containsKey("userActivityRate"));
    }

    @Test
    void testGetMonthlyReport() {
        // 测试获取月报数据
        Map<String, Object> monthlyReport = checkinService.getMonthlyReport(testTopic.getId());
        assertNotNull(monthlyReport);
        
        // 检查报告中是否包含必要的字段
        assertTrue(monthlyReport.containsKey("metrics"));
        assertTrue(monthlyReport.containsKey("userDetails"));
        assertTrue(monthlyReport.containsKey("heatmapData"));
        assertTrue(monthlyReport.containsKey("trendData"));
        assertTrue(monthlyReport.containsKey("weeklyStats"));
        assertTrue(monthlyReport.containsKey("startDate"));
        assertTrue(monthlyReport.containsKey("endDate"));
        assertTrue(monthlyReport.containsKey("topicName"));

        // 检查metrics字段
        Map<String, Object> metrics = (Map<String, Object>) monthlyReport.get("metrics");
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("totalCheckinCount"));
        assertTrue(metrics.containsKey("participantCount"));
        assertTrue(metrics.containsKey("averageCheckinCount"));
        assertTrue(metrics.containsKey("userActivityRate"));

        // 检查weeklyStats字段
        Map<String, Object> weeklyStats = (Map<String, Object>) monthlyReport.get("weeklyStats");
        assertNotNull(weeklyStats);
        assertTrue(weeklyStats.containsKey("weeks"));
    }

    @Test
    void testGetWeeklyReportWithNonExistentTopic() {
        // 测试获取不存在主题的周报数据
        Map<String, Object> weeklyReport = checkinService.getWeeklyReport(9999L);
        assertNotNull(weeklyReport);
        assertTrue(weeklyReport.containsKey("error"));
        assertEquals("主题不存在", weeklyReport.get("error"));
    }

    @Test
    void testGetMonthlyReportWithNonExistentTopic() {
        // 测试获取不存在主题的月报数据
        Map<String, Object> monthlyReport = checkinService.getMonthlyReport(9999L);
        assertNotNull(monthlyReport);
        assertTrue(monthlyReport.containsKey("error"));
        assertEquals("主题不存在", monthlyReport.get("error"));
    }

    @Test
    void testCheckTopicValid() {
        // 测试检查主题是否有效
        boolean isValid = checkinService.isTopicValid(testTopic.getId());
        assertTrue(isValid);
    }

    @Test
    void testCheckTopicValidWithNonExistentTopic() {
        // 测试检查不存在的主题是否有效
        boolean isValid = checkinService.isTopicValid(9999L);
        assertFalse(isValid);
    }

    @Test
    void testHasUserCheckedInTopicToday() {
        // 测试用户今日是否已打卡主题
        boolean hasCheckedIn = checkinService.hasUserCheckedInTopicToday(testUser, testTopic.getId());
        assertFalse(hasCheckedIn);
    }

    @Test
    void testGetTopicRemainingTime() {
        // 测试获取主题剩余时间
        Map<String, Long> remainingTime = checkinService.getTopicRemainingTime(testTopic.getId());
        assertNotNull(remainingTime);
        assertTrue(remainingTime.containsKey("days"));
        assertTrue(remainingTime.containsKey("hours"));
    }

    @Test
    void testGetTopicRemainingTimeWithNonExistentTopic() {
        // 测试获取不存在主题的剩余时间
        Map<String, Long> remainingTime = checkinService.getTopicRemainingTime(9999L);
        assertNull(remainingTime);
    }
}
