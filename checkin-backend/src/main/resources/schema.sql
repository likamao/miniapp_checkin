-- 创建用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `openid` VARCHAR(100) NOT NULL COMMENT '微信openid',
  `unionid` VARCHAR(100) COMMENT '微信unionid',
  `nickname` VARCHAR(50) COMMENT '用户昵称',
  `allow_stats_display` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否允许统计展示',
  `profile_setup_completed` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否完成资料设置',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 创建角色表
CREATE TABLE IF NOT EXISTS `role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `description` VARCHAR(255),
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 创建权限表
CREATE TABLE IF NOT EXISTS `permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `code` VARCHAR(50) NOT NULL,
  `description` VARCHAR(255),
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_name` (`name`),
  UNIQUE KEY `uk_permission_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 创建用户角色关联表
CREATE TABLE IF NOT EXISTS `user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`),
  CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 创建角色权限关联表
CREATE TABLE IF NOT EXISTS `role_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_id` BIGINT NOT NULL,
  `permission_id` BIGINT NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`),
  CONSTRAINT `fk_role_permission_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_role_permission_permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

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
  `start_datetime` DATETIME NOT NULL COMMENT '开始时间',
  `end_datetime` DATETIME NOT NULL COMMENT '结束时间',
  `duration_days` INT NOT NULL DEFAULT 7 COMMENT '有效期天数',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-有效，0-已过期',
  `created_by` BIGINT(20) COMMENT '创建者ID',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_start_datetime` (`start_datetime`),
  KEY `idx_end_datetime` (`end_datetime`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡主题表';

-- 创建主题打卡记录表
CREATE TABLE IF NOT EXISTS `checkin_topic_record` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `topic_id` BIGINT(20) NOT NULL COMMENT '主题ID',
  `checkin_record_id` BIGINT(20) COMMENT '打卡记录ID',
  `checkin_date` VARCHAR(10) NOT NULL COMMENT '打卡日期 (YYYY-MM-DD)',
  `checkin_datetime` DATETIME NOT NULL COMMENT '打卡时间',
  `consecutive_days` INT NOT NULL DEFAULT 1 COMMENT '连续打卡天数',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_topic_date` (`user_id`, `topic_id`, `checkin_date`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_topic_id` (`topic_id`),
  KEY `idx_checkin_date` (`checkin_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='主题打卡记录表';
