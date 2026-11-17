-- 澄清题库来源：quiz_question 中的 8 套"XXXX 年 408 真题"卷为 AI 按考试大纲考点命制的练习题，
-- 并非当年原题；历年真题原卷以 study_material 中 2009-2026 的 PDF 为准（category='408 真题'）。
UPDATE `quiz_question`
SET `paper`  = REPLACE(`paper`, ' 年 408 真题', ' 模拟卷·AI 命制'),
    `source` = CONCAT(LEFT(`paper`, 4), ' 考点精练（按大纲命制，非原题）')
WHERE `paper` LIKE '%408 真题';

UPDATE `quiz_question`
SET `source` = REPLACE(`source`, '真题', '考点精练')
WHERE `source` LIKE '%真题%';
