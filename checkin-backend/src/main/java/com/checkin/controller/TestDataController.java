package com.checkin.controller;

import com.checkin.entity.CheckinRecord;
import com.checkin.entity.CheckinTopic;
import com.checkin.entity.CheckinTopicRecord;
import com.checkin.entity.User;
import com.checkin.repository.CheckinRecordRepository;
import com.checkin.repository.CheckinTopicRecordRepository;
import com.checkin.repository.CheckinTopicRepository;
import com.checkin.repository.UserRepository;
import com.checkin.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/test")
public class TestDataController {

    private static final Logger logger = LoggerFactory.getLogger(TestDataController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CheckinTopicRepository checkinTopicRepository;

    @Autowired
    private CheckinRecordRepository checkinRecordRepository;

    @Autowired
    private CheckinTopicRecordRepository checkinTopicRecordRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 查询所有主题数据
     * GET /api/test/topics
     */
    @GetMapping("/topics")
    public ResponseEntity<Map<String, Object>> getAllTopics() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<CheckinTopic> topics = checkinTopicRepository.findAll();
            
            List<Map<String, Object>> topicList = new ArrayList<>();
            for (CheckinTopic topic : topics) {
                Map<String, Object> topicData = new HashMap<>();
                topicData.put("id", topic.getId());
                topicData.put("title", topic.getTitle());
                topicData.put("description", topic.getDescription());
                topicData.put("startDatetime", dateFormat.format(topic.getStartDatetime()));
                topicData.put("endDatetime", dateFormat.format(topic.getEndDatetime()));
                topicData.put("durationDays", topic.getDurationDays());
                topicData.put("status", topic.getStatus());
                topicData.put("createdBy", topic.getCreatedBy());
                topicList.add(topicData);
            }

            response.put("success", true);
            response.put("count", topicList.size());
            response.put("topics", topicList);
            response.put("message", "查询主题数据成功");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("查询主题数据失败", e);
            response.put("success", false);
            response.put("error", "查询主题数据失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 查询所有用户数据
     * GET /api/test/users
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<User> users = userRepository.findAll();
            
            List<Map<String, Object>> userList = new ArrayList<>();
            for (User user : users) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("openid", user.getOpenid());
                userData.put("unionid", user.getUnionid());
                userData.put("nickname", user.getNickname());
                userData.put("createdAt", dateFormat.format(user.getCreatedAt()));
                userData.put("allowStatsDisplay", user.getAllowStatsDisplay());
                userData.put("profileSetupCompleted", user.getProfileSetupCompleted());
                userList.add(userData);
            }

            response.put("success", true);
            response.put("count", userList.size());
            response.put("users", userList);
            response.put("message", "查询用户数据成功");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("查询用户数据失败", e);
            response.put("success", false);
            response.put("error", "查询用户数据失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 为每个用户在每个主题下生成 7 条打卡记录
     * POST /api/test/generate-checkins
     * 
     * @param request 请求参数，包含 daysBack（可选，默认 7 天）
     */
    @PostMapping("/generate-checkins")
    public ResponseEntity<Map<String, Object>> generateCheckins(@RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 参数验证
            int daysBack = 7; // 默认生成过去 7 天的打卡记录
            if (request != null && request.containsKey("daysBack")) {
                try {
                    daysBack = Integer.parseInt(request.get("daysBack").toString());
                    if (daysBack < 1 || daysBack > 365) {
                        response.put("success", false);
                        response.put("error", "daysBack 参数必须在 1-365 之间");
                        return ResponseEntity.badRequest().body(response);
                    }
                } catch (NumberFormatException e) {
                    response.put("success", false);
                    response.put("error", "daysBack 参数必须是数字");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            logger.info("开始生成测试打卡数据，天数：{}", daysBack);

            // 查询所有用户和主题
            List<User> users = userRepository.findAll();
            List<CheckinTopic> topics = checkinTopicRepository.findAll();

            if (users.isEmpty()) {
                response.put("success", false);
                response.put("error", "系统中没有用户数据");
                return ResponseEntity.badRequest().body(response);
            }

            if (topics.isEmpty()) {
                response.put("success", false);
                response.put("error", "系统中没有主题数据");
                return ResponseEntity.badRequest().body(response);
            }

            logger.info("找到用户数：{}, 主题数：{}", users.size(), topics.size());

            // 统计信息
            int totalGenerated = 0;
            int failedUsers = 0;
            int failedTopics = 0;
            Map<String, Integer> userCheckinCount = new ConcurrentHashMap<>();

            // 为每个用户在每个主题下生成打卡记录
            for (User user : users) {
                try {
                    int userTotalCheckins = 0;

                    for (CheckinTopic topic : topics) {
                        try {
                            // 检查主题是否在有效期内
                            Calendar topicEndCal = Calendar.getInstance();
                            topicEndCal.setTime(topic.getEndDatetime());
                            Calendar today = Calendar.getInstance();
                            
                            if (topicEndCal.before(today)) {
                                logger.debug("主题 {} 已过期，跳过", topic.getTitle());
                                continue;
                            }

                            // 生成过去 7 天的打卡记录
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(Calendar.HOUR_OF_DAY, 0);
                            calendar.set(Calendar.MINUTE, 0);
                            calendar.set(Calendar.SECOND, 0);
                            calendar.set(Calendar.MILLISECOND, 0);

                            int generatedForTopic = 0;
                            for (int i = 0; i < daysBack; i++) {
                                calendar.add(Calendar.DAY_OF_MONTH, -1);
                                Date checkinDate = calendar.getTime();
                                String dateStr = dateFormat.format(checkinDate);

                                // 检查是否已存在该日期的打卡记录
                                boolean exists = checkinTopicRecordRepository
                                    .findByUserIdAndTopicIdAndDate(user.getId(), topic.getId(), dateStr)
                                    .isPresent();

                                if (!exists) {
                                    // 创建打卡记录
                                    CheckinRecord checkinRecord = new CheckinRecord();
                                    checkinRecord.setUser(user);
                                    checkinRecord.setTitle(topic.getTitle() + " - 第" + (i + 1) + "天打卡");
                                    checkinRecord.setContent("这是我在" + topic.getTitle() + "主题下的打卡内容，坚持就是胜利！");
                                    checkinRecord.setCheckinTime(checkinDate);
                                    checkinRecord.setCreatedAt(new Date());
                                    checkinRecord.setUpdatedAt(new Date());
                                    
                                    CheckinRecord savedRecord = checkinRecordRepository.save(checkinRecord);

                                    // 创建主题打卡记录
                                    CheckinTopicRecord topicRecord = new CheckinTopicRecord();
                                    topicRecord.setUserId(user.getId());
                                    topicRecord.setTopicId(topic.getId());
                                    topicRecord.setCheckinRecordId(savedRecord.getId());
                                    topicRecord.setCheckinDate(dateStr);
                                    topicRecord.setCheckinDatetime(checkinDate);
                                    topicRecord.setConsecutiveDays(i + 1);
                                    topicRecord.setCreatedAt(new Date());
                                    topicRecord.setUpdatedAt(new Date());

                                    checkinTopicRecordRepository.save(topicRecord);
                                    generatedForTopic++;
                                    totalGenerated++;
                                }
                            }

                            userTotalCheckins += generatedForTopic;
                            if (generatedForTopic > 0) {
                                logger.debug("用户 {} 在主题 {} 下生成 {} 条记录", 
                                    user.getNickname(), topic.getTitle(), generatedForTopic);
                            }

                        } catch (Exception e) {
                            logger.error("为用户 {} 在主题 {} 下生成打卡记录失败：{}", 
                                user.getNickname(), topic.getTitle(), e.getMessage());
                            failedTopics++;
                        }
                    }

                    userCheckinCount.put(user.getNickname(), userTotalCheckins);
                    logger.info("用户 {} 总共生成 {} 条打卡记录", user.getNickname(), userTotalCheckins);

                } catch (Exception e) {
                    logger.error("为用户 {} 生成打卡记录失败：{}", user.getNickname(), e.getMessage());
                    failedUsers++;
                }
            }

            // 构建响应
            response.put("success", true);
            response.put("message", "生成打卡记录成功");
            response.put("totalGenerated", totalGenerated);
            response.put("userCount", users.size());
            response.put("topicCount", topics.size());
            response.put("failedUsers", failedUsers);
            response.put("failedTopics", failedTopics);
            response.put("daysBack", daysBack);
            
            List<Map<String, Object>> userStats = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : userCheckinCount.entrySet()) {
                Map<String, Object> stat = new HashMap<>();
                stat.put("nickname", entry.getKey());
                stat.put("checkinCount", entry.getValue());
                userStats.add(stat);
            }
            response.put("userStats", userStats);

            logger.info("生成打卡记录完成，总共生成 {} 条记录", totalGenerated);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("生成打卡记录失败", e);
            response.put("success", false);
            response.put("error", "生成打卡记录失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 清空测试数据（可选，用于重新生成）
     * DELETE /api/test/clear-checkins
     */
    @DeleteMapping("/clear-checkins")
    public ResponseEntity<Map<String, Object>> clearCheckins() {
        Map<String, Object> response = new HashMap<>();
        try {
            logger.info("开始清空打卡记录...");
            
            // 先删除主题打卡记录
            checkinTopicRecordRepository.deleteAll();
            
            // 再删除打卡记录
            checkinRecordRepository.deleteAll();
            
            response.put("success", true);
            response.put("message", "清空打卡记录成功");
            
            logger.info("清空打卡记录完成");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("清空打卡记录失败", e);
            response.put("success", false);
            response.put("error", "清空打卡记录失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取数据库连接状态
     * GET /api/test/db-status
     */
    @GetMapping("/db-status")
    public ResponseEntity<Map<String, Object>> getDatabaseStatus() {
        Map<String, Object> response = new HashMap<>();
        try {
            // 测试数据库连接
            long userCount = userRepository.count();
            long topicCount = checkinTopicRepository.count();
            long checkinCount = checkinRecordRepository.count();
            long topicCheckinCount = checkinTopicRecordRepository.count();

            response.put("success", true);
            response.put("message", "数据库连接正常");
            response.put("database", "MySQL");
            response.put("status", "connected");
            
            Map<String, Long> statistics = new HashMap<>();
            statistics.put("userCount", userCount);
            statistics.put("topicCount", topicCount);
            statistics.put("checkinRecordCount", checkinCount);
            statistics.put("topicCheckinRecordCount", topicCheckinCount);
            response.put("statistics", statistics);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("检查数据库状态失败", e);
            response.put("success", false);
            response.put("error", "检查数据库状态失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 生成测试 Token（仅用于开发测试）
     * GET /api/test/generate-token/{userId}
     */
    @GetMapping("/generate-token/{userId}")
    public ResponseEntity<Map<String, Object>> generateToken(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("error", "用户不存在，userId: " + userId);
                return ResponseEntity.badRequest().body(response);
            }

            User user = userOpt.get();
            String token = jwtUtil.generateToken(userId);

            response.put("success", true);
            response.put("message", "生成 Token 成功");
            response.put("userId", userId);
            response.put("nickname", user.getNickname());
            response.put("token", token);

            logger.info("生成测试 Token，用户ID: {}, 昵称: {}", userId, user.getNickname());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("生成 Token 失败", e);
            response.put("success", false);
            response.put("error", "生成 Token 失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
