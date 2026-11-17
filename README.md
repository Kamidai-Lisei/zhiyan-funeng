# 智研赋能 · 个性化考研智能服务平台

> 在 AI 判题系统（AIGCOJ）基础上演进而来。核心技术是 **AI 长期记忆、学习决策推演与资源价值评估**，构建动态进化的个性化考研智能服务体系 —— **一人一档、一人一策、一人一资源**，前期聚焦计算机考研（408）。
>
> 三大 AI 引擎：**个人画像**（刷题/打卡/错题持续沉淀为动态学情档案）、**决策模拟器**（冲稳保多情景择校概率推演）、**资源价值引擎**（按画像薄弱点为资料排序适配度）；辅以个性化学习规划、错题诊断、在线判题、408 精讲、论坛、就业与复试辅导、AI 简历诊断。
>
> 技术栈：**Spring Boot 2.7 + MyBatis-Plus + MySQL 8 + Redis + Sa-Token**（后端） / **Vue 3 + Vite + TypeScript + Pinia + Arco Design**（前端），AI 能力接入 **DeepSeek**。

---

## 一、目录结构

```
.
├── aigcoj-backend/          # 后端（Spring Boot，端口 8121，context-path=/api）
│   ├── src/main/java/com/jay/aigcoj/
│   │   ├── controller/      # 接口层（题目 / 提交 / 资料 / 论坛 / 学习 / 简历 / AI / 统计 / 搜索 / 文件）
│   │   ├── service/         # 业务层
│   │   ├── judge/           # 判题：沙箱策略 + 判题策略
│   │   ├── model/           # entity / dto / vo / enums
│   │   └── bot/ds/DSApi.java# DeepSeek 调用封装
│   ├── sql/                 # 初始化与增量脚本
│   └── data/uploads/        # 上传文件与内置 PDF 资料
├── aigcoj-portal/           # 新前端（Vue 3 + Vite + TS，端口 5173）
│   └── src/
│       ├── views/           # 15 个业务页面
│       ├── components/      # 图标、花卉装饰、Markdown、Monaco 编辑器、热力图、搜索、AI 助手
│       ├── assets/fonts/    # Lora 衬线字体（自托管，离线可用）
│       ├── content/408/     # 408 精讲 Markdown（14 章）
│       └── styles/main.css  # 田园核风设计系统（Design Tokens + 组件样式）
├── aigcoj-frontend/         # 【旧版前端】原 Vite2 + Ant Design 版本，仅作留档
└── scripts/gen_materials.py # 内置 PDF 资料生成脚本（ReportLab）
```

---

## 二、快速启动

### 1. 依赖

| 组件 | 版本 / 说明 |
| --- | --- |
| JDK | 8+（实测 Temurin 17） |
| MySQL | 8.x，库名 `aigcoj_pro1` |
| Redis | 6+（Sa-Token 会话共享，密码 `123456`） |
| Node.js | 18+ |

### 2. 初始化数据库

```bash
cd aigcoj-backend/sql
mysql -uroot -p123456 --default-character-set=utf8mb4 < aigcoj_pro1.sql     # 原始库结构 + 基础数据
mysql -uroot -p123456 --default-character-set=utf8mb4 < upgrade_v2.sql       # 平台新表（资料/简历/打卡/评论/收藏…）
mysql -uroot -p123456 --default-character-set=utf8mb4 < seed_questions.sql   # 16 道算法题（含测试用例）
mysql -uroot -p123456 --default-character-set=utf8mb4 < seed_questions_v2.sql # 追加 12 道编程题（四科 + 算法，共 28 题）
mysql -uroot -p123456 --default-character-set=utf8mb4 < seed_content.sql     # 资料 / 帖子 / 打卡 / 学习进度
mysql -uroot -p123456 --default-character-set=utf8mb4 < upgrade_zhiyan.sql   # 智研赋能：AI 长期记忆表
mysql -uroot -p123456 --default-character-set=utf8mb4 < seed_quiz.sql        # 408 在线习题：四科章节练习 + 往年真题（24 题）
```

> 演示账号密码统一为 **`kaoyan2026`**（后端要求密码 ≥ 8 位，哈希 = md5("panghu"+密码)）：
>
> | 账号 | 密码 | 角色 |
> | --- | --- | --- |
> | `jayjay` | `kaoyan2026` | 管理员 |
> | `aiaiai` | `kaoyan2026` | 管理员 |

### 3. 启动后端

```bash
cd aigcoj-backend
./mvnw -DskipTests package
java -jar target/aigcoj-backend-0.0.1-SNAPSHOT.jar --server.port=8121
```

关键配置在 `src/main/resources/application.yml`：

```yaml
codesandbox:
  type: local                      # local / remote / thirdParty / example / ai
file:
  upload-dir: ./data/uploads
ai:
  deepseek-key: ${AI_DEEPSEEK_KEY:sk-...}
```

> 注意：线上环境请用环境变量覆盖 `AI_DEEPSEEK_KEY`、数据库密码与 Redis 密码。

### 4. 启动前端

```bash
cd aigcoj-portal
npm install
npm run dev        # http://localhost:5173
npm run build      # 生产构建
```

Vite 已配置代理：`/api/* → http://localhost:8121`，Session Cookie 同源携带。

---

## 三、功能地图

| 模块 | 路由 | 能力 |
| --- | --- | --- |
| 首页 | `/` | 一页封面 + 三大 AI 引擎入口（画像/推演/规划）+ 学习场景链接列 + 考研倒计时；其余内容都在各自场景里 |
| 我的画像 | `/portrait` | 一人一档：提交/正确率/打卡/学时总览、知识点掌握度条、薄弱点与优势标签、AI 画像总结、AI 长期记忆时间线 |
| 决策推演 | `/simulator` | 输入目标院校与估分，输出冲/稳/保三情景上岸概率 + AI 推演报告（画像自动作为输入），历史推演存档 |
| 学习规划 | `/plan` | 基于画像与目标日期生成基础/强化/冲刺/复试全周期规划，历史版本留档 |
| 题库 | `/problems` | 全编程题（28 道，对标蓝桥题库）：科目/难度/关键词筛选、通过率、我的 AC 状态、收藏、分页 |
| 在线做题 | `/problems/:id` | Monaco 编辑器（Java / C++ / Python）、多语言草稿本地保存、提交轮询判题、**按测试用例计分（得分 + 通过用例数 + 失败用例详情）**、AI 代码点评、题目讨论区、我的提交 |
| 408 在线习题 | `/learn/quiz` | 四科章节练习 + 往年真题（选择题）：在线作答即时判分、逐题解析、按科目统计正确率 |
| 我的提交 | `/submissions` | 语言/状态筛选、代码查看弹窗 |
| 错题本 | `/wrong-book` | 未通过题目自动归集、收藏、重新挑战 |
| 408 学习 | `/learn/408` | 四科 14 章结构化笔记（Markdown）、阅读进度、章节完成标记、上/下一篇 |
| 资料库 | `/materials` | 资源价值引擎：登录后按画像薄弱点计算每份资料的适配度并智能排序；分类侧栏、最新/最热/收藏排序、PDF 在线预览、收藏与下载、管理员上传 |
| 论坛 | `/forum` | 最新/热门/精华、标签筛选、Markdown 发帖与预览、点赞收藏、评论、相关推荐、活跃榜 |
| 就业与复试 | `/career` | 复试时间线、简历评分卡、求职方向、面试高频题折叠面板、复试经验帖 |
| 简历工坊 | `/career/resume` | 结构化编辑 + 实时纸张预览、三套模板、AI 诊断（评分 + 亮点/问题/改写建议）、打印导出 PDF |
| 个人中心 | `/me` | 资料卡、连续打卡/时长/AC/排名、热力图、备考计划、我的帖子、资料收藏、快捷入口 |
| 管理后台 | `/admin` | 数据概览（7 日活跃柱状图、排行榜、热门三榜）、用户角色管理、题库浏览、资料上传与删除 |
| 全局 | — | `⌘/Ctrl + K` 全局搜索（题目/资料/帖子）、右下角 AI 学习助手（5 种场景） |

### 视觉设计（田园核风 Cottagecore）

整站围绕「想住进去的乡下午后」来做：亚麻纸底、大地色与花色、衬线字体、手缝的边。

| 类别 | 取值 |
| --- | --- |
| 主色 Meadow | `#5a8f5a`（按钮、链接、进度） |
| 纸底 Cloth | `#faf6f0`（叠了 SVG 噪点 + 经纬织纹） |
| 卡片 Lighter Cloth | `#fffdf8` |
| 麦黄 Wheat | `#f5d75f`（高亮、徽标、打卡柱） |
| 木棕 Timber | `#8b7355`（次级文字、描边） |
| 野蔷薇 Wild Rose | `#d4a0a0`（情绪化强调、PDF 徽章） |
| 字体 | `Lora`（自托管 woff2） + `Noto Serif SC / 宋体`，正文 15.5px / 1.75，标题 36–52px |
| 圆角 | 卡片 24px、按钮 999px、输入框 16px |
| 阴影 | `0 4px 10px rgba(90,143,90,.20)`，hover 扩到 `0 8px 20px` |
| 分隔 | 2px 虚线（dash 6 / gap 4），替代直线 |
| 装饰 | `components/Floral.vue`：叶片、小花、蘑菇、麦穗四种手绘线稿，放在卡片角落，允许 -8°~8° 倾斜 |

动效约定：hover 上浮 2px 并旋转 0.8°，过渡 500ms ease-in-out；全局带 `prefers-reduced-motion` 降级（关掉位移与旋转）。禁令：不用冷蓝灰、不用锐角与硬边框、不用霓虹色、不用科技工业感元素。

两个实现细节：

1. **Arco 调色板**：Arco 把 `--primary-*` 与 `--color-*` 定义在 `body` 上，优先级高于 `:root`，所以覆盖必须写在 `body` 里（见 `main.css` 末尾），否则按钮仍然是默认蓝。
2. **Monaco 主题**：`components/CodeEditor.vue` 里用 `monaco.editor.defineTheme('linen', …)` 注册暖色主题（米白底、木棕行号、草绿光标），避免编辑器割裂。

### 判题与 AI

- **代码沙箱**（策略模式）：`LocalCodeSandbox`（本机 javac / g++ / python，编译 20s、运行 5s、输出 1MB 上限）、远程沙箱、AI 沙箱、示例沙箱；`codesandbox.type` 切换，数据库 `switch` 表可动态开关 AI 判题。
- **判题策略**：`DefaultJudgeStrategy`、`JavaLanguageJudgeStrategy`，统一处理 `Compile Error / Runtime Error / Time Limit Exceeded / Accepted / Wrong Answer`，按行 trim 对比 stdout；**按通过用例比例计分**（`judgeInfo.score / passed / total / detail`），提交记录页与做题页展示得分与失败用例详情；C++ 沙箱已修复 Windows `.exe` 后缀问题，运行时错误不再中断整轮判题。
- **AI 能力**（DeepSeek chat completions）：代码点评、简历诊断（要求先输出 `评分：<n>` 再分节，失败时降级到本地规则引擎）、学习助手（study/code/interview/resume/plan 五种系统提示词）。
- **智研赋能三引擎**（`PortraitService` + `AiController`）：`/study/portrait` 聚合画像（按题目标签统计掌握度、薄弱/优势标签、学习投入）；`/ai/simulate` 择校推演（画像作为 prompt 输入，冲稳保概率由本地 logistic 启发式模型给出，AI 不可用时整体降级为本地模板报告）；`/ai/plan/generate` 全周期规划；`/ai/portrait/summary` 画像总结；所有产出写入 `ai_memory` 表形成 AI 长期记忆，经 `/ai/memory` 读取。

---

## 四、数据库增量

- `upgrade_v2.sql`：`code_analysis`、`study_material`、`material_favour`、`resume`、`resume_analysis`、`study_checkin`、`study_progress`、`study_plan`、`post_comment`、`question_favour`。
- `upgrade_zhiyan.sql`：`ai_memory`（AI 长期记忆：portrait 画像快照 / simulate 推演报告 / plan 学习规划）。

> 大整数主键（雪花 ID）在后端以**字符串**下发，前端统一按 `string` 处理，避免 JS 精度丢失（如 `2000000000000000002`）。

---

## 五、内置资料

`aigcoj-backend/data/uploads/material/` 下由 `scripts/gen_materials.py`（ReportLab + SimHei，CJK 自动换行）生成 9 份 PDF，配合 `seed_content.sql` 的 13 条资料记录，可直接在线预览：

408 数据结构核心考点手册、计算机组成原理（Cache 与存储体系）、操作系统（进程/内存/文件）、计算机网络（TCP/IP 高频考点）、408 历年真题分类汇编、考研数学一公式速查、考研英语高频词汇精选、考研政治冲刺背诵提纲、计算机复试面试 200 问。

---

## 六、常见问题

| 现象 | 处理 |
| --- | --- |
| 登录提示「密码错误」 | 后端要求密码 ≥ 8 位；演示账号密码为 `kaoyan2026` |
| 提交后一直「判题中」 | 查看后端日志：本地沙箱需要机器上有 `javac` / `g++` / `python`；异步判题异常会写入 `judgeInfo.message` |
| 资料 PDF 404 | 确认 `file.upload-dir` 指向的目录存在，且 `WebMvcConfig` 的 `/file/**` 映射生效 |
| 前端 5173 启动报缓存清理失败 | 已配置 `cacheDir: node_modules/.vite-dev` 规避 |
| 判题开关 | `select * from switch`：`AIJudge` flag=1 时启用 AI 沙箱，flag=2 时按 `application.yml` 的 `codesandbox.type` |
