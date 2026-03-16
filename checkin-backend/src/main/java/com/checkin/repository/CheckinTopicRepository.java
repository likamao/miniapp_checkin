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

    @Query("SELECT t FROM CheckinTopic t WHERE t.visibility = 'public' OR t.createdBy = :userId ORDER BY t.createdAt DESC")
    List<CheckinTopic> findVisibleTopics(@Param("userId") Long userId);

    @Query("SELECT t FROM CheckinTopic t WHERE (t.visibility = 'public' OR t.createdBy = :userId) AND t.startDatetime <= :date AND t.endDatetime >= :date AND t.status = 1 ORDER BY t.createdAt DESC")
    List<CheckinTopic> findActiveVisibleTopics(@Param("userId") Long userId, @Param("date") Date date);

    @Query("SELECT t FROM CheckinTopic t WHERE t.visibility = 'public' ORDER BY t.createdAt DESC")
    List<CheckinTopic> findAdminTopics();

    @Query("SELECT t FROM CheckinTopic t WHERE t.createdBy = :userId ORDER BY t.createdAt DESC")
    List<CheckinTopic> findByCreatedBy(@Param("userId") Long userId);
}
