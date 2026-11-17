-- ============================================================
-- 考研学习平台 扩展表结构（v2）
-- 数据库：aigcoj_pro1
-- ============================================================
USE `aigcoj_pro1`;

-- 代码分析结果
CREATE TABLE IF NOT EXISTS `code_analysis` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `question_submit_id` bigint NOT NULL COMMENT '提交记录 id',
  `analysis_result` longtext COMMENT 'AI 分析结果',
  `code_score` int DEFAULT NULL COMMENT '代码评分',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_submit` (`question_submit_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 代码分析';

-- 学习资料
CREATE TABLE IF NOT EXISTS `study_material` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL COMMENT '资料标题',
  `description` varchar(1024) DEFAULT NULL COMMENT '简介',
  `category` varchar(64) NOT NULL COMMENT '分类：408/数学/英语/政治/复试/经验',
  `subject` varchar(32) DEFAULT NULL COMMENT '408 科目：ds/co/os/cn',
  `fileType` varchar(32) DEFAULT 'pdf' COMMENT '文件类型',
  `fileUrl` varchar(1024) DEFAULT NULL COMMENT '文件地址',
  `coverUrl` varchar(1024) DEFAULT NULL COMMENT '封面',
  `fileSize` bigint DEFAULT '0' COMMENT '文件大小（字节）',
  `downloadNum` int NOT NULL DEFAULT '0',
  `favourNum` int NOT NULL DEFAULT '0',
  `viewNum` int NOT NULL DEFAULT '0',
  `tags` varchar(512) DEFAULT NULL COMMENT '标签 json 数组',
  `uploaderId` bigint NOT NULL DEFAULT '1',
  `status` int NOT NULL DEFAULT '1' COMMENT '1 上架 0 下架',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `isDelete` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_subject` (`subject`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习资料';

CREATE TABLE IF NOT EXISTS `material_favour` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `materialId` bigint NOT NULL,
  `userId` bigint NOT NULL,
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_material_user` (`materialId`,`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资料收藏';

-- 简历
CREATE TABLE IF NOT EXISTS `resume` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `userId` bigint NOT NULL,
  `title` varchar(255) NOT NULL DEFAULT '我的简历',
  `targetSchool` varchar(255) DEFAULT NULL COMMENT '目标院校',
  `targetMajor` varchar(255) DEFAULT NULL COMMENT '目标专业',
  `content` longtext COMMENT '结构化简历 json',
  `score` int DEFAULT '0' COMMENT 'AI 评分',
  `template` varchar(32) DEFAULT 'classic' COMMENT '模板',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `isDelete` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历';

CREATE TABLE IF NOT EXISTS `resume_analysis` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `resumeId` bigint NOT NULL,
  `userId` bigint NOT NULL,
  `score` int DEFAULT NULL,
  `result` longtext,
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_resume` (`resumeId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历诊断记录';

-- 打卡
CREATE TABLE IF NOT EXISTS `study_checkin` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `userId` bigint NOT NULL,
  `checkinDate` date NOT NULL,
  `duration` int DEFAULT '0' COMMENT '学习分钟数',
  `content` varchar(512) DEFAULT NULL COMMENT '今日总结',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`userId`,`checkinDate`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习打卡';

-- 408 章节进度
CREATE TABLE IF NOT EXISTS `study_progress` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `userId` bigint NOT NULL,
  `module` varchar(32) NOT NULL,
  `slug` varchar(128) NOT NULL,
  `finished` tinyint NOT NULL DEFAULT '1',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_chapter` (`userId`,`module`,`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='章节学习进度';

-- 学习计划
CREATE TABLE IF NOT EXISTS `study_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `userId` bigint NOT NULL,
  `targetDate` date DEFAULT NULL COMMENT '考试日期',
  `dailyMinutes` int DEFAULT '180',
  `subjects` varchar(255) DEFAULT NULL COMMENT '重点科目',
  `motto` varchar(255) DEFAULT NULL,
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习计划';

-- 帖子评论
CREATE TABLE IF NOT EXISTS `post_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `postId` bigint NOT NULL,
  `userId` bigint NOT NULL,
  `content` varchar(5000) NOT NULL,
  `parentId` bigint NOT NULL DEFAULT '0',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `isDelete` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_post` (`postId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子评论';

-- 题目收藏 / 错题本
CREATE TABLE IF NOT EXISTS `question_favour` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `questionId` bigint NOT NULL,
  `userId` bigint NOT NULL,
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_question_user` (`questionId`,`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目收藏';

-- 判题开关改为 2：使用本地沙箱（保留 AI 能力用于代码分析）
UPDATE `switch` SET `flag` = 2 WHERE `id` = 1;
UPDATE `switch` SET `flag` = 1 WHERE `id` = 2;
