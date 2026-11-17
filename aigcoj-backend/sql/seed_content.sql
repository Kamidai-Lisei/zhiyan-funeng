USE `aigcoj_pro1`;

-- 演示账号：jayjay / kaoyan2026（管理员），aiaiai / kaoyan2026
-- 密码规则：md5('panghu' + 明文)，后端要求明文长度 >= 8
UPDATE `user` SET `userPassword` = '8720cbd6e7a400d9857bc95fcd396d4a',
  `userAvatar` = '',
  `userProfile` = '24 考研 408 选手｜记录复习与上岸'
WHERE `id` = 1908152443967729665;
UPDATE `user` SET `userPassword` = '8720cbd6e7a400d9857bc95fcd396d4a', `userAvatar` = ''
WHERE `id` = 1886334158867619845;

REPLACE INTO `study_material`
(`id`,`title`,`description`,`category`,`subject`,`fileType`,`fileUrl`,`coverUrl`,`fileSize`,`downloadNum`,`favourNum`,`viewNum`,`tags`,`uploaderId`,`status`,`createTime`,`updateTime`,`isDelete`)
VALUES
(4000000000000000001,'408 数据结构核心考点手册','线性表、树、图、排序与查找的系统梳理，附常考结论与易错点。','408','ds','pdf','/api/file/material/ds-handbook.pdf','',43000,328,96,1520,'["数据结构","核心考点","一轮复习"]',1,1,NOW(),NOW(),0),
(4000000000000000002,'计算机组成原理：Cache 与存储体系','Cache 映射、命中率计算、替换算法与写策略，重点计算题全解析。','408','co','pdf','/api/file/material/co-cache.pdf','',31000,265,74,1180,'["计组","Cache","计算题"]',1,1,NOW(),NOW(),0),
(4000000000000000003,'操作系统：进程、内存与文件','进程调度、同步互斥、死锁、分页分段与页面置换，一册吃透 OS 大题。','408','os','pdf','/api/file/material/os-process.pdf','',49000,301,88,1390,'["操作系统","进程","大题"]',1,1,NOW(),NOW(),0),
(4000000000000000004,'计算机网络：TCP/IP 高频考点','三次握手、四次挥手、拥塞控制、IP 与路由，协议细节与计算题。','408','cn','pdf','/api/file/material/cn-tcp.pdf','',41000,288,81,1260,'["计算机网络","TCP","协议"]',1,1,NOW(),NOW(),0),
(4000000000000000005,'408 历年真题分类汇编使用指南','按套卷、专题、错题三轮刷真题的方法与答题技巧。','408',NULL,'pdf','/api/file/material/408-papers.pdf','',40000,412,132,2180,'["408","真题","刷题方法"]',1,1,NOW(),NOW(),0),
(4000000000000000006,'考研数学一公式速查','高数、线代、概率核心公式，考前 30 天随身复盘。','数学',NULL,'pdf','/api/file/material/math-formula.pdf','',33000,356,110,1740,'["数学一","公式","考前"]',1,1,NOW(),NOW(),0),
(4000000000000000007,'考研英语高频词汇精选','动词、名词、形容词与写作替换，每天 30 个稳步积累。','英语',NULL,'pdf','/api/file/material/english-5500.pdf','',33000,298,84,1320,'["英语","词汇","写作"]',1,1,NOW(),NOW(),0),
(4000000000000000008,'考研政治冲刺背诵提纲','马原、毛中特、史纲、思修法基与时政，按模块记忆。','政治',NULL,'pdf','/api/file/material/politics-outline.pdf','',42000,241,67,1090,'["政治","背诵","冲刺"]',1,1,NOW(),NOW(),0),
(4000000000000000009,'计算机复试面试 200 问（精选）','专业基础、项目追问、算法手撕、英语口语与综合面试。','复试',NULL,'pdf','/api/file/material/interview-200.pdf','',47000,389,145,1960,'["复试","面试","英语"]',1,1,NOW(),NOW(),0),
(4000000000000000010,'数据结构代码模板合集','链表、树、图、排序的 Java / Python 模板，可直接默写。','408','ds','pdf','/api/file/material/ds-handbook.pdf','',43000,176,52,860,'["数据结构","代码模板","默写"]',1,1,NOW(),NOW(),0),
(4000000000000000011,'操作系统页面置换专题训练','FIFO、LRU、OPT、Clock 四种算法的计算步骤与真题。','408','os','pdf','/api/file/material/os-process.pdf','',49000,154,41,720,'["操作系统","页面置换","专题"]',1,1,NOW(),NOW(),0),
(4000000000000000012,'计算机网络 HTTP 与 DNS 专题','HTTP 版本演进、状态码、DNS 查询过程与缓存机制。','408','cn','pdf','/api/file/material/cn-tcp.pdf','',41000,143,38,680,'["计算机网络","HTTP","DNS"]',1,1,NOW(),NOW(),0),
(4000000000000000013,'408 复习计划模板与进度表','按月份拆分的四科复习计划，可直接照着执行。','408',NULL,'pdf','/api/file/material/408-papers.pdf','',40000,207,63,940,'["408","学习计划","模板"]',1,1,NOW(),NOW(),0);

REPLACE INTO `post`
(`id`,`title`,`content`,`tags`,`thumbNum`,`favourNum`,`userId`,`createTime`,`updateTime`,`isDelete`)
VALUES
(3000000000000000001,'408 从 90 到 130：我的三轮复习法',
'## 背景\n本科双非，一战 408 90 分，二战 130。踩过的坑比走过的路还多，把我的复习节奏分享给学弟学妹。\n\n## 第一轮：搭框架（3-6 月）\n- 数据结构跟王道，每章画一张思维导图\n- 计组重点啃存储层次与指令系统\n- OS 把进程、内存两条主线吃透\n- 计网按分层记协议，别死背\n\n## 第二轮：刷真题（7-9 月）\n按套卷限时 180 分钟，做完只统计失分点，不急着看解析。\n\n## 第三轮：错题回炉（10-12 月）\n每道错题写三行：错因、正确思路、下次怎么避免。\n\n> 408 不是背出来的，是做题做出来的。',
'["经验","408","复习方法"]',126,58,1908152443967729665,NOW(),NOW(),0),
(3000000000000000002,'数据结构：二叉树遍历的 5 种写法',
'## 先序 / 中序 / 后序\n递归写法最直观，但面试更爱考非递归。\n\n```java\nvoid preorder(TreeNode root) {\n    if (root == null) return;\n    visit(root);\n    preorder(root.left);\n    preorder(root.right);\n}\n```\n\n## 非递归要点\n- 先序：根入栈，出栈访问，右左入栈\n- 中序：一路向左，出栈访问，转向右\n- 后序：根右左入栈，最后反转\n\n## 层序遍历\nBFS + 队列，按层记录 size。\n\n## Morris\n空间 O(1)，利用空闲右指针，适合进阶。',
'["数据结构","二叉树","笔记"]',88,42,1908152443967729665,NOW(),NOW(),0),
(3000000000000000003,'操作系统页面置换：FIFO 与 LRU 到底怎么算',
'## FIFO\n先进先出，用一个指针循环替换。\n\n**例题**：3 个物理块，序列 1 2 3 4 1 2 5 1 2 3 4 5\n缺页次数 = 10。\n\n## LRU\n淘汰最近最久未使用的页面。手算时画一个栈：每次访问把页面移到栈顶。\n\n## Belady 异常\nFIFO 可能出现物理块增加、缺页反而增加的情况，LRU 不会。\n\n## 答题模板\n1. 画物理块表格\n2. 每次访问标命中/缺页\n3. 统计缺页次数与缺页率',
'["操作系统","页面置换","笔记"]',64,31,1908152443967729665,NOW(),NOW(),0),
(3000000000000000004,'计算机复试英语口语模板（亲测有效）',
'## 自我介绍（1 分钟）\nGood morning, professors. I am XXX, majoring in Computer Science. During my undergraduate study, I focused on data structures and database systems...\n\n## 为什么选择本校本专业\n- 学科实力与导师方向\n- 本科项目与课程的延续\n- 明确的科研兴趣\n\n## 项目介绍\nUse STAR: Situation, Task, Action, Result.\n\n## 不会答怎么办\nKeep calm, ask for clarification, and talk about what you know.\n\n> 背模板不如练到能自然说出来。',
'["复试","英语","口语"]',73,36,1908152443967729665,NOW(),NOW(),0),
(3000000000000000005,'双非上岸 985：择校与心态',
'## 择校\n看三点：专业课科目、复试比例、近三年录取分数。别只看分数线，要看录取名单的实际分数分布。\n\n## 心态\n- 不要和别人比进度\n- 每周留半天休息\n- 记录每天完成的事，而不是没完成的事\n\n## 冲刺期\n固定作息，减少信息摄入，专注错题与真题。\n\n> 上岸的人不是最聪明的，是最后还在按计划走的人。',
'["经验","择校","心态"]',95,47,1908152443967729665,NOW(),NOW(),0),
(3000000000000000006,'TCP 三次握手为什么不是两次？',
'## 核心原因\n两次握手无法确认客户端的接收能力，也可能因历史连接请求造成服务端资源浪费。\n\n## 三次握手做了什么\n1. 客户端 SYN，服务端确认客户端发送能力\n2. 服务端 SYN+ACK，客户端确认服务端收发能力\n3. 客户端 ACK，服务端确认客户端接收能力\n\n## 如果只有两次\n旧的 SYN 到达服务端，服务端直接建立连接并等待，客户端却不理它，连接空占资源。\n\n## 面试追问\n- 为什么 TIME_WAIT 要 2MSL？\n- SYN 洪泛怎么防？\n- 半连接队列与全连接队列？',
'["计算机网络","TCP","面试"]',142,66,1908152443967729665,NOW(),NOW(),0),
(3000000000000000007,'求推荐好用的 408 刷题顺序',
'目前数据结构过完一遍，计组刚开，OS 和计网还没动。\n\n想请教大家：\n1. 四科应该并行还是串行？\n2. 真题什么时候开始比较合适？\n3. 每天 408 分配多少时间？\n\n目标 110+，求过来人指点。',
'["求助","408","规划"]',38,12,1908152443967729665,NOW(),NOW(),0);

REPLACE INTO `post_comment` (`id`,`postId`,`userId`,`content`,`parentId`,`createTime`,`isDelete`) VALUES
(5000000000000000001,3000000000000000001,1886334158867619845,'三轮节奏很清晰，第二轮开始真题会不会太早？',0,NOW(),0),
(5000000000000000002,3000000000000000001,1908152443967729665,'如果基础过完一遍就不早，真题是最好的查漏工具。',5000000000000000001,NOW(),0),
(5000000000000000003,3000000000000000006,1886334158867619845,'补充一点：两次握手还可能让服务端反复接收旧 SYN。',0,NOW(),0),
(5000000000000000004,3000000000000000007,1886334158867619845,'建议数据结构 + 计组并行，OS 和计网稍后接上，真题 9 月开始。',0,NOW(),0);

-- 最近 14 天打卡
INSERT INTO `study_checkin` (`userId`,`checkinDate`,`duration`,`content`,`createTime`) VALUES
(1908152443967729665,DATE_SUB(CURDATE(), INTERVAL 0 DAY),220,'数据结构：图的最短路径 + 2 道题',NOW()),
(1908152443967729665,DATE_SUB(CURDATE(), INTERVAL 1 DAY),180,'计组：Cache 映射计算',NOW()),
(1908152443967729665,DATE_SUB(CURDATE(), INTERVAL 2 DAY),200,'OS：页面置换专题',NOW()),
(1908152443967729665,DATE_SUB(CURDATE(), INTERVAL 3 DAY),150,'计网：TCP 拥塞控制',NOW()),
(1908152443967729665,DATE_SUB(CURDATE(), INTERVAL 5 DAY),240,'408 真题一套 + 复盘',NOW()),
(1908152443967729665,DATE_SUB(CURDATE(), INTERVAL 6 DAY),120,'英语单词 60 个',NOW()),
(1908152443967729665,DATE_SUB(CURDATE(), INTERVAL 7 DAY),190,'数据结构：排序算法',NOW()),
(1908152443967729665,DATE_SUB(CURDATE(), INTERVAL 9 DAY),210,'数学：多元函数微分',NOW()),
(1908152443967729665,DATE_SUB(CURDATE(), INTERVAL 10 DAY),170,'OS：进程同步',NOW()),
(1908152443967729665,DATE_SUB(CURDATE(), INTERVAL 12 DAY),160,'计组：指令系统',NOW())
ON DUPLICATE KEY UPDATE `duration` = VALUES(`duration`), `content` = VALUES(`content`);

-- 408 章节进度
INSERT INTO `study_progress` (`userId`,`module`,`slug`,`finished`,`createTime`,`updateTime`) VALUES
(1908152443967729665,'ds','intro',1,NOW(),NOW()),
(1908152443967729665,'ds','linear',1,NOW(),NOW()),
(1908152443967729665,'co','intro',1,NOW(),NOW()),
(1908152443967729665,'os','intro',1,NOW(),NOW()),
(1908152443967729665,'cn','intro',1,NOW(),NOW())
ON DUPLICATE KEY UPDATE `finished` = VALUES(`finished`);

-- 学习计划
INSERT INTO `study_plan` (`userId`,`targetDate`,`dailyMinutes`,`subjects`,`motto`,`createTime`,`updateTime`) VALUES
(1908152443967729665,'2026-12-19',240,'数据结构,计算机组成原理,操作系统,计算机网络','把每一天的复习，变成上岸的确定性。',NOW(),NOW())
ON DUPLICATE KEY UPDATE `targetDate` = VALUES(`targetDate`), `dailyMinutes` = VALUES(`dailyMinutes`);

-- ============ 收藏演示数据 ============
DELETE FROM `material_favour` WHERE `userId` = 1908152443967729665;
INSERT INTO `material_favour` (`materialId`,`userId`) VALUES
(4000000000000000001,1908152443967729665),
(4000000000000000004,1908152443967729665),
(4000000000000000006,1908152443967729665);

DELETE FROM `question_favour` WHERE `userId` = 1908152443967729665;
INSERT INTO `question_favour` (`questionId`,`userId`) VALUES
(2000000000000000002,1908152443967729665),
(2000000000000000005,1908152443967729665),
(2000000000000000007,1908152443967729665);

-- ============ 简历演示数据（前端简历工坊读取 content JSON） ============
REPLACE INTO `resume` (`id`,`userId`,`title`,`targetSchool`,`targetMajor`,`content`,`score`,`template`,`createTime`,`updateTime`,`isDelete`)
VALUES (1,1908152443967729665,'李同学的简历','浙江大学','计算机技术（专硕）',
'{"name":"李同学","phone":"188****4321","email":"li@example.com","city":"杭州","summary":"计算机科学与技术应届本科生，专业排名前 8%，408 专业课基础扎实，熟悉 Java / Spring Boot 后端开发，做过两个完整的工程实践项目，期望攻读计算机技术专硕。","education":[{"school":"杭州电子科技大学","major":"计算机科学与技术","degree":"本科","time":"2022.09 - 2026.06","gpa":"3.78/4.0（专业前 8%）"}],"projects":[{"name":"基于大语言模型的编程题评分系统","role":"后端负责人","time":"2025.03 - 2025.10","desc":"实现代码沙箱（本地/远程/AI 三种策略）、判题策略与 AI 代码点评，支撑 16 道题目的在线评测；设计资料库与学习打卡模块，累计服务 200+ 用户。","tech":"Spring Boot、MyBatis-Plus、Redis、Sa-Token、MySQL、Vue3"},{"name":"408 考研学习平台前端","role":"前端开发","time":"2025.06 - 2025.11","desc":"使用 Vue3 + Vite + TypeScript 搭建题库、论坛、资料库与简历工坊四大模块，集成 Monaco 编辑器与 PDF 预览。","tech":"Vue3、TypeScript、Pinia、Arco Design"}],"skills":["Java / Spring Boot / MyBatis-Plus","MySQL / Redis / Git / Linux","数据结构与算法（LeetCode 300+）","408 四门专业课系统复习"],"awards":["校级一等奖学金（2023、2024）","蓝桥杯 Java 组省一等奖","全国大学生数学建模竞赛省二等奖"]}',
72,'classic',NOW(),NOW(),0);
