package com.checkin.service;

import com.checkin.entity.CheckinRecord;
import com.checkin.entity.CheckinStatistics;
import com.checkin.entity.User;
import com.checkin.repository.CheckinRecordRepository;
import com.checkin.repository.CheckinStatisticsRepository;
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
}