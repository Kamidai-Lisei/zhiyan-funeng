import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '../layouts/AppLayout.vue'
import { useAuthStore } from '../stores/auth'

export const router = createRouter({
  history: createWebHistory(),
  scrollBehavior: () => ({ top: 0 }),
  routes: [
    {
      path: '/',
      component: AppLayout,
      children: [
        { path: '', name: 'home', component: () => import('../views/HomeView.vue'), meta: { title: '首页' } },
        { path: 'problems', name: 'problems', component: () => import('../views/ProblemsView.vue'), meta: { title: '题库' } },
        { path: 'problems/:id', name: 'problem-detail', component: () => import('../views/ProblemDetailView.vue'), meta: { title: '在线做题' } },
        { path: 'submissions', name: 'submissions', component: () => import('../views/SubmissionsView.vue'), meta: { title: '提交记录', requiresAuth: true } },
        { path: 'wrong-book', name: 'wrong-book', component: () => import('../views/WrongBookView.vue'), meta: { title: '错题本', requiresAuth: true } },

        { path: 'learn/408', name: 'learn', component: () => import('../views/LearnHomeView.vue'), meta: { title: '408 学习' } },
        { path: 'learn/quiz', name: 'quiz', component: () => import('../views/QuizView.vue'), meta: { title: '在线习题' } },
        { path: 'learn/408/:module', name: 'learn-module', component: () => import('../views/LearnModuleView.vue'), meta: { title: '408 章节' } },
        { path: 'learn/408/:module/:slug', name: 'learn-article', component: () => import('../views/LearnArticleView.vue'), meta: { title: '408 笔记' } },

        { path: 'materials', name: 'materials', component: () => import('../views/MaterialsView.vue'), meta: { title: '资料库' } },
        { path: 'forum', name: 'forum', component: () => import('../views/ForumView.vue'), meta: { title: '论坛' } },
        { path: 'forum/new', name: 'post-new', component: () => import('../views/PostEditorView.vue'), meta: { title: '发布帖子', requiresAuth: true } },
        { path: 'forum/:id', name: 'post-detail', component: () => import('../views/PostDetailView.vue'), meta: { title: '帖子详情' } },
        { path: 'forum/:id/edit', name: 'post-edit', component: () => import('../views/PostEditorView.vue'), meta: { title: '编辑帖子', requiresAuth: true } },

        { path: 'career', name: 'career', component: () => import('../views/CareerView.vue'), meta: { title: '就业与复试' } },
        { path: 'career/resume', name: 'resume', component: () => import('../views/ResumeBuilderView.vue'), meta: { title: '简历工坊', requiresAuth: true } },
        { path: 'me', name: 'me', component: () => import('../views/ProfileView.vue'), meta: { title: '个人中心', requiresAuth: true } },
        { path: 'portrait', name: 'portrait', component: () => import('../views/PortraitView.vue'), meta: { title: '我的画像', requiresAuth: true } },
        { path: 'simulator', name: 'simulator', component: () => import('../views/SimulatorView.vue'), meta: { title: '决策推演', requiresAuth: true } },
        { path: 'plan', name: 'plan', component: () => import('../views/PlanView.vue'), meta: { title: '学习规划', requiresAuth: true } },
        { path: 'admin', name: 'admin', component: () => import('../views/AdminView.vue'), meta: { title: '管理后台', requiresAuth: true, requiresAdmin: true } },
        { path: '403', name: 'forbidden', component: () => import('../views/ForbiddenView.vue'), meta: { title: '无访问权限' } },
      ],
    },
    { path: '/login', name: 'login', component: () => import('../views/LoginView.vue'), meta: { title: '登录', standalone: true } },
    { path: '/register', name: 'register', component: () => import('../views/RegisterView.vue'), meta: { title: '注册', standalone: true } },
    { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('../views/NotFoundView.vue'), meta: { title: '页面不存在', standalone: true } },
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  await auth.hydrate()

  if (to.meta?.requiresAuth && !auth.isAuthed) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  // 仅管理员可进入：普通用户访问后台时给出明确的无权限提示页
  if (to.meta?.requiresAdmin && !auth.isAdmin) {
    return { path: '/403', query: { from: 'admin' } }
  }
  if ((to.path === '/login' || to.path === '/register') && auth.isAuthed) {
    return { path: '/' }
  }
  return true
})

router.afterEach((to) => {
  const title = (to.meta?.title as string) || ''
  document.title = title ? `${title} · 智研赋能` : '智研赋能 · 个性化考研智能服务平台'
})
