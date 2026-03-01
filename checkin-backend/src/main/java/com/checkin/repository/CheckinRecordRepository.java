package com.checkin.repository;

import com.checkin.entity.CheckinRecord;
import com.checkin.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface CheckinRecordRepository extends JpaRepository<CheckinRecord, Long> {
    List<CheckinRecord> findByUser(User user);
    List<CheckinRecord> findByUserAndCheckinTimeBetween(User user, Date startDate, Date endDate);
    Page<CheckinRecord> findByUser(User user, Pageable pageable);
    Page<CheckinRecord> findByUserAndCheckinTimeBetween(User user, Date startDate, Date endDate, Pageable pageable);
}