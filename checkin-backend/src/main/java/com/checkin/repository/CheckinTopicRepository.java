package com.checkin.repository;

import com.checkin.entity.CheckinTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface CheckinTopicRepository extends JpaRepository<CheckinTopic, Long> {

    @Query("SELECT t FROM CheckinTopic t WHERE t.startDatetime <= :date AND t.endDatetime >= :date AND t.status = 1")
    List<CheckinTopic> findActiveTopics(@Param("date") Date date);

    @Query("SELECT t FROM CheckinTopic t ORDER BY t.createdAt DESC")
    List<CheckinTopic> findAllOrderByCreatedAtDesc();

    @Query("SELECT t FROM CheckinTopic t WHERE t.status = 1 ORDER BY t.createdAt DESC")
    List<CheckinTopic> findAllActiveTopics();

    @Query("SELECT t FROM CheckinTopic t WHERE t.status = 0 ORDER BY t.updatedAt DESC")
    List<CheckinTopic> findAllExpiredTopics();
}
