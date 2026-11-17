# -*- coding: utf-8 -*-
"""生成考研资料库演示 PDF。运行：python scripts/gen_materials.py"""
import os
from xml.sax.saxutils import escape

from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.units import mm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import Paragraph, SimpleDocTemplate, Spacer

FONT = r"C:\Windows\Fonts\simhei.ttf"
OUT = os.path.join(os.path.dirname(__file__), "..", "aigcoj-backend", "data", "uploads", "material")
os.makedirs(OUT, exist_ok=True)
pdfmetrics.registerFont(TTFont("SimHei", FONT))

TITLE = ParagraphStyle("title", fontName="SimHei", fontSize=21, leading=30, spaceAfter=4)
SUB = ParagraphStyle("sub", fontName="SimHei", fontSize=9.5, leading=15, textColor="#6b7280", spaceAfter=14)
H = ParagraphStyle("h", fontName="SimHei", fontSize=13.5, leading=22, spaceBefore=8, spaceAfter=4, textColor="#111827")
BODY = ParagraphStyle("body", fontName="SimHei", fontSize=10.5, leading=18, wordWrap="CJK", textColor="#374151", spaceAfter=8)


def build(filename, title, subtitle, sections):
    doc = SimpleDocTemplate(
        os.path.join(OUT, filename), pagesize=A4,
        leftMargin=20 * mm, rightMargin=20 * mm, topMargin=18 * mm, bottomMargin=18 * mm,
    )
    story = [Paragraph(escape(title), TITLE), Paragraph(escape(subtitle), SUB)]
    for heading, body in sections:
        story.append(Paragraph(escape(heading), H))
        text = escape(body).replace("\n", "<br/>")
        story.append(Paragraph(text, BODY))
    story.append(Spacer(1, 10))
    story.append(Paragraph("© 考研学习平台 · 仅供学习交流使用", SUB))
    doc.build(story)
    print("generated", filename)


build(
    "ds-handbook.pdf",
    "408 数据结构核心考点手册",
    "考研学习平台 · 内部资料 · 建议配合章节练习使用",
    [
        ("一、线性表", "顺序表：随机访问 O(1)，插入删除平均 O(n)。链表：插入删除 O(1)，查找 O(n)。\n"
                    "常考：头插法与尾插法、双链表删除、循环链表判空、静态链表。\n"
                    "易错：顺序表删除元素后长度变化、链表断链顺序。"),
        ("二、栈与队列", "栈：后进先出，应用有括号匹配、表达式求值、递归、DFS。\n"
                     "队列：先进先出，应用有 BFS、层序遍历、缓冲区。\n"
                     "循环队列：队空 front==rear，队满 (rear+1)%MaxSize==front，元素个数 (rear-front+MaxSize)%MaxSize。"),
        ("三、树与二叉树", "二叉树性质：n0=n2+1；第 i 层最多 2^(i-1) 个结点；高度 h 最多 2^h-1 个结点。\n"
                      "遍历：先序、中序、后序、层序。由先序+中序或后序+中序可唯一确定二叉树。\n"
                      "线索二叉树：利用空指针指向遍历前驱/后继。\n"
                      "哈夫曼树：带权路径长度最小，n 个叶子共 2n-1 个结点。"),
        ("四、图", "存储：邻接矩阵适合稠密图，邻接表适合稀疏图。\n"
                 "遍历：DFS 借助栈/递归，BFS 借助队列。\n"
                 "最小生成树：Prim O(n^2) 适合稠密图，Kruskal O(ElogE) 适合稀疏图。\n"
                 "最短路径：Dijkstra 不能处理负权，Floyd 可处理负权但不能有负环。\n"
                 "拓扑排序：有向无环图，入度为 0 入队。关键路径：最早开始时间与最晚开始时间相等。"),
        ("五、查找与排序", "折半查找要求有序顺序表，时间复杂度 O(log n)。\n"
                      "B 树/B+ 树：数据库与文件系统索引，B+ 树叶子结点串联适合范围查询。\n"
                      "排序稳定性：冒泡、插入、归并、基数稳定；选择、快排、堆排不稳定。\n"
                      "快排平均 O(nlogn)，最坏 O(n^2)；堆排序建堆 O(n)，排序 O(nlogn)。"),
    ],
)

build(
    "co-cache.pdf",
    "计算机组成原理：Cache 与存储体系",
    "考研学习平台 · 408 专题 · 重点计算题",
    [
        ("一、存储层次", "寄存器 → Cache → 主存 → 辅存。越靠近 CPU 速度越快、容量越小、成本越高。\n"
                    "Cache 解决 CPU 与主存速度不匹配；虚拟存储解决主存容量不足。"),
        ("二、Cache 映射方式", "直接映射：主存块号 mod Cache 行数，冲突高、成本低。\n"
                          "全相联映射：任意块可放任意行，冲突低、比较器多。\n"
                          "组相联映射：组间直接、组内全相联，折中方案。\n"
                          "地址划分：标记 tag | 组号/行号 | 块内地址。"),
        ("三、命中率与平均访问时间", "命中率 H = 命中次数 / 总访问次数。\n"
                              "平均访问时间 = H × Cache 访问时间 + (1-H) × 主存访问时间。\n"
                              "若采用先访问 Cache 再访问主存：T = H×Tc + (1-H)×(Tc+Tm)。"),
        ("四、替换算法与写策略", "替换：随机、FIFO、LRU、LFU。LRU 命中率通常较高。\n"
                          "写直达 write-through：同时写 Cache 与主存，一致性好、开销大。\n"
                          "写回 write-back：只写 Cache，置脏位，替换时写回主存。"),
        ("五、常考计算", "例：Cache 访问 1ns，主存 10ns，命中率 95%，先 Cache 后主存：\n"
                    "T = 0.95×1 + 0.05×(1+10) = 1.5ns。\n"
                    "注意题目是否说明“同时访问”还是“先访问 Cache”。"),
    ],
)

build(
    "os-process.pdf",
    "操作系统：进程、内存与文件",
    "考研学习平台 · 408 专题 · 高频大题",
    [
        ("一、进程与线程", "进程是资源分配的基本单位，线程是 CPU 调度的基本单位。\n"
                     "进程状态：创建、就绪、运行、阻塞、终止。就绪→运行由调度引起，运行→阻塞由请求资源引起。\n"
                     "PCB 包含标识、状态、程序计数器、寄存器、资源清单、优先级等。"),
        ("二、调度算法", "FCFS 先来先服务：简单，平均等待时间较长，可能饥饿。\n"
                    "SJF 短作业优先：平均等待最短，长作业可能饥饿。\n"
                    "RR 时间片轮转：响应快，时间片过大退化为 FCFS。\n"
                    "优先级调度：可抢占/非抢占，静态/动态优先级。"),
        ("三、同步与互斥", "临界区管理四原则：空闲让进、忙则等待、有限等待、让权等待。\n"
                     "信号量 P/V：P 申请资源，V 释放资源。互斥信号量初值 1，同步信号量初值 0。\n"
                     "经典问题：生产者-消费者、读者-写者、哲学家进餐。"),
        ("四、死锁", "四个必要条件：互斥、占有并等待、不可剥夺、循环等待。\n"
                 "处理策略：预防（破坏条件）、避免（银行家算法）、检测与解除、忽略。\n"
                 "银行家算法：安全序列判断，Need = Max - Allocation。"),
        ("五、内存管理", "分页：页号 + 页内偏移，无外部碎片，有内部碎片。\n"
                    "分段：段号 + 段内偏移，便于共享与保护，有外部碎片。\n"
                    "页面置换：OPT、FIFO、LRU、Clock。FIFO 可能出现 Belady 异常。\n"
                    "虚拟地址转换：页表基址 + 页号 → 物理块号，再拼接页内偏移。"),
        ("六、文件系统", "文件分配：连续、链接、索引。索引分配支持随机访问，适合大文件。\n"
                    "目录：FCB、inode。硬链接共享 inode，软链接保存路径。\n"
                    "磁盘调度：FCFS、SSTF、SCAN、C-SCAN、LOOK。"),
    ],
)

build(
    "cn-tcp.pdf",
    "计算机网络：TCP/IP 高频考点",
    "考研学习平台 · 408 专题 · 协议与计算",
    [
        ("一、分层模型", "OSI 七层：物理、数据链路、网络、传输、会话、表示、应用。\n"
                   "TCP/IP 四层：网络接口、网际、传输、应用。\n"
                   "各层设备：集线器（物理）、交换机（链路）、路由器（网络）、网关（应用）。"),
        ("二、TCP 连接管理", "三次握手：SYN → SYN+ACK → ACK，防止历史连接、同步序号、确认双方收发能力。\n"
                       "四次挥手：FIN → ACK → FIN → ACK，TIME_WAIT 等待 2MSL。\n"
                       "SYN 洪泛攻击：半连接队列耗尽，可用 SYN Cookie 防御。"),
        ("三、可靠传输与流量控制", "序号、确认、重传、校验和、去重。\n"
                          "停止等待：信道利用率低；滑动窗口：连续发送，提高利用率。\n"
                          "流量控制：接收窗口 rwnd；拥塞控制：慢开始、拥塞避免、快重传、快恢复。\n"
                          "发送窗口 = min(rwnd, cwnd)。"),
        ("四、TCP 与 UDP", "TCP：面向连接、可靠、字节流、首部 20B、一对一。\n"
                      "UDP：无连接、不可靠、数据报、首部 8B、支持广播/多播。\n"
                      "适用：TCP 文件传输、邮件、网页；UDP 视频、DNS、实时游戏。"),
        ("五、网络层", "IP 地址分类与子网划分；CIDR 无分类编址；NAT 地址转换。\n"
                  "ARP：IP → MAC；ICMP：差错报告与询问；DHCP：动态分配 IP。\n"
                  "路由算法：RIP（距离向量，跳数）、OSPF（链路状态，Dijkstra）、BGP（路径向量）。"),
        ("六、HTTP 与 DNS", "HTTP/1.1 持久连接、管线化；HTTP/2 多路复用；HTTP/3 基于 QUIC/UDP。\n"
                       "常见状态码：200、301、302、304、400、403、404、500、502。\n"
                       "DNS 递归查询与迭代查询，缓存 TTL。"),
    ],
)

build(
    "math-formula.pdf",
    "考研数学一公式速查",
    "考研学习平台 · 公共课资料 · 考前复盘",
    [
        ("高等数学", "泰勒公式：f(x)=f(x0)+f'(x0)(x-x0)+f''(x0)(x-x0)^2/2!+…\n"
                 "常用等价无穷小：sinx~x，tanx~x，ln(1+x)~x，1-cosx~x^2/2，e^x-1~x。\n"
                 "洛必达法则、拉格朗日中值定理、积分中值定理。"),
        ("多元函数", "全微分 dz = zx dx + zy dy。\n"
                 "极值必要条件：偏导为 0；充分条件：AC-B^2 > 0，A>0 极小，A<0 极大。\n"
                 "二重积分极坐标：dxdy = r dr dθ。"),
        ("级数", "几何级数 ∑q^n 收敛当 |q|<1。\np 级数 ∑1/n^p 收敛当 p>1。\n"
               "幂级数收敛半径 R = 1/lim|a(n+1)/a(n)|。\n"
               "傅里叶级数系数公式要熟记。"),
        ("线性代数", "行列式性质、按行展开；矩阵秩 r(AB) ≤ min(r(A),r(B))。\n"
                 "特征值：|A-λE|=0；相似对角化需 n 个线性无关特征向量。\n"
                 "正定二次型：顺序主子式全大于 0。"),
        ("概率论", "常见分布：二项、泊松、均匀、指数、正态。\n"
                "期望性质：E(aX+bY)=aE(X)+bE(Y)；方差 D(aX+b)=a^2D(X)。\n"
                "大数定律与中心极限定理；矩估计与最大似然估计。"),
    ],
)

build(
    "english-5500.pdf",
    "考研英语高频词汇精选",
    "考研学习平台 · 公共课资料 · 每天 30 个",
    [
        ("高频动词", "allocate 分配；anticipate 预期；assess 评估；attribute 归因；comply 遵守；\n"
                 "constitute 构成；derive 源于；diminish 减少；enhance 提升；exceed 超过；\n"
                 "facilitate 促进；implement 实施；imply 暗示；indicate 表明；justify 证明合理；\n"
                 "maintain 维持；modify 修改；obtain 获得；perceive 察觉；pursue 追求。"),
        ("高频名词", "alternative 替代方案；capacity 能力；consequence 后果；constraint 限制；\n"
                 "criterion 标准；dimension 维度；emphasis 强调；framework 框架；hypothesis 假设；\n"
                 "incentive 激励；initiative 倡议；mechanism 机制；paradigm 范式；phenomenon 现象；\n"
                 "priority 优先；prospect 前景；strategy 策略；tendency 趋势；variable 变量。"),
        ("高频形容词", "ambiguous 模糊的；coherent 连贯的；comprehensive 全面的；crucial 关键的；\n"
                  "deliberate 故意的；empirical 经验的；explicit 明确的；feasible 可行的；\n"
                  "inherent 固有的；innovative 创新的；intense 强烈的；notable 显著的；\n"
                  "preliminary 初步的；reluctant 不情愿的；substantial 大量的；vulnerable 脆弱的。"),
        ("写作替换", "important → crucial / vital / indispensable\n"
                 "many → numerous / a multitude of\n"
                 "show → demonstrate / illustrate / reveal\n"
                 "think → argue / maintain / contend\n"
                 "cause → give rise to / result in / trigger"),
    ],
)

build(
    "politics-outline.pdf",
    "考研政治冲刺背诵提纲",
    "考研学习平台 · 公共课资料 · 按模块记忆",
    [
        ("马原", "唯物论：物质决定意识，意识反作用于物质。\n"
              "辩证法：两大特征（联系、发展）、三大规律（对立统一、量变质变、否定之否定）、五对范畴。\n"
              "认识论：实践是认识的基础；感性认识与理性认识；真理的绝对性与相对性。\n"
              "唯物史观：社会存在决定社会意识；生产力与生产关系；经济基础与上层建筑。"),
        ("毛中特", "新民主主义革命总路线；三大法宝：统一战线、武装斗争、党的建设。\n"
               "社会主义改造；改革开放；新时代中国特色社会主义思想的核心要义。\n"
               "中国式现代化：人口规模巨大、全体人民共同富裕、物质文明与精神文明协调、人与自然和谐、和平发展。"),
        ("史纲", "近代以来两大历史任务；五四运动；中国共产党成立；长征；抗日战争；\n"
              "新中国成立；三大改造；改革开放；新时代十年伟大变革。"),
        ("思修法基", "人生观、价值观、理想信念；爱国主义；社会主义核心价值观；\n"
                 "道德修养；法治思维；宪法与法律体系。"),
        ("时政", "关注当年重要会议、重要讲话、重要成就；答题时结合材料，先原理后分析。\n"
              "分析题结构：点明原理 → 结合材料 → 联系实际 → 总结提升。"),
    ],
)

build(
    "interview-200.pdf",
    "计算机复试面试 200 问（精选）",
    "考研学习平台 · 复试资料 · 中英文对照思路",
    [
        ("专业基础", "1. 进程与线程的区别？\n2. TCP 三次握手为什么不是两次？\n3. 死锁的四个必要条件？\n"
                 "4. 虚拟内存的作用？\n5. 数据库索引为什么用 B+ 树？\n6. 哈希冲突的解决方法？\n"
                 "7. 时间复杂度 O(nlogn) 的排序有哪些？\n8. 堆与栈的区别？\n9. 编译型语言与解释型语言？\n"
                 "10. 面向对象的三大特性？"),
        ("项目追问", "1. 你在这个项目中负责什么？\n2. 遇到最大的技术难点是什么，如何解决？\n"
                 "3. 为什么选择这个技术方案？有没有替代方案？\n4. 系统并发量多大，如何优化？\n"
                 "5. 如果重新做一遍，你会怎么改进？\n6. 项目中如何保证数据一致性？\n"
                 "7. 做过哪些性能优化，效果如何量化？"),
        ("算法手撕", "高频：两数之和、反转链表、二叉树层序遍历、最大子数组和、快排、二分查找、\n"
                 "括号匹配、LRU 缓存、合并有序链表、Top K。\n"
                 "答题步骤：确认输入输出 → 说思路 → 复杂度分析 → 写代码 → 自测边界。"),
        ("英语口语", "Introduce yourself. / Why do you choose this university? / Describe your project.\n"
                 "What is your greatest strength? / Where do you see yourself in five years?\n"
                 "准备 1 分钟、2 分钟两个版本，突出专业兴趣与科研潜力。"),
        ("综合面试", "为什么考研？为什么跨考？本科成绩如何解释？读过哪些专业书？\n"
                 "对目标导师方向了解多少？未来研究计划？如何平衡科研与工程？\n"
                 "回答原则：真诚、具体、有例子、不夸大。"),
    ],
)

build(
    "408-papers.pdf",
    "408 历年真题分类汇编使用指南",
    "考研学习平台 · 真题资料 · 三轮刷题法",
    [
        ("资料说明", "本册按数据结构、计算机组成原理、操作系统、计算机网络四科分类，\n"
                 "收录近年真题题型与分值分布，适合第二轮专题突破与第三轮查漏补缺。"),
        ("第一轮：按套卷", "限时 180 分钟完成整套真题，模拟考场节奏。\n"
                     "做完后统计四科得分率，标记知识盲区，不急于看解析。"),
        ("第二轮：按专题", "将错题按知识点归类：线性表、树、图、Cache、指令、进程、内存、TCP 等。\n"
                     "每个专题集中 2-3 天，先看笔记再做同类题，形成题型反射。"),
        ("第三轮：按错题", "只做错题与高频题，控制在一周内完成。\n"
                     "每道错题写三行：错因、正确思路、下次如何避免。"),
        ("答题技巧", "选择题：排除法、特值法、量纲法；不会的先标记，避免耗时。\n"
                 "大题：先写关键步骤与公式，再计算；单位、进制、地址范围要看清。\n"
                 "网络大题：画拓扑、标地址、列端口；OS 大题：画进程状态与页表。"),
    ],
)
