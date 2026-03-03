package com.checkin.controller;

import com.checkin.entity.CheckinRecord;
import com.checkin.entity.CheckinStatistics;
import com.checkin.entity.CheckinTopic;
import com.checkin.entity.CheckinTopicRecord;
import com.checkin.entity.User;
import com.checkin.repository.UserRepository;
import com.checkin.service.CheckinService;
import com.checkin.service.PermissionService;
import com.checkin.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkin")
public class CheckinController {

    @Autowired
    private CheckinService checkinService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PermissionService permissionService;

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

    // 主题相关接口
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
        // 只获取当前用户在主题中的打卡记录
        Page<CheckinTopicRecord> recordPage = checkinService.getUserTopicCheckinRecords(user.getId(), topicId, pageable);

        List<Map<String, Object>> records = recordPage.getContent().stream().map(record -> {
            Map<String, Object> recordMap = new HashMap<>();
            recordMap.put("id", record.getId());
            recordMap.put("userId", record.getUserId());
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
}
