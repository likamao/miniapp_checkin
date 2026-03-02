-- 创建用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `openid` VARCHAR(100) NOT NULL COMMENT '微信openid',
  `unionid` VARCHAR(100) COMMENT '微信unionid',
  `nickname` VARCHAR(50) COMMENT '用户昵称',
  `avatar_url` VARCHAR(255) COMMENT '头像URL',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 创建打卡记录表
CREATE TABLE IF NOT EXISTS `checkin_record` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `title` VARCHAR(50) NOT NULL COMMENT '打卡标题',
  `content` VARCHAR(500) NOT NULL COMMENT '打卡内容',
  `checkin_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '打卡时间',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_checkin_time` (`checkin_time`),
  CONSTRAINT `fk_checkin_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡记录表';

-- 创建统计表
CREATE TABLE IF NOT EXISTS `checkin_statistics` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `year` INT(4) NOT NULL COMMENT '年份',
  `month` INT(2) NOT NULL COMMENT '月份',
  `week` INT(2) COMMENT '周数',
  `checkin_count` INT(10) DEFAULT 0 COMMENT '打卡次数',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_year_month_week` (`user_id`, `year`, `month`, `week`),
  CONSTRAINT `fk_statistics_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡统计表';

-- 创建主题表
CREATE TABLE IF NOT EXISTS `checkin_topic` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(100) NOT NULL COMMENT '主题标题',
  `description` VARCHAR(500) COMMENT '主题描述',
  `start_date` DATE NOT NULL COMMENT '开始日期',
  `end_date` DATE NOT NULL COMMENT '结束日期',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_start_date` (`start_date`),
  KEY `idx_end_date` (`end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡主题表';

-- 创建主题打卡关联表
CREATE TABLE IF NOT EXISTS `checkin_topic_record` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `topic_id` BIGINT(20) NOT NULL COMMENT '主题ID',
  `checkin_record_id` BIGINT(20) NOT NULL COMMENT '打卡记录ID',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_topic_record` (`user_id`, `topic_id`, `checkin_record_id`),
  CONSTRAINT `fk_topic_record_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_topic_record_topic` FOREIGN KEY (`topic_id`) REFERENCES `checkin_topic` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_topic_record_checkin` FOREIGN KEY (`checkin_record_id`) REFERENCES `checkin_record` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='主题打卡关联表';