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
import java.util.List;

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
    public CheckinRecord createCheckinRecordWithTopic(User user, String title, String content, Long topicId) {
        // 创建打卡记录
        CheckinRecord record = createCheckinRecord(user, title, content);
        
        // 关联主题
        if (topicId != null) {
            CheckinTopicRecord topicRecord = new CheckinTopicRecord();
            topicRecord.setUserId(user.getId());
            topicRecord.setTopicId(topicId);
            topicRecord.setCheckinRecordId(record.getId());
            topicRecord.setCreatedAt(new Date());
            topicRecord.setUpdatedAt(new Date());
            checkinTopicRecordRepository.save(topicRecord);
        }
        
        return record;
    }

    public List<CheckinTopic> getActiveTopics() {
        return checkinTopicRepository.findActiveTopics(new Date());
    }

    public List<CheckinTopic> getAllTopics() {
        return checkinTopicRepository.findAllOrderByStartDateDesc();
    }

    public CheckinTopic getTopicById(Long topicId) {
        return checkinTopicRepository.findById(topicId).orElse(null);
    }

    public long getTopicCheckinCount(Long topicId) {
        return checkinTopicRecordRepository.countUniqueUsersByTopicId(topicId);
    }

    public boolean hasUserCheckedInTopic(User user, Long topicId) {
        List<CheckinTopicRecord> records = checkinTopicRecordRepository.findByUserIdAndTopicId(user.getId(), topicId);
        return !records.isEmpty();
    }

    // 创建主题
    public CheckinTopic createTopic(String title, String description) {
        CheckinTopic topic = new CheckinTopic();
        topic.setTitle(title);
        topic.setDescription(description);
        
        // 设置主题的开始和结束日期，默认为当前周
        Calendar calendar = Calendar.getInstance();
        // 设置为周一开始
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        topic.setStartDate(calendar.getTime());
        
        // 设置为周日结束
        calendar.add(Calendar.DAY_OF_WEEK, 6);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        topic.setEndDate(calendar.getTime());
        
        topic.setCreatedAt(new Date());
        topic.setUpdatedAt(new Date());
        
        return checkinTopicRepository.save(topic);
    }
}
