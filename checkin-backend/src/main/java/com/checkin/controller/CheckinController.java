package com.checkin.controller;

import com.checkin.entity.CheckinRecord;
import com.checkin.entity.CheckinTopic;
import com.checkin.entity.CheckinTopicRecord;
import com.checkin.entity.User;
import com.checkin.service.CheckinService;
import com.checkin.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkin")
public class CheckinController {

    @Autowired
    private CheckinService checkinService;

    @Autowired
    private PermissionService permissionService;

    /**
     * 创建打卡记录
     * 
     * @param request 请求参数，包含title（打卡标题）和content（打卡内容）
     * @param user 当前登录用户信息
     * @return 包含创建的打卡记录的响应
     * @throws RuntimeException 权限不足时抛出
     */
    @PostMapping("/create")
    public Map<String, Object> createCheckin(@RequestBody Map<String, String> request, @RequestAttribute("user") User user) {
        // 检查用户是否有打卡创建权限
        if (!permissionService.hasPermission(user, "checkin:create")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "权限不足：没有打卡创建权限");
            return errorResponse;
        }

        String title = request.get("title");
        String content = request.get("content");

        CheckinRecord record = checkinService.createCheckinRecord(user, title, content);

        Map<String, Object> response = new HashMap<>();
        response.put("record", record);

        return response;
    }

    /**
     * 获取打卡记录列表
     * 
     * @param user 当前登录用户信息
     * @param year 可选，年份
     * @param month 可选，月份
     * @param page 当前页码，默认1
     * @param pageSize 每页大小，默认10
     * @return 包含打卡记录列表和分页信息的响应
     * @throws RuntimeException 权限不足时抛出
     */
    @GetMapping("/records")
    public Map<String, Object> getCheckinRecords(
            @RequestAttribute("user") User user,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        // 检查用户是否有打卡查询权限
        if (!permissionService.hasPermission(user, "checkin:read")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "权限不足：没有打卡查询权限");
            return errorResponse;
        }

        // 构建分页请求
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        Page<CheckinRecord> recordPage;
        if (year != null) {
            recordPage = checkinService.getCheckinRecordsByYearAndMonth(user, year, month, pageable);
        } else {
            recordPage = checkinService.getCheckinRecords(user, pageable);
        }

        // 处理记录，格式化时间
        List<Map<String, Object>> formattedRecords = recordPage.getContent().stream().map(record -> {
            Map<String, Object> formattedRecord = new HashMap<>();
            formattedRecord.put("id", record.getId());
            formattedRecord.put("title", record.getTitle());
            formattedRecord.put("content", record.getContent());
            formattedRecord.put("checkinTime", checkinService.formatDateTime(record.getCheckinTime()));
            formattedRecord.put("createdAt", checkinService.formatDateTime(record.getCreatedAt()));
            formattedRecord.put("updatedAt", checkinService.formatDateTime(record.getUpdatedAt()));
            return formattedRecord;
        }).toList();

        // 构建响应数据
        Map<String, Object> response = new HashMap<>();
        response.put("records", formattedRecords);
        response.put("pagination", Map.of(
                "total", recordPage.getTotalElements(),
                "totalPages", recordPage.getTotalPages(),
                "currentPage", page,
                "pageSize", pageSize
        ));

        return response;
    }

    /**
     * 获取打卡统计信息
     * 
     * @param user 当前登录用户信息
     * @return 包含最近7天打卡次数的响应
     * @throws RuntimeException 权限不足时抛出
     */
    @GetMapping("/statistics")
    public Map<String, Object> getCheckinStatistics(@RequestAttribute("user") User user) {
        // 检查用户是否有打卡统计权限
        if (!permissionService.hasPermission(user, "checkin:statistics")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "权限不足：没有打卡统计权限");
            return errorResponse;
        }

        long recent7DaysCount = checkinService.getRecent7DaysCheckinCount(user);

        Map<String, Object> response = new HashMap<>();
        response.put("recent7DaysCount", recent7DaysCount);

        return response;
    }

    /**
     * 检查今日是否已打卡
     * 
     * @param user 当前登录用户信息
     * @return 包含今日打卡状态的响应
     * @throws RuntimeException 权限不足时抛出
     */
    @GetMapping("/check-today")
    public Map<String, Object> checkTodayCheckin(@RequestAttribute("user") User user) {
        // 检查用户是否有打卡查询权限
        if (!permissionService.hasPermission(user, "checkin:read")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "权限不足：没有打卡查询权限");
            return errorResponse;
        }

        boolean hasCheckedInToday = checkinService.hasCheckedInToday(user);

        Map<String, Object> response = new HashMap<>();
        response.put("hasCheckedInToday", hasCheckedInToday);

        return response;
    }

    /**
     * 获取所有主题列表
     * 
     * @param user 当前登录用户信息
     * @return 包含主题列表的响应
     * @throws RuntimeException 权限不足时抛出
     */
    @GetMapping("/topics")
    public Map<String, Object> getTopics(@RequestAttribute("user") User user) {
        // 检查用户是否有打卡查询权限
        if (!permissionService.hasPermission(user, "checkin:read")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "权限不足：没有打卡查询权限");
            return errorResponse;
        }

        List<Map<String, Object>> topics = checkinService.getAllTopics().stream().map(topic -> {
            Map<String, Object> topicMap = new HashMap<>();
            topicMap.put("id", topic.getId());
            topicMap.put("title", topic.getTitle());
            topicMap.put("description", topic.getDescription());
            topicMap.put("startDatetime", checkinService.formatDateTime(topic.getStartDatetime()));
            topicMap.put("endDatetime", checkinService.formatDateTime(topic.getEndDatetime()));
            topicMap.put("durationDays", topic.getDurationDays());
            topicMap.put("status", topic.getStatus());
            topicMap.put("checkinCount", checkinService.getTopicCheckinCount(topic.getId()));
            topicMap.put("hasCheckedInToday", checkinService.hasUserCheckedInTopicToday(user, topic.getId()));
            return topicMap;
        }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("topics", topics);

        return response;
    }

    /**
     * 获取主题详情
     * 
     * @param topicId 主题ID
     * @param user 当前登录用户信息
     * @return 包含主题详情的响应
     * @throws RuntimeException 权限不足时抛出
     */
    @GetMapping("/topics/{topicId}")
    public Map<String, Object> getTopicDetail(@PathVariable Long topicId, @RequestAttribute("user") User user) {
        // 检查用户是否有打卡查询权限
        if (!permissionService.hasPermission(user, "checkin:read")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "权限不足：没有打卡查询权限");
            return errorResponse;
        }

        CheckinTopic topic = checkinService.getTopicById(topicId);
        if (topic == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "主题不存在");
            return errorResponse;
        }

        Map<String, Object> topicMap = new HashMap<>();
        topicMap.put("id", topic.getId());
        topicMap.put("title", topic.getTitle());
        topicMap.put("description", topic.getDescription());
        topicMap.put("startDatetime", checkinService.formatDateTime(topic.getStartDatetime()));
        topicMap.put("endDatetime", checkinService.formatDateTime(topic.getEndDatetime()));
        topicMap.put("durationDays", topic.getDurationDays());
        topicMap.put("status", topic.getStatus());
        topicMap.put("checkinCount", checkinService.getTopicCheckinCount(topic.getId()));
        topicMap.put("hasCheckedInToday", checkinService.hasUserCheckedInTopicToday(user, topic.getId()));
        topicMap.put("remainingTime", checkinService.getTopicRemainingTime(topic.getId()));

        Map<String, Object> response = new HashMap<>();
        response.put("topic", topicMap);

        return response;
    }

    /**
     * 创建主题
     * 
     * @param request 请求参数，包含title（主题标题）、description（主题描述）和durationDays（持续天数）
     * @param user 当前登录用户信息
     * @return 包含创建的主题信息的响应
     * @throws RuntimeException 权限不足或参数错误时抛出
     */
    @PostMapping("/topics")
    public Map<String, Object> createTopic(@RequestBody Map<String, Object> request, @RequestAttribute("user") User user) {
        // 检查用户是否有主题创建权限
        if (!permissionService.hasPermission(user, "checkin:create")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "权限不足：没有主题创建权限");
            return errorResponse;
        }

        String title = (String) request.get("title");
        String description = (String) request.get("description");
        Integer durationDays = request.get("durationDays") != null ? Integer.valueOf(request.get("durationDays").toString()) : null;

        if (title == null || title.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "主题标题不能为空");
            return errorResponse;
        }

        if (title.length() > 100) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "主题标题不能超过100个字符");
            return errorResponse;
        }

        CheckinTopic topic = checkinService.createTopic(title, description, durationDays, user);

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> topicMap = new HashMap<>();
        topicMap.put("id", topic.getId());
        topicMap.put("title", topic.getTitle());
        topicMap.put("description", topic.getDescription());
        topicMap.put("startDatetime", checkinService.formatDateTime(topic.getStartDatetime()));
        topicMap.put("endDatetime", checkinService.formatDateTime(topic.getEndDatetime()));
        topicMap.put("durationDays", topic.getDurationDays());
        response.put("topic", topicMap);

        return response;
    }

    /**
     * 更新主题
     * 
     * @param topicId 主题ID
     * @param request 请求参数，包含title（主题标题）、description（主题描述）和durationDays（持续天数）
     * @param user 当前登录用户信息
     * @return 包含更新后的主题信息的响应
     * @throws RuntimeException 权限不足、参数错误或主题不存在时抛出
     */
    @PutMapping("/topics/{topicId}")
    public Map<String, Object> updateTopic(@PathVariable Long topicId, @RequestBody Map<String, Object> request, @RequestAttribute("user") User user) {
        // 检查用户是否有主题更新权限
        if (!permissionService.hasPermission(user, "checkin:create")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "权限不足：没有主题更新权限");
            return errorResponse;
        }

        String title = (String) request.get("title");
        String description = (String) request.get("description");
        Integer durationDays = request.get("durationDays") != null ? Integer.valueOf(request.get("durationDays").toString()) : null;

        if (title == null || title.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "主题标题不能为空");
            return errorResponse;
        }

        if (title.length() > 100) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "主题标题不能超过100个字符");
            return errorResponse;
        }

        CheckinTopic topic = checkinService.updateTopic(topicId, title, description, durationDays, user);

        if (topic == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "主题不存在或更新失败");
            return errorResponse;
        }

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> topicMap = new HashMap<>();
        topicMap.put("id", topic.getId());
        topicMap.put("title", topic.getTitle());
        topicMap.put("description", topic.getDescription());
        topicMap.put("startDatetime", checkinService.formatDateTime(topic.getStartDatetime()));
        topicMap.put("endDatetime", checkinService.formatDateTime(topic.getEndDatetime()));
        topicMap.put("durationDays", topic.getDurationDays());
        response.put("topic", topicMap);

        return response;
    }

    /**
     * 检查主题今日是否已打卡
     * 
     * @param topicId 主题ID
     * @param user 当前登录用户信息
     * @return 包含今日打卡状态的响应
     * @throws RuntimeException 权限不足时抛出
     */
    @GetMapping("/check-topic-today/{topicId}")
    public Map<String, Object> checkTopicCheckinToday(@PathVariable Long topicId, @RequestAttribute("user") User user) {
        // 检查用户是否有打卡查询权限
        if (!permissionService.hasPermission(user, "checkin:read")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "权限不足：没有打卡查询权限");
            return errorResponse;
        }

        boolean hasCheckedInToday = checkinService.hasUserCheckedInTopicToday(user, topicId);

        Map<String, Object> response = new HashMap<>();
        response.put("hasCheckedInToday", hasCheckedInToday);

        return response;
    }

    /**
     * 主题打卡
     * 
     * @param topicId 主题ID
     * @param request 请求参数，包含title（打卡标题）和content（打卡内容）
     * @param user 当前登录用户信息
     * @return 包含打卡记录的响应
     * @throws RuntimeException 权限不足、参数错误、主题无效或已打卡时抛出
     */
    @PostMapping("/topics/{topicId}/checkin")
    public Map<String, Object> checkinTopic(@PathVariable Long topicId, @RequestBody Map<String, String> request, @RequestAttribute("user") User user) {
        // 检查用户是否有打卡创建权限
        if (!permissionService.hasPermission(user, "checkin:create")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "权限不足：没有打卡创建权限");
            return errorResponse;
        }

        String title = request.get("title");
        String content = request.get("content");

        if (title == null || title.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "打卡标题不能为空");
            return errorResponse;
        }

        CheckinTopicRecord topicRecord = checkinService.checkinTopic(user, topicId, title, content);

        if (topicRecord == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            if (!checkinService.isTopicValid(topicId)) {
                errorResponse.put("error", "主题无效或已过期");
            } else {
                errorResponse.put("error", "今天已经对该主题打了卡");
            }
            return errorResponse;
        }

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> recordMap = new HashMap<>();
        recordMap.put("id", topicRecord.getId());
        recordMap.put("topicId", topicRecord.getTopicId());
        recordMap.put("checkinDate", topicRecord.getCheckinDate());
        recordMap.put("checkinDatetime", checkinService.formatDateTime(topicRecord.getCheckinDatetime()));
        recordMap.put("consecutiveDays", topicRecord.getConsecutiveDays());
        recordMap.put("checkinCount", checkinService.getTopicCheckinCount(topicId));
        response.put("record", recordMap);

        return response;
    }

    /**
     * 获取主题打卡记录
     * 
     * @param topicId 主题ID
     * @param page 当前页码，默认1
     * @param pageSize 每页大小，默认20
     * @param user 当前登录用户信息
     * @return 包含打卡记录列表和分页信息的响应
     * @throws RuntimeException 权限不足时抛出
     */
    @GetMapping("/topics/{topicId}/checkin-records")
    public Map<String, Object> getTopicCheckinRecords(
            @PathVariable Long topicId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestAttribute("user") User user) {
        // 检查用户是否有打卡查询权限
        if (!permissionService.hasPermission(user, "checkin:read")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "权限不足：没有打卡查询权限");
            return errorResponse;
        }

        Pageable pageable = PageRequest.of(page - 1, pageSize);
        // 获取主题的所有打卡记录
        Page<CheckinTopicRecord> recordPage = checkinService.getTopicCheckinRecords(topicId, pageable);

        List<Map<String, Object>> records = recordPage.getContent().stream().map(record -> {
            Map<String, Object> recordMap = new HashMap<>();
            recordMap.put("id", record.getId());
            recordMap.put("userId", record.getUserId());
            
            // 获取用户昵称
            String nickname = checkinService.getUserNickname(record.getUserId());
            // 如果用户允许展示信息且有昵称，则显示昵称，否则显示"微信用户"
            recordMap.put("nickname", nickname != null && !nickname.isEmpty() ? nickname : "微信用户");
            
            recordMap.put("checkinDate", record.getCheckinDate());
            recordMap.put("checkinDatetime", checkinService.formatDateTime(record.getCheckinDatetime()));
            recordMap.put("consecutiveDays", record.getConsecutiveDays());
            return recordMap;
        }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("records", records);
        response.put("pagination", Map.of(
                "total", recordPage.getTotalElements(),
                "totalPages", recordPage.getTotalPages(),
                "currentPage", page,
                "pageSize", pageSize
        ));

        return response;
    }

    /**
     * 获取周报数据
     * 
     * @param topicId 主题ID
     * @param user 当前登录用户信息
     * @return 包含周报数据的响应
     * @throws RuntimeException 权限不足或主题不存在时抛出
     */
    @GetMapping("/topics/{topicId}/weekly-report")
    public Map<String, Object> getWeeklyReport(
            @PathVariable Long topicId,
            @RequestAttribute("user") User user) {
        // 检查用户是否有管理员权限或是否是主题发布者
        CheckinTopic topic = checkinService.getTopicById(topicId);
        if (topic == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "主题不存在");
            return errorResponse;
        }

        boolean isAdmin = permissionService.hasRole(user, "ADMIN");
        boolean isTopicCreator = topic.getCreatedBy() != null && topic.getCreatedBy().equals(user.getId());
        
        if (!isAdmin && !isTopicCreator) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "权限不足：只有管理员或主题发布者才能查看报告");
            return errorResponse;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("report", checkinService.getWeeklyReport(topicId));
        return response;
    }

    /**
     * 获取月报数据
     * 
     * @param topicId 主题ID
     * @param user 当前登录用户信息
     * @return 包含月报数据的响应
     * @throws RuntimeException 权限不足或主题不存在时抛出
     */
    @GetMapping("/topics/{topicId}/monthly-report")
    public Map<String, Object> getMonthlyReport(
            @PathVariable Long topicId,
            @RequestAttribute("user") User user) {
        // 检查用户是否有管理员权限或是否是主题发布者
        CheckinTopic topic = checkinService.getTopicById(topicId);
        if (topic == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "主题不存在");
            return errorResponse;
        }

        boolean isAdmin = permissionService.hasRole(user, "ADMIN");
        boolean isTopicCreator = topic.getCreatedBy() != null && topic.getCreatedBy().equals(user.getId());
        
        if (!isAdmin && !isTopicCreator) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "权限不足：只有管理员或主题发布者才能查看报告");
            return errorResponse;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("report", checkinService.getMonthlyReport(topicId));
        return response;
    }
}
