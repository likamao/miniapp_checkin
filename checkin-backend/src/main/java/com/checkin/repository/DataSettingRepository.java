package com.checkin.repository;

import com.checkin.entity.DataSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataSettingRepository extends JpaRepository<DataSetting, Long> {

    @Query("SELECT ds FROM DataSetting ds WHERE ds.status = 1 ORDER BY ds.displayOrder ASC")
    List<DataSetting> findAllActiveSettings();
}
