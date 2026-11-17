# -*- coding: utf-8 -*-
"""按 data/uploads/material 目录中实际存在的真实试卷文件，重建 study_material 表记录。

用法：
    python scripts/rebuild_materials.py            # 生成 sql/rebuild_materials.sql
说明：
    - 只登记磁盘上真实存在的 PDF，避免出现"点开 404"的死链
    - 全部为本地 PDF，fileUrl 指向 /api/file/material/<文件名>
"""
import json
import os
import re
import sys

BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
UPLOAD = os.path.join(BASE, 'aigcoj-backend', 'data', 'uploads', 'material')
OUT_SQL = os.path.join(BASE, 'aigcoj-backend', 'sql', 'rebuild_materials.sql')

rows = []  # (title, desc, category, subject, filename, tags)


def add(title, desc, category, subject, filename, tags):
    if not os.path.exists(os.path.join(UPLOAD, filename)):
        return
    rows.append((title, desc, category, subject or '', filename, tags))


# ---------- 408 真题原卷 2009-2026 ----------
for name in sorted(os.listdir(UPLOAD)):
    m = re.fullmatch(r'zt408-(\d{4})(-ans)?\.pdf', name)
    if not m:
        continue
    year, is_ans = m.group(1), bool(m.group(2))
    if is_ans:
        add(
            f'{year} 年全国硕士研究生招生考试 408 真题 · 参考答案与解析',
            f'{year} 年 408 真题参考答案与逐题解析（社区整理版，与原卷题号一一对应），含选择题答案速查与 41-47 题大题评分要点。',
            '408 真题答案', '', name, [year, '408', '答案解析'],
        )
    else:
        add(
            f'{year} 年全国硕士研究生招生考试 408 计算机学科专业基础 真题原卷',
            f'{year} 年 408 统考真题原卷（数据结构 45 分 / 计算机组成原理 45 分 / 操作系统 35 分 / 计算机网络 25 分），A4 版面可直接打印模考。',
            '408 真题', '', name, [year, '408', '真题原卷'],
        )

# ---------- 数学一 1987-2022 ----------
for name in sorted(os.listdir(UPLOAD)):
    m = re.fullmatch(r'math1-(\d{4})\.pdf', name)
    if m:
        y = m.group(1)
        add(
            f'{y} 年考研数学（一）真题',
            f'{y} 年全国硕士研究生入学统一考试数学（一）真题原卷，含选择题、填空题与解答题完整题面，可直接打印。',
            '数学一真题', 'math', name, [y, '数学一', '真题原卷'],
        )

# ---------- 英语一 2002-2022 ----------
for name in sorted(os.listdir(UPLOAD)):
    m = re.fullmatch(r'en1-(\d{4})\.pdf', name)
    if m:
        y = m.group(1)
        add(
            f'{y} 年考研英语（一）真题',
            f'{y} 年全国硕士研究生招生考试英语（一）真题原卷，含完形填空、阅读理解、新题型、翻译与写作全部题型。',
            '英语一真题', 'english', name, [y, '英语一', '真题原卷'],
        )

# ---------- 政治 2014-2024 ----------
for name in sorted(os.listdir(UPLOAD)):
    m = re.fullmatch(r'zz-(\d{4})\.pdf', name)
    if m:
        y = m.group(1)
        add(
            f'{y} 年考研政治（思想政治理论）真题',
            f'{y} 年全国硕士研究生招生考试思想政治理论真题原卷，含单项选择 16 题、多项选择 17 题、材料分析 5 题。',
            '政治真题', 'politics', name, [y, '政治', '真题原卷'],
        )

# ---------- 408 仿真模拟卷 ----------
moni_desc = {
    '1': '数据结构专项（线性表 / 树 / 图 / 排序）',
    '2': '计算机组成原理专项（数据表示 / Cache / 指令流水）',
    '3': '操作系统专项（进程同步 / 内存管理 / 文件系统）',
    '4': '计算机网络专项（体系结构 / TCP / 路由）',
    '5': '全科综合卷（一）—— 按真题题型结构配比',
    '6': '全科综合卷（二）—— 侧重计算与综合分析题',
    '7': '全科综合卷（三）—— 侧重概念辨析与简答',
    '8': '考前冲刺卷 —— 高频错点集中训练',
}
for name in sorted(os.listdir(UPLOAD)):
    m = re.fullmatch(r'moni-(\d)\.pdf', name)
    if m:
        n = m.group(1)
        add(
            f'408 仿真模拟卷（{n}）— {moni_desc.get(n, "综合训练")}',
            f'本平台按 408 真题题型结构命制的仿真模拟卷（非历年真题），用于知识点串联训练，建议限时 180 分钟完成。',
            '408 模拟题', '', name, [n, '408', '模拟卷'],
        )

# ---------- 408 教材配套习题 ----------
exercise = {
    'exercise-ds.pdf': ('ds', '数据结构', '线性表、树与二叉树、图、查找、排序五大板块配套习题，含算法设计题思路。'),
    'exercise-co.pdf': ('co', '计算机组成原理', '数据表示与运算、存储系统、指令系统、CPU 与流水线配套习题。'),
    'exercise-os.pdf': ('os', '操作系统', '进程与线程、内存管理、文件系统、I/O 管理配套习题。'),
    'exercise-cn.pdf': ('cn', '计算机网络', '体系结构、数据链路层、网络层、传输层、应用层配套习题。'),
}
for fname, (subj, label, desc) in exercise.items():
    add(f'408 {label} · 教材配套习题精选', desc, '408 习题', subj, fname, [label, '408', '习题'])

# ---------- 408 讲义与专题 ----------
handbook = {
    'ds-handbook.pdf': ('ds', '408 数据结构核心考点手册', '线性表到排序的考点串讲与代码模板，适合一轮复习建立框架。'),
    'co-cache.pdf': ('co', '计算机组成原理：Cache 与存储体系', 'Cache 映射方式、替换算法、平均访问时间计算专项，408 计算题高频。'),
    'os-process.pdf': ('os', '操作系统：进程、内存与文件', 'PV 操作、死锁、页表与地址转换、文件索引结构综合讲解。'),
    'cn-tcp.pdf': ('cn', '计算机网络：TCP/IP 高频考点', '三次握手四次挥手、拥塞控制、子网划分与路由聚合专题。'),
}
for fname, (subj, title, desc) in handbook.items():
    add(title, desc, '408', subj, fname, ['408', '讲义', '考点'])

add('408 考试大纲（最新版）解读', '408 统考考试大纲全文与变动点解读，含四科分值分布与题型结构说明。',
    '408 大纲', '', 'outline-408.pdf', ['408', '大纲', '考纲'])

# ---------- 方法论 ----------
methods = {
    1: '408 三轮复习法：基础—强化—冲刺的时间分配',
    2: '真题使用方法：从做题到复盘的正确路径',
    3: '数据结构算法题的答题模板与得分点',
    4: '计组计算题（Cache / 流水线 / 浮点数）速算技巧',
    5: '操作系统 PV 大题拆解套路',
    6: '计网子网划分与路由聚合的固定步骤',
    7: '错题本的正确用法：如何避免重复踩坑',
    8: '考前 30 天冲刺计划表',
    9: '考场时间分配与答题顺序建议',
    10: '二战 / 跨考考生的复习节奏调整',
}
for i, title in methods.items():
    add(title, f'复习方法论第 {i} 篇：{title}。结合 408 学科特点给出的可执行建议。',
        '方法论', '', f'method-{i}.pdf', ['方法论', '复习规划'])

# ---------- 公共课资料 ----------
add('考研英语大纲词汇 5500（乱序版）', '考研英语大纲要求掌握的 5500 词汇，乱序排列，适合滚动背诵。',
    '公共课资料', 'english', 'english-5500.pdf', ['英语', '词汇', '大纲'])
add('考研数学一公式速查手册', '高数、线代、概率论核心公式汇总，考前速查用。',
    '公共课资料', 'math', 'math-formula.pdf', ['数学一', '公式', '速查'])
add('考研政治复习大纲与考点框架', '马原、毛中特、史纲、思修法基、时政五大板块考点框架。',
    '公共课资料', 'politics', 'politics-outline.pdf', ['政治', '大纲', '考点'])

# ---------- 复试 ----------
add('计算机考研复试面试 200 问', '专业课面试、项目追问、英语口语、综合素质四类共 200 个高频问题与回答思路。',
    '复试', '', 'interview-200.pdf', ['复试', '面试', '口语'])

if not rows:
    print('ERROR: 未找到任何试卷文件，请先下载资料')
    sys.exit(1)


def q(s):
    return "'" + str(s).replace('\\', '\\\\').replace("'", "''") + "'"


lines = [
    '-- 由 scripts/rebuild_materials.py 自动生成，请勿手工编辑',
    '-- 内容来源：本地历年真题原卷（数学一 1987-2022 为本地真题文件；',
    '-- 408 2009-2026 来自 neville-studio/408-exam-paper 原始重排版；',
    '-- 英语一 2002-2022 来自 youngflysky/KaoYanZhenTi-PDF；',
    '-- 政治 2014-2024 来自郑州工商学院马克思主义学院公开真题附件）',
    'DELETE FROM `study_material`;',
    'INSERT INTO `study_material` (`id`,`title`,`description`,`category`,`subject`,`fileType`,`fileUrl`,',
    '  `fileSize`,`downloadNum`,`favourNum`,`viewNum`,`tags`,`uploaderId`,`status`,`isDelete`) VALUES',
]
values = []
start_id = 4000000000000000001
for idx, (title, desc, cat, subj, fname, tags) in enumerate(rows):
    size = os.path.getsize(os.path.join(UPLOAD, fname))
    values.append('({},{},{},{},{},{},{},{},0,0,0,{},1,1,0)'.format(
        start_id + idx, q(title), q(desc), q(cat), q(subj) if subj else 'NULL',
        q('pdf'), q('/api/file/material/' + fname), size,
        q(json.dumps(tags, ensure_ascii=False)),
    ))
lines.append(',\n'.join(values) + ';')

with open(OUT_SQL, 'w', encoding='utf-8') as f:
    f.write('\n'.join(lines) + '\n')

stat = {}
for r in rows:
    stat[r[2]] = stat.get(r[2], 0) + 1
print(f'生成 {len(rows)} 条资料记录 -> {OUT_SQL}')
for k, v in sorted(stat.items(), key=lambda x: -x[1]):
    print(f'  {k}: {v}')
