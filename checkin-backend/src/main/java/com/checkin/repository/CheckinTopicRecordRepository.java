package com.checkin.repository;

import com.checkin.entity.CheckinTopicRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface CheckinTopicRecordRepository extends JpaRepository<CheckinTopicRecord, Long> {

    @Query("SELECT COUNT(DISTINCT r.userId) FROM CheckinTopicRecord r WHERE r.topicId = :topicId")
    Long countUniqueUsersByTopicId(@Param("topicId") Long topicId);

    @Query("SELECT r FROM CheckinTopicRecord r WHERE r.userId = :userId AND r.topicId = :topicId")
    List<CheckinTopicRecord> findByUserIdAndTopicId(@Param("userId") Long userId, @Param("topicId") Long topicId);

    @Query("SELECT r FROM CheckinTopicRecord r WHERE r.checkinRecordId = :checkinRecordId")
    List<CheckinTopicRecord> findByCheckinRecordId(@Param("checkinRecordId") Long checkinRecordId);

    Optional<CheckinTopicRecord> findByUserIdAndTopicIdAndCheckinDate(
            @Param("userId") Long userId, 
            @Param("topicId") Long topicId, 
            @Param("checkinDate") String checkinDate);

    @Query("SELECT r FROM CheckinTopicRecord r WHERE r.topicId = :topicId ORDER BY r.checkinDatetime DESC")
    Page<CheckinTopicRecord> findByTopicId(@Param("topicId") Long topicId, Pageable pageable);

    @Query("SELECT r FROM CheckinTopicRecord r WHERE r.userId = :userId AND r.topicId = :topicId ORDER BY r.checkinDatetime DESC")
    List<CheckinTopicRecord> findByUserIdAndTopicIdOrderByCheckinDatetimeDesc(
            @Param("userId") Long userId, 
            @Param("topicId") Long topicId);

    @Query("SELECT r FROM CheckinTopicRecord r WHERE r.userId = :userId AND r.topicId = :topicId AND r.checkinDate = :checkinDate")
    Optional<CheckinTopicRecord> findByUserIdAndTopicIdAndDate(
            @Param("userId") Long userId, 
            @Param("topicId") Long topicId, 
            @Param("checkinDate") String checkinDate);

    @Query("SELECT MAX(r.consecutiveDays) FROM CheckinTopicRecord r WHERE r.userId = :userId AND r.topicId = :topicId")
    Optional<Integer> findMaxConsecutiveDaysByUserIdAndTopicId(
            @Param("userId") Long userId, 
            @Param("topicId") Long topicId);

    @Query("SELECT r FROM CheckinTopicRecord r WHERE r.userId = :userId AND r.topicId = :topicId ORDER BY r.checkinDatetime DESC")
    List<CheckinTopicRecord> findRecentByUserIdAndTopicId(
            @Param("userId") Long userId, 
            @Param("topicId") Long topicId);
    
    @Query("SELECT r FROM CheckinTopicRecord r WHERE r.userId = :userId AND r.topicId = :topicId ORDER BY r.checkinDatetime DESC")
    Page<CheckinTopicRecord> findByUserIdAndTopicId(
            @Param("userId") Long userId, 
            @Param("topicId") Long topicId, 
            Pageable pageable);
    
    @Query("SELECT r FROM CheckinTopicRecord r WHERE r.topicId = :topicId AND r.checkinDatetime BETWEEN :startDate AND :endDate ORDER BY r.checkinDatetime DESC")
    List<CheckinTopicRecord> findByTopicIdAndCheckinDatetimeBetween(
            @Param("topicId") Long topicId, 
            @Param("startDate") Date startDate, 
            @Param("endDate") Date endDate);

    @Query("SELECT DISTINCT r.topicId FROM CheckinTopicRecord r WHERE r.userId = :userId")
    List<Long> findDistinctTopicIdsByUserId(@Param("userId") Long userId);
}
