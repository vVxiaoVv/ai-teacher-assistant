-- ============================================
-- AI教师助手系统数据库初始化脚本
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `ai_teacher` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `ai_teacher`;

-- ============================================
-- 1. 用户表
-- ============================================
CREATE TABLE IF NOT EXISTS `user` (
                                      `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                      `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `role` VARCHAR(20) NOT NULL DEFAULT 'admin' COMMENT '角色',
    `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态',
    `avatar_url` VARCHAR(1000) COMMENT '头像URL',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_status` (`status`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 视频分析记录表
-- ============================================
CREATE TABLE IF NOT EXISTS `video_analysis_record` (
                                                       `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                       `video_url` VARCHAR(500) NOT NULL COMMENT '视频URL',
    `title` VARCHAR(500) COMMENT '标题（课题+时间）',
    `formatted_message` TEXT COMMENT '格式化后的分析结果',
    `raw_message` TEXT COMMENT '原始分析结果',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_user_id` BIGINT COMMENT '创建者ID（操作人ID）',
    PRIMARY KEY (`id`),
    KEY `idx_video_url` (`video_url`(255)),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_create_user_id` (`create_user_id`),
    KEY `idx_title` (`title`(255))
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频分析记录表';

-- ============================================
-- 3. 学生画像表
-- ============================================
CREATE TABLE IF NOT EXISTS `student_portrait` (
                                                  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                  `student_name` VARCHAR(100) NOT NULL COMMENT '学生姓名',
    `photo_url` VARCHAR(500) COMMENT '学生照片URL',
    `characteristics` TEXT COMMENT '学生特点描述',
    `age` INT COMMENT '学生年龄',
    `exam_history` TEXT COMMENT '历史考试信息（JSON格式）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建者ID',
    `update_user_id` BIGINT COMMENT '更新者ID',
    PRIMARY KEY (`id`),
    KEY `idx_student_name` (`student_name`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_create_user_id` (`create_user_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生画像表';

-- ============================================
-- 更新学生画像表（添加新字段）
-- ============================================
-- 如果表已存在但缺少age字段，添加age字段
ALTER TABLE `student_portrait` ADD COLUMN IF NOT EXISTS `age` INT COMMENT '学生年龄';
-- 如果表已存在但缺少exam_history字段，添加exam_history字段
ALTER TABLE `student_portrait` ADD COLUMN IF NOT EXISTS `exam_history` TEXT COMMENT '历史考试信息（JSON格式）';

-- ============================================
-- 更新用户表（添加新字段）
-- ============================================
-- 如果表已存在但缺少age字段，添加age字段
ALTER TABLE `user` ADD COLUMN IF NOT EXISTS `age` INT COMMENT '年龄';
-- 如果表已存在但缺少subject字段，添加subject字段
ALTER TABLE `user` ADD COLUMN IF NOT EXISTS `subject` VARCHAR(50) COMMENT '学科';

-- ============================================
-- 4. 教案表
-- ============================================
CREATE TABLE IF NOT EXISTS `lesson_plan` (
                                             `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                             `title` VARCHAR(200) NOT NULL COMMENT '教案标题',
    `content` TEXT NOT NULL COMMENT '教案内容',
    `upload_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `create_user_id` BIGINT COMMENT '创建者ID',
    `update_user_id` BIGINT COMMENT '更新者ID',
    PRIMARY KEY (`id`),
    KEY `idx_title` (`title`),
    KEY `idx_upload_time` (`upload_time`),
    KEY `idx_create_user_id` (`create_user_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教案表';

-- ============================================
-- 5. 逐字稿表
-- ============================================
CREATE TABLE IF NOT EXISTS `script` (
                                        `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                        `lesson_plan_id` BIGINT NOT NULL COMMENT '关联的教案ID',
                                        `title` VARCHAR(200) NOT NULL COMMENT '逐字稿标题',
    `content` TEXT NOT NULL COMMENT '逐字稿内容',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建者ID',
    PRIMARY KEY (`id`),
    KEY `idx_lesson_plan_id` (`lesson_plan_id`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_script_lesson_plan` FOREIGN KEY (`lesson_plan_id`) REFERENCES `lesson_plan` (`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='逐字稿表';

-- ============================================
-- 6. 视频范例表
-- ============================================
CREATE TABLE IF NOT EXISTS `video_example` (
                                               `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                               `lesson_plan_id` BIGINT NOT NULL COMMENT '关联的教案ID',
                                               `video_url` VARCHAR(500) NOT NULL COMMENT '视频URL',
    `topic` VARCHAR(200) NOT NULL COMMENT '视频主题',
    `description` TEXT COMMENT '视频描述',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_user_id` BIGINT COMMENT '创建者ID',
    `update_user_id` BIGINT COMMENT '更新者ID',
    PRIMARY KEY (`id`),
    KEY `idx_lesson_plan_id` (`lesson_plan_id`),
    KEY `idx_topic` (`topic`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_video_example_lesson_plan` FOREIGN KEY (`lesson_plan_id`) REFERENCES `lesson_plan` (`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频范例表';

-- ============================================
-- 7. 教师画像表
-- ============================================
CREATE TABLE IF NOT EXISTS `teacher_portrait` (
                                                  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                  `user_id` BIGINT NOT NULL COMMENT '用户ID（关联user表）',
                                                  `description` TEXT COMMENT '文字描述（教师特点分析）',
                                                  `teaching_foundation` DOUBLE COMMENT '教学基本功分数（0-100）',
                                                  `teaching_process_design` DOUBLE COMMENT '教学过程设计分数（0-100）',
                                                  `teaching_manner` DOUBLE COMMENT '教态分数（0-100）',
                                                  `multimedia_and_blackboard` DOUBLE COMMENT '多媒体与板书运用分数（0-100）',
                                                  `classroom_atmosphere` DOUBLE COMMENT '课堂气氛分数（0-100）',
                                                  `time_rhythm_control` DOUBLE COMMENT '时间节奏掌控分数（0-100）',
                                                  `scoring_rule` TEXT COMMENT '打分规则说明',
                                                  `ai_response` TEXT COMMENT 'AI返回的完整内容（原始响应）',
                                                  `history_count` INT COMMENT '使用的历史记录数量',
                                                  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                                  PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教师画像表';

-- ============================================
-- 初始化数据
-- ============================================

-- 插入默认管理员用户
-- 用户名: admin, 密码: admin123
INSERT INTO `user` (`username`, `password`, `role`, `status`, `avatar_url`, `create_time`, `update_time`)
VALUES ('admin', 'admin123', 'admin', 'active', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin&backgroundColor=b6e3f4', NOW(), NOW())
    ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- 插入测试用户（可选）
-- 用户名: test, 密码: test123
INSERT INTO `user` (`username`, `password`, `role`, `status`, `avatar_url`, `create_time`, `update_time`)
VALUES ('test', 'test123', 'admin', 'active', 'https://api.dicebear.com/7.x/avataaars/svg?seed=test&backgroundColor=c7d2fe', NOW(), NOW())
    ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- 插入5条火影忍者角色测试数据（username使用中文，使用真实的Q版头像URL，包含年龄和学科信息）
INSERT INTO `user` (`username`, `password`, `role`, `status`, `avatar_url`, `age`, `subject`, `create_time`, `update_time`) VALUES
                                                                                                              ('漩涡鸣人', '123456', 'user', 'active', 'https://img2.baidu.com/it/u=2282259983,3465502484&fm=253&app=138&f=JPEG?w=500&h=500', 17, '体育', NOW(), NOW()),
                                                                                                              ('宇智波佐助', '123456', 'user', 'active', 'https://img1.baidu.com/it/u=3097808718,4220946449&fm=253&app=138&f=JPEG?w=502&h=500', 17, '数学', NOW(), NOW()),
                                                                                                              ('春野樱', '123456', 'user', 'active', 'https://ww1.sinaimg.cn/mw690/88a76bfagy1hpod6zz7tvj20wr0wm46n.jpg', 17, '医学', NOW(), NOW()),
                                                                                                              ('旗木卡卡西', '123456', 'user', 'active', 'https://gips2.baidu.com/it/u=3192270850,3934380473&fm=3074&app=3074&f=JPEG', 30, '语文', NOW(), NOW()),
                                                                                                              ('自来也', '123456', 'user', 'active', 'https://picx.zhimg.com/v2-030fd1405b70e2ce43a406499cf06a9a_r.jpg?source=2c26e567', 50, '文学', NOW(), NOW()),
                                                                                                              ('纲手', '123456', 'user', 'active', 'https://img2.baidu.com/it/u=1254216982,210015761&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500', 55, '医学', NOW(), NOW()),
                                                                                                              ('大蛇丸', '123456', 'user', 'active', 'https://img1.baidu.com/it/u=1720919251,1803841280&fm=253&app=138&f=JPEG?w=500&h=500', 50, '化学', NOW(), NOW()),
                                                                                                              ('我爱罗', '123456', 'user', 'active', 'https://img.bugela.com/uploads/2022/03/10/TX10068_03.jpg', 17, '地理', NOW(), NOW()),
                                                                                                              ('宇智波斑', '123456', 'user', 'active', 'https://img2.baidu.com/it/u=2809123456,3712345678&fm=253&app=138&f=JPEG?w=500&h=500', 80, '历史', NOW(), NOW())
    ON DUPLICATE KEY UPDATE `update_time` = NOW(), `avatar_url` = VALUES(`avatar_url`), `age` = VALUES(`age`), `subject` = VALUES(`subject`);

-- ============================================
-- 插入学生画像测试数据
-- ============================================

-- 插入懒羊羊
INSERT INTO `student_portrait` (`student_name`, `photo_url`, `characteristics`, `create_time`, `update_time`)
VALUES (
           '懒羊羊',
           'https://img0.baidu.com/it/u=1417527418,1094663789&fm=253&app=138&f=JPEG?w=500&h=500',
           '一天天就知道傻吃nie睡，学习成绩稳定倒数前三，一问三不知，三棍子打不出一个闷屁，上课就在那摆弄手指头',
           NOW(),
           NOW()
       )
    ON DUPLICATE KEY UPDATE
                         `photo_url` = VALUES(`photo_url`),
                         `characteristics` = VALUES(`characteristics`),
                         `update_time` = NOW();

-- 插入美羊羊
INSERT INTO `student_portrait` (`student_name`, `photo_url`, `characteristics`, `create_time`, `update_time`)
VALUES (
           '美羊羊',
           'https://img2.baidu.com/it/u=1105171189,1856072744&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=500',
           '爱打扮爱化妆的小女孩，班花万人迷，不怎么好好学习，但是人挺聪明，稍微学点就能超过一半以上的学生',
           NOW(),
           NOW()
       )
    ON DUPLICATE KEY UPDATE
                         `photo_url` = VALUES(`photo_url`),
                         `characteristics` = VALUES(`characteristics`),
                         `update_time` = NOW();

-- 插入沸羊羊
INSERT INTO `student_portrait` (`student_name`, `photo_url`, `characteristics`, `create_time`, `update_time`)
VALUES (
           '沸羊羊',
           'https://wx4.sinaimg.cn/mw690/005HKrQ2ly1hpzh5gw39pj30u00u0ai8.jpg',
           '黑皮体育生，一身腱子肉，学习成绩中游，体育成绩拿奖拿到手软',
           NOW(),
           NOW()
       )
    ON DUPLICATE KEY UPDATE
                         `photo_url` = VALUES(`photo_url`),
                         `characteristics` = VALUES(`characteristics`),
                         `update_time` = NOW();

-- 插入喜羊羊
INSERT INTO `student_portrait` (`student_name`, `photo_url`, `characteristics`, `create_time`, `update_time`)
VALUES (
           '喜羊羊',
           'https://5b0988e595225.cdn.sohucs.com/images/20200512/997f9a4a0b6a4e70aab49956b3768c74.png',
           '学习委员，各科都很强，常年考试第一，老师不会都找他',
           NOW(),
           NOW()
       )
    ON DUPLICATE KEY UPDATE
                         `photo_url` = VALUES(`photo_url`),
                         `characteristics` = VALUES(`characteristics`),
                         `update_time` = NOW();

-- 插入暖羊羊
INSERT INTO `student_portrait` (`student_name`, `photo_url`, `characteristics`, `create_time`, `update_time`)
VALUES (
           '暖羊羊',
           'https://pic.rmb.bdstatic.com/bjh/events/e5ef583e5114ffa67f4664637b6156eb.png@h_1280',
           '学习很刻苦很努力，就是笨，干学也不会，很努力的学习勉强维持个中游的水平',
           NOW(),
           NOW()
       )
    ON DUPLICATE KEY UPDATE
                         `photo_url` = VALUES(`photo_url`),
                         `characteristics` = VALUES(`characteristics`),
                         `update_time` = NOW();

-- 插入慢羊羊
INSERT INTO `student_portrait` (`student_name`, `photo_url`, `characteristics`, `create_time`, `update_time`)
VALUES (
           '慢羊羊',
           'https://wx1.sinaimg.cn/mw690/009gzukegy1i5586w1razj30ie0ie3zj.jpg',
           '上课就爱留号，一天天也不知道他寻思啥呢，就是不寻思学习',
           NOW(),
           NOW()
       )
    ON DUPLICATE KEY UPDATE
                         `photo_url` = VALUES(`photo_url`),
                         `characteristics` = VALUES(`characteristics`),
                         `update_time` = NOW();

-- 插入灰太狼
INSERT INTO `student_portrait` (`student_name`, `photo_url`, `characteristics`, `create_time`, `update_time`)
VALUES (
           '灰太狼',
           'https://wx1.sinaimg.cn/mw690/005JCY4Qgy1i5rh9r3xbij30zu1hotef.jpg',
           '话匣子，一上课就跟同桌唠嗑，嘴就叭叭叭不停',
           NOW(),
           NOW()
       )
    ON DUPLICATE KEY UPDATE
                         `photo_url` = VALUES(`photo_url`),
                         `characteristics` = VALUES(`characteristics`),
                         `update_time` = NOW();

-- ============================================
-- 初始化完成
-- ============================================
