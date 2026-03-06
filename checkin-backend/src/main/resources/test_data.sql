-- Test data generation for check-in system
-- This script generates 100 users, 100 topics, and 7 check-in records per user per topic
-- Based on the actual database schema

-- -- Insert roles
-- INSERT INTO `role` (name, description) VALUES
-- ('USER', '普通用户'),
-- ('ADMIN', '管理员');

-- -- Insert permissions
-- INSERT INTO `permission` (name, code, description) VALUES
-- ('查看打卡记录', 'VIEW_CHECKIN', '查看自己的打卡记录'),
-- ('创建打卡记录', 'CREATE_CHECKIN', '创建新的打卡记录'),
-- ('编辑打卡记录', 'EDIT_CHECKIN', '编辑自己的打卡记录'),
-- ('删除打卡记录', 'DELETE_CHECKIN', '删除自己的打卡记录'),
-- ('查看统计数据', 'VIEW_STATISTICS', '查看统计数据'),
-- ('管理用户', 'MANAGE_USERS', '管理用户（管理员权限）'),
-- ('管理主题', 'MANAGE_TOPICS', '管理打卡主题（管理员权限）');

-- -- Assign permissions to roles
-- INSERT INTO `role_permission` (role_id, permission_id) VALUES
-- (1, 1), (1, 2), (1, 3), (1, 4), (1, 5),
-- (2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7);

-- Insert 100 users into user table
INSERT INTO `user` (openid, unionid, nickname, allow_stats_display, profile_setup_completed) VALUES
('openid001', 'unionid001', '阳光达人', 1, 1),
('openid002', 'unionid002', '快乐小天使', 1, 1),
('openid003', 'unionid003', '运动健将', 1, 1),
('openid004', 'unionid004', '学习标兵', 1, 1),
('openid005', 'unionid005', '美食家', 1, 1),
('openid006', 'unionid006', '旅行爱好者', 1, 1),
('openid007', 'unionid007', '阅读达人', 1, 1),
('openid008', 'unionid008', '音乐迷', 1, 1),
('openid009', 'unionid009', '电影爱好者', 1, 1),
('openid010', 'unionid010', '健身达人', 1, 1),
('openid011', 'unionid011', '摄影爱好者', 1, 1),
('openid012', 'unionid012', '编程高手', 1, 1),
('openid013', 'unionid013', '书法爱好者', 1, 1),
('openid014', 'unionid014', '绘画达人', 1, 1),
('openid015', 'unionid015', '舞蹈爱好者', 1, 1),
('openid016', 'unionid016', '瑜伽达人', 1, 1),
('openid017', 'unionid017', '志愿者', 1, 1),
('openid018', 'unionid018', '环保主义者', 1, 1),
('openid019', 'unionid019', '宠物爱好者', 1, 1),
('openid020', 'unionid020', '园艺达人', 1, 1),
('openid021', 'unionid021', '烹饪爱好者', 1, 1),
('openid022', 'unionid022', '手工达人', 1, 1),
('openid023', 'unionid023', '科技迷', 1, 1),
('openid024', 'unionid024', '历史爱好者', 1, 1),
('openid025', 'unionid025', '天文爱好者', 1, 1),
('openid026', 'unionid026', '地理达人', 1, 1),
('openid027', 'unionid027', '文学爱好者', 1, 1),
('openid028', 'unionid028', '哲学爱好者', 1, 1),
('openid029', 'unionid029', '心理学爱好者', 1, 1),
('openid030', 'unionid030', '经济学爱好者', 1, 1),
('openid031', 'unionid031', '政治学爱好者', 1, 1),
('openid032', 'unionid032', '社会学爱好者', 1, 1),
('openid033', 'unionid033', '人类学爱好者', 1, 1),
('openid034', 'unionid034', '语言学爱好者', 1, 1),
('openid035', 'unionid035', '数学爱好者', 1, 1),
('openid036', 'unionid036', '物理学爱好者', 1, 1),
('openid037', 'unionid037', '化学爱好者', 1, 1),
('openid038', 'unionid038', '生物学爱好者', 1, 1),
('openid039', 'unionid039', '医学爱好者', 1, 1),
('openid040', 'unionid040', '工程学爱好者', 1, 1),
('openid041', 'unionid041', '建筑学爱好者', 1, 1),
('openid042', 'unionid042', '设计爱好者', 1, 1),
('openid043', 'unionid043', '时尚爱好者', 1, 1),
('openid044', 'unionid044', '美容爱好者', 1, 1),
('openid045', 'unionid045', '健康爱好者', 1, 1),
('openid046', 'unionid046', '营养爱好者', 1, 1),
('openid047', 'unionid047', '睡眠爱好者', 1, 1),
('openid048', 'unionid048', '冥想爱好者', 1, 1),
('openid049', 'unionid049', '太极爱好者', 1, 1),
('openid050', 'unionid050', '武术爱好者', 1, 1),
('openid051', 'unionid051', '足球爱好者', 1, 1),
('openid052', 'unionid052', '篮球爱好者', 1, 1),
('openid053', 'unionid053', '排球爱好者', 1, 1),
('openid054', 'unionid054', '网球爱好者', 1, 1),
('openid055', 'unionid055', '乒乓球爱好者', 1, 1),
('openid056', 'unionid056', '羽毛球爱好者', 1, 1),
('openid057', 'unionid057', '游泳爱好者', 1, 1),
('openid058', 'unionid058', '跑步爱好者', 1, 1),
('openid059', 'unionid059', '骑行爱好者', 1, 1),
('openid060', 'unionid060', '徒步爱好者', 1, 1),
('openid061', 'unionid061', '登山爱好者', 1, 1),
('openid062', 'unionid062', '滑雪爱好者', 1, 1),
('openid063', 'unionid063', '冲浪爱好者', 1, 1),
('openid064', 'unionid064', '潜水爱好者', 1, 1),
('openid065', 'unionid065', '跳伞爱好者', 1, 1),
('openid066', 'unionid066', '攀岩爱好者', 1, 1),
('openid067', 'unionid067', '滑板爱好者', 1, 1),
('openid068', 'unionid068', '轮滑爱好者', 1, 1),
('openid069', 'unionid069', '瑜伽球爱好者', 1, 1),
('openid070', 'unionid070', '普拉提爱好者', 1, 1),
('openid071', 'unionid071', '爵士舞爱好者', 1, 1),
('openid072', 'unionid072', '肚皮舞爱好者', 1, 1),
('openid073', 'unionid073', '街舞爱好者', 1, 1),
('openid074', 'unionid074', '民族舞爱好者', 1, 1),
('openid075', 'unionid075', '芭蕾舞爱好者', 1, 1),
('openid076', 'unionid076', '现代舞爱好者', 1, 1),
('openid077', 'unionid077', '踢踏舞爱好者', 1, 1),
('openid078', 'unionid078', '交谊舞爱好者', 1, 1),
('openid079', 'unionid079', '国标舞爱好者', 1, 1),
('openid080', 'unionid080', '拉丁舞爱好者', 1, 1),
('openid081', 'unionid081', '弗拉门戈舞爱好者', 1, 1),
('openid082', 'unionid082', '探戈爱好者', 1, 1),
('openid083', 'unionid083', '华尔兹爱好者', 1, 1),
('openid084', 'unionid084', '恰恰爱好者', 1, 1),
('openid085', 'unionid085', '桑巴爱好者', 1, 1),
('openid086', 'unionid086', '伦巴爱好者', 1, 1),
('openid087', 'unionid087', '牛仔舞爱好者', 1, 1),
('openid088', 'unionid088', '斗牛舞爱好者', 1, 1),
('openid089', 'unionid089', '街舞Breaking爱好者', 1, 1),
('openid090', 'unionid090', '街舞Popping爱好者', 1, 1),
('openid091', 'unionid091', '街舞Locking爱好者', 1, 1),
('openid092', 'unionid092', '街舞Waacking爱好者', 1, 1),
('openid093', 'unionid093', '街舞House爱好者', 1, 1),
('openid094', 'unionid094', '街舞Krump爱好者', 1, 1),
('openid095', 'unionid095', '街舞Vogue爱好者', 1, 1),
('openid096', 'unionid096', '街舞Turfing爱好者', 1, 1),
('openid097', 'unionid097', '街舞Jazz Funk爱好者', 1, 1),
('openid098', 'unionid098', '街舞Urban爱好者', 1, 1),
('openid099', 'unionid099', '街舞Commercial爱好者', 1, 1),
('openid100', 'unionid100', '街舞Hip-hop爱好者', 1, 1);

-- Assign USER role to all users
INSERT INTO `user_role` (user_id, role_id) 
SELECT id, 1 FROM `user`;

-- Insert 100 topics into checkin_topic table
INSERT INTO `checkin_topic` (title, description, start_datetime, end_datetime, duration_days, status, created_by) VALUES
('每日早起', '每天早上6点前起床', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('晨跑锻炼', '每天早上进行30分钟晨跑', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('阅读打卡', '每天阅读30分钟以上', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('冥想练习', '每天进行10分钟冥想', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('喝水提醒', '每天喝够8杯水', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('健康饮食', '每天摄入均衡营养', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('睡眠管理', '每天11点前睡觉', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('运动打卡', '每天进行30分钟运动', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('学习英语', '每天学习英语30分钟', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('写日记', '每天写日记记录生活', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('练习书法', '每天练习书法30分钟', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('绘画练习', '每天进行绘画创作', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('音乐练习', '每天练习乐器30分钟', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('舞蹈练习', '每天进行舞蹈练习', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('编程学习', '每天学习编程1小时', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('写作练习', '每天写作500字以上', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('摄影练习', '每天拍摄一张照片', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('瑜伽练习', '每天进行30分钟瑜伽', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('拉伸练习', '每天进行15分钟拉伸', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('力量训练', '每天进行30分钟力量训练', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('有氧运动', '每天进行30分钟有氧运动', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('骑行锻炼', '每天骑行30分钟以上', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('徒步旅行', '每周进行一次徒步旅行', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('登山活动', '每月进行一次登山活动', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('游泳锻炼', '每周进行3次游泳', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('球类运动', '每周进行2次球类运动', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('武术练习', '每天进行武术练习', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('太极练习', '每天进行太极练习', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('冥想正念', '每天进行正念冥想', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('情绪管理', '每天记录情绪状态', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('压力释放', '每天进行压力释放活动', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('社交活动', '每天与朋友交流', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('家庭互动', '每天与家人互动', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('志愿服务', '每月参与一次志愿服务', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('环保行动', '每天进行一项环保行动', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('健康检查', '定期进行健康检查', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('疫苗接种', '按时接种疫苗', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('疾病预防', '了解疾病预防知识', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('急救知识', '学习急救知识', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('营养知识', '学习营养知识', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('烹饪技能', '每天学习一道新菜', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('家务劳动', '每天完成一项家务', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('理财规划', '每天进行理财规划', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('时间管理', '每天进行时间管理', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('目标设定', '每天设定并完成目标', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('习惯养成', '培养良好的生活习惯', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('自我提升', '每天进行自我提升活动', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('职业发展', '每天关注职业发展', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('人际关系', '每天维护人际关系', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('兴趣培养', '每天培养兴趣爱好', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('知识学习', '每天学习新知识', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('技能培训', '每天进行技能培训', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('创意活动', '每天进行创意活动', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('思维训练', '每天进行思维训练', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('记忆力训练', '每天进行记忆力训练', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('注意力训练', '每天进行注意力训练', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('逻辑思维', '每天进行逻辑思维训练', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('创新思维', '每天进行创新思维训练', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('批判性思维', '每天进行批判性思维训练', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('问题解决', '每天练习问题解决能力', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('决策能力', '每天练习决策能力', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('沟通能力', '每天练习沟通能力', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('领导力', '每天练习领导力', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('团队合作', '每天练习团队合作能力', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('演讲能力', '每天练习演讲能力', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('写作能力', '每天练习写作能力', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('阅读能力', '每天练习阅读能力', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('学习能力', '每天练习学习能力', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('适应能力', '每天练习适应能力', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('抗压能力', '每天练习抗压能力', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('情绪智商', '每天练习情绪智商', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('社交智商', '每天练习社交智商', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('自我认知', '每天进行自我认知', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('自我接纳', '每天练习自我接纳', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('自我肯定', '每天进行自我肯定', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('自我激励', '每天进行自我激励', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('自我约束', '每天练习自我约束', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('自我管理', '每天进行自我管理', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('精力管理', '每天进行精力管理', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('健康管理', '每天进行健康管理', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('财富管理', '每天进行财富管理', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('关系管理', '每天进行关系管理', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('目标管理', '每天进行目标管理', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('计划管理', '每天进行计划管理', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('执行管理', '每天进行执行管理', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('复盘管理', '每天进行复盘管理', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('学习管理', '每天进行学习管理', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('创新管理', '每天进行创新管理', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('风险管理', '每天进行风险管理', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1),
('机遇管理', '每天进行机遇管理', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 7, 1, 1);

-- Generate check-in records for each user and topic
-- This part uses a stored procedure to generate the records
DELIMITER //
CREATE PROCEDURE GenerateCheckins()
BEGIN
    DECLARE user_id BIGINT DEFAULT 1;
    DECLARE topic_id BIGINT DEFAULT 1;
    DECLARE checkin_day INT DEFAULT 1;
    DECLARE checkin_datetime DATETIME;
    DECLARE checkin_date VARCHAR(10);
    DECLARE consecutive_days INT;
    DECLARE checkin_title VARCHAR(50);
    DECLARE checkin_content VARCHAR(500);
    DECLARE max_user_id BIGINT;
    DECLARE max_topic_id BIGINT;
    
    -- Get actual max user ID and topic ID
    SELECT COALESCE(MAX(id), 0) INTO max_user_id FROM `user`;
    SELECT COALESCE(MAX(id), 0) INTO max_topic_id FROM `checkin_topic`;
    
    -- Check if users and topics exist
    IF max_user_id = 0 THEN
        SELECT 'Error: No users found!' AS result;
        RETURN;
    END IF;
    
    IF max_topic_id = 0 THEN
        SELECT 'Error: No topics found!' AS result;
        RETURN;
    END IF;
    
    -- Loop through all users
    WHILE user_id <= max_user_id DO
        -- Loop through all topics
        SET topic_id = 1;
        WHILE topic_id <= max_topic_id DO
            -- Get topic title for check-in record
            SELECT title INTO checkin_title FROM `checkin_topic` WHERE id = topic_id;
            
            -- If title is null, set a default value
            IF checkin_title IS NULL THEN
                SET checkin_title = CONCAT('主题', topic_id);
            END IF;
            
            -- Generate 7 check-in records per user per topic
            SET checkin_day = 1;
            SET consecutive_days = 1;
            WHILE checkin_day <= 7 DO
                -- Generate realistic datetime (last 30 days)
                SET checkin_datetime = DATE_SUB(NOW(), INTERVAL (30 - checkin_day) DAY);
                SET checkin_datetime = TIMESTAMP(checkin_datetime, SEC_TO_TIME(FLOOR(RAND() * 86400)));
                
                -- Format checkin_date as YYYY-MM-DD
                SET checkin_date = DATE_FORMAT(checkin_datetime, '%Y-%m-%d');
                
                -- Generate check-in content
                SET checkin_content = CONCAT('完成了第 ', checkin_day, ' 天的', checkin_title, '，继续加油！');
                
                -- Insert check-in record into checkin_record
                INSERT INTO `checkin_record` (user_id, title, content, checkin_time)
                VALUES (user_id, checkin_title, checkin_content, checkin_datetime);
                
                -- Get the last inserted checkin_record id
                SET @last_checkin_id = LAST_INSERT_ID();
                
                -- Insert check-in record into checkin_topic_record
                INSERT INTO `checkin_topic_record` (user_id, topic_id, checkin_record_id, checkin_date, checkin_datetime, consecutive_days)
                VALUES (user_id, topic_id, @last_checkin_id, checkin_date, checkin_datetime, consecutive_days);
                
                -- Insert/update statistics
                INSERT INTO `checkin_statistics` (user_id, year, month, week, checkin_count)
                VALUES (
                    user_id, 
                    YEAR(checkin_datetime), 
                    MONTH(checkin_datetime), 
                    WEEK(checkin_datetime), 
                    1
                )
                ON DUPLICATE KEY UPDATE 
                    checkin_count = checkin_count + 1,
                    updated_at = CURRENT_TIMESTAMP;
                
                -- Increment consecutive days
                SET consecutive_days = consecutive_days + 1;
                SET checkin_day = checkin_day + 1;
            END WHILE;
            SET topic_id = topic_id + 1;
        END WHILE;
        SET user_id = user_id + 1;
    END WHILE;
    
    SELECT CONCAT('Successfully generated check-ins for ', max_user_id, ' users and ', max_topic_id, ' topics') AS result;
END //
DELIMITER ;

-- Call the stored procedure to generate check-in records
CALL GenerateCheckins();

-- Drop the stored procedure as it's no longer needed
DROP PROCEDURE IF EXISTS GenerateCheckins;

-- Verify the data generation
SELECT 'Users count:' AS description, COUNT(*) AS count FROM `user`;
SELECT 'Topics count:' AS description, COUNT(*) AS count FROM `checkin_topic`;
SELECT 'Check-in records count:' AS description, COUNT(*) AS count FROM `checkin_record`;
SELECT 'Topic check-ins count:' AS description, COUNT(*) AS count FROM `checkin_topic_record`;
SELECT 'Statistics count:' AS description, COUNT(*) AS count FROM `checkin_statistics`;
SELECT 'Check-ins per user per topic:' AS description, COUNT(*) / (100 * 100) AS average FROM `checkin_topic_record`;