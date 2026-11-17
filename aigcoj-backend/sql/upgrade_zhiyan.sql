-- =============================================================
-- 智研赋能 · 升级脚本（在已有 aigcoj 库上执行）
-- 新增 AI 长期记忆表：承载画像快照 / 择校推演报告 / 个性化规划
-- =============================================================

CREATE TABLE IF NOT EXISTS `ai_memory` (
  `id`         bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `userId`     bigint       NOT NULL COMMENT '用户 id',
  `memoryType` varchar(32)  NOT NULL DEFAULT 'note' COMMENT 'portrait 画像 / simulate 推演 / plan 规划 / note 笔记',
  `title`      varchar(256) NOT NULL DEFAULT '' COMMENT '记忆标题',
  `content`    text         NULL COMMENT '记忆内容（markdown）',
  `createTime` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_type` (`userId`, `memoryType`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='AI 长期记忆（一人一档）';

-- 27 考研初试日为 2026-12-19，修正旧默认目标日期
UPDATE `study_plan` SET `targetDate` = '2026-12-19' WHERE `targetDate` = '2026-12-26';
