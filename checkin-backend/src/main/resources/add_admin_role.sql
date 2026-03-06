    -- 为指定openID的用户添加管理员角色
    -- 1. 查找用户ID
    SET @user_id = (SELECT id FROM `user` WHERE openid = 'XXXXXXX');

    -- 2. 查找ADMIN角色ID
    SET @admin_role_id = (SELECT id FROM `role` WHERE name = 'ADMIN');

    -- 3. 为用户分配管理员角色
    INSERT INTO `user_role` (`user_id`, `role_id`, `created_at`, `updated_at`)
    VALUES (@user_id, @admin_role_id, NOW(), NOW())
    ON DUPLICATE KEY UPDATE `updated_at` = NOW();

    -- 4. 显示结果
    SELECT CONCAT('用户ID: ', @user_id, ', 管理员角色ID: ', @admin_role_id) AS result;
