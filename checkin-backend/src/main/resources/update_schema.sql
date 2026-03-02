-- 更新主题表，添加缺失的字段
ALTER TABLE `checkin_topic` ADD COLUMN `start_datetime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间';
ALTER TABLE `checkin_topic` ADD COLUMN `end_datetime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '结束时间';
ALTER TABLE `checkin_topic` ADD COLUMN `duration_days` INT NOT NULL DEFAULT 7 COMMENT '有效期天数';
ALTER TABLE `checkin_topic` ADD COLUMN `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-有效，0-已过期';
ALTER TABLE `checkin_topic` ADD COLUMN `created_by` BIGINT(20) COMMENT '创建者ID';

-- 更新主题打卡记录表，添加缺失的字段
ALTER TABLE `checkin_topic_record` ADD COLUMN `checkin_date` VARCHAR(10) NOT NULL COMMENT '打卡日期 (YYYY-MM-DD)';
ALTER TABLE `checkin_topic_record` ADD COLUMN `checkin_datetime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '打卡时间';
ALTER TABLE `checkin_topic_record` ADD COLUMN `consecutive_days` INT NOT NULL DEFAULT 1 COMMENT '连续打卡天数';

-- 添加唯一索引
ALTER TABLE `checkin_topic_record` ADD UNIQUE KEY `uk_user_topic_date` (`user_id`, `topic_id`, `checkin_date`);

-- 添加索引
ALTER TABLE `checkin_topic` ADD KEY `idx_start_datetime` (`start_datetime`);
ALTER TABLE `checkin_topic` ADD KEY `idx_end_datetime` (`end_datetime`);
ALTER TABLE `checkin_topic` ADD KEY `idx_status` (`status`);

ALTER TABLE `checkin_topic_record` ADD KEY `idx_user_id` (`user_id`);
ALTER TABLE `checkin_topic_record` ADD KEY `idx_topic_id` (`topic_id`);
ALTER TABLE `checkin_topic_record` ADD KEY `idx_checkin_date` (`checkin_date`);