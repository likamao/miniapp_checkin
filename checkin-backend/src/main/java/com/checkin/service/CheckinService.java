package com.checkin.service;

import com.checkin.entity.CheckinRecord;
import com.checkin.entity.CheckinStatistics;
import com.checkin.entity.CheckinTopic;
import com.checkin.entity.CheckinTopicRecord;
import com.checkin.entity.User;
import com.checkin.repository.CheckinRecordRepository;
import com.checkin.repository.CheckinStatisticsRepository;
import com.checkin.repository.CheckinTopicRecordRepository;
import com.checkin.repository.CheckinTopicRepository;
import com.checkin.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.OptionalInt;

@Service
public class CheckinService {

    @Autowired
    private CheckinRecordRepository checkinRecordRepository;

    @Autowired
    private CheckinStatisticsRepository checkinStatisticsRepository;

    @Autowired
    private CheckinTopicRepository checkinTopicRepository;

    @Autowired
    private CheckinTopicRecordRepository checkinTopicRecordRepository;

    @Autowired
    private UserRepository userRepository;



    /**
     * 创建打卡记录
     * 
     * @param user 用户信息
     * @param title 打卡标题
     * @param content 打卡内容
     * @return 创建的打卡记录
     */
    public CheckinRecord createCheckinRecord(User user, String title, String content) {
        CheckinRecord record = new CheckinRecord();
        record.setUser(user);
        record.setTitle(title);
        record.setContent(content);
        record.setCheckinTime(new Date());
        record.setCreatedAt(new Date());
        record.setUpdatedAt(new Date());

        CheckinRecord savedRecord = checkinRecordRepository.save(record);

        // 更新统计数据
        updateStatistics(user, record.getCheckinTime());

        return savedRecord;
    }

    /**
     * 获取用户的所有打卡记录
     * 
     * @param user 用户信息
     * @return 打卡记录列表
     */
    public List<CheckinRecord> getCheckinRecords(User user) {
        return checkinRecordRepository.findByUser(user);
    }

    /**
     * 获取指定日期范围内的打卡记录
     * 
     * @param user 用户信息
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 打卡记录列表
     */
    public List<CheckinRecord> getCheckinRecordsByDateRange(User user, Date startDate, Date endDate) {
        return checkinRecordRepository.findByUserAndCheckinTimeBetween(user, startDate, endDate);
    }

    /**
     * 更新统计数据
     * 
     * @param user 用户信息
     * @param checkinTime 打卡时间
     */
    private void updateStatistics(User user, Date checkinTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(checkinTime);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int week = calendar.get(Calendar.WEEK_OF_MONTH);

        // 更新周统计
        CheckinStatistics weekStat = checkinStatisticsRepository.findByUserAndYearAndMonthAndWeek(user, year, month, week);
        if (weekStat == null) {
            weekStat = new CheckinStatistics();
            weekStat.setUser(user);
            weekStat.setYear(year);
            weekStat.setMonth(month);
            weekStat.setWeek(week);
            weekStat.setCheckinCount(1);
            weekStat.setCreatedAt(new Date());
            weekStat.setUpdatedAt(new Date());
        } else {
            weekStat.setCheckinCount(weekStat.getCheckinCount() + 1);
            weekStat.setUpdatedAt(new Date());
        }
        checkinStatisticsRepository.save(weekStat);

        // 更新月统计
        List<CheckinStatistics> monthStats = checkinStatisticsRepository.findByUserAndYearAndMonth(user, year, month);
        int totalCount = monthStats.stream().mapToInt(CheckinStatistics::getCheckinCount).sum();

        // 这里可以添加月统计的逻辑，根据实际需求调整
    }

    /**
     * 获取用户的周统计数据
     * 
     * @param user 用户信息
     * @param year 年份
     * @param month 月份
     * @return 统计数据列表
     */
    public List<CheckinStatistics> getWeeklyStatistics(User user, int year, int month) {
        return checkinStatisticsRepository.findByUserAndYearAndMonth(user, year, month);
    }

    /**
     * 获取最近7天的打卡次数
     * 
     * @param user 用户信息
     * @return 打卡次数
     */
    public long getRecent7DaysCheckinCount(User user) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date startDate = calendar.getTime();
        Date endDate = new Date();
        
        List<CheckinRecord> records = checkinRecordRepository.findByUserAndCheckinTimeBetween(user, startDate, endDate);
        return records.size();
    }

    /**
     * 检查用户今日是否已打卡
     * 
     * @param user 用户信息
     * @return 是否已打卡
     */
    public boolean hasCheckedInToday(User user) {
        Calendar calendar = Calendar.getInstance();
        // 设置为今天的开始时间
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startOfDay = calendar.getTime();
        
        // 设置为今天的结束时间
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        Date endOfDay = calendar.getTime();
        
        List<CheckinRecord> records = checkinRecordRepository.findByUserAndCheckinTimeBetween(user, startOfDay, endOfDay);
        return !records.isEmpty();
    }

    /**
     * 分页获取用户的打卡记录
     * 
     * @param user 用户信息
     * @param pageable 分页参数
     * @return 分页的打卡记录
     */
    public Page<CheckinRecord> getCheckinRecords(User user, Pageable pageable) {
        return checkinRecordRepository.findByUser(user, pageable);
    }

    /**
     * 分页获取指定年份和月份的打卡记录
     * 
     * @param user 用户信息
     * @param year 年份
     * @param month 月份，可为null（表示按年查询）
     * @param pageable 分页参数
     * @return 分页的打卡记录
     */
    public Page<CheckinRecord> getCheckinRecordsByYearAndMonth(User user, int year, Integer month, Pageable pageable) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        
        Date startDate, endDate;
        
        if (month != null) {
            // 按月查询
            calendar.set(Calendar.MONTH, month - 1); // Calendar.MONTH 从0开始
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            startDate = calendar.getTime();
            
            // 下个月的第一天
            calendar.add(Calendar.MONTH, 1);
            endDate = calendar.getTime();
        } else {
            // 按年查询
            calendar.set(Calendar.MONTH, 0); // 1月
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            startDate = calendar.getTime();
            
            // 下一年的第一天
            calendar.add(Calendar.YEAR, 1);
            endDate = calendar.getTime();
        }
        
        return checkinRecordRepository.findByUserAndCheckinTimeBetween(user, startDate, endDate, pageable);
    }

    /**
     * 格式化日期时间
     * 
     * @param date 日期对象
     * @return 格式化后的日期时间字符串
     */
    public String formatDateTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    /**
     * 获取所有活跃的主题
     * 
     * @return 活跃主题列表
     */
    public List<CheckinTopic> getActiveTopics() {
        return checkinTopicRepository.findActiveTopics(new Date());
    }

    /**
     * 获取所有主题
     * 
     * @return 主题列表
     */
    public List<CheckinTopic> getAllTopics() {
        return checkinTopicRepository.findAllOrderByCreatedAtDesc();
    }

    /**
     * 根据ID获取主题
     * 
     * @param topicId 主题ID
     * @return 主题信息，不存在则返回null
     */
    public CheckinTopic getTopicById(Long topicId) {
        return checkinTopicRepository.findById(topicId).orElse(null);
    }

    /**
     * 获取主题的打卡用户数量
     * 
     * @param topicId 主题ID
     * @return 打卡用户数量
     */
    public long getTopicCheckinCount(Long topicId) {
        return checkinTopicRecordRepository.countUniqueUsersByTopicId(topicId);
    }

    /**
     * 创建主题
     * 
     * @param title 主题标题
     * @param description 主题描述
     * @param durationDays 持续天数，可为null（默认7天）
     * @param creator 创建者信息
     * @return 创建的主题
     */
    public CheckinTopic createTopic(String title, String description, Integer durationDays, User creator) {
        CheckinTopic topic = new CheckinTopic();
        topic.setTitle(title);
        topic.setDescription(description);
        
        if (durationDays == null) {
            durationDays = 7; // 默认7天
        }
        topic.setDurationDays(durationDays);
        topic.setStatus(1);
        topic.setCreatedBy(creator.getId());
        
        // 设置主题的开始和结束时间
        Calendar calendar = Calendar.getInstance();
        Date startDatetime = calendar.getTime();
        topic.setStartDatetime(startDatetime);
        
        calendar.add(Calendar.DAY_OF_YEAR, durationDays);
        topic.setEndDatetime(calendar.getTime());
        
        topic.setCreatedAt(new Date());
        topic.setUpdatedAt(new Date());
        
        return checkinTopicRepository.save(topic);
    }

    /**
     * 更新主题
     * 
     * @param topicId 主题ID
     * @param title 主题标题
     * @param description 主题描述
     * @param durationDays 持续天数，可为null
     * @param user 操作用户信息
     * @return 更新后的主题，主题不存在则返回null
     */
    public CheckinTopic updateTopic(Long topicId, String title, String description, Integer durationDays, User user) {
        CheckinTopic topic = getTopicById(topicId);
        if (topic == null) {
            return null;
        }
        
        topic.setTitle(title);
        topic.setDescription(description);
        
        if (durationDays != null && !durationDays.equals(topic.getDurationDays())) {
            topic.setDurationDays(durationDays);
            // 如果有效期变更，重新计算结束时间
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(topic.getStartDatetime());
            calendar.add(Calendar.DAY_OF_YEAR, durationDays);
            topic.setEndDatetime(calendar.getTime());
        }
        
        topic.setUpdatedAt(new Date());
        
        return checkinTopicRepository.save(topic);
    }

    /**
     * 检查主题是否有效
     * 
     * @param topicId 主题ID
     * @return 主题是否有效
     */
    public boolean isTopicValid(Long topicId) {
        CheckinTopic topic = getTopicById(topicId);
        if (topic == null) {
            return false;
        }
        if (topic.getStatus() == 0) {
            return false;
        }
        Date now = new Date();
        return now.after(topic.getStartDatetime()) && now.before(topic.getEndDatetime());
    }

    /**
     * 检查用户今天是否已打卡该主题
     * 
     * @param user 用户信息
     * @param topicId 主题ID
     * @return 是否已打卡
     */
    public boolean hasUserCheckedInTopicToday(User user, Long topicId) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new Date());
        
        return checkinTopicRecordRepository.findByUserIdAndTopicIdAndCheckinDate(
                user.getId(), topicId, today).isPresent();
    }

    /**
     * 主题打卡
     * 
     * @param user 用户信息
     * @param topicId 主题ID
     * @param title 打卡标题
     * @param content 打卡内容
     * @return 主题打卡记录，主题无效或已打卡则返回null
     */
    public CheckinTopicRecord checkinTopic(User user, Long topicId, String title, String content) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat datetimeSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String today = sdf.format(new Date());
        Date now = new Date();
        
        // 检查主题是否有效
        if (!isTopicValid(topicId)) {
            return null;
        }
        
        // 检查今天是否已打卡
        if (hasUserCheckedInTopicToday(user, topicId)) {
            return null;
        }
        
        // 计算连续打卡天数
        int consecutiveDays = calculateConsecutiveDays(user.getId(), topicId, today);
        
        // 创建打卡记录
        CheckinRecord checkinRecord = createCheckinRecord(user, title, content);
        
        // 创建主题打卡记录
        CheckinTopicRecord topicRecord = new CheckinTopicRecord();
        topicRecord.setUserId(user.getId());
        topicRecord.setTopicId(topicId);
        topicRecord.setCheckinRecordId(checkinRecord.getId());
        topicRecord.setCheckinDate(today);
        topicRecord.setCheckinDatetime(now);
        topicRecord.setConsecutiveDays(consecutiveDays);
        topicRecord.setCreatedAt(now);
        topicRecord.setUpdatedAt(now);
        
        return checkinTopicRecordRepository.save(topicRecord);
    }

    /**
     * 计算连续打卡天数
     * 
     * @param userId 用户ID
     * @param topicId 主题ID
     * @param today 今天的日期字符串（yyyy-MM-dd）
     * @return 连续打卡天数
     */
    private int calculateConsecutiveDays(Long userId, Long topicId, String today) {
        List<CheckinTopicRecord> records = checkinTopicRecordRepository.findByUserIdAndTopicIdOrderByCheckinDatetimeDesc(userId, topicId);
        
        if (records.isEmpty()) {
            return 1;
        }
        
        // 检查昨天是否有打卡
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(sdf.parse(today));
        } catch (Exception e) {
            return 1;
        }
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        String yesterday = sdf.format(calendar.getTime());
        
        CheckinTopicRecord lastRecord = records.get(0);
        if (yesterday.equals(lastRecord.getCheckinDate())) {
            return lastRecord.getConsecutiveDays() + 1;
        } else {
            return 1;
        }
    }

    /**
     * 获取主题的打卡记录
     * 
     * @param topicId 主题ID
     * @param pageable 分页参数
     * @return 分页的打卡记录
     */
    public Page<CheckinTopicRecord> getTopicCheckinRecords(Long topicId, Pageable pageable) {
        return checkinTopicRecordRepository.findByTopicId(topicId, pageable);
    }

    /**
     * 获取当前用户在主题中的打卡记录
     * 
     * @param userId 用户ID
     * @param topicId 主题ID
     * @param pageable 分页参数
     * @return 分页的打卡记录
     */
    public Page<CheckinTopicRecord> getUserTopicCheckinRecords(Long userId, Long topicId, Pageable pageable) {
        return checkinTopicRecordRepository.findByUserIdAndTopicId(userId, topicId, pageable);
    }

    /**
     * 获取用户昵称
     * 
     * @param userId 用户ID
     * @return 用户昵称，如果用户不存在则返回null
     */
    public String getUserNickname(Long userId) {
        return userRepository.findById(userId)
                .map(User::getNickname)
                .orElse(null);
    }

    /**
     * 获取用户在主题中的最高连续打卡天数
     * 
     * @param userId 用户ID
     * @param topicId 主题ID
     * @return 最高连续打卡天数
     */
    public int getUserMaxConsecutiveDays(Long userId, Long topicId) {
        return checkinTopicRecordRepository.findMaxConsecutiveDaysByUserIdAndTopicId(userId, topicId).orElse(0);
    }

    /**
     * 检查并更新主题状态
     * 
     * @param topicId 主题ID
     */
    public void checkAndUpdateTopicStatus(Long topicId) {
        CheckinTopic topic = getTopicById(topicId);
        if (topic == null) {
            return;
        }
        
        Date now = new Date();
        if (now.after(topic.getEndDatetime()) && topic.getStatus() == 1) {
            topic.setStatus(0);
            topic.setUpdatedAt(new Date());
            checkinTopicRepository.save(topic);
        }
    }

    /**
     * 获取主题剩余有效期
     * 
     * @param topicId 主题ID
     * @return 包含剩余天数和小时数的Map，主题不存在则返回null
     */
    public Map<String, Long> getTopicRemainingTime(Long topicId) {
        CheckinTopic topic = getTopicById(topicId);
        if (topic == null) {
            return null;
        }
        
        Date now = new Date();
        long diff = topic.getEndDatetime().getTime() - now.getTime();
        
        Map<String, Long> result = new HashMap<>();
        if (diff <= 0) {
            result.put("days", 0L);
            result.put("hours", 0L);
        } else {
            long days = diff / (1000 * 60 * 60 * 24);
            long hours = (diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
            result.put("days", days);
            result.put("hours", hours);
        }
        
        return result;
    }

    /**
     * 获取周报数据
     * 
     * @param topicId 主题ID
     * @return 周报数据
     */
    public Map<String, Object> getWeeklyReport(Long topicId) {
        Map<String, Object> report = new HashMap<>();
        
        // 计算本周时间范围（周一至周日）
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int daysToMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - 2;
        calendar.add(Calendar.DAY_OF_YEAR, -daysToMonday);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startDate = calendar.getTime();
        
        calendar.add(Calendar.DAY_OF_YEAR, 6);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        Date endDate = calendar.getTime();
        
        // 获取主题信息
        CheckinTopic topic = getTopicById(topicId);
        if (topic == null) {
            report.put("error", "主题不存在");
            return report;
        }
        
        // 获取本周打卡记录
        List<CheckinTopicRecord> records = checkinTopicRecordRepository.findByTopicIdAndCheckinDatetimeBetween(topicId, startDate, endDate);
        
        // 计算关键指标
        Map<String, Object> metrics = calculateMetrics(records, startDate, endDate);
        report.put("metrics", metrics);
        
        // 获取用户打卡详情
        Map<String, Object> userDetails = getUserCheckinDetails(records);
        report.put("userDetails", userDetails);
        
        // 生成热力日历数据
        List<Map<String, Object>> heatmapData = generateHeatmapData(records, startDate, endDate);
        report.put("heatmapData", heatmapData);
        
        // 生成趋势图数据
        Map<String, Object> trendData = generateTrendData(records, startDate, endDate);
        report.put("trendData", trendData);
        
        // 添加时间范围信息
        report.put("startDate", formatDateTime(startDate));
        report.put("endDate", formatDateTime(endDate));
        report.put("topicName", topic.getTitle());
        
        return report;
    }
    
    /**
     * 计算关键业务指标
     * 
     * @param records 打卡记录列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 关键指标Map
     */
    private Map<String, Object> calculateMetrics(List<CheckinTopicRecord> records, Date startDate, Date endDate) {
        Map<String, Object> metrics = new HashMap<>();
        
        // 计算总打卡次数
        metrics.put("totalCheckinCount", records.size());
        
        // 计算参与用户数
        Set<Long> userIds = records.stream().map(CheckinTopicRecord::getUserId).collect(Collectors.toSet());
        metrics.put("participantCount", userIds.size());
        
        // 计算平均打卡次数
        if (!userIds.isEmpty()) {
            double avgCheckinCount = (double) records.size() / userIds.size();
            metrics.put("averageCheckinCount", avgCheckinCount);
        } else {
            metrics.put("averageCheckinCount", 0);
        }
        
        // 计算用户活跃度（打卡用户数 / 总用户数，这里简化处理）
        // 实际项目中需要从用户表获取总用户数
        metrics.put("userActivityRate", userIds.isEmpty() ? 0 : 1.0);
        
        // 计算连续打卡天数分布
        Map<Integer, Long> consecutiveDaysDistribution = records.stream()
                .collect(Collectors.groupingBy(CheckinTopicRecord::getConsecutiveDays, Collectors.counting()));
        metrics.put("consecutiveDaysDistribution", consecutiveDaysDistribution);
        
        // 计算最高连续打卡天数
        OptionalInt maxConsecutiveDays = records.stream()
                .mapToInt(CheckinTopicRecord::getConsecutiveDays)
                .max();
        metrics.put("maxConsecutiveDays", maxConsecutiveDays.orElse(0));
        
        return metrics;
    }
    
    /**
     * 获取用户打卡详情
     * 
     * @param records 打卡记录列表
     * @return 用户打卡详情Map
     */
    private Map<String, Object> getUserCheckinDetails(List<CheckinTopicRecord> records) {
        Map<Long, List<CheckinTopicRecord>> userRecords = records.stream()
                .collect(Collectors.groupingBy(CheckinTopicRecord::getUserId));
        
        List<Map<String, Object>> userDetails = new ArrayList<>();
        
        userRecords.forEach((userId, userRecordList) -> {
            Map<String, Object> userDetail = new HashMap<>();
            userDetail.put("userId", userId);
            userDetail.put("checkinCount", userRecordList.size());
            
            // 获取用户信息，添加隐私设置和昵称
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                userDetail.put("allowStatsDisplay", user.getAllowStatsDisplay());
                // 添加用户昵称，确保数据一致性
                String nickname = user.getNickname();
                userDetail.put("nickname", nickname != null && !nickname.trim().isEmpty() ? nickname : "用户" + userId);
                userDetail.put("userId", userId); // 保留userId用于内部使用
            } else {
                userDetail.put("allowStatsDisplay", true); // 默认允许显示
                userDetail.put("nickname", "用户" + userId);
            }
            
            // 计算用户最高连续打卡天数
            OptionalInt maxConsecutiveDays = userRecordList.stream()
                    .mapToInt(CheckinTopicRecord::getConsecutiveDays)
                    .max();
            userDetail.put("maxConsecutiveDays", maxConsecutiveDays.orElse(0));
            
            // 获取最后打卡时间
            Optional<Date> lastCheckinTime = userRecordList.stream()
                    .map(CheckinTopicRecord::getCheckinDatetime)
                    .max(Date::compareTo);
            lastCheckinTime.ifPresent(time -> userDetail.put("lastCheckinTime", formatDateTime(time)));
            
            // 计算打卡日期列表
            List<String> checkinDates = userRecordList.stream()
                    .map(CheckinTopicRecord::getCheckinDate)
                    .collect(Collectors.toList());
            userDetail.put("checkinDates", checkinDates);
            
            // 获取详细的打卡记录信息
            List<Map<String, Object>> checkinDetails = new ArrayList<>();
            for (CheckinTopicRecord topicRecord : userRecordList) {
                Map<String, Object> checkinDetail = new HashMap<>();
                // 获取对应的CheckinRecord
                Optional<CheckinRecord> checkinRecordOpt = checkinRecordRepository.findById(topicRecord.getCheckinRecordId());
                if (checkinRecordOpt.isPresent()) {
                    CheckinRecord checkinRecord = checkinRecordOpt.get();
                    checkinDetail.put("title", checkinRecord.getTitle());
                    checkinDetail.put("content", checkinRecord.getContent());
                    checkinDetail.put("checkinTime", formatDateTime(checkinRecord.getCheckinTime()));
                    checkinDetail.put("checkinDate", topicRecord.getCheckinDate());
                }
                checkinDetails.add(checkinDetail);
            }
            userDetail.put("checkinDetails", checkinDetails);
            
            userDetails.add(userDetail);
        });
        
        // 按打卡次数排序
        userDetails.sort((a, b) -> {
            int countA = (int) a.get("checkinCount");
            int countB = (int) b.get("checkinCount");
            return Integer.compare(countB, countA);
        });
        
        Map<String, Object> result = new HashMap<>();
        result.put("users", userDetails);
        result.put("totalUsers", userDetails.size());
        
        return result;
    }
    
    /**
     * 生成热力日历数据
     * 
     * @param records 打卡记录列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 热力日历数据
     */
    private List<Map<String, Object>> generateHeatmapData(List<CheckinTopicRecord> records, Date startDate, Date endDate) {
        List<Map<String, Object>> heatmapData = new ArrayList<>();
        
        // 按日期分组统计打卡次数
        Map<String, Long> dateCheckinCount = records.stream()
                .collect(Collectors.groupingBy(CheckinTopicRecord::getCheckinDate, Collectors.counting()));
        
        // 生成日期范围内的所有日期
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        
        while (!calendar.getTime().after(endDate)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String dateStr = sdf.format(calendar.getTime());
            
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", dateStr);
            dayData.put("checkinCount", dateCheckinCount.getOrDefault(dateStr, 0L));
            dayData.put("dayOfWeek", calendar.get(Calendar.DAY_OF_WEEK));
            
            heatmapData.add(dayData);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        
        return heatmapData;
    }
    
    /**
     * 生成趋势图数据
     * 
     * @param records 打卡记录列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 趋势图数据
     */
    private Map<String, Object> generateTrendData(List<CheckinTopicRecord> records, Date startDate, Date endDate) {
        Map<String, Object> trendData = new HashMap<>();
        
        // 按日期分组统计打卡次数
        Map<String, Long> dateCheckinCount = records.stream()
                .collect(Collectors.groupingBy(CheckinTopicRecord::getCheckinDate, Collectors.counting()));
        
        // 生成日期和打卡次数列表
        List<String> dates = new ArrayList<>();
        List<Long> checkinCounts = new ArrayList<>();
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        
        while (!calendar.getTime().after(endDate)) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
            String dateStr = sdf.format(calendar.getTime());
            String fullDateStr = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
            
            dates.add(dateStr);
            checkinCounts.add(dateCheckinCount.getOrDefault(fullDateStr, 0L));
            
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        
        trendData.put("dates", dates);
        trendData.put("checkinCounts", checkinCounts);
        
        return trendData;
    }

    /**
     * 获取月报数据
     * 
     * @param topicId 主题ID
     * @return 月报数据
     */
    public Map<String, Object> getMonthlyReport(Long topicId) {
        Map<String, Object> report = new HashMap<>();
        
        // 计算本月时间范围
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startDate = calendar.getTime();
        
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        Date endDate = calendar.getTime();
        
        // 获取主题信息
        CheckinTopic topic = getTopicById(topicId);
        if (topic == null) {
            report.put("error", "主题不存在");
            return report;
        }
        
        // 获取本月打卡记录
        List<CheckinTopicRecord> records = checkinTopicRecordRepository.findByTopicIdAndCheckinDatetimeBetween(topicId, startDate, endDate);
        
        // 计算关键指标
        Map<String, Object> metrics = calculateMetrics(records, startDate, endDate);
        report.put("metrics", metrics);
        
        // 获取用户打卡详情
        Map<String, Object> userDetails = getUserCheckinDetails(records);
        report.put("userDetails", userDetails);
        
        // 生成热力日历数据
        List<Map<String, Object>> heatmapData = generateHeatmapData(records, startDate, endDate);
        report.put("heatmapData", heatmapData);
        
        // 生成趋势图数据
        Map<String, Object> trendData = generateTrendData(records, startDate, endDate);
        report.put("trendData", trendData);
        
        // 按周统计数据
        Map<String, Object> weeklyStats = generateWeeklyStats(records, startDate, endDate);
        report.put("weeklyStats", weeklyStats);
        
        // 添加时间范围信息
        report.put("startDate", formatDateTime(startDate));
        report.put("endDate", formatDateTime(endDate));
        report.put("topicName", topic.getTitle());
        
        return report;
    }
    
    /**
     * 按周统计数据
     * 
     * @param records 打卡记录列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 按周统计数据
     */
    private Map<String, Object> generateWeeklyStats(List<CheckinTopicRecord> records, Date startDate, Date endDate) {
        Map<String, Object> weeklyStats = new HashMap<>();
        List<Map<String, Object>> weeks = new ArrayList<>();
        
        // 按周分组
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        
        while (!calendar.getTime().after(endDate)) {
            // 计算本周开始（周一）
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            int daysToMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - 2;
            Calendar weekStart = (Calendar) calendar.clone();
            weekStart.add(Calendar.DAY_OF_YEAR, -daysToMonday);
            weekStart.set(Calendar.HOUR_OF_DAY, 0);
            weekStart.set(Calendar.MINUTE, 0);
            weekStart.set(Calendar.SECOND, 0);
            weekStart.set(Calendar.MILLISECOND, 0);
            
            // 计算本周结束（周日）
            Calendar weekEnd = (Calendar) weekStart.clone();
            weekEnd.add(Calendar.DAY_OF_YEAR, 6);
            weekEnd.set(Calendar.HOUR_OF_DAY, 23);
            weekEnd.set(Calendar.MINUTE, 59);
            weekEnd.set(Calendar.SECOND, 59);
            weekEnd.set(Calendar.MILLISECOND, 999);
            
            // 过滤本周的记录
            List<CheckinTopicRecord> weekRecords = records.stream()
                    .filter(record -> record.getCheckinDatetime().after(weekStart.getTime()) && 
                                     record.getCheckinDatetime().before(weekEnd.getTime()))
                    .collect(Collectors.toList());
            
            // 计算本周指标
            Map<String, Object> weekData = new HashMap<>();
            weekData.put("weekStart", formatDateTime(weekStart.getTime()));
            weekData.put("weekEnd", formatDateTime(weekEnd.getTime()));
            weekData.put("checkinCount", weekRecords.size());
            
            // 计算本周参与用户数
            Set<Long> weekUserIds = weekRecords.stream()
                    .map(CheckinTopicRecord::getUserId)
                    .collect(Collectors.toSet());
            weekData.put("participantCount", weekUserIds.size());
            
            weeks.add(weekData);
            
            // 移动到下一周
            calendar.add(Calendar.DAY_OF_YEAR, 7);
        }
        
        weeklyStats.put("weeks", weeks);
        return weeklyStats;
    }
}
