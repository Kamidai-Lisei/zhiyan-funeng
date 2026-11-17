/*
SQLyog Ultimate v12.08 (64 bit)
MySQL - 8.0.33 : Database - aigcoj_pro1
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`aigcoj_pro1` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `aigcoj_pro1`;

/*Table structure for table `post` */

DROP TABLE IF EXISTS `post`;

CREATE TABLE `post` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `title` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '内容',
  `tags` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签列表（json 数组）',
  `thumbNum` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  `favourNum` int NOT NULL DEFAULT '0' COMMENT '收藏数',
  `userId` bigint NOT NULL COMMENT '创建用户 id',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子';

/*Data for the table `post` */

/*Table structure for table `post_favour` */

DROP TABLE IF EXISTS `post_favour`;

CREATE TABLE `post_favour` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `postId` bigint NOT NULL COMMENT '帖子 id',
  `userId` bigint NOT NULL COMMENT '创建用户 id',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_postId` (`postId`),
  KEY `idx_userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子收藏';

/*Data for the table `post_favour` */

/*Table structure for table `post_thumb` */

DROP TABLE IF EXISTS `post_thumb`;

CREATE TABLE `post_thumb` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `postId` bigint NOT NULL COMMENT '帖子 id',
  `userId` bigint NOT NULL COMMENT '创建用户 id',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_postId` (`postId`),
  KEY `idx_userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子点赞';

/*Data for the table `post_thumb` */

/*Table structure for table `question` */

DROP TABLE IF EXISTS `question`;

CREATE TABLE `question` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `title` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '内容',
  `tags` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签列表（json 数组）',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '题目答案',
  `submitNum` int NOT NULL DEFAULT '0' COMMENT '题目提交数',
  `acceptedNum` int NOT NULL DEFAULT '0' COMMENT '题目通过数',
  `judgeCase` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '判题用例（json 数组）',
  `judgeConfig` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '判题配置（json 对象）',
  `thumbNum` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  `favourNum` int NOT NULL DEFAULT '0' COMMENT '收藏数',
  `userId` bigint NOT NULL COMMENT '创建用户 id',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_userId` (`userId`)
) ENGINE=InnoDB AUTO_INCREMENT=1917575981229215746 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目';

/*Data for the table `question` */

insert  into `question`(`id`,`title`,`content`,`tags`,`answer`,`submitNum`,`acceptedNum`,`judgeCase`,`judgeConfig`,`thumbNum`,`favourNum`,`userId`,`createTime`,`updateTime`,`isDelete`) values (1869392320718675970,'两数之和','### Description\n\n输入两个数字，输出它们之和\n\n### Input\n\n一行两个数字A,B(0<=A,B<100)\n\n### Output\n\n输出这两个数字之和\n### Sample Input\n\n1 2\n\n### Sample Output\n\n3','[\"简单\",\"入门\"]','public class Main {\n  public static void main(String[] args) {\n    int a = Integer.parseInt(args[0]);\n    int b = Integer.parseInt(args[1]);\n    System.out.print(a + b);\n  }\n}',85,71,'[{\"input\":\"1 1\",\"output\":\"2\"},{\"input\":\"1 2\",\"output\":\"3\"}]','{\"timeLimit\":5,\"memoryLimit\":20000,\"stackLimit\":5000}',0,0,1,'2024-12-18 22:40:35','2025-04-05 01:10:33',0);

/*Table structure for table `question_comment` */

DROP TABLE IF EXISTS `question_comment`;

CREATE TABLE `question_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `questionId` bigint DEFAULT NULL COMMENT '问题id',
  `userId` bigint DEFAULT NULL COMMENT '用户id',
  `userName` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户昵称',
  `userAvatar` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户头像',
  `content` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '评论内容',
  `parentId` bigint DEFAULT NULL COMMENT '父级评论id',
  `commentNum` int DEFAULT NULL COMMENT '回复条数',
  `likeCount` int DEFAULT NULL COMMENT '点赞数量',
  `isLike` tinyint(1) DEFAULT NULL COMMENT '是否点赞',
  `likeListId` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '点赞id列表',
  `inputShow` tinyint(1) DEFAULT NULL COMMENT '是否显示输入框',
  `fromId` bigint DEFAULT NULL COMMENT '被回复的记录id',
  `fromName` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '回复人名称',
  `gmtModified` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `gmtCreate` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `isDeleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除 1（true）已删除， 0（false）未删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1908208623817691138 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `question_comment` */

insert  into `question_comment`(`id`,`questionId`,`userId`,`userName`,`userAvatar`,`content`,`parentId`,`commentNum`,`likeCount`,`isLike`,`likeListId`,`inputShow`,`fromId`,`fromName`,`gmtModified`,`gmtCreate`,`isDeleted`) values (1908161451004907521,1869392320718675970,1908152443967729665,'Jay','https://tse3-mm.cn.bing.net/th/id/OIP-C.sJw8rxtlIkLSQ2FCO5CIxgHaHa?rs=1&pid=ImgDetMain','这道题怎么做',-1,2,0,0,'[]',0,-1,'','2025-04-04 22:15:16','2025-04-04 22:15:16',0),(1908161684694704129,1869392320718675970,1886334158867619845,'AI助手','https://tse4-mm.cn.bing.net/th/id/OIP-C.K_QFIZsqhnGF2Uos2JXzpAHaHa?rs=1&pid=ImgDetMain','直接读取输入的两个整数相加输出即可，例如用C++可以这样写：cin>>a>>b; cout<<a+b;',1908161451004907521,1,0,0,'[]',1,1908152443967729665,'Jay','2025-04-04 22:16:12','2025-04-04 22:16:12',0),(1908185801569452034,1869392320718675970,1908152443967729665,'Jay','https://tse3-mm.cn.bing.net/th/id/OIP-C.sJw8rxtlIkLSQ2FCO5CIxgHaHa?rs=1&pid=ImgDetMain','我也很好奇',-1,2,1,1,'[1908152443967729665]',0,-1,'','2025-04-04 23:52:02','2025-04-04 23:52:02',0),(1908185836327649281,1869392320718675970,1886334158867619845,'AI助手','https://tse4-mm.cn.bing.net/th/id/OIP-C.K_QFIZsqhnGF2Uos2JXzpAHaHa?rs=1&pid=ImgDetMain','请问您具体对什么感到好奇呢？我可以帮您解答关于这道题目或其他编程相关的问题。',1908185801569452034,1,NULL,NULL,'[]',1,1908152443967729665,'Jay','2025-04-04 23:52:10','2025-04-04 23:52:10',1),(1908185876412612609,1869392320718675970,1908152443967729665,'Jay','https://tse3-mm.cn.bing.net/th/id/OIP-C.sJw8rxtlIkLSQ2FCO5CIxgHaHa?rs=1&pid=ImgDetMain','不知道',1908185801569452034,NULL,NULL,NULL,'[]',NULL,1886334158867619845,'AI助手','2025-04-04 23:52:19','2025-04-04 23:52:19',1),(1908185944167399425,1869392320718675970,1886334158867619845,'AI助手','https://tse4-mm.cn.bing.net/th/id/OIP-C.K_QFIZsqhnGF2Uos2JXzpAHaHa?rs=1&pid=ImgDetMain','你好，请问你具体对什么感到好奇呢？可以详细说明一下吗？',1908185801569452034,NULL,NULL,NULL,'[]',NULL,1908152443967729665,'Jay','2025-04-04 23:52:36','2025-04-04 23:52:36',1),(1908193782688235521,1869392320718675970,1908152443967729665,'Jay','https://tse3-mm.cn.bing.net/th/id/OIP-C.sJw8rxtlIkLSQ2FCO5CIxgHaHa?rs=1&pid=ImgDetMain','帮我讲一下思路\n',-1,NULL,NULL,NULL,'[]',NULL,-1,'','2025-04-05 00:23:44','2025-04-05 00:23:44',0),(1908193812241301505,1869392320718675970,1886334158867619845,'AI助手','https://tse4-mm.cn.bing.net/th/id/OIP-C.K_QFIZsqhnGF2Uos2JXzpAHaHa?rs=1&pid=ImgDetMain','这道题的思路很简单：直接读取输入的两个整数，将它们相加后输出结果即可，注意题目保证输入范围在0到100之间所以不需要额外处理。',1908193782688235521,NULL,NULL,NULL,'[]',NULL,1908152443967729665,'Jay','2025-04-05 00:23:52','2025-04-05 00:23:52',0),(1908194872708153346,1869392320718675970,1908152443967729665,'Jay','https://tse3-mm.cn.bing.net/th/id/OIP-C.sJw8rxtlIkLSQ2FCO5CIxgHaHa?rs=1&pid=ImgDetMain','明白了',1908161451004907521,NULL,NULL,NULL,'[]',NULL,1886334158867619845,'AI助手','2025-04-05 00:28:04','2025-04-05 00:28:04',0),(1908208623817691137,1869392320718675970,1886334158867619845,'AI助手','https://tse4-mm.cn.bing.net/th/id/OIP-C.K_QFIZsqhnGF2Uos2JXzpAHaHa?rs=1&pid=ImgDetMain','请问您具体对什么感到好奇呢？我可以帮您解答与编程或算法相关的问题。',1908185801569452034,NULL,NULL,NULL,'[]',NULL,1908152443967729665,'Jay','2025-04-05 01:22:43','2025-04-05 01:22:43',0);

/*Table structure for table `question_submit` */

DROP TABLE IF EXISTS `question_submit`;

CREATE TABLE `question_submit` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `language` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '编程语言',
  `code` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户代码',
  `judgeInfo` text COLLATE utf8mb4_unicode_ci COMMENT '判题信息（json 对象）',
  `status` int NOT NULL DEFAULT '0' COMMENT '判题状态（0 - 待判题、1 - 判题中、2 - 成功、3 - 失败）',
  `questionId` bigint NOT NULL COMMENT '题目 id',
  `userId` bigint NOT NULL COMMENT '创建用户 id',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_questionId` (`questionId`),
  KEY `idx_userId` (`userId`)
) ENGINE=InnoDB AUTO_INCREMENT=1917575996911718402 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目提交';

/*Data for the table `question_submit` */

insert  into `question_submit`(`id`,`language`,`code`,`judgeInfo`,`status`,`questionId`,`userId`,`createTime`,`updateTime`,`isDelete`) values (1908153514928685057,'java','public class Main {\n  public static void main(String[] args) {\n    int a = Integer.parseInt(args[0]);\n    int b = Integer.parseInt(args[1]);\n    System.out.print(a + b);\n  }\n}','{\"message\":\"Accepted\",\"memory\":10240,\"time\":15}',2,1869392320718675970,1908152443967729665,'2025-04-04 21:43:44','2025-04-04 21:43:52',0),(1908188002555641858,'java','public class Main {\n  public static void main(String[] args) {\n    int a = Integer.parseInt(args[0]);\n    int b = Integer.parseInt(args[1]);\n    System.out.print(a + b);\n  }\n}','{\"message\":\"Accepted\",\"memory\":12345,\"time\":12}',2,1869392320718675970,1908152443967729665,'2025-04-05 00:00:46','2025-04-05 00:00:53',0),(1908192629028143106,'java','public class Main {\n  public static void main(String[] args) {\n    int a = Integer.parseInt(args[0]);\n    int b = Integer.parseInt(args[1]);\n    System.out.print(a + b);\n  }\n}','{\"message\":\"Accepted\",\"memory\":12345,\"time\":42}',2,1869392320718675970,1908152443967729665,'2025-04-05 00:19:09','2025-04-05 00:19:18',0),(1908193335634149377,'java','public class Main {\n  public static void main(String[] args) {\n    int a = Integer.parseInt(args[0]);\n    int b = Integer.parseInt(args[1]);\n    System.out.print(a + b);\n  }\n}','{\"message\":\"Accepted\",\"memory\":10240,\"time\":15}',2,1869392320718675970,1908152443967729665,'2025-04-05 00:21:58','2025-04-05 00:22:04',0),(1908193410598944770,'cpp','public class Main {\n  public static void main(String[] args) {\n    int a = Integer.parseInt(args[0]);\n    int b = Integer.parseInt(args[1]);\n    System.out.print(a + b);\n  }\n}','{\"message\":\"Wrong Answer\",\"memory\":0,\"time\":0}',2,1869392320718675970,1908152443967729665,'2025-04-05 00:22:16','2025-04-05 00:22:22',0),(1908193519885729794,'cpp','#include <iostream>\n#include <cstdlib> // 用于 atoi\n\nint main(int argc, char* argv[]) {\n    if (argc < 3) {\n        std::cerr << \"请提供两个数字作为参数\" << std::endl;\n        return 1;\n    }\n\n    int a = std::atoi(argv[1]); // 字符串转整数\n    int b = std::atoi(argv[2]);\n    std::cout << a + b << std::endl;\n\n    return 0;\n}','{\"message\":\"Accepted\",\"memory\":1024,\"time\":5}',2,1869392320718675970,1908152443967729665,'2025-04-05 00:22:42','2025-04-05 00:22:49',0);

/*Table structure for table `switch` */

DROP TABLE IF EXISTS `switch`;

CREATE TABLE `switch` (
  `id` bigint DEFAULT NULL,
  `flag` tinyint(1) DEFAULT NULL,
  `exegesis` text COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `switch` */

insert  into `switch`(`id`,`flag`,`exegesis`) values (1,1,'AIJudge'),(2,1,'AIBot');

/*Table structure for table `user` */

DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `userAccount` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账号',
  `userPassword` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `unionId` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '微信开放平台id',
  `mpOpenId` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '公众号openId',
  `userName` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户昵称',
  `userAvatar` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户头像',
  `userProfile` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户简介',
  `userRole` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'user' COMMENT '用户角色：user/admin/ban',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_unionId` (`unionId`)
) ENGINE=InnoDB AUTO_INCREMENT=1908152443967729666 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户';

/*Data for the table `user` */

insert  into `user`(`id`,`userAccount`,`userPassword`,`unionId`,`mpOpenId`,`userName`,`userAvatar`,`userProfile`,`userRole`,`createTime`,`updateTime`,`isDelete`) values (1886334158867619845,'aiaiai','19889b89a3729917320465ada5deb74f',NULL,NULL,'AI','https://tse4-mm.cn.bing.net/th/id/OIP-C.K_QFIZsqhnGF2Uos2JXzpAHaHa?rs=1&pid=ImgDetMain','AIBOT','admin','2025-03-09 18:27:42','2025-04-04 22:02:20',0),(1908152443967729665,'jayjay','19889b89a3729917320465ada5deb74f',NULL,NULL,'Jay','https://tse3-mm.cn.bing.net/th/id/OIP-C.sJw8rxtlIkLSQ2FCO5CIxgHaHa?rs=1&pid=ImgDetMain','周杰伦','admin','2025-04-04 21:39:29','2025-04-04 23:23:10',0);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
