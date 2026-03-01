package com.checkin.controller;

import com.checkin.entity.CheckinRecord;
import com.checkin.entity.CheckinStatistics;
import com.checkin.entity.User;
import com.checkin.repository.UserRepository;
import com.checkin.service.CheckinService;
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

    @PostMapping("/create")
    public Map<String, Object> createCheckin(@RequestBody Map<String, String> request, @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "User not found");
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
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "User not found");
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
    public Map<String, Object> getCheckinStatistics(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "User not found");
            return errorResponse;
        }

        long recent7DaysCount = checkinService.getRecent7DaysCheckinCount(user);

        Map<String, Object> response = new HashMap<>();
        response.put("recent7DaysCount", recent7DaysCount);

        return response;
    }

    @GetMapping("/check-today")
    public Map<String, Object> checkTodayCheckin(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "User not found");
            return errorResponse;
        }

        boolean hasCheckedInToday = checkinService.hasCheckedInToday(user);

        Map<String, Object> response = new HashMap<>();
        response.put("hasCheckedInToday", hasCheckedInToday);

        return response;
    }
}