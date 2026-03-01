package com.checkin.repository;

import com.checkin.entity.CheckinStatistics;
import com.checkin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckinStatisticsRepository extends JpaRepository<CheckinStatistics, Long> {
    CheckinStatistics findByUserAndYearAndMonthAndWeek(User user, Integer year, Integer month, Integer week);
    List<CheckinStatistics> findByUserAndYearAndMonth(User user, Integer year, Integer month);
}