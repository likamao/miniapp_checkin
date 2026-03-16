-- RBAC权限初始化脚本

-- 插入角色
INSERT INTO `role` (`name`, `description`, `created_at`, `updated_at`) VALUES
('ADMIN', '管理员角色，拥有所有权限', NOW(), NOW()),
('PUBLISHER', '发布者角色，拥有部分管理权限', NOW(), NOW()),
('VIEWER', '查看者角色，可查看公开主题的完整打卡记录', NOW(), NOW()),
('USER', '普通用户角色，拥有基础权限', NOW(), NOW())
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`), `updated_at` = NOW();

-- 插入权限
INSERT INTO `permission` (`name`, `code`, `description`, `created_at`, `updated_at`) VALUES
('用户查询权限', 'user:read', '查看用户信息的权限', NOW(), NOW()),
('用户创建权限', 'user:create', '创建用户的权限', NOW(), NOW()),
('用户更新权限', 'user:update', '更新用户信息的权限', NOW(), NOW()),
('用户删除权限', 'user:delete', '删除用户的权限', NOW(), NOW()),
('打卡查询权限', 'checkin:read', '查看打卡记录的权限', NOW(), NOW()),
('打卡详情权限', 'checkin:view-details', '查看打卡详情（包括标题和内容）的权限', NOW(), NOW()),
('打卡创建权限', 'checkin:create', '创建打卡记录的权限', NOW(), NOW()),
('打卡统计权限', 'checkin:statistics', '查看打卡统计的权限', NOW(), NOW())
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`), `updated_at` = NOW();

-- 管理员角色分配所有权限
INSERT INTO `role_permission` (`role_id`, `permission_id`, `created_at`, `updated_at`) VALUES
((SELECT `id` FROM `role` WHERE `name` = 'ADMIN'), (SELECT `id` FROM `permission` WHERE `code` = 'user:read'), NOW(), NOW()),
((SELECT `id` FROM `role` WHERE `name` = 'ADMIN'), (SELECT `id` FROM `permission` WHERE `code` = 'user:create'), NOW(), NOW()),
((SELECT `id` FROM `role` WHERE `name` = 'ADMIN'), (SELECT `id` FROM `permission` WHERE `code` = 'user:update'), NOW(), NOW()),
((SELECT `id` FROM `role` WHERE `name` = 'ADMIN'), (SELECT `id` FROM `permission` WHERE `code` = 'user:delete'), NOW(), NOW()),
((SELECT `id` FROM `role` WHERE `name` = 'ADMIN'), (SELECT `id` FROM `permission` WHERE `code` = 'checkin:read'), NOW(), NOW()),
((SELECT `id` FROM `role` WHERE `name` = 'ADMIN'), (SELECT `id` FROM `permission` WHERE `code` = 'checkin:view-details'), NOW(), NOW()),
((SELECT `id` FROM `role` WHERE `name` = 'ADMIN'), (SELECT `id` FROM `permission` WHERE `code` = 'checkin:create'), NOW(), NOW()),
((SELECT `id` FROM `role` WHERE `name` = 'ADMIN'), (SELECT `id` FROM `permission` WHERE `code` = 'checkin:statistics'), NOW(), NOW())
ON DUPLICATE KEY UPDATE `updated_at` = NOW();

-- 发布者角色分配部分权限
INSERT INTO `role_permission` (`role_id`, `permission_id`, `created_at`, `updated_at`) VALUES
((SELECT `id` FROM `role` WHERE `name` = 'PUBLISHER'), (SELECT `id` FROM `permission` WHERE `code` = 'user:read'), NOW(), NOW()),
((SELECT `id` FROM `role` WHERE `name` = 'PUBLISHER'), (SELECT `id` FROM `permission` WHERE `code` = 'checkin:read'), NOW(), NOW()),
((SELECT `id` FROM `role` WHERE `name` = 'PUBLISHER'), (SELECT `id` FROM `permission` WHERE `code` = 'checkin:view-details'), NOW(), NOW()),
((SELECT `id` FROM `role` WHERE `name` = 'PUBLISHER'), (SELECT `id` FROM `permission` WHERE `code` = 'checkin:create'), NOW(), NOW()),
((SELECT `id` FROM `role` WHERE `name` = 'PUBLISHER'), (SELECT `id` FROM `permission` WHERE `code` = 'checkin:statistics'), NOW(), NOW())
ON DUPLICATE KEY UPDATE `updated_at` = NOW();

-- 查看者角色：拥有 USER 的所有权限 + 可查看公开主题的完整打卡记录
INSERT INTO `role_permission` (`role_id`, `permission_id`, `created_at`, `updated_at`) VALUES
((SELECT `id` FROM `role` WHERE `name` = 'VIEWER'), (SELECT `id` FROM `permission` WHERE `code` = 'checkin:read'), NOW(), NOW()),
((SELECT `id` FROM `role` WHERE `name` = 'VIEWER'), (SELECT `id` FROM `permission` WHERE `code` = 'checkin:create'), NOW(), NOW()),
((SELECT `id` FROM `role` WHERE `name` = 'VIEWER'), (SELECT `id` FROM `permission` WHERE `code` = 'checkin:statistics'), NOW(), NOW()),
((SELECT `id` FROM `role` WHERE `name` = 'VIEWER'), (SELECT `id` FROM `permission` WHERE `code` = 'checkin:view-details'), NOW(), NOW())
ON DUPLICATE KEY UPDATE `updated_at` = NOW();

-- 普通用户角色分配基础权限
INSERT INTO `role_permission` (`role_id`, `permission_id`, `created_at`, `updated_at`) VALUES
((SELECT `id` FROM `role` WHERE `name` = 'USER'), (SELECT `id` FROM `permission` WHERE `code` = 'checkin:read'), NOW(), NOW()),
((SELECT `id` FROM `role` WHERE `name` = 'USER'), (SELECT `id` FROM `permission` WHERE `code` = 'checkin:create'), NOW(), NOW()),
((SELECT `id` FROM `role` WHERE `name` = 'USER'), (SELECT `id` FROM `permission` WHERE `code` = 'checkin:statistics'), NOW(), NOW())
ON DUPLICATE KEY UPDATE `updated_at` = NOW();
