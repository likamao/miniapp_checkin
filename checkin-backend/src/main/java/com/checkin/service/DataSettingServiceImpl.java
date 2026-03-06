package com.checkin.service;

import com.checkin.entity.DataSetting;
import com.checkin.repository.DataSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataSettingServiceImpl implements DataSettingService {

    @Autowired
    private DataSettingRepository dataSettingRepository;

    @Override
    public List<DataSetting> getAllActiveSettings() {
        return dataSettingRepository.findAllActiveSettings();
    }
}
