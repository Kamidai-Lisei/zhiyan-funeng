# 研途 408 前端（aigcoj-portal）

Vue 3 + Vite + TypeScript + Pinia + Arco Design Vue 构建的考研学习平台前端。

## 启动

```bash
npm install
npm run dev     # http://localhost:5173
npm run build   # 类型检查 + 生产构建
```

后端默认 `http://localhost:8121/api`，`vite.config.ts` 中已将 `/api` 代理到后端（同源，Session Cookie 正常携带）。

## 目录

```
src/
├── api/          # axios 封装与各模块接口
├── components/   # Icon / MarkdownView / CodeEditor / Heatmap / AppHeader / SearchDialog / AiAssistant ...
├── content/408/  # 408 精讲 Markdown 与目录元数据
├── layouts/      # AppLayout（头部 + 内容 + 页脚 + 搜索 + AI 助手）
├── router/       # 路由与登录/管理员守卫
├── stores/       # auth（登录态）、ui（搜索/侧栏）
├── styles/       # main.css 设计系统
├── types/        # 接口类型
├── utils/        # 时间、大小、通过率、标签格式化
└── views/        # 15 个页面
```

## 设计约定

- 主色 `#2563eb`（学术蓝），浅灰背景 `#f6f8fc`，卡片圆角 14–16px，阴影克制。
- 大整数 ID 一律按 `string` 处理，比较时使用 `String(a) === String(b)`。
- 登录状态由 `stores/auth.ts` 统一维护，`router.beforeEach` 中 `requiresAuth` / `requiresAdmin` 守卫。

演示账号：`jayjay / kaoyan2026`（管理员）、`aiaiai / kaoyan2026`。
