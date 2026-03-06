package com.checkin.service;

import com.checkin.entity.DataSetting;

import java.util.List;

public interface DataSettingService {

    List<DataSetting> getAllActiveSettings();
}
