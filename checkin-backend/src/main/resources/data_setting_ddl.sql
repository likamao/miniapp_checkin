-- 创建数据设置表
CREATE TABLE IF NOT EXISTS `data_setting` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `icon` VARCHAR(20) NOT NULL COMMENT '图标（emoji）',
  `text` VARCHAR(500) NOT NULL COMMENT '设置说明文本',
  `display_order` INT(10) NOT NULL DEFAULT 0 COMMENT '显示顺序，数值越小越靠前',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_display_order` (`display_order`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据设置表';
