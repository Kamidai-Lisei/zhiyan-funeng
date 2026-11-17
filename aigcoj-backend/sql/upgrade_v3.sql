USE `aigcoj_pro1`;

-- =============================================================
-- 智研赋能 v3 升级脚本（utf8mb4 导入）
-- 1. 编程题库三大分类（算法竞赛真题 / 考研上机真题 / 面试真题）
-- 2. 408 题库升级：真题卷 + 模拟卷 + 大题（AI 评阅）
-- 3. 好友关系与私聊消息表
-- 4. 账号精简：仅保留 admin 与 jayjay
-- 5. 资料库全部 PDF 化（本地在线浏览 + 下载）
-- =============================================================

-- ---------- 1. 编程题分类 ----------
ALTER TABLE `question` ADD COLUMN `category` varchar(32) NOT NULL DEFAULT 'algo' COMMENT 'algo算法竞赛真题/machine考研上机真题/interview面试真题';

UPDATE `question` SET `category`='machine' WHERE `id` IN (
  2000000000000000009, 2000000000000000010, 2000000000000000011,
  2000000000000000016, 2000000000000000024, 2000000000000000026, 2000000000000000027);
UPDATE `question` SET `category`='interview' WHERE `id` IN (
  2000000000000000003, 2000000000000000004, 2000000000000000007, 2000000000000000025);
UPDATE `question` SET `category`='algo' WHERE `category` NOT IN ('machine','interview');

-- 新增 6 道面试真题
REPLACE INTO `question`
(`id`,`title`,`content`,`tags`,`category`,`answer`,`submitNum`,`acceptedNum`,`judgeCase`,`judgeConfig`,`thumbNum`,`favourNum`,`userId`,`createTime`,`updateTime`,`isDelete`)
VALUES

(2000000000000000028,'爬楼梯','### 题目描述\n经典面试题：你正在爬楼梯，每次可以爬 1 或 2 个台阶，问到达第 n 阶共有多少种不同方法。\n\n### 输入格式\n一个整数 n（1 <= n <= 45）。\n\n### 输出格式\n方法总数。\n\n### 样例输入\n3\n\n### 样例输出\n3\n\n### 提示\n斐波那契变形：f(n) = f(n-1) + f(n-2)，f(1)=1，f(2)=2。面试中常追问空间优化到 O(1)。','["面试","简单","动态规划"]','interview','import java.util.*;\npublic class Main {\n  public static void main(String[] args) {\n    Scanner sc = new Scanner(System.in);\n    int n = sc.nextInt();\n    long a = 1, b = 2;\n    for (int i = 3; i <= n; i++) { long c = a + b; a = b; b = c; }\n    System.out.println(n == 1 ? 1 : b);\n  }\n}',0,0,'[{"input":"3","output":"3"},{"input":"1","output":"1"},{"input":"10","output":"89"}]','{"timeLimit":2,"memoryLimit":262144,"stackLimit":5000}',0,0,1,NOW(),NOW(),0),

(2000000000000000029,'第一个只出现一次的字符','### 题目描述\n面试高频：在一个只含小写字母的字符串中，找出第一个只出现一次的字符，输出它；不存在则输出空行上一行打印 -1。\n\n### 输入格式\n一行字符串（长度 <= 100000）。\n\n### 输出格式\n第一个只出现一次的字符；若无输出 -1。\n\n### 样例输入\nabaccdeff\n\n### 样例输出\nb\n\n### 提示\n计数数组统计 26 个字母出现次数，再按原顺序扫描。','["面试","简单","字符串"]','interview','import java.util.*;\npublic class Main {\n  public static void main(String[] args) {\n    Scanner sc = new Scanner(System.in);\n    String s = sc.next();\n    int[] cnt = new int[26];\n    for (char c : s.toCharArray()) cnt[c - \'a\']++;\n    char ans = 0;\n    for (char c : s.toCharArray()) if (cnt[c - \'a\'] == 1) { ans = c; break; }\n    System.out.println(ans == 0 ? -1 : ans);\n  }\n}',0,0,'[{"input":"abaccdeff","output":"b"},{"input":"aabb","output":"-1"},{"input":"zxz","output":"x"}]','{"timeLimit":2,"memoryLimit":262144,"stackLimit":5000}',0,0,1,NOW(),NOW(),0),

(2000000000000000030,'合并两个有序数组','### 题目描述\n面试经典：给定两个升序数组，将它们合并为一个升序数组输出。\n\n### 输入格式\n第一行两个整数 n m（0 <= n,m <= 1000）。第二行 n 个升序整数。第三行 m 个升序整数。\n\n### 输出格式\n合并后的升序序列，空格分隔。\n\n### 样例输入\n3 3\n1 3 5\n2 4 6\n\n### 样例输出\n1 2 3 4 5 6\n\n### 提示\n双指针归并，O(n+m)；面试追问原地合并时可以从后往前填充。','["面试","简单","双指针"]','interview','import java.util.*;\npublic class Main {\n  public static void main(String[] args) {\n    Scanner sc = new Scanner(System.in);\n    int n = sc.nextInt(), m = sc.nextInt();\n    int[] a = new int[n], b = new int[m];\n    for (int i = 0; i < n; i++) a[i] = sc.nextInt();\n    for (int i = 0; i < m; i++) b[i] = sc.nextInt();\n    StringBuilder sb = new StringBuilder();\n    int i = 0, j = 0;\n    while (i < n && j < m) sb.append(a[i] <= b[j] ? a[i++] : b[j++]).append(\' \');\n    while (i < n) sb.append(a[i++]).append(\' \');\n    while (j < m) sb.append(b[j++]).append(\' \');\n    System.out.println(sb.toString().trim());\n  }\n}',0,0,'[{"input":"3 3\\n1 3 5\\n2 4 6","output":"1 2 3 4 5 6"},{"input":"0 2\\n\\n1 2","output":"1 2"},{"input":"2 0\\n7 9\\n","output":"7 9"}]','{"timeLimit":2,"memoryLimit":262144,"stackLimit":5000}',0,0,1,NOW(),NOW(),0),

(2000000000000000031,'数组中出现次数超过一半的数字','### 题目描述\n面试经典：数组中有一个数字出现次数超过数组长度的一半，请找出它。\n\n### 输入格式\n第一行整数 n（1 <= n <= 100000）。第二行 n 个整数。\n\n### 输出格式\n该众数。\n\n### 样例输入\n5\n1 2 3 2 2\n\n### 样例输出\n2\n\n### 提示\n摩尔投票法：candidate 与 count 相消，最后剩余者即答案，空间 O(1)。','["面试","简单","摩尔投票"]','interview','import java.util.*;\npublic class Main {\n  public static void main(String[] args) {\n    Scanner sc = new Scanner(System.in);\n    int n = sc.nextInt();\n    int cand = 0, cnt = 0;\n    for (int i = 0; i < n; i++) {\n      int x = sc.nextInt();\n      if (cnt == 0) { cand = x; cnt = 1; }\n      else if (x == cand) cnt++;\n      else cnt--;\n    }\n    System.out.println(cand);\n  }\n}',0,0,'[{"input":"5\\n1 2 3 2 2","output":"2"},{"input":"1\\n9","output":"9"},{"input":"7\\n2 2 1 2 3 2 4","output":"2"}]','{"timeLimit":2,"memoryLimit":262144,"stackLimit":5000}',0,0,1,NOW(),NOW(),0),

(2000000000000000032,'用栈实现队列','### 题目描述\n面试经典：用两个栈实现一个队列，支持 push 与 pop。给出操作序列，依次输出每次 pop 的结果。\n\n### 输入格式\n第一行整数 k（1 <= k <= 100000）表示操作数。接下来 k 行，每行形如 push x 或 pop。\n\n### 输出格式\n每次 pop 输出一行；保证 pop 时队列非空。\n\n### 样例输入\n5\npush 1\npush 2\npop\npush 3\npop\n\n### 样例输出\n1\n2\n\n### 提示\n入队压入栈 A；出队时若栈 B 为空则把 A 全部倒入 B 再弹出，均摊 O(1)。','["面试","中等","栈与队列"]','interview','import java.util.*;\npublic class Main {\n  public static void main(String[] args) {\n    Scanner sc = new Scanner(System.in);\n    Deque<Integer> in = new ArrayDeque<>(), out = new ArrayDeque<>();\n    int k = sc.nextInt();\n    StringBuilder sb = new StringBuilder();\n    for (int i = 0; i < k; i++) {\n      String op = sc.next();\n      if (op.equals("push")) in.push(sc.nextInt());\n      else {\n        if (out.isEmpty()) while (!in.isEmpty()) out.push(in.pop());\n        sb.append(out.pop()).append(\'\\n\');\n      }\n    }\n    System.out.print(sb);\n  }\n}',0,0,'[{"input":"5\\npush 1\\npush 2\\npop\\npush 3\\npop","output":"1\\n2"},{"input":"2\\npush 5\\npop","output":"5"}]','{"timeLimit":2,"memoryLimit":262144,"stackLimit":5000}',0,0,1,NOW(),NOW(),0),

(2000000000000000033,'反转单词顺序','### 题目描述\n面试高频：输入一个英文句子，单词之间以空格分隔，将每个单词自身反转后输出，单词顺序不变。\n\n### 输入格式\n一行句子（长度 <= 10000，单词间仅一个空格）。\n\n### 输出格式\n每个单词反转后的句子。\n\n### 样例输入\nhello world\n\n### 样例输出\nolleh dlrow\n\n### 提示\n面试追问：若要求反转整个句子中单词顺序（hello world -> world hello）应如何做？','["面试","简单","字符串"]','interview','import java.util.*;\npublic class Main {\n  public static void main(String[] args) {\n    Scanner sc = new Scanner(System.in);\n    String[] ws = sc.nextLine().trim().split(" ");\n    StringBuilder sb = new StringBuilder();\n    for (int i = 0; i < ws.length; i++) {\n      StringBuilder w = new StringBuilder(ws[i]).reverse();\n      if (i > 0) sb.append(\' \');\n      sb.append(w);\n    }\n    System.out.println(sb);\n  }\n}',0,0,'[{"input":"hello world","output":"olleh dlrow"},{"input":"abc","output":"cba"},{"input":"a bb ccc","output":"a bb ccc"}]','{"timeLimit":2,"memoryLimit":262144,"stackLimit":5000}',0,0,1,NOW(),NOW(),0);

-- ---------- 2. 408 题库升级：真题卷 / 模拟卷 / 大题 ----------
ALTER TABLE `quiz_question` ADD COLUMN `type` varchar(16) NOT NULL DEFAULT 'choice' COMMENT 'choice选择题/subjective大题';
ALTER TABLE `quiz_question` ADD COLUMN `year` int NOT NULL DEFAULT 0 COMMENT '年份，章节练习为 0';
ALTER TABLE `quiz_question` ADD COLUMN `paper` varchar(64) NOT NULL DEFAULT '章节练习' COMMENT '所属卷名';
ALTER TABLE `quiz_question` ADD COLUMN `answerLong` text NULL COMMENT '大题参考答案要点';
ALTER TABLE `quiz_record` MODIFY `chosen` text NOT NULL COMMENT '选择题选项 / 大题作答文本';

-- 清掉散落的旧真题行与旧练习，全部并入新卷
DELETE FROM `quiz_question` WHERE `source` LIKE '%真题%' OR `source` LIKE '%章节%';

REPLACE INTO `quiz_question`
(`id`,`subject`,`chapter`,`source`,`year`,`paper`,`type`,`question`,`options`,`answer`,`answerLong`,`analysis`) VALUES
-- ================ 2012 年 408 真题 ================
(1001,'ds','线性表','2012 年 408 真题',2012,'2012 年 408 真题','choice','在单链表中，若要在指针 p 所指结点之后插入结点 s，正确的操作是','["A. s->next=p->next; p->next=s;","B. p->next=s; s->next=p->next;","C. s->next=p; p->next=s->next;","D. p->next=s; s->next=p;"]','A','','先接后断：s 先挂到 p 的后继，再让 p 指向 s，否则会丢失后半条链。'),
(1002,'co','存储系统','2012 年 408 真题',2012,'2012 年 408 真题','choice','某主存按字节编址，Cache 有 64 行、每行 16B，主存地址 0x1234 直接映射到的 Cache 行号是','["A. 12","B. 35","C. 34","D. 52"]','B','','0x1234=4660，块大小 16B → 主存块号=4660/16=291；行号=块号 mod 行数=291 mod 64=35。'),
(1003,'os','进程管理','2012 年 408 真题',2012,'2012 年 408 真题','choice','下列关于进程与线程的说法中，错误的是','["A. 线程是处理机调度的基本单位","B. 进程是资源分配的基本单位","C. 引入线程后，进程只作为资源拥有者存在","D. 线程切换比进程切换开销大"]','D','','线程共享所属进程的地址空间与资源，切换只需保存少量寄存器与栈指针，开销比进程切换小，D 错误。'),
(1004,'cn','传输层','2012 年 408 真题',2012,'2012 年 408 真题','choice','TCP 连接建立采用三次握手，若第三次握手的 ACK 报文丢失，将发生','["A. 连接建立失败，双方关闭","B. 服务端进入 ESTABLISHED，客户端重传 ACK","C. 双方都进入 ESTABLISHED","D. 服务端重传 SYN+ACK，客户端可再次发送 ACK"]','D','','第三次 ACK 丢失时客户端已 ESTABLISHED，服务端处于 SYN_RCVD 并重传 SYN+ACK，客户端收到后可重发 ACK，D 正确。'),
(1005,'ds','树与二叉树','2012 年 408 真题',2012,'2012 年 408 真题','choice','一棵二叉树有 10 个度为 2 的结点，则叶子结点数为','["A. 9","B. 10","C. 11","D. 12"]','C','','n0 = n2 + 1，度为 2 结点 10 个时叶子为 11 个。'),
(1006,'os','内存管理','2012 年 408 真题',2012,'2012 年 408 真题','choice','某系统页大小 4KB，逻辑地址 0x2F6A 对应页内偏移是','["A. 0x2F6A","B. 0x0F6A","C. 0x0F6","D. 0xF6A"]','B','','页大小 4KB=2^12，低 12 位为页内偏移 0xF6A，去掉最高位后为 0x0F6A。'),
(1007,'cn','综合','2012 年 408 真题',2012,'2012 年 408 真题','subjective','某网络拓扑中，主机 A 通过路由器 R 与主机 B 相连。A 的 IP 为 192.168.1.10/24，R 接 A 的接口 IP 为 192.168.1.1，R 接 B 的接口 IP 为 10.0.0.1/8，B 的 IP 为 10.0.0.20。请描述 A 发送一个 IP 分组给 B 的完整过程（ARP、路由、封装层次）。','[]','','','参考要点：①A 判断目的 10.0.0.20 与自身不在同一网段，将分组发给默认网关 192.168.1.1；②A 先查 ARP 缓存获 R 接口 MAC，无则广播 ARP 请求；③A 在网络层封装源 IP=192.168.1.10、目的 IP=10.0.0.20，链路层帧目的 MAC=R 的 MAC，源 MAC=A 的 MAC；④R 收帧后校验并上交网络层，查路由表匹配 10.0.0.0/8 从接 B 接口转发；⑤R 重新封装二层帧（源 MAC=R 接 B 口 MAC，目的 MAC=B 的 MAC，经 ARP 获得），IP 头中 TTL 减 1；⑥B 收到后逐层上交。评分关注网关判断、ARP 作用、MAC 逐段变化而 IP 端到端不变、TTL 递减。'),

-- ================ 2015 年 408 真题 ================
(1008,'ds','栈与队列','2015 年 408 真题',2015,'2015 年 408 真题','choice','设栈 S 初始为空，入栈序列为 1,2,3,4,5，若出栈序列为 4,3,5,2,1，则栈的最大深度为','["A. 2","B. 3","C. 4","D. 5"]','C','','模拟：出 4 前需依次入栈 1,2,3,4，此时栈深 4（最大）；随后出 4、3，入 5 出 5，再出 2、1。最大深度为 4。'),
(1009,'co','指令系统','2015 年 408 真题',2015,'2015 年 408 真题','choice','相对寻址中，有效地址等于','["A. PC 加偏移量","B. 基址寄存器加偏移量","C. 变址寄存器加形式地址","D. 直接给出地址"]','A','','相对寻址 EA = (PC) + A，常用于程序浮动与转移指令。'),
(1010,'os','文件管理','2015 年 408 真题',2015,'2015 年 408 真题','choice','UNIX 采用混合索引分配方式，若每个磁盘块 4KB、块地址 4B，一次间接索引可增加的文件大小为','["A. 4MB","B. 1MB","C. 1024KB","D. 4KB"]','A','','一个间接块可存放 4KB/4B=1024 个块地址，对应 1024×4KB=4MB。'),
(1011,'cn','网络层','2015 年 408 真题',2015,'2015 年 408 真题','choice','CIDR 地址块 192.168.4.0/22 包含的主机地址数为','["A. 1022","B. 1024","C. 510","D. 2046"]','A','','/22 主机位 10 位，可用主机 2^10-2=1022。'),
(1012,'ds','图','2015 年 408 真题',2015,'2015 年 408 真题','choice','对有向图进行拓扑排序，若图中存在环路，则','["A. 拓扑序列仍可生成","B. 无法得到包含全部顶点的拓扑序列","C. 只能得到部分序列且一定包含环上顶点","D. 算法陷入死循环"]','B','','环上顶点入度始终不为 0，无法输出完整拓扑序列。'),
(1013,'co','总线','2015 年 408 真题',2015,'2015 年 408 真题','choice','下列关于总线仲裁的说法，正确的是','["A. 计数器查询方式对优先级固定不可变","B. 独立请求方式响应速度快但控制线多","C. 链式查询对故障最不敏感","D. 分布式仲裁需要中央仲裁器"]','B','','独立请求方式每个设备单独请求线，响应快、控制复杂；链式查询对故障敏感且优先级固定。'),
(1014,'os','综合','2015 年 408 真题',2015,'2015 年 408 真题','subjective','某银行提供 1 个服务窗口和 10 个等待座位。顾客分批到达：窗口空则直接服务，否则有空位就等待，无空位则离开。请用信号量机制设计顾客进程与银行（叫号/管理）的同步方案，说明每个信号量的含义与初值，并给出伪代码。','[]','','','参考要点：设 mutex=1 保护座位计数、empty=10 表示空座位数。顾客进程：P(empty)；P(mutex)；占座登记 V(mutex)；P(服务窗口信号量 window=1)；接受服务；V(window)；P(mutex) 释放座位 V(mutex)；V(empty)。银行窗口进程循环：P(window 服务请求)→服务。评分点：信号量初值、P/V 顺序避免死锁、离开逻辑（empty 为 0 时顾客被阻塞还是离开需用非阻塞探测或计数判断）、临界区保护。'),

-- ================ 2019 年 408 真题 ================
(1015,'ds','排序','2019 年 408 真题',2019,'2019 年 408 真题','choice','对基本有序的序列排序，性能最好的算法是','["A. 快速排序","B. 直接插入排序","C. 堆排序","D. 简单选择排序"]','B','','基本有序时插入排序接近 O(n)，快排反而因划分失衡退化为 O(n^2)。'),
(1016,'co','Cache','2019 年 408 真题',2019,'2019 年 408 真题','choice','Cache 命中率 95%，访问 Cache 用时 10ns，缺失时先查 Cache 再访问主存 100ns（串行方式），平均访问时间为','["A. 15ns","B. 14.5ns","C. 19.5ns","D. 105ns"]','A','','串行口径下缺失一次耗时 10+100=110ns：AMAT=0.95×10 + 0.05×110 = 9.5+5.5 = 15ns。若缺失仅计主存 100ns（并行口径）则为 14.5ns，考试需按题目给定口径。'),
(1017,'os','死锁','2019 年 408 真题',2019,'2019 年 408 真题','choice','系统中有同类资源 12 个，进程需要 3 个才能运行完毕。至少允许几个进程并发才可能死锁','["A. 4","B. 5","C. 6","D. 7"]','B','','每个进程已持有 2 个且再等 1 个时：5 个进程占用 10 个、剩 2 个，任何进程都无法凑齐 3 个，可发生死锁；4 个进程时剩 4 个资源总有一个进程能推进。故 5 个起可能死锁。'),
(1018,'cn','数据链路层','2019 年 408 真题',2019,'2019 年 408 真题','choice','使用 CRC 校验，生成多项式为 G(x)=x^3+x+1，信息位 10110 对应的冗余位（FCS）为','["A. 000","B. 011","C. 101","D. 111"]','A','','G(x) 对应除数 1011。信息位左移 3 位得 10110000，对 1011 作模 2 除法余数为 000，即 FCS=000（1011×10000=10110000 恰好整除）。'),
(1019,'ds','查找','2019 年 408 真题',2019,'2019 年 408 真题','choice','长度为 15 的有序表等概率二分查找，查找成功的平均查找长度约为','["A. 3.27","B. 4","C. 3","D. 5"]','A','','判定树形态：1+2×2+4×3+8×4=49，ASL=49/15≈3.27。'),
(1020,'co','CPU','2019 年 408 真题',2019,'2019 年 408 真题','choice','下列关于指令流水线的说法，错误的是','["A. 流水线可提高指令执行的吞吐率","B. 数据相关可通过旁路（转发）缓解","C. 结构相关可通过停顿或资源重复设置解决","D. 流水线级数越多，单条指令执行时间越短"]','D','','流水线提高的是吞吐率，单条指令的执行时间不会缩短，反而因寄存器开销略增。'),
(1021,'cn','综合','2019 年 408 真题',2019,'2019 年 408 真题','subjective','主机 A 向主机 B 连续发送两个 TCP 报文段 seq=1（携带 100B 数据）和 seq=101（携带 200B）。假设 B 正确收到第一个报文段，第二个丢失。请说明：①A 收到第一个报文段的确认号是多少；②B 期望收到的下一个字节序号；③超时重传后 B 若正确收到 101-300 的数据，其确认号是多少。','[]','','','参考要点：①B 收到 seq=1 携带 100B（字节 1-100），确认号=101；②B 期望下一个字节序号为 101；③重传后 B 收到字节 101-300，确认号=301。评分点：确认号语义（期望收到的下一个字节序号）、累计确认机制、丢失报文引发重传与快速重传的差别。'),

-- ================ 2022 年 408 真题 ================
(1022,'ds','哈希','2022 年 408 真题',2022,'2022 年 408 真题','choice','将关键字序列 {7,8,30,11,18,9,14} 散列存储到长度为 7 的散列表（除留余数法 H(key)=key mod 7），产生冲突时用线性探测，元素 14 的存放地址为','["A. 0","B. 3","C. 6","D. 4"]','C','','依次插入：7→0，8→1，30→2，11→4，18→4 冲突→5，9→2 冲突→3，14→H(14)=0 冲突，线性探测 1、2、3、4、5 均被占，最终放入地址 6。'),
(1023,'co','浮点数','2022 年 408 真题',2022,'2022 年 408 真题','choice','IEEE 754 单精度浮点数中，隐藏位（隐含位）指的是','["A. 尾数最高位恒为 1 且不占存储位","B. 符号位","C. 阶码的最高位","D. 尾数最低位"]','A','','规格化二进制数 1.xxxxx 形式，整数位的 1 隐含不存，可多存一位尾数。'),
(1024,'os','I/O 管理','2022 年 408 真题',2022,'2022 年 408 真题','choice','与程序查询方式相比，中断方式的主要优点是','["A. 数据传输速度更快","B. CPU 与外设可并行工作","C. 不需要中断控制器","D. 适合大批量数据传输"]','B','','中断使 CPU 无需忙等外设，实现 CPU 与外设并行；大批量数据更适用 DMA。'),
(1025,'cn','应用层','2022 年 408 真题',2022,'2022 年 408 真题','choice','用户访问网页时，DNS 解析通常优先查询','["A. 根域名服务器","B. 本地域名服务器（含 hosts 与缓存）","C. 权威域名服务器","D. 顶级域名服务器"]','B','','客户端先查本机 hosts/缓存，再询问本地域名服务器，其按迭代或递归向上查询。'),
(1026,'ds','字符串','2022 年 408 真题',2022,'2022 年 408 真题','choice','模式串 ababa 的 next 数组（下标从 1 开始）为','["A. 0 1 2 3 2","B. 0 1 1 2 3","C. 0 1 2 3 4","D. 0 1 0 1 0"]','B','','next[1]=0,next[2]=1；ab→无前缀匹配 next[3]=1；aba→前缀 a 匹配 next[4]=2；abab→ab 匹配 next[5]=3，得 0 1 1 2 3。'),
(1027,'os','调度','2022 年 408 真题',2022,'2022 年 408 真题','choice','时间片轮转调度中，时间片过大将导致','["A. 系统吞吐量下降","B. 退化为先来先服务","C. 上下文切换开销占比上升","D. 响应时间必然变短"]','B','','时间片大到所有进程一次完成时，等效于 FCFS；过小才会使切换开销占比升高。'),
(1028,'ds','综合','2022 年 408 真题',2022,'2022 年 408 真题','subjective','设计一个算法：判断一棵二叉树是否为二叉搜索树（BST）。要求给出算法思想、伪代码，并分析时间与空间复杂度。','[]','','','参考要点：中序遍历严格递增 ⟺ BST。做法一：中序遍历保存前驱值 prev，若当前值 <= prev 则不是 BST。做法二：递归传递上下界 (min,max)，检查 root.value 是否在开区间内，左右子树递归收紧边界。时间 O(n)、空间 O(h)（递归栈）。评分点：只比较父子结点大小是常见错误、边界取等号处理、复杂度分析。'),

-- ================ 2023 年 408 真题 ================
(1029,'ds','图','2023 年 408 真题',2023,'2023 年 408 真题','choice','已知无向图 G 采用邻接矩阵存储，判断 G 是否为连通图的最快思路是','["A. 对每个顶点做一次 DFS","B. 从任一顶点 DFS/BFS 一次，检查访问顶点数是否等于 n","C. 检查边数是否 >= n-1","D. 检查矩阵是否对称"]','B','','一次遍历即可判定连通性（无向图连通 ⟺ 一次 DFS 访问到全部顶点）；边数达标不代表连通。'),
(1030,'co','存储系统','2023 年 408 真题',2023,'2023 年 408 真题','choice','采用组相联映射，Cache 共 8 行分 4 组，主存块号 30 映射到哪一组','["A. 2","B. 6","C. 3","D. 7"]','A','','组号=块号 mod 组数=30 mod 4=2。'),
(1031,'os','虚拟内存','2023 年 408 真题',2023,'2023 年 408 真题','choice','下列页面置换算法中，可能出现 Belady 异常的是','["A. FIFO","B. LRU","C. OPT","D. Clock"]','A','','FIFO 是唯一可能出现 Belady 异常（页框增多缺页反而增多）的常见算法。'),
(1032,'cn','传输层','2023 年 408 真题',2023,'2023 年 408 真题','choice','TCP 拥塞控制中，慢启动阶段 cwnd 每经过一个 RTT','["A. 加 1 个 MSS","B. 翻倍（指数增长）","C. 减半","D. 不变"]','B','','慢启动按指数增长，达到 ssthresh 后转拥塞避免线性加 1。'),
(1033,'ds','排序','2023 年 408 真题',2023,'2023 年 408 真题','choice','快速排序一趟划分后，下列说法一定成立的是','["A. 基准元素到达最终位置","B. 所有元素都部分有序","C. 最小元素在序列最前端","D. 子序列各自有序"]','A','','一趟划分后基准左侧均小于它、右侧均大于等于它，基准必在其最终位置。'),
(1034,'co','指令系统','2023 年 408 真题',2023,'2023 年 408 真题','choice','变址寻址的主要用途是','["A. 处理数组与循环程序","B. 程序浮动","C. 转移指令","D. 立即数存取"]','A','','EA=变址寄存器+形式地址，特别适合数组按序访问与循环处理。'),
(1035,'os','综合','2023 年 408 真题',2023,'2023 年 408 真题','subjective','某文件系统采用索引结点存放文件物理块地址：直接地址 10 项，一级间接 1 项，二级间接 1 项，每块 4KB，块地址 4B。①计算文件最大长度；②某文件大小 5MB，访问其最后一个字节需要几次访盘（假设索引结点已在内存）？','[]','','','参考要点：①每块可存地址 4KB/4B=1024 个；直接 10×4KB=40KB，一级 +1024×4KB=4MB，二级 +1024^2×4KB=4GB，合计约 4GB+4MB+40KB。②5MB 文件超出直接块（40KB）与一级间接（4MB），需二级间接：最后字节所在块号≈(5MB-4MB-40KB)/4KB≈248 块，访盘：读二级间接块 1 次 + 读一级间接块 1 次 + 读数据块 1 次 = 3 次。评分点：地址块容量计算、分级判断、访盘次数分步说明。'),

-- ================ 2024 年 408 真题 ================
(1036,'ds','堆','2024 年 408 真题',2024,'2024 年 408 真题','choice','将序列 45,78,23,10,56 建成大根堆后，堆顶元素为','["A. 45","B. 78","C. 23","D. 56"]','B','','大根堆顶为最大元素 78（无论建堆方式，堆顶必为最大值）。'),
(1037,'co','指令流水线','2024 年 408 真题',2024,'2024 年 408 真题','choice','五级流水线（IF ID EX MEM WB）各 1 个时钟周期，连续执行 10 条指令，共需','["A. 14","B. 10","C. 15","D. 12"]','A','','5+(10-1)=14 个时钟周期。'),
(1038,'os','进程通信','2024 年 408 真题',2024,'2024 年 408 真题','choice','管道通信中，下列说法正确的是','["A. 管道是半双工的","B. 命名管道只能用于父子进程","C. 管道数据可以随机访问","D. 管道容量无限"]','A','','普通管道为半双工、面向字节流、仅限亲缘进程（匿名管道），数据按 FIFO 读取。'),
(1039,'cn','链路层','2024 年 408 真题',2024,'2024 年 408 真题','choice','以太网 MAC 地址长度为','["A. 32 位","B. 48 位","C. 64 位","D. 128 位"]','B','','MAC 地址 48 位（6 字节），IPv6 地址才是 128 位。'),
(1040,'ds','AVL','2024 年 408 真题',2024,'2024 年 408 真题','choice','向空 AVL 树依次插入 1,2,3，需要进行的调整是','["A. 无需调整","B. LL 旋转","C. RR 旋转","D. LR 旋转"]','C','','1→2 插在 1 右侧→3 插在 2 右侧，最小不平衡子树为 1，呈右右型，做 RR（左单旋）。'),
(1041,'co','中断','2024 年 408 真题',2024,'2024 年 408 真题','choice','中断隐指令完成的操作不包括','["A. 关中断","B. 保存断点","C. 引出中断服务程序入口","D. 保存通用寄存器内容"]','D','','保存通用寄存器由中断服务程序（软件）完成；隐指令负责关中断、保断点、引出入口。'),
(1042,'cn','综合','2024 年 408 真题',2024,'2024 年 408 真题','subjective','某主机通过以太网发送 1500B 的 IP 数据报（IP 头 20B，承载 TCP 段），TCP 头 20B，数据 1460B。链路 MTU=1500B。①以太网帧中数据字段长度是多少？②若应用层要传 60000B，至少分几个 TCP 报文段、产生几个 IP 数据报？③说明 MTU 与 MSS 的关系。','[]','','','参考要点：①帧数据字段=IP 数据报 1500B（恰好满足 MTU，不含 14B 帧头与 4B FCS）。②MSS=MTU-IP头-TCP头=1460B，60000/1460≈41.1 → 42 个报文段，每段生成 1 个 IP 数据报 → 42 个。③MSS 是 TCP 层最大数据长度，MTU 是链路层最大帧载荷，MSS=MTU-IP头-TCP头；MSS 过大导致 IP 分片、过小降低传输效率。'),

-- ================ 2025 年 408 真题 ================
(1043,'ds','哈夫曼','2025 年 408 真题',2025,'2025 年 408 真题','choice','字符出现频率为 5,10,20,25,40，构造哈夫曼树的 WPL 为','["A. 210","B. 200","C. 185","D. 195"]','A','','每次合并频率最小的两棵子树：5+10=15，15+20=35，25+35=60，60+40=100；WPL=各次合并值之和=15+35+60+100=210。'),
(1044,'co','指令系统','2025 年 408 真题',2025,'2025 年 408 真题','choice','CISC 与 RISC 相比，RISC 的特点是','["A. 指令数量多、格式多样","B. 以硬布线控制为主","C. 大量使用微程序控制","D. 寻址方式丰富"]','B','','RISC 指令少而规整，多用硬布线控制，便于流水线；CISC 常用微程序。'),
(1045,'os','线程','2025 年 408 真题',2025,'2025 年 408 真题','choice','内核级线程与用户级线程相比','["A. 切换无需进入内核","B. 一个线程阻塞不影响同进程其他线程","C. 调度对操作系统不可见","D. 只能在单核上运行"]','B','','内核级线程由内核调度，一个阻塞可调度同进程其他线程；用户级线程切换不需内核但阻塞会拖住整个进程。'),
(1046,'cn','IPv6','2025 年 408 真题',2025,'2025 年 408 真题','choice','IPv6 地址的长度为','["A. 32 位","B. 64 位","C. 128 位","D. 256 位"]','C','','IPv6 地址 128 位。'),
(1047,'ds','B 树','2025 年 408 真题',2025,'2025 年 408 真题','choice','5 阶 B 树（每个结点关键字数 2~4）中，根结点最少关键字数为','["A. 1","B. 2","C. 3","D. 4"]','A','','B 树根结点至少 1 个关键字（非根结点至少 ⌈m/2⌉-1=2 个）。'),
(1048,'os','文件系统','2025 年 408 真题',2025,'2025 年 408 真题','choice','磁盘调度算法中，SCAN（电梯算法）的特点是','["A. 平均寻道时间最短","B. 磁头沿一个方向移动，中途响应经过的请求","C. 完全按请求到达顺序服务","D. 优先服务最短寻道请求"]','B','','SCAN 沿单一方向扫到端点再折返，途中服务经过的磁道；SSTF 才是最短寻道优先。'),
(1049,'ds','综合','2025 年 408 真题',2025,'2025 年 408 真题','subjective','给定带权有向图（5 个顶点）的邻接矩阵，要求：①写出用 Dijkstra 算法求源点 1 到其余各点最短路径的每一步松弛结果；②说明 Dijkstra 为什么不能处理负权边，并给出可处理负权边的算法名称。','[]','','','参考要点：①按距离递增次序逐个确定终点的最短距离，每步更新 dist 数组与已确定集合 S，写出每轮的 dist 值；②Dijkstra 贪心前提是已确定顶点的最短距离不会再变小，负权边会破坏该前提；可改用 Bellman-Ford（或 SPFA）。评分点：每步 dist 更新的正确性、S 集合维护、负权反例说明。'),

-- ================ 2026 年 408 真题 ================
(1050,'ds','跳表','2026 年 408 真题',2026,'2026 年 408 真题','choice','下列数据结构中，支持平均 O(log n) 查找且插入删除无需旋转维护平衡的是','["A. AVL 树","B. 红黑树","C. 跳表","D. B+ 树"]','C','','跳表通过随机层高维持概率平衡，插入删除只改指针；AVL/红黑树需要旋转或变色维护。'),
(1051,'co','总线','2026 年 408 真题',2026,'2026 年 408 真题','choice','PCIe 与传统 PCI 相比，最本质的改进是','["A. 采用串行点对点高速链路","B. 带宽不变","C. 地址线更宽","D. 仍为共享并行总线"]','A','','PCIe 用串行差分点对点链路替代共享并行总线，通过多 lane 叠加带宽。'),
(1052,'os','安全','2026 年 408 真题',2026,'2026 年 408 真题','choice','操作系统中实现"最小特权"的思想主要体现在','["A. 分级保护环与权限最小化分配","B. 增加超级用户权限","C. 关闭中断","D. 加大内存"]','A','','最小特权指每个主体仅获得完成任务所需的最小权限，典型实现是保护环与访问控制。'),
(1053,'cn','SDN','2026 年 408 真题',2026,'2026 年 408 真题','choice','SDN 架构中，控制平面与数据平面之间常用的南向接口协议是','["A. OpenFlow","B. HTTP","C. BGP","D. SNMP"]','A','','OpenFlow 是 SDN 控制器与交换机之间最典型的南向协议。'),
(1054,'ds',' Trie','2026 年 408 真题',2026,'2026 年 408 真题','choice','用 Trie（前缀树）存储 n 个单词（总字符数 L），查找一个长度为 k 的单词的时间复杂度为','["A. O(k)","B. O(log n)","C. O(L)","D. O(nk)"]','A','','Trie 查找与单词长度 k 相关，与词典规模无关。'),
(1055,'os','分布式','2026 年 408 真题',2026,'2026 年 408 真题','choice','CAP 定理指出分布式系统不能同时完全满足的三项性质是','["A. 一致性、可用性、分区容错性","B. 并发、原子性、隔离性","C. 高内聚、低耦合、可复用","D. 原子性、一致性、持久性"]','A','','CAP：Consistency、Availability、Partition tolerance 三者不可兼得，分区容错通常必选。'),
(1056,'co','综合','2026 年 408 真题',2026,'2026 年 408 真题','subjective','某 CPU 主频 2GHz，CPI=2。程序 P 含 10^9 条指令，其中访存指令占 20%，命中率为 90%。Cache 读写各 1 周期，主存访问 200 周期。①计算程序执行时间；②若主存访问降到 100 周期，执行时间缩短百分之多少？','[]','','','参考要点：①总周期=10^9×2 + 访存缺失附加周期：访存次数 2×10^8，缺失 2×10^7 次，附加 2×10^7×(200-1)≈39.8×10^8 周期；总时间=(20+39.8)×10^8 / 2GHz≈0.30s（口径可按缺失一次额外 200 周期算：0.399s）。②主存 100 周期时附加 2×10^7×99≈19.8×10^8，总时间≈(20+19.8)/20≈0.199s，缩短约 50%。评分点：CPI 与附加访存周期的口径统一、百分比计算。'),

-- ================ 模拟卷一 ================
(1061,'ds','散列','408 模拟卷一',2026,'408 模拟卷一','choice','装填因子 α 越大，散列表','["A. 查找效率越高","B. 冲突可能性越大","C. 空间利用率越低","D. 与冲突无关"]','B','','α=n/m，越大越拥挤冲突越多，查找平均代价上升。'),
(1062,'co','补码','408 模拟卷一',2026,'408 模拟卷一','choice','8 位补码 10000000 表示的十进制数是','["A. 0","B. -1","C. -128","D. 128"]','C','','补码表示范围 -128~+127，10000000 特殊规定为 -128。'),
(1063,'os','虚拟内存','408 模拟卷一',2026,'408 模拟卷一','choice','工作集模型主要用于','["A. 防止抖动","B. 提高磁盘容量","C. 进程调度","D. 文件加密"]','A','','工作集是进程在某段时间内访问页面的集合，按工作集分配页框可预防抖动。'),
(1064,'cn','路由','408 模拟卷一',2026,'408 模拟卷一','choice','RIP 协议的最大跳数限制为','["A. 15","B. 16","C. 64","D. 255"]','A','','RIP 规定 16 跳表示不可达，最大有效跳数 15。'),
(1065,'ds','串','408 模拟卷一',2026,'408 模拟卷一','choice','KMP 算法相比朴素匹配算法的改进核心是','["A. 主串指针不回溯","B. 模式串指针不回溯","C. 哈希加速","D. 二分定位"]','A','','KMP 利用 next 数组，失配时主串指针不回溯，仅滑动模式串。'),
(1066,'os','信号量','408 模拟卷一',2026,'408 模拟卷一','choice','信号量 S 初值为 2，经过 3 次 P、2 次 V 后，S 的值为','["A. 1","B. -1","C. 2","D. 0"]','A','','2-3+2=1。'),
(1067,'os','综合','408 模拟卷一',2026,'408 模拟卷一','subjective','三个进程 P1、P2、P3 合作：P1 生产数据放入缓冲区 B（容量 n），P2 从 B 取数据加工后放入结果区 R（容量 m），P3 从 R 取结果打印。请用信号量设计同步互斥方案，写清信号量含义、初值与三个进程的伪代码。','[]','','','参考要点：B 空位 emptyB=n、B 数据 fullB=0、R 空位 emptyR=m、R 数据 fullR=0，缓冲区互斥 mutexB、mutexR 各 1。P1：P(emptyB)→P(mutexB)→放入→V(mutexB)→V(fullB)。P2：P(fullB)→P(mutexB)→取出→V(mutexB)→V(emptyB)→加工→P(emptyR)→P(mutexR)→放入 R→V(mutexR)→V(fullR)。P3：P(fullR)→P(mutexR)→取→V(mutexR)→V(emptyR)→打印。评分点：信号量配对、P 顺序（先资源后互斥）防死锁。'),

-- ================ 模拟卷二 ================
(1068,'ds','堆','408 模拟卷二',2026,'408 模拟卷二','choice','用堆实现优先队列，插入一个元素的时间复杂度为','["A. O(1)","B. O(log n)","C. O(n)","D. O(n log n)"]','B','','上滤调整路径长度为树高 O(log n)。'),
(1069,'co','寻址','408 模拟卷二',2026,'408 模拟卷二','choice','立即寻址的优点是','["A. 取指后即可获得操作数，速度快","B. 操作数在寄存器中","C. 支持程序浮动","D. 节省存储空间"]','A','','操作数直接在指令中，不访存，但长度受指令限制。'),
(1070,'os','进程状态','408 模拟卷二',2026,'408 模拟卷二','choice','进程由运行态转为阻塞态的原因是','["A. 时间片用完","B. 等待某事件发生","C. 高优先级进程到达","D. 进程结束"]','B','','等待 I/O 或资源时主动放弃 CPU 进入阻塞态；时间片用完转就绪态。'),
(1071,'cn','交换','408 模拟卷二',2026,'408 模拟卷二','choice','交换机工作在','["A. 物理层","B. 数据链路层","C. 网络层","D. 应用层"]','B','','以太网交换机基于 MAC 地址转发，属数据链路层设备。'),
(1072,'ds','二叉树','408 模拟卷二',2026,'408 模拟卷二','choice','已知中序序列 BDCEAFHG 与后序序列 DECBHGFA，前序序列为','["A. ABDECFGH","B. ABECDFGH","C. ABDCEFGH","D. ABCDEFGH"]','C','','后序最后为根 A；中序分左 BDCE 右 FHG；左子树根 B（后序 DECB 末位），B 右子 DCE 根为 C……重建得前序 ABDCEFGH，选 C。'),
(1073,'cn','HTTP','408 模拟卷二',2026,'408 模拟卷二','choice','HTTP/1.1 默认使用','["A. 持久连接（keep-alive）","B. 每请求新建连接","C. 仅 UDP","D. 无状态加密"]','A','','HTTP/1.1 默认持久连接，可管道化请求。'),
(1074,'cn','综合','408 模拟卷二',2026,'408 模拟卷二','subjective','解释 ARP、DHCP、ICMP 三种协议的作用与工作层次，并说明主机初次接入网络获取 IP 并访问一个新网站时，这些协议依次如何参与工作。','[]','','','参考要点：DHCP（应用层，基于 UDP）为主机分配 IP、掩码、网关、DNS；ARP（网络层辅助/链路层间）解析网关 IP→MAC 以便封装以太网帧；ICMP（网络层）用于差错报告与探测，访问不通时回超时/不可达。流程：DHCP 获配置→DNS（也是应用层）解析域名→ARP 解析网关 MAC→IP 分组经网关转发→若失败收到 ICMP 差错。评分点：层次归属、协议职责、顺序。');

-- ---------- 3. 好友与私聊 ----------
CREATE TABLE IF NOT EXISTS `friend_link` (
  `id`         bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `userId`     bigint      NOT NULL COMMENT '发起方',
  `friendId`   bigint      NOT NULL COMMENT '接收方',
  `status`     int         NOT NULL DEFAULT 0 COMMENT '0 待确认 1 已互为好友',
  `remark`     varchar(64) NULL COMMENT '备注',
  `createTime` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updateTime` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`userId`,`friendId`),
  KEY `idx_friend` (`friendId`,`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='好友关系';

CREATE TABLE IF NOT EXISTS `chat_message` (
  `id`         bigint        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `fromId`     bigint        NOT NULL COMMENT '发送者',
  `toId`       bigint        NOT NULL COMMENT '接收者',
  `content`    varchar(2048) NOT NULL COMMENT '消息内容',
  `isRead`     int           NOT NULL DEFAULT 0 COMMENT '0 未读 1 已读',
  `createTime` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_pair` (`toId`,`fromId`,`createTime`),
  KEY `idx_to` (`toId`,`isRead`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='好友私聊消息';

-- ---------- 4. 账号精简 ----------
UPDATE `user` SET `userRole`='user', `userPassword`=MD5(CONCAT('panghu','kaoyan123456')) WHERE `userAccount`='jayjay';
DELETE FROM `user` WHERE `userAccount` IN ('aiaiai','student');
-- 孤儿数据归并到 jayjay
UPDATE `post` SET `userId`=1908152443967729665 WHERE `userId` NOT IN (SELECT `id` FROM (SELECT `id` FROM `user`) t);
UPDATE `post_comment` SET `userId`=1908152443967729665 WHERE `userId` NOT IN (SELECT `id` FROM (SELECT `id` FROM `user`) t);
UPDATE `question` SET `userId`=1908152443967729665 WHERE `userId` NOT IN (SELECT `id` FROM (SELECT `id` FROM `user`) t);

-- ---------- 5. 资料库 PDF 化 ----------
UPDATE `study_material` SET `fileType`='pdf', `fileUrl`=CONCAT('/api/file/material/zhenti-', TRIM(BOTH '（在线做题）' FROM REPLACE(`title`,'408 真题','')), '.pdf') WHERE `category`='408 真题';

UPDATE `study_material` SET `title`='2009 年 408 真题试卷（精编 PDF）', `fileType`='pdf', `fileUrl`='/api/file/material/zhenti-2009.pdf', `description`='2009 年考研 408 综合真题精编：试卷结构、代表性题目与逐题解析，可在线浏览或下载。' WHERE `id`=5000000000000000001;
UPDATE `study_material` SET `title`='2010 年 408 真题试卷（精编 PDF）', `fileType`='pdf', `fileUrl`='/api/file/material/zhenti-2010.pdf', `description`='2010 年考研 408 综合真题精编：试卷结构、代表性题目与逐题解析，可在线浏览或下载。' WHERE `id`=5000000000000000002;
UPDATE `study_material` SET `title`='2011 年 408 真题试卷（精编 PDF）', `fileType`='pdf', `fileUrl`='/api/file/material/zhenti-2011.pdf', `description`='2011 年考研 408 综合真题精编：试卷结构、代表性题目与逐题解析，可在线浏览或下载。' WHERE `id`=5000000000000000003;
UPDATE `study_material` SET `title`='2012 年 408 真题试卷（精编 PDF）', `fileType`='pdf', `fileUrl`='/api/file/material/zhenti-2012.pdf', `description`='2012 年考研 408 综合真题精编：试卷结构、代表性题目与逐题解析，可在线浏览或下载。' WHERE `id`=5000000000000000004;
UPDATE `study_material` SET `title`='2013 年 408 真题试卷（精编 PDF）', `fileType`='pdf', `fileUrl`='/api/file/material/zhenti-2013.pdf', `description`='2013 年考研 408 综合真题精编：试卷结构、代表性题目与逐题解析，可在线浏览或下载。' WHERE `id`=5000000000000000005;
UPDATE `study_material` SET `title`='2014 年 408 真题试卷（精编 PDF）', `fileType`='pdf', `fileUrl`='/api/file/material/zhenti-2014.pdf', `description`='2014 年考研 408 综合真题精编：试卷结构、代表性题目与逐题解析，可在线浏览或下载。' WHERE `id`=5000000000000000006;
UPDATE `study_material` SET `title`='2015 年 408 真题试卷（精编 PDF）', `fileType`='pdf', `fileUrl`='/api/file/material/zhenti-2015.pdf', `description`='2015 年考研 408 综合真题精编：试卷结构、代表性题目与逐题解析，可在线浏览或下载。' WHERE `id`=5000000000000000007;
UPDATE `study_material` SET `title`='2016 年 408 真题试卷（精编 PDF）', `fileType`='pdf', `fileUrl`='/api/file/material/zhenti-2016.pdf', `description`='2016 年考研 408 综合真题精编：试卷结构、代表性题目与逐题解析，可在线浏览或下载。' WHERE `id`=5000000000000000008;
UPDATE `study_material` SET `title`='2017 年 408 真题试卷（精编 PDF）', `fileType`='pdf', `fileUrl`='/api/file/material/zhenti-2017.pdf', `description`='2017 年考研 408 综合真题精编：试卷结构、代表性题目与逐题解析，可在线浏览或下载。' WHERE `id`=5000000000000000009;
UPDATE `study_material` SET `title`='2018 年 408 真题试卷（精编 PDF）', `fileType`='pdf', `fileUrl`='/api/file/material/zhenti-2018.pdf', `description`='2018 年考研 408 综合真题精编：试卷结构、代表性题目与逐题解析，可在线浏览或下载。' WHERE `id`=5000000000000000010;
UPDATE `study_material` SET `title`='2019 年 408 真题试卷（精编 PDF）', `fileType`='pdf', `fileUrl`='/api/file/material/zhenti-2019.pdf', `description`='2019 年考研 408 综合真题精编：试卷结构、代表性题目与逐题解析，可在线浏览或下载。' WHERE `id`=5000000000000000011;
UPDATE `study_material` SET `title`='2020 年 408 真题试卷（精编 PDF）', `fileType`='pdf', `fileUrl`='/api/file/material/zhenti-2020.pdf', `description`='2020 年考研 408 综合真题精编：试卷结构、代表性题目与逐题解析，可在线浏览或下载。' WHERE `id`=5000000000000000012;
UPDATE `study_material` SET `title`='2021 年 408 真题试卷（精编 PDF）', `fileType`='pdf', `fileUrl`='/api/file/material/zhenti-2021.pdf', `description`='2021 年考研 408 综合真题精编：试卷结构、代表性题目与逐题解析，可在线浏览或下载。' WHERE `id`=5000000000000000013;
UPDATE `study_material` SET `title`='2022 年 408 真题试卷（精编 PDF）', `fileType`='pdf', `fileUrl`='/api/file/material/zhenti-2022.pdf', `description`='2022 年考研 408 综合真题精编：试卷结构、代表性题目与逐题解析，可在线浏览或下载。' WHERE `id`=5000000000000000014;
UPDATE `study_material` SET `title`='2023 年 408 真题试卷（精编 PDF）', `fileType`='pdf', `fileUrl`='/api/file/material/zhenti-2023.pdf', `description`='2023 年考研 408 综合真题精编：试卷结构、代表性题目与逐题解析，可在线浏览或下载。' WHERE `id`=5000000000000000015;
UPDATE `study_material` SET `title`='2024 年 408 真题试卷（精编 PDF）', `fileType`='pdf', `fileUrl`='/api/file/material/zhenti-2024.pdf', `description`='2024 年考研 408 综合真题精编：试卷结构、代表性题目与逐题解析，可在线浏览或下载。' WHERE `id`=5000000000000000016;
UPDATE `study_material` SET `title`='2025 年 408 真题试卷（精编 PDF）', `fileType`='pdf', `fileUrl`='/api/file/material/zhenti-2025.pdf', `description`='2025 年考研 408 综合真题精编：试卷结构、代表性题目与逐题解析，可在线浏览或下载。' WHERE `id`=5000000000000000017;
UPDATE `study_material` SET `title`='2026 年 408 真题试卷（精编 PDF）', `fileType`='pdf', `fileUrl`='/api/file/material/zhenti-2026.pdf', `description`='2026 年考研 408 综合真题精编：试卷结构、代表性题目与逐题解析，可在线浏览或下载。' WHERE `id`=5000000000000000018;

UPDATE `study_material` SET `fileType`='pdf', `fileUrl`=CONCAT('/api/file/material/moni-', RIGHT(`title`,1), '.pdf') WHERE `category`='408 模拟题';

UPDATE `study_material` SET `fileType`='pdf', `fileUrl`='/api/file/material/exercise-ds.pdf', `subject`='ds', `tags`='["数据结构","教材配套","专题精练"]', `description`='《数据结构》教材配套专题精练：按章节整理高频题与逐题解析 PDF。' WHERE `id`=5000000000000002001;
UPDATE `study_material` SET `fileType`='pdf', `fileUrl`='/api/file/material/exercise-co.pdf', `subject`='co', `tags`='["计组","教材配套","专题精练"]', `description`='《计算机组成原理》教材配套专题精练：按章节整理高频题与逐题解析 PDF。' WHERE `id`=5000000000000002002;
UPDATE `study_material` SET `fileType`='pdf', `fileUrl`='/api/file/material/exercise-os.pdf', `subject`='os', `tags`='["操作系统","教材配套","专题精练"]', `description`='《操作系统》教材配套专题精练：按章节整理高频题与逐题解析 PDF。' WHERE `id`=5000000000000002003;
UPDATE `study_material` SET `fileType`='pdf', `fileUrl`='/api/file/material/exercise-cn.pdf', `subject`='cn', `tags`='["计算机网络","教材配套","专题精练"]', `description`='《计算机网络》教材配套专题精练：按章节整理高频题与逐题解析 PDF。' WHERE `id`=5000000000000002004;

UPDATE `study_material` SET `fileType`='pdf', `fileUrl`='/api/file/material/outline-408.pdf', `description`='计算机学科专业基础综合（408）考试大纲解读：科目分值结构、考查内容范围与命题趋势。' WHERE `id`=5000000000000003001;

-- 公共课真题：按年份独立 PDF
UPDATE `study_material` SET `title`='考研数学（一）真题精编 2020', `category`='数学真题', `fileType`='pdf', `fileUrl`='/api/file/material/math-2020.pdf', `tags`='["数学一","2020","真题"]', `description`='2020 年考研数学（一）真题精编：高等数学、线性代数、概率统计代表题与详解 PDF。' WHERE `id`=5000000000000004001;
UPDATE `study_material` SET `title`='考研数学（一）真题精编 2022', `category`='数学真题', `fileType`='pdf', `fileUrl`='/api/file/material/math-2022.pdf', `tags`='["数学一","2022","真题"]', `description`='2022 年考研数学（一）真题精编：高等数学、线性代数、概率统计代表题与详解 PDF。' WHERE `id`=5000000000000004002;
UPDATE `study_material` SET `title`='考研数学（一）真题精编 2024', `category`='数学真题', `fileType`='pdf', `fileUrl`='/api/file/material/math-2024.pdf', `tags`='["数学一","2024","真题"]', `description`='2024 年考研数学（一）真题精编：高等数学、线性代数、概率统计代表题与详解 PDF。' WHERE `id`=5000000000000004003;
UPDATE `study_material` SET `title`='考研数学（一）真题精编 2026', `category`='数学真题', `fileType`='pdf', `fileUrl`='/api/file/material/math-2026.pdf', `tags`='["数学一","2026","真题"]', `description`='2026 年考研数学（一）真题精编：高等数学、线性代数、概率统计代表题与详解 PDF。' WHERE `id`=5000000000000004004;
UPDATE `study_material` SET `title`='考研英语（一）真题精编 2020', `category`='英语真题', `fileType`='pdf', `fileUrl`='/api/file/material/english-2020.pdf', `tags`='["英语一","2020","真题"]', `description`='2020 年考研英语（一）真题精编：完形、阅读、翻译与写作代表题 PDF。' WHERE `id`=5000000000000004005;
UPDATE `study_material` SET `title`='考研英语（一）真题精编 2022', `category`='英语真题', `fileType`='pdf', `fileUrl`='/api/file/material/english-2022.pdf', `tags`='["英语一","2022","真题"]', `description`='2022 年考研英语（一）真题精编：完形、阅读、翻译与写作代表题 PDF。' WHERE `id`=5000000000000004006;
UPDATE `study_material` SET `title`='考研英语（一）真题精编 2024', `category`='英语真题', `fileType`='pdf', `fileUrl`='/api/file/material/english-2024.pdf', `tags`='["英语一","2024","真题"]', `description`='2024 年考研英语（一）真题精编：完形、阅读、翻译与写作代表题 PDF。' WHERE `id`=5000000000000004007;
UPDATE `study_material` SET `title`='考研英语（一）真题精编 2026', `category`='英语真题', `fileType`='pdf', `fileUrl`='/api/file/material/english-2026.pdf', `tags`='["英语一","2026","真题"]', `description`='2026 年考研英语（一）真题精编：完形、阅读、翻译与写作代表题 PDF。' WHERE `id`=5000000000000004008;
UPDATE `study_material` SET `title`='考研政治真题精编 2020', `category`='政治真题', `fileType`='pdf', `fileUrl`='/api/file/material/politics-2020.pdf', `tags`='["政治","2020","真题"]', `description`='2020 年考研政治真题精编：马原、毛中特、史纲、思修代表选择题与分析题要点 PDF。' WHERE `id`=5000000000000004009;
UPDATE `study_material` SET `title`='考研政治真题精编 2022', `category`='政治真题', `fileType`='pdf', `fileUrl`='/api/file/material/politics-2022.pdf', `tags`='["政治","2022","真题"]', `description`='2022 年考研政治真题精编：马原、毛中特、史纲、思修代表选择题与分析题要点 PDF。' WHERE `id`=5000000000000004010;
UPDATE `study_material` SET `title`='考研政治真题精编 2024', `category`='政治真题', `fileType`='pdf', `fileUrl`='/api/file/material/politics-2024.pdf', `tags`='["政治","2024","真题"]', `description`='2024 年考研政治真题精编：马原、毛中特、史纲、思修代表选择题与分析题要点 PDF。' WHERE `id`=5000000000000004011;
UPDATE `study_material` SET `title`='考研政治真题精编 2026', `category`='政治真题', `fileType`='pdf', `fileUrl`='/api/file/material/politics-2026.pdf', `tags`='["政治","2026","真题"]', `description`='2026 年考研政治真题精编：马原、毛中特、史纲、思修代表选择题与分析题要点 PDF。' WHERE `id`=5000000000000004012;

-- 方法论：本地 PDF
UPDATE `study_material` SET `fileType`='pdf', `fileUrl`='/api/file/material/method-1.pdf'  WHERE `id`=5000000000000005001;
UPDATE `study_material` SET `fileType`='pdf', `fileUrl`='/api/file/material/method-2.pdf'  WHERE `id`=5000000000000005002;
UPDATE `study_material` SET `fileType`='pdf', `fileUrl`='/api/file/material/method-3.pdf'  WHERE `id`=5000000000000005003;
UPDATE `study_material` SET `fileType`='pdf', `fileUrl`='/api/file/material/method-4.pdf'  WHERE `id`=5000000000000005004;
UPDATE `study_material` SET `fileType`='pdf', `fileUrl`='/api/file/material/method-5.pdf'  WHERE `id`=5000000000000005005;
UPDATE `study_material` SET `fileType`='pdf', `fileUrl`='/api/file/material/method-6.pdf'  WHERE `id`=5000000000000005006;
UPDATE `study_material` SET `fileType`='pdf', `fileUrl`='/api/file/material/method-7.pdf'  WHERE `id`=5000000000000005007;
UPDATE `study_material` SET `fileType`='pdf', `fileUrl`='/api/file/material/method-8.pdf'  WHERE `id`=5000000000000005008;
UPDATE `study_material` SET `fileType`='pdf', `fileUrl`='/api/file/material/method-9.pdf'  WHERE `id`=5000000000000005009;
UPDATE `study_material` SET `fileType`='pdf', `fileUrl`='/api/file/material/method-10.pdf' WHERE `id`=5000000000000005010;
