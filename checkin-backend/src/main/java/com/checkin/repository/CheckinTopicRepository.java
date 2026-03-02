package com.checkin.repository;

import com.checkin.entity.CheckinTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface CheckinTopicRepository extends JpaRepository<CheckinTopic, Long> {

    @Query("SELECT t FROM CheckinTopic t WHERE t.startDate <= :date AND t.endDate >= :date")
    List<CheckinTopic> findActiveTopics(@Param("date") Date date);

    @Query("SELECT t FROM CheckinTopic t ORDER BY t.startDate DESC")
    List<CheckinTopic> findAllOrderByStartDateDesc();
}
