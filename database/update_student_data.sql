-- ============================================
-- 数据更新脚本：为学生画像补全字段并插入测试数据
-- 执行日期：2025-04-15
-- ============================================

USE `ai_teacher`;

-- ============================================
-- 1. 为现有数据补全字段
-- ============================================
-- 为懒羊羊补全数据
UPDATE `student_portrait` 
SET `age` = 10,
    `exam_history` = '[
        {"examName": "期中考试", "examDate": "2024-11-15", "subject": "语文", "score": 45, "fullScore": 100, "rank": 45, "remarks": "基础薄弱，需要加强"},
        {"examName": "期中考试", "examDate": "2024-11-15", "subject": "数学", "score": 38, "fullScore": 100, "rank": 48, "remarks": "计算能力差"},
        {"examName": "期中考试", "examDate": "2024-11-15", "subject": "英语", "score": 42, "fullScore": 100, "rank": 46, "remarks": "单词记忆困难"}
    ]'
WHERE `student_name` = '懒羊羊';

-- 为美羊羊补全数据
UPDATE `student_portrait` 
SET `age` = 10,
    `exam_history` = '[
        {"examName": "期中考试", "examDate": "2024-11-15", "subject": "语文", "score": 82, "fullScore": 100, "rank": 12, "remarks": "作文写得很好"},
        {"examName": "期中考试", "examDate": "2024-11-15", "subject": "数学", "score": 75, "fullScore": 100, "rank": 18, "remarks": "应用题需要加强"},
        {"examName": "期中考试", "examDate": "2024-11-15", "subject": "英语", "score": 88, "fullScore": 100, "rank": 8, "remarks": "口语表达流利"}
    ]'
WHERE `student_name` = '美羊羊';

-- 为沸羊羊补全数据
UPDATE `student_portrait` 
SET `age` = 11,
    `exam_history` = '[
        {"examName": "期中考试", "examDate": "2024-11-15", "subject": "语文", "score": 65, "fullScore": 100, "rank": 28, "remarks": "阅读理解需要加强"},
        {"examName": "期中考试", "examDate": "2024-11-15", "subject": "数学", "score": 68, "fullScore": 100, "rank": 25, "remarks": "几何题做得不错"},
        {"examName": "期中考试", "examDate": "2024-11-15", "subject": "英语", "score": 62, "fullScore": 100, "rank": 30, "remarks": "语法需要加强"},
        {"examName": "运动会", "examDate": "2024-10-20", "subject": "体育", "score": 98, "fullScore": 100, "rank": 1, "remarks": "100米短跑冠军"}
    ]'
WHERE `student_name` = '沸羊羊';

-- 为喜羊羊补全数据
UPDATE `student_portrait` 
SET `age` = 10,
    `exam_history` = '[
        {"examName": "期中考试", "examDate": "2024-11-15", "subject": "语文", "score": 95, "fullScore": 100, "rank": 2, "remarks": "作文满分"},
        {"examName": "期中考试", "examDate": "2024-11-15", "subject": "数学", "score": 98, "fullScore": 100, "rank": 1, "remarks": "奥数竞赛一等奖"},
        {"examName": "期中考试", "examDate": "2024-11-15", "subject": "英语", "score": 92, "fullScore": 100, "rank": 3, "remarks": "听力满分"},
        {"examName": "月考", "examDate": "2024-10-20", "subject": "语文", "score": 93, "fullScore": 100, "rank": 1, "remarks": "持续优秀"},
        {"examName": "月考", "examDate": "2024-10-20", "subject": "数学", "score": 96, "fullScore": 100, "rank": 1, "remarks": "解题思路清晰"}
    ]'
WHERE `student_name` = '喜羊羊';

-- 为暖羊羊补全数据
UPDATE `student_portrait` 
SET `age` = 11,
    `exam_history` = '[
        {"examName": "期中考试", "examDate": "2024-11-15", "subject": "语文", "score": 72, "fullScore": 100, "rank": 22, "remarks": "很努力，继续加油"},
        {"examName": "期中考试", "examDate": "2024-11-15", "subject": "数学", "score": 68, "fullScore": 100, "rank": 25, "remarks": "基础题掌握不错"},
        {"examName": "期中考试", "examDate": "2024-11-15", "subject": "英语", "score": 70, "fullScore": 100, "rank": 23, "remarks": "单词记得很牢"}
    ]'
WHERE `student_name` = '暖羊羊';

-- 为慢羊羊补全数据
UPDATE `student_portrait` 
SET `age` = 10,
    `exam_history` = '[
        {"examName": "期中考试", "examDate": "2024-11-15", "subject": "语文", "score": 55, "fullScore": 100, "rank": 38, "remarks": "注意力不集中"},
        {"examName": "期中考试", "examDate": "2024-11-15", "subject": "数学", "score": 52, "fullScore": 100, "rank": 40, "remarks": "上课走神"},
        {"examName": "期中考试", "examDate": "2024-11-15", "subject": "英语", "score": 58, "fullScore": 100, "rank": 36, "remarks": "需要提高专注力"}
    ]'
WHERE `student_name` = '慢羊羊';

-- 为灰太狼补全数据
UPDATE `student_portrait` 
SET `age` = 11,
    `exam_history` = '[
        {"examName": "期中考试", "examDate": "2024-11-15", "subject": "语文", "score": 78, "fullScore": 100, "rank": 16, "remarks": "口才很好"},
        {"examName": "期中考试", "examDate": "2024-11-15", "subject": "数学", "score": 85, "fullScore": 100, "rank": 10, "remarks": "逻辑思维强"},
        {"examName": "期中考试", "examDate": "2024-11-15", "subject": "英语", "score": 75, "fullScore": 100, "rank": 18, "remarks": "上课爱说话"}
    ]'
WHERE `student_name` = '灰太狼';

-- ============================================
-- 2. 插入新的测试数据
-- ============================================
-- 插入红太狼
INSERT INTO `student_portrait` (`student_name`, `photo_url`, `characteristics`, `age`, `exam_history`, `create_time`, `update_time`)
SELECT '红太狼',
       'https://img2.baidu.com/it/u=3650234567,1234567890&fm=253&app=138&f=JPEG?w=500&h=500',
       '脾气暴躁，对自己和他人要求都很高，学习成绩优秀，但性格强势，喜欢指挥别人',
       11,
       '[
        {"examName": "期中考试", "examDate": "2024-11-15", "subject": "语文", "score": 90, "fullScore": 100, "rank": 4, "remarks": "作文立意深刻"},
        {"examName": "期中考试", "examDate": "2024-11-15", "subject": "数学", "score": 92, "fullScore": 100, "rank": 3, "remarks": "解题严谨"},
        {"examName": "期中考试", "examDate": "2024-11-15", "subject": "英语", "score": 88, "fullScore": 100, "rank": 8, "remarks": "语法掌握很好"}
    ]',
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM `student_portrait` WHERE `student_name` = '红太狼');

-- 插入小灰灰
INSERT INTO `student_portrait` (`student_name`, `photo_url`, `characteristics`, `age`, `exam_history`, `create_time`, `update_time`)
SELECT '小灰灰',
       'https://img1.baidu.com/it/u=1234567890,9876543210&fm=253&app=138&f=JPEG?w=500&h=500',
       '天真可爱，学习成绩一般，但很有礼貌，乐于助人，和同学们相处得很好',
       8,
       '[
        {"examName": "期中考试", "examDate": "2024-11-15", "subject": "语文", "score": 65, "fullScore": 100, "rank": 28, "remarks": "需要加强阅读"},
        {"examName": "期中考试", "examDate": "2024-11-15", "subject": "数学", "score": 60, "fullScore": 100, "rank": 32, "remarks": "计算需要细心"},
        {"examName": "期中考试", "examDate": "2024-11-15", "subject": "英语", "score": 62, "fullScore": 100, "rank": 30, "remarks": "单词量需要增加"}
    ]',
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM `student_portrait` WHERE `student_name` = '小灰灰');
