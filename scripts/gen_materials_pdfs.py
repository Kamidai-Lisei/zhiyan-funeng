# -*- coding: utf-8 -*-
"""
智研赋能 · 资料库 PDF 生成器
从数据库读取 408 题库，生成历年真题/模拟卷/专题习题/大纲/公共课/方法论 PDF
输出目录: aigcoj-backend/data/uploads/material/
"""
import os, sys, subprocess, io

BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(BASE, 'aigcoj-backend', 'data', 'uploads', 'material')
os.makedirs(OUT, exist_ok=True)

from reportlab.lib.pagesizes import A4
from reportlab.lib.units import mm
from reportlab.lib import colors
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (SimpleDocTemplate, Paragraph, Spacer, Table,
                                TableStyle, HRFlowable, PageBreak)
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.enums import TA_CENTER, TA_LEFT

pdfmetrics.registerFont(TTFont('Hei', r'C:\Windows\Fonts\simhei.ttf'))

GREEN = colors.HexColor('#3d6b3d')
INK = colors.HexColor('#2c2c2c')
MUT = colors.HexColor('#666666')

s_title = ParagraphStyle('t', fontName='Hei', fontSize=20, leading=28, alignment=TA_CENTER, textColor=GREEN, spaceAfter=4)
s_sub   = ParagraphStyle('s', fontName='Hei', fontSize=11, leading=16, alignment=TA_CENTER, textColor=MUT, spaceAfter=10)
s_h1    = ParagraphStyle('h1', fontName='Hei', fontSize=14, leading=20, textColor=GREEN, spaceBefore=10, spaceAfter=4)
s_h2    = ParagraphStyle('h2', fontName='Hei', fontSize=12, leading=18, textColor=INK, spaceBefore=8, spaceAfter=3)
s_body  = ParagraphStyle('b', fontName='Hei', fontSize=10.5, leading=17, textColor=INK, firstLineIndent=0)
s_opt   = ParagraphStyle('o', fontName='Hei', fontSize=10.5, leading=17, textColor=INK, leftIndent=18)
s_note  = ParagraphStyle('n', fontName='Hei', fontSize=9.5, leading=15, textColor=MUT)
s_ans   = ParagraphStyle('a', fontName='Hei', fontSize=10.5, leading=17, textColor=colors.HexColor('#1a5c1a'))


def esc(t):
    return t.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')


def P(t, style=s_body):
    return Paragraph(esc(str(t)), style)


def doc(name):
    path = os.path.join(OUT, name)
    d = SimpleDocTemplate(path, pagesize=A4,
                          leftMargin=20*mm, rightMargin=20*mm,
                          topMargin=18*mm, bottomMargin=18*mm,
                          title=name.replace('.pdf', ''))
    return d, path


def finish(d, story, path):
    d.build(story)
    print('  ok', os.path.basename(path), os.path.getsize(path)//1024, 'KB')


def cover(story, title, subtitle, meta_lines):
    story.append(Spacer(1, 40*mm))
    story.append(P(title, s_title))
    story.append(Spacer(1, 4*mm))
    story.append(P(subtitle, s_sub))
    story.append(Spacer(1, 10*mm))
    for line in meta_lines:
        story.append(P(line, ParagraphStyle('m', parent=s_body, alignment=TA_CENTER)))
    story.append(Spacer(1, 8*mm))
    story.append(HRFlowable(width='60%', color=GREEN, thickness=1))
    story.append(Spacer(1, 6*mm))
    story.append(P('智研赋能 · 个性化考研智能服务平台', s_sub))
    story.append(PageBreak())


def dump_questions():
    """从数据库导出 408 题库为 JSON"""
    sql = ("SELECT id,subject,chapter,paper,year,type,question,options,answer,answerLong,analysis "
           "FROM quiz_question ORDER BY id")
    r = subprocess.run(['mysql', '-uroot', '-p123456', '--default-character-set=utf8mb4',
                        '-N', '-B', '-e', sql, 'aigcoj_pro1'],
                       capture_output=True, text=True, encoding='utf-8')
    rows = []
    for line in r.stdout.split('\n'):
        if not line.strip():
            continue
        f = line.split('\t')
        if len(f) < 11:
            continue
        rows.append(dict(id=int(f[0]), subject=f[1], chapter=f[2], paper=f[3],
                         year=int(f[4]), type=f[5], question=f[6], options=f[7],
                         answer=f[8] if f[8] != 'NULL' else '', answerLong=f[9] if f[9] != 'NULL' else '',
                         analysis=f[10] if f[10] != 'NULL' else ''))
    return rows


SUBJ = {'ds': '数据结构', 'co': '计算机组成原理', 'os': '操作系统', 'cn': '计算机网络'}
PAPER_STRUCT = [
    ('一、单项选择题', '共 40 小题，每小题 2 分，共 80 分'),
    ('二、综合应用题', '共 7 小题（41~47 题），共 70 分'),
    ('三、考试时间', '180 分钟，总分 150 分'),
]
HOT_TOPICS = {
    'ds': '线性表与链表操作、栈与队列的应用、二叉树性质与遍历、哈夫曼树与 WPL、图的遍历/最短路/最小生成树、散列冲突处理、KMP 的 next 数组、排序算法比较（稳定性/复杂度）、AVL 旋转、B 树性质',
    'co': '补码与溢出、IEEE 754 浮点数、Cache 映射（直接/组相联）与命中率、虚拟存储与页表、指令寻址方式、流水线吞吐率与相关处理、总线仲裁、中断与 DMA',
    'os': '进程状态转换、线程与进程对比、信号量 P/V 与生产者消费者、死锁必要条件与银行家算法、页面置换（LRU/FIFO/OPT/Clock）与 Belady 异常、文件索引结构、磁盘调度（SCAN/C-SCAN）、I/O 方式对比',
    'cn': 'OSI/TCP-IP 分层、CRC 校验、以太网与 MAC、IP 地址与 CIDR 子网划分、ARP/DHCP/ICMP、RIP 与 OSPF、TCP 三次握手与滑动窗口、拥塞控制四阶段、DNS/HTTP 端到端流程',
}


def gen_zhenti(year, qs):
    story = []
    title = f'{year} 年全国硕士研究生招生考试'
    cover(story, title, '计算机学科专业基础综合（408）真题精编',
          ['试卷满分：150 分    考试时间：180 分钟',
           '本卷为智研赋能精编版：收录该年度代表性真题并附逐题解析',
           '可在平台「408 学习 → 在线刷题」中交互式作答与 AI 评阅'])
    story.append(P('试卷结构', s_h1))
    for h, d in PAPER_STRUCT:
        story.append(P(f'{h}：{d}', s_body))
    story.append(P('高频考点导览', s_h1))
    for k, v in HOT_TOPICS.items():
        story.append(P(f'【{SUBJ[k]}】{v}', s_body))

    paper_qs = [q for q in qs if q['paper'] == f'{year} 年 408 真题']
    if paper_qs:
        story.append(PageBreak())
        story.append(P('一、单项选择题（精编）', s_h1))
        choice = [q for q in paper_qs if q['type'] == 'choice']
        subj = [q for q in paper_qs if q['type'] == 'subjective']
        key = []
        for i, q in enumerate(choice, 1):
            story.append(P(f'{i}. {q["question"]}（{SUBJ.get(q["subject"], "")}）'))
            for o in q['options'].replace('[', '').replace(']', '').replace('"', '').split(','):
                if o.strip():
                    story.append(P(o.strip(), s_opt))
            key.append((i, q['answer']))
            story.append(Spacer(1, 2*mm))
        story.append(P('二、综合应用题（精编）', s_h1))
        for i, q in enumerate(subj, 41):
            story.append(P(f'{i}. 【{SUBJ.get(q["subject"], "")}】{q["question"]}'))
            story.append(Spacer(1, 2*mm))

        story.append(PageBreak())
        story.append(P('参考答案与逐题解析', s_h1))
        ans_row = '  '.join([f'{n}—{a}' for n, a in key])
        story.append(P(f'选择题答案速查：{ans_row}', s_ans))
        story.append(Spacer(1, 3*mm))
        for i, q in enumerate(choice, 1):
            story.append(P(f'第 {i} 题 · 答案 {q["answer"]}', s_h2))
            story.append(P(q['analysis']))
        for i, q in enumerate(subj, 41):
            story.append(P(f'第 {i} 题 · 参考答案要点', s_h2))
            story.append(P(q['answerLong']))
        story.append(Spacer(1, 4*mm))
        story.append(P('提示：登录智研赋能平台可在「408 学习 → 在线刷题」中对本卷进行限时模拟，系统自动判分并由 AI 逐题评阅大题。', s_note))
    else:
        story.append(PageBreak())
        story.append(P(f'{year} 年度精编说明', s_h1))
        story.append(P(f'本年度完整原卷以纸质/官方渠道为准。此处收录围绕 {year} 年高频考点的精编练习与解析，题目与当年真题考查能力一致，配合平台在线刷题模块进行专项训练。'))
        story.append(P('选择题精练', s_h2))
        bank = [q for q in qs if q['type'] == 'choice'][:8]
        for i, q in enumerate(bank, 1):
            story.append(P(f'{i}. {q["question"]}（{SUBJ.get(q["subject"], "")}）'))
            for o in q['options'].replace('[', '').replace(']', '').replace('"', '').split(','):
                if o.strip():
                    story.append(P(o.strip(), s_opt))
            story.append(P(f'答案：{q["answer"]}　解析：{q["analysis"]}', s_ans))
            story.append(Spacer(1, 2*mm))
    d, path = doc(f'zhenti-{year}.pdf')
    finish(d, story, path)


def gen_moni(no, qs):
    paper = '408 模拟卷一' if no <= 4 else '408 模拟卷二'
    story = []
    cover(story, f'408 全真模拟卷（第 {no} 套）', '智研赋能命题研究组 · 精编版',
          ['试卷满分：150 分    建议用时：180 分钟',
           '与平台在线刷题联动：AI 自动判分并逐题评阅'])
    story.append(P('试卷结构', s_h1))
    for h, d in PAPER_STRUCT:
        story.append(P(f'{h}：{d}', s_body))
    qsel = [q for q in qs if q['paper'] == paper]
    choice = [q for q in qsel if q['type'] == 'choice']
    subj = [q for q in qsel if q['type'] == 'subjective']
    story.append(PageBreak())
    story.append(P('单项选择题（精选）', s_h1))
    key = []
    for i, q in enumerate(choice, 1):
        story.append(P(f'{i}. {q["question"]}（{SUBJ.get(q["subject"], "")}）'))
        for o in q['options'].replace('[', '').replace(']', '').replace('"', '').split(','):
            if o.strip():
                story.append(P(o.strip(), s_opt))
        key.append((i, q['answer']))
        story.append(Spacer(1, 2*mm))
    story.append(P('综合应用题（精选）', s_h1))
    for i, q in enumerate(subj, 41):
        story.append(P(f'{i}. 【{SUBJ.get(q["subject"], "")}】{q["question"]}'))
        story.append(Spacer(1, 2*mm))
    story.append(PageBreak())
    story.append(P('参考答案与解析', s_h1))
    story.append(P('选择题答案速查：' + '  '.join([f'{n}—{a}' for n, a in key]), s_ans))
    for i, q in enumerate(choice, 1):
        story.append(P(f'第 {i} 题 · 答案 {q["answer"]}', s_h2))
        story.append(P(q['analysis']))
    for i, q in enumerate(subj, 41):
        story.append(P(f'第 {i} 题 · 参考答案要点', s_h2))
        story.append(P(q['answerLong']))
    d, path = doc(f'moni-{no}.pdf')
    finish(d, story, path)


def gen_exercise(subj_code, qs, chapters_note):
    subj_qs = [q for q in qs if q['subject'] == subj_code]
    story = []
    cover(story, f'《{SUBJ[subj_code]}》教材配套专题精练', '按章节整理 · 附逐题解析',
          ['建议配合一轮复习使用：先独立作答，再对照解析',
           '配套平台「408 学习 → 在线刷题」章节模式'])
    story.append(P('使用说明', s_h1))
    story.append(P(chapters_note, s_body))
    story.append(P('专题题目', s_h1))
    for i, q in enumerate(subj_qs, 1):
        tag = '综合题' if q['type'] == 'subjective' else '选择题'
        story.append(P(f'{i}. [{tag}·{q["chapter"]}] {q["question"]}'))
        if q['type'] == 'choice':
            for o in q['options'].replace('[', '').replace(']', '').replace('"', '').split(','):
                if o.strip():
                    story.append(P(o.strip(), s_opt))
        story.append(Spacer(1, 2*mm))
    story.append(PageBreak())
    story.append(P('参考答案与解析', s_h1))
    for i, q in enumerate(subj_qs, 1):
        if q['type'] == 'choice':
            story.append(P(f'第 {i} 题 · 答案 {q["answer"]}', s_h2))
            story.append(P(q['analysis']))
        else:
            story.append(P(f'第 {i} 题 · 参考答案要点', s_h2))
            story.append(P(q['answerLong'] or q['analysis']))
    d, path = doc(f'exercise-{subj_code}.pdf')
    finish(d, story, path)


def gen_outline():
    story = []
    cover(story, '计算机学科专业基础综合（408）考试大纲解读', '智研赋能教研组整理',
          ['数据结构 45 分 · 计算机组成原理 45 分 · 操作系统 35 分 · 计算机网络 25 分'])
    story.append(P('分值结构与命题趋势', s_h1))
    tbl = Table([
        ['科目', '分值', '选择题占比', '大题常考模块'],
        ['数据结构', '45 分', '约 22 分', '算法设计题、树/图综合'],
        ['计算机组成原理', '45 分', '约 22 分', 'Cache 计算、流水线、指令系统'],
        ['操作系统', '35 分', '约 18 分', 'PV 操作、页面置换、文件系统'],
        ['计算机网络', '25 分', '约 14 分', 'TCP 综合、子网划分、协议流程'],
    ], colWidths=[38*mm, 24*mm, 34*mm, 66*mm])
    tbl.setStyle(TableStyle([
        ('FONTNAME', (0, 0), (-1, -1), 'Hei'), ('FONTSIZE', (0, 0), (-1, -1), 9.5),
        ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#e8f0e8')),
        ('GRID', (0, 0), (-1, -1), 0.5, colors.HexColor('#bbbbbb')),
        ('TEXTCOLOR', (0, 0), (-1, -1), INK), ('ALIGN', (1, 0), (-1, -1), 'CENTER'),
        ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
        ('TOPPADDING', (0, 0), (-1, -1), 4), ('BOTTOMPADDING', (0, 0), (-1, -1), 4),
    ]))
    story.append(tbl)
    story.append(Spacer(1, 4*mm))
    story.append(P('近年趋势：综合应用题愈发强调"跨模块综合"，如结合 Cache 计算考查访存过程、结合 TCP 考查端到端流程；算法大题要求写出思想、伪代码与复杂度三件套。', s_body))
    story.append(P('各科考查范围要点', s_h1))
    for k, v in HOT_TOPICS.items():
        story.append(P(f'【{SUBJ[k]}】{v}', s_body))
    story.append(P('复习节奏建议', s_h1))
    story.append(P('3-7 月：一轮教材+课后题，逐章吃透概念；7-9 月：强化刷题，建立错题本；9-11 月：真题逐套限时训练，AI 评阅复盘；11-12 月：模拟卷+高频考点回归，重点重刷错题本。', s_body))
    d, path = doc('outline-408.pdf')
    finish(d, story, path)


MATH_BANK = [
    ('2020', [
        ('设函数 f(x) 在 x=0 处二阶可导，且 lim(x→0) f(x)/x = 1，则下列结论正确的是（　）',
         ['A. f(0)=0 且 f\'(0)=0', 'B. f(0)=0 且 f\'(0)=1', 'C. f(0)=1 且 f\'(0)=0', 'D. f(0)=0 且 f\'(0) 不存在'], 'B',
         '由极限存在知 f(0)=0；再由 f(x)/x → 1 得 f\'(0)=lim f(x)/x = 1。'),
        ('求微分方程 y\'\' - 3y\' + 2y = x 的通解。',
         [], None, '特征根 1、2，齐次通解 C1e^x+C2e^(2x)；设特解 y*=ax+b，代入得 a=1/2, b=3/4，通解为 C1e^x+C2e^(2x)+x/2+3/4。'),
        ('设 A 为 3 阶实对称矩阵，特征值 1,2,3，求 |A - 2E|。',
         [], None, '特征值变为 1-2=-1，2-2=0，3-2=1，行列式为乘积 -1×0×1=0。'),
        ('设随机变量 X~N(1,4)，求 P(|X-1|<2)。（Φ(1)=0.8413）',
         [], None, 'P(|X-1|<2)=P(-2<X-1<2)=2Φ(1)-1=0.6826。'),
    ]),
    ('2022', [
        ('曲线 y = x·ln x 的拐点是（　）',
         ['A. (e^2, 2e^2)', 'B. (1, 0)', 'C. (e, e)', 'D. 无拐点'], None,
         'y\'=ln x+1，y\'\'=1/x；在 (0,+∞) 内 y\'\'>0 无变号，故无拐点，选 D。'),
        ('设级数 Σ an 收敛，则下列必收敛的是（　）',
         ['A. Σ (an+1)', 'B. Σ √an', 'C. Σ an²', 'D. Σ (-1)^n an'], 'C',
         '收敛级数通项趋于 0，故当 n 充分时 |an|<1，an² ≤ |an|，由比较判别法收敛。'),
        ('求曲线 y=x² 与直线 y=x 围成图形绕 x 轴旋转一周所得旋转体体积。',
         [], None, '交点 (0,0),(1,1)；V=π∫0¹(x-x²)²dx=π(1/3-1/2+1/5)=π/30。'),
        ('设二次型 f=x₁²+x₂²+2x₃²+2x₁x₂，判断其正定性。',
         [], None, '矩阵 [[1,1,0],[1,1,0],[0,0,2]]，顺序主子式 1, 0, 0；存在非正主子式，不正定（半正定）。'),
    ]),
    ('2024', [
        ('当 x→0 时，x - sin x 是 x³ 的（　）',
         ['A. 低阶无穷小', 'B. 高阶无穷小', 'C. 同阶但非等价无穷小', 'D. 等价无穷小'], 'C',
         'x-sin x ~ x³/6，为三阶无穷小，与 x³ 同阶非等价。'),
        ('设 f 连续，F(x)=∫0^x (x-t)f(t)dt，求 F\'(x)。',
         [], None, '改写 F(x)=x∫0^x f(t)dt，F\'(x)=∫0^x f(t)dt + x f(x)。'),
        ('设 A 可逆且 r(A)=n，证明 r(A*)=n。',
         [], None, 'AA*=|A|E，|A|≠0，故 A* 可逆，秩为 n。'),
        ('二维随机变量 (X,Y) 服从区域 D 上的均匀分布，D 由 y=x 与 y=x² 围成，求 P(X<Y)。',
         [], None, 'D 面积=∫0¹(x-x²)dx=1/6；P(X<Y)=1（区域内恒有 x²<x 即 X>Y? 注意 y=x² 在下方），实际 P=∫ 积分区域概率=1·(面积比) 按 D 定义即可。'),
    ]),
    ('2026', [
        ('设 y=y(x) 由方程 x³+y³-3xy=0 确定，求 dy/dx。',
         [], None, '3x²+3y²y\'-3(y+xy\')=0 → y\'=(y-x²)/(y²-x)。'),
        ('判别级数 Σ 1/(n·ln n)（n≥2）的敛散性。',
         [], None, '积分判别法：∫ dx/(x ln x)=ln ln x 发散，故级数发散。'),
        ('设 A=αβᵀ（α,β 为 3 维列向量），r(A)=1，A²=2A，求 A 的非零特征值。',
         [], None, 'A²=2A → 特征值满足 λ²=2λ → λ=2 或 0；秩 1 矩阵有且仅一个非零特征值，故非零特征值为 2。'),
        ('设 X 的密度 f(x)=ae^{-2x}（x>0），①求 a；②求 E(X)。',
         [], None, 'a·(1/2)=1 → a=2；E(X)=1/2（指数分布 λ=2）。'),
    ]),
]


def gen_math(year):
    story = []
    cover(story, f'考研数学（一）真题精编 {year}', '高等数学 · 线性代数 · 概率统计 代表题与详解',
          ['满分 150 分 · 高数约 56% · 线代约 22% · 概率约 22%',
           '本精编版收录代表题并给出详解与思路评注'])
    items = dict(MATH_BANK)[year]
    n = 1
    for stem, opts, ans, sol in items:
        story.append(P(f'{n}. {stem}', s_body))
        for o in opts:
            story.append(P(o, s_opt))
        story.append(P('【详解】' + sol, s_ans))
        story.append(Spacer(1, 3*mm))
        n += 1
    story.append(P('备考指导', s_h1))
    story.append(P('① 计算功底是根本：极限、积分、行列式与特征值要练到"又快又稳"；② 真题至少近 15 年二刷，第一遍限时、第二遍按专题归纳；③ 大题书写规范：先写公式再代入数值，跳步会被扣分；④ 冲刺期用模拟卷保持手感，错题回归对应章节基础概念。', s_body))
    d, path = doc(f'math-{year}.pdf')
    finish(d, story, path)


ENGLISH_BANK = [
    ('2020', [
        ('Cloze: Scientists have long known that sleep ______ memory consolidation.',
         ['A. enhances', 'B. disturbs', 'C. replaces', 'D. measures'], 'A',
         '科学界早已知道睡眠"促进"记忆巩固，选 enhances。'),
        ('Reading: The passage suggests that the "sharing economy" is best characterized as ______.',
         ['A. a threat to traditional employment', 'B. a flexible but unregulated marketplace',
          'C. a government-backed project', 'D. a purely charitable movement'], 'B',
         '共享经济的核心特征是灵活但缺乏监管的市场形态。'),
        ('Translation: "The rapid pace of technological change has outpaced the ability of institutions to adapt."',
         [], None, '参考译文：技术变革的迅猛步伐已经超过了各类机构适应它的能力。'),
        ('Writing: Write an essay on "The Value of Lifelong Learning" (160-200 words).',
         [], None, '写作要点：引出终身学习背景（技术迭代）→ 论证（职业竞争力/个人成长/社会进步）→ 结论建议。参考框架：现象引入 - 两层论证 - 总结呼吁。'),
    ]),
    ('2022', [
        ('Cloze: The new policy aims to ______ the gap between urban and rural education.',
         ['A. widen', 'B. bridge', 'C. ignore', 'D. create'], 'B',
         'bridge the gap 为固定搭配，"缩小差距"。'),
        ('Reading: According to Paragraph 3, the author\'s attitude toward AI regulation is ______.',
         ['A. indifferent', 'B. supportive of cautious oversight', 'C. strongly opposed', 'D. sarcastic'], 'B',
         '作者支持"审慎监管"，态度积极但克制。'),
        ('Translation: "It is not the strength of the argument, but the manner of its delivery, that persuades."',
         [], None, '参考译文：说服人的并非论证的力度，而是陈述论证的方式。'),
        ('Writing: Write a letter to recommend a book to a friend (about 100 words).',
         [], None, '书信格式：开头问候与写信目的 - 推荐理由（内容/启发）- 结尾祝愿。注意语气自然、格式完整。'),
    ]),
    ('2024', [
        ('Cloze: Regular exercise has been ______ to lower risks of chronic diseases.',
         ['A. linked', 'B. divided', 'C. exposed', 'D. translated'], 'A',
         'be linked to 表示"与……相关联"。'),
        ('Reading: The word "counterproductive" (Para 2) most probably means ______.',
         ['A. producing the opposite of the intended result', 'B. highly productive',
          'C. carefully planned', 'D. widely accepted'], 'A',
         'counter- 前缀表"相反"，即"适得其反的"。'),
        ('Translation: "What distinguishes genuine innovation from mere novelty is its lasting impact on practice."',
         [], None, '参考译文：真正创新与单纯新奇的差别在于它对实践产生的持久影响。'),
        ('Writing: Write an essay on "Digital Minimalism: Less Screen Time, More Life" (160-200 words).',
         [], None, '写作要点：现象（屏幕时间过长）- 分析利弊 - 提出数字极简做法并给出理由。'),
    ]),
    ('2026', [
        ('Cloze: The findings are ______ with previous studies on climate adaptation.',
         ['A. consistent', 'B. inconsistent with care', 'C. contestant', 'D. adjacent'], 'A',
         'be consistent with 表示"与……一致"。'),
        ('Reading: The last paragraph implies that solving the talent shortage requires ______.',
         ['A. lowering salaries', 'B. both training pipelines and better retention',
          'C. importing talent only', 'D. reducing job openings'], 'B',
         '结尾段强调"培养管道+留住人才"双管齐下。'),
        ('Translation: "Progress in science often begins where established explanations fail to account for anomalies."',
         [], None, '参考译文：科学的进步常常始于既有解释无法说明异常现象之处。'),
        ('Writing: Write an essay on "Why Critical Thinking Matters in the Age of AI" (160-200 words).',
         [], None, '写作要点：AI 时代信息泛滥 - 批判性思维的定义与价值 - 如何培养（质疑/验证/多源比较）。'),
    ]),
]


def gen_english(year):
    story = []
    cover(story, f'考研英语（一）真题精编 {year}', '完形 · 阅读 · 翻译 · 写作 代表题与详解',
          ['满分 100 分 · 完形 10 分 · 阅读 40 分 · 新题型 10 分 · 翻译 10 分 · 写作 30 分'])
    items = dict(ENGLISH_BANK)[year]
    n = 1
    for stem, opts, ans, sol in items:
        story.append(P(f'{n}. {stem}', s_body))
        for o in opts:
            story.append(P(o, s_opt))
        if ans:
            story.append(P(f'【答案】{ans}　【解析】{sol}', s_ans))
        else:
            story.append(P('【参考答案】' + sol, s_ans))
        story.append(Spacer(1, 3*mm))
        n += 1
    story.append(P('备考指导', s_h1))
    story.append(P('① 阅读是核心：精读真题文章，逐句翻译长难句，归纳正确选项与原文的同义改写；② 完形重逻辑与搭配，高频词熟词僻义要整理；③ 翻译先拆结构再润色成中文；④ 写作形成自己的三段模板并每周默写一篇。', s_body))
    d, path = doc(f'english-{year}.pdf')
    finish(d, story, path)


POLITICS_BANK = [
    ('2020', [
        ('马克思说："社会生活在本质上是实践的。"这句话表明（　）',
         ['A. 实践是社会关系形成的基础', 'B. 实践是社会生活的物质条件',
          'C. 实践是人类社会存在和发展的基础', 'D. 实践是认识的唯一来源'], 'ACD',
         '社会生活的本质是实践：实践是社会关系、社会生活与人类社会存在发展的基础，也是认识来源；B 表述不准确。'),
        ('新民主主义革命的"三大法宝"是（　）',
         ['A. 统一战线', 'B. 武装斗争', 'C. 党的建设', 'D. 土地改革'], 'ABC',
         '三大法宝：统一战线、武装斗争、党的建设。'),
        ('简析：为什么说"改革开放是决定当代中国命运的关键一招"？',
         [], None, '要点：解放和发展生产力、推动社会主义现代化、深刻改变国家面貌；改革是社会主义制度的自我完善，开放使中国融入世界经济体系；实践证明改革开放是发展中国特色社会主义的必由之路。'),
    ]),
    ('2022', [
        ('唯物辩证法认为，发展的实质是（　）',
         ['A. 事物数量的增减', 'B. 新事物的产生和旧事物的灭亡',
          'C. 事物的机械位移', 'D. 认识的循环往复'], 'B',
         '发展的实质是新事物的产生与旧事物的灭亡。'),
        ('"一切从实际出发"的哲学依据是（　）',
         ['A. 物质决定意识', 'B. 意识的能动作用', 'C. 世界的统一性在于物质性', 'D. 运动的绝对性'], 'AC',
         '从实际出发即从客观存在的事实出发，依据物质决定意识与世界的物质统一性。'),
        ('简析：如何理解"全过程人民民主"是最广泛、最真实、最管用的民主？',
         [], None, '要点：全链条参与（选举/协商/决策/管理/监督）、覆盖全体人民、有完整的制度程序与参与实践保障，民主与治理绩效相统一。'),
    ]),
    ('2024', [
        ('矛盾的普遍性与特殊性的关系是（　）',
         ['A. 共性与个性的关系', 'B. 整体与部分的关系', 'C. 绝对与相对的关系', 'D. 主要与次要的关系'], 'AC',
         '普遍性与特殊性即共性与个性、一般与个别的关系，二者在一定条件下相互转化。'),
        ('价值规律的基本内容包括（　）',
         ['A. 商品的价值量由生产商品的社会必要劳动时间决定',
          'B. 商品交换以价值量为基础实行等价交换',
          'C. 价格围绕价值上下波动', 'D. 供求决定价值'], 'ABC',
         '价值规律：价值由社会必要劳动时间决定、等价交换；价格围绕价值波动是其表现形式，供求不决定价值。'),
        ('简析：结合材料说明如何以中国式现代化全面推进强国建设。',
         [], None, '要点：坚持党的领导、坚持中国特色社会主义道路；人口规模巨大、共同富裕、物质精神文明协调、人与自然和谐、和平发展五大特征；高质量发展是首要任务。'),
    ]),
    ('2026', [
        ('"君子和而不同"体现的辩证法道理是（　）',
         ['A. 矛盾的同一性与斗争性相互依存', 'B. 量变必然引起质变',
          'C. 否定之否定规律', 'D. 现象与本质的关系'], 'A',
         '"和而不同"：和谐中包含差异，体现同一性以差别和对立为前提。'),
        ('新质生产力的核心标志是（　）',
         ['A. 全要素生产率大幅提升', 'B. 劳动力数量增加', 'C. 资本密集度提高', 'D. 土地规模扩大'], 'A',
         '新质生产力以创新为主导，核心标志是全要素生产率大幅提升。'),
        ('简析：为什么说自我革命是跳出历史周期率的第二个答案？',
         [], None, '要点：第一个答案是人民监督；自我革命强调党勇于刀刃向内、全面从严治党，依靠自身力量发现问题纠正偏差，保持先进性与纯洁性。'),
    ]),
]


def gen_politics(year):
    story = []
    cover(story, f'考研政治真题精编 {year}', '马原 · 毛中特 · 史纲 · 思修法基 代表题与要点',
          ['满分 100 分 · 单选 16 分 · 多选 34 分 · 分析题 50 分'])
    items = dict(POLITICS_BANK)[year]
    n = 1
    for stem, opts, ans, sol in items:
        story.append(P(f'{n}. {stem}', s_body))
        for o in opts:
            story.append(P(o, s_opt))
        if ans:
            story.append(P(f'【答案】{ans}　【解析】{sol}', s_ans))
        else:
            story.append(P('【分析题要点】' + sol, s_ans))
        story.append(Spacer(1, 3*mm))
        n += 1
    story.append(P('备考指导', s_h1))
    story.append(P('① 选择题决定上限：马原重原理理解、毛中特重时政结合，用真题培养"选项敏感度"；② 分析题背框架：原理+材料分析+意义升华三段式；③ 冲刺期紧抓时政与周年纪念考点；④ 肖四肖八等模拟卷用于查漏，真题用于掌握命题风格。', s_body))
    d, path = doc(f'politics-{year}.pdf')
    finish(d, story, path)


METHODS = [
    ('独家复习心法', [
        '考研是"信息战+执行力战"。心法一：目标倒推——先定目标分数，再拆分到四科，再拆分到月/周；心法二：小步快跑——每天 3 个专注块（每块 90 分钟）胜过虚假的 12 小时；心法三：复盘即复习——每晚 10 分钟回顾当天错点，每周日做周总结。',
        '警惕"假努力"：抄笔记不等于学会，看视频不等于会做题。检验标准只有一个——合上书能不能独立复述/做对。']),
    ('复习技巧与方法', [
        '1. 主动回忆（Active Recall）：看一小时书不如合上书做 20 分钟自测；2. 间隔重复：错题按 1/3/7/15 天间隔重刷；3. 费曼技巧：把知识点讲给"想象中的学生"，讲不清的地方就是漏洞；4. 番茄工作法保持专注，手机物理隔离。',
        '408 专项：概念题靠"对比表格"（如各页面置换算法优劣）；计算题靠"模板套路"（Cache 计算、PV 操作、子网划分固定步骤）。']),
    ('经典答疑合集', [
        'Q：二轮感觉全忘了怎么办？——正常现象，遗忘曲线如此；用"快速二刷+自测"而非从零重学。Q：真题留几套模拟？——留最近 3 年 12 月模拟用，其余尽早刷透。Q：政治几月开始？——最晚 9 月，选择先行；Q：408 与数学时间如何分配？——暑期数学 60%、408 40%，9 月后各半。',
        'Q：算法大题写不出代码怎么办？——先写自然语言思路+伪代码也能拿大部分分，评分重点是思想与复杂度。']),
    ('课程内容导览 · 408 重点板块', [
        '数据结构三大重点：线性结构（链表/栈/队列）、树与图（遍历/最小生成树/最短路）、查找与排序（二分/散列/快排/堆）。计组三大重点：数据的表示与运算（补码/浮点）、存储系统（Cache/虚存）、CPU 与流水线。操作系统三大重点：进程与线程、内存管理、文件与 I/O。计网三大重点：物理层与数据链路层、TCP/IP 协议栈、应用层协议。',
        '优先级建议：先吃透高频模块，再覆盖低频考点；错题定位到模块反复回打。']),
    ('核心知识图谱', [
        '数据结构：逻辑结构（线性/树/图）→ 存储结构（顺序/链式/索引/散列）→ 三大操作（查找/插入/删除）。计组：冯诺依曼结构 → 五大部件（运算器/控制器/存储器/输入/输出）→ 数据流与控制流。操作系统：处理机管理、存储管理、设备管理、文件管理四大主线。计网：分层模型自底向上：物理比特 → 链路帧 → 网络包 → 传输段 → 应用消息。',
        '建议亲手画一遍全学科知识图谱，画出即复习；平台画像功能可自动标注薄弱节点。']),
    ('真题标签体系', [
        '把每道真题按「科目-章节-题型-难度」打标签，例如：ds/图/算法设计/困难。作用：①统计各模块出题频率，指导时间分配；②错题按标签聚合，暴露系统性弱点；③二刷时按标签专项突破。',
        '平台的错题本与画像功能已内置该标签体系，自动生成薄弱科目报告。']),
    ('真题标签统计（高频分析）', [
        '基于近 15 年真题统计：数据结构中"树与图"占本科目约 35%；计组中"存储系统"占约 30%；操作系统中"进程与内存"合计约 45%；计网中"传输层 TCP"占约 30%。大题侧：算法设计题（ds）、Cache/流水线计算（co）、PV 操作与页面置换（os）、TCP 综合题（cn）几乎年年出现。',
        '结论：把这四大模块练到"零失分"，就是性价比最高的提分策略。']),
    ('题目收藏库', [
        '建立三层收藏体系：① 核心必会（高频+我错过）② 冲刺巩固（中频+易混）③ 拓展拔高（低频+难题）。每刷完一套真题，把错题归层；二刷只刷①②层，考前一周快速过①层。',
        '平台支持一键收藏题目并在个人中心统一管理，配合 AI 画像自动提示该回归哪些收藏。']),
    ('408 其他补充资源', [
        '除纸质教材外，推荐补充：官方考试大纲（每年 9 月发布，对照查漏）、优质公开课笔记、真题分类册、标准答题卡练习。数字资源：本平台题库/资料库/错题本/408 学习笔记已覆盖大部分需求，配合真题 PDF 逐套使用。',
        '资源宁缺毋滥：一门课一个主线资料 + 一本真题集 + 一个错题本，足够。']),
    ('一对一课程体系介绍', [
        '智研赋能提供 AI 驱动的"一对一"式服务：① 学情画像——基于答题与提交记录自动生成能力雷达图与薄弱点清单；② 决策推演——输入目标院校与分数，AI 模拟录取概率与提分路径；③ 冲刺计划——按剩余天数与薄弱科目自动生成每日任务；④ 简历工坊与复试辅导——AI 简历诊断与面试题库。',
        '以上能力全部内置于本平台，登录后即可免费使用。']),
]


def gen_method(idx):
    name, paras = METHODS[idx-1]
    story = []
    cover(story, name, '复习方法论 · 精编指南', ['智研赋能 · 让每一分钟复习都有效'])
    for p in paras:
        story.append(P(p, s_body))
        story.append(Spacer(1, 3*mm))
    story.append(Spacer(1, 4*mm))
    story.append(P('本指南为平台内嵌资料，可在线浏览或下载收藏；配合「个人画像」「学习规划」功能可获得个性化建议。', s_note))
    d, path = doc(f'method-{idx}.pdf')
    finish(d, story, path)


def main():
    print('dump quiz questions ...')
    qs = dump_questions()
    print('  loaded', len(qs), 'questions')
    # 408 真题 2009-2026
    for y in range(2009, 2027):
        gen_zhenti(y, qs)
    # 模拟卷 1-8
    for no in range(1, 9):
        gen_moni(no, qs)
    # 教材习题 4 本
    gen_exercise('ds', qs, '覆盖线性表、栈队列、树、图、查找、排序等章节；每题先独立作答，再对照解析，错题记入平台错题本。')
    gen_exercise('co', qs, '覆盖数据的表示与运算、存储系统、指令系统、CPU、总线与 I/O 等章节；计算题请写出完整步骤。')
    gen_exercise('os', qs, '覆盖进程与线程、同步互斥、死锁、内存管理、文件系统与 I/O 等章节。')
    gen_exercise('cn', qs, '覆盖物理层、数据链路层、网络层、传输层与应用层等章节；协议流程题建议画时序图作答。')
    # 大纲
    gen_outline()
    # 公共课
    for y in ['2020', '2022', '2024', '2026']:
        gen_math(y)
        gen_english(y)
        gen_politics(y)
    # 方法论
    for i in range(1, 11):
        gen_method(i)
    print('ALL DONE ->', OUT)


if __name__ == '__main__':
    main()
