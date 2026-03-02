package com.checkin.controller;

import com.checkin.entity.CheckinRecord;
import com.checkin.entity.CheckinStatistics;
import com.checkin.entity.CheckinTopic;
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
            topicMap.put("startDate", checkinService.formatDateTime(topic.getStartDate()));
            topicMap.put("endDate", checkinService.formatDateTime(topic.getEndDate()));
            return topicMap;
        }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("topics", topics);

        return response;
    }

    @PostMapping("/create-with-topic")
    public Map<String, Object> createCheckinWithTopic(@RequestBody Map<String, Object> request, @RequestAttribute("user") User user) {
        // 检查用户是否有打卡创建权限
        if (!permissionService.hasPermission(user, "checkin:create")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "权限不足：没有打卡创建权限");
            return errorResponse;
        }

        String title = (String) request.get("title");
        String content = (String) request.get("content");
        Long topicId = request.get("topicId") != null ? Long.valueOf(request.get("topicId").toString()) : null;

        CheckinRecord record = checkinService.createCheckinRecordWithTopic(user, title, content, topicId);

        Map<String, Object> response = new HashMap<>();
        response.put("record", record);

        return response;
    }

    @GetMapping("/topic-checkin-count/{topicId}")
    public Map<String, Object> getTopicCheckinCount(@PathVariable Long topicId, @RequestAttribute("user") User user) {
        // 检查用户是否有打卡查询权限
        if (!permissionService.hasPermission(user, "checkin:read")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "权限不足：没有打卡查询权限");
            return errorResponse;
        }

        long count = checkinService.getTopicCheckinCount(topicId);

        Map<String, Object> response = new HashMap<>();
        response.put("count", count);

        return response;
    }

    @GetMapping("/check-topic/{topicId}")
    public Map<String, Object> checkTopicCheckin(@PathVariable Long topicId, @RequestAttribute("user") User user) {
        // 检查用户是否有打卡查询权限
        if (!permissionService.hasPermission(user, "checkin:read")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "权限不足：没有打卡查询权限");
            return errorResponse;
        }

        boolean hasCheckedInTopic = checkinService.hasUserCheckedInTopic(user, topicId);

        Map<String, Object> response = new HashMap<>();
        response.put("hasCheckedInTopic", hasCheckedInTopic);

        return response;
    }

    @PostMapping("/topics")
    public Map<String, Object> createTopic(@RequestBody Map<String, String> request, @RequestAttribute("user") User user) {
        // 检查用户是否有主题创建权限
        if (!permissionService.hasPermission(user, "checkin:create")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "权限不足：没有主题创建权限");
            return errorResponse;
        }

        String title = request.get("title");
        String description = request.get("description");

        if (title == null || title.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "主题标题不能为空");
            return errorResponse;
        }

        if (title.length() < 10) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "主题标题至少需要10个字符");
            return errorResponse;
        }

        CheckinTopic topic = checkinService.createTopic(title, description);

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> topicMap = new HashMap<>();
        topicMap.put("id", topic.getId());
        topicMap.put("title", topic.getTitle());
        topicMap.put("description", topic.getDescription());
        topicMap.put("startDate", checkinService.formatDateTime(topic.getStartDate()));
        topicMap.put("endDate", checkinService.formatDateTime(topic.getEndDate()));
        response.put("topic", topicMap);

        return response;
    }
}