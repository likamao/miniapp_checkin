package com.checkin.repository;

import com.checkin.entity.CheckinTopicRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CheckinTopicRecordRepository extends JpaRepository<CheckinTopicRecord, Long> {

    @Query("SELECT COUNT(DISTINCT r.userId) FROM CheckinTopicRecord r WHERE r.topicId = :topicId")
    Long countUniqueUsersByTopicId(@Param("topicId") Long topicId);

    @Query("SELECT r FROM CheckinTopicRecord r WHERE r.userId = :userId AND r.topicId = :topicId")
    List<CheckinTopicRecord> findByUserIdAndTopicId(@Param("userId") Long userId, @Param("topicId") Long topicId);

    @Query("SELECT r FROM CheckinTopicRecord r WHERE r.checkinRecordId = :checkinRecordId")
    List<CheckinTopicRecord> findByCheckinRecordId(@Param("checkinRecordId") Long checkinRecordId);
}
