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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<CheckinRecord> getCheckinRecords(User user) {
        return checkinRecordRepository.findByUser(user);
    }

    public List<CheckinRecord> getCheckinRecordsByDateRange(User user, Date startDate, Date endDate) {
        return checkinRecordRepository.findByUserAndCheckinTimeBetween(user, startDate, endDate);
    }

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

    public List<CheckinStatistics> getWeeklyStatistics(User user, int year, int month) {
        return checkinStatisticsRepository.findByUserAndYearAndMonth(user, year, month);
    }

    public long getRecent7DaysCheckinCount(User user) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date startDate = calendar.getTime();
        Date endDate = new Date();
        
        List<CheckinRecord> records = checkinRecordRepository.findByUserAndCheckinTimeBetween(user, startDate, endDate);
        return records.size();
    }

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

    public Page<CheckinRecord> getCheckinRecords(User user, Pageable pageable) {
        return checkinRecordRepository.findByUser(user, pageable);
    }

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

    public String formatDateTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    // 主题相关方法
    public List<CheckinTopic> getActiveTopics() {
        return checkinTopicRepository.findActiveTopics(new Date());
    }

    public List<CheckinTopic> getAllTopics() {
        return checkinTopicRepository.findAllOrderByCreatedAtDesc();
    }

    public CheckinTopic getTopicById(Long topicId) {
        return checkinTopicRepository.findById(topicId).orElse(null);
    }

    public long getTopicCheckinCount(Long topicId) {
        return checkinTopicRecordRepository.countUniqueUsersByTopicId(topicId);
    }

    // 创建主题
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

    // 更新主题
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

    // 检查主题是否有效
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

    // 检查用户今天是否已打卡该主题
    public boolean hasUserCheckedInTopicToday(User user, Long topicId) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new Date());
        
        return checkinTopicRecordRepository.findByUserIdAndTopicIdAndCheckinDate(
                user.getId(), topicId, today).isPresent();
    }

    // 主题打卡
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

    // 计算连续打卡天数
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

    // 获取主题的打卡记录
    public Page<CheckinTopicRecord> getTopicCheckinRecords(Long topicId, Pageable pageable) {
        return checkinTopicRecordRepository.findByTopicId(topicId, pageable);
    }

    // 获取用户在主题中的最高连续打卡天数
    public int getUserMaxConsecutiveDays(Long userId, Long topicId) {
        return checkinTopicRecordRepository.findMaxConsecutiveDaysByUserIdAndTopicId(userId, topicId).orElse(0);
    }

    // 检查并更新主题状态
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

    // 获取主题剩余有效期
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
}
