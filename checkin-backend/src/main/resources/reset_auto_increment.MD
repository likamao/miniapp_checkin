-- 重置用户表自增ID
ALTER TABLE `user` AUTO_INCREMENT = 1;

-- 重置角色表自增ID
ALTER TABLE `role` AUTO_INCREMENT = 1;

-- 重置权限表自增ID
ALTER TABLE `permission` AUTO_INCREMENT = 1;

-- 重置用户角色关联表自增ID
ALTER TABLE `user_role` AUTO_INCREMENT = 1;

-- 重置角色权限关联表自增ID
ALTER TABLE `role_permission` AUTO_INCREMENT = 1;

-- 重置打卡记录表自增ID
ALTER TABLE `checkin_record` AUTO_INCREMENT = 1;

-- 重置统计表自增ID
ALTER TABLE `checkin_statistics` AUTO_INCREMENT = 1;

-- 重置主题表自增ID
ALTER TABLE `checkin_topic` AUTO_INCREMENT = 1;

-- 重置主题打卡记录表自增ID
ALTER TABLE `checkin_topic_record` AUTO_INCREMENT = 1;