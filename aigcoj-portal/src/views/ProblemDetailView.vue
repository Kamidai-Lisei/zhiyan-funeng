<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import dayjs from 'dayjs'
import { questionApi } from '../api/question'
import { errorMessage } from '../api/http'
import { useAuthStore } from '../stores/auth'
import { acceptance, fromNow, toTags } from '../utils/format'
import MarkdownView from '../components/MarkdownView.vue'
import CodeEditor from '../components/CodeEditor.vue'
import Icon from '../components/Icon.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const qid = String(route.params.id)
const loading = ref(true)
const question = ref<any>(null)
const tab = ref('statement')
const language = ref('java')
const code = ref('')
const submitting = ref(false)
const result = ref<any>(null)
const comments = ref<any[]>([])
const commentText = ref('')
const commentLoading = ref(false)
const submissions = ref<any[]>([])
const favours = ref<(number | string)[]>([])
const analyzeOpen = ref(false)
const analysis = ref('')
const analyzing = ref(false)

const templates: Record<string, string> = {
  java: `import java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        // 在此编写你的代码\n\n    }\n}\n`,
  python: `import sys\n\ndef main():\n    data = sys.stdin.read().split()\n    # 在此编写你的代码\n\nif __name__ == "__main__":\n    main()\n`,
  cpp: `#include <bits/stdc++.h>\nusing namespace std;\n\nint main() {\n    // 在此编写你的代码\n\n    return 0;\n}\n`,
}

const tags = computed(() => toTags(question.value?.tags))
const level = computed(() => tags.value.find((t: string) => ['简单', '中等', '困难'].includes(t)) || '中等')
const levelColor = computed(() => (level.value === '简单' ? 'green' : level.value === '困难' ? 'rose' : 'amber'))
const hasFavour = computed(() => favours.value.map(String).includes(qid))

const resultOk = computed(() => result.value?.judgeInfo?.message === 'Accepted')

const MESSAGE_CN: Record<string, string> = {
  'Accepted': '答案正确',
  'Wrong Answer': '答案错误',
  'Compile Error': '编译错误',
  'Runtime Error': '运行错误',
  'Time Limit Exceeded': '运行超时',
  'Memory Limit Exceeded': '内存超限',
}

const statusText = computed(() => {
  if (!result.value) return ''
  if (result.value.status === 3) return '判题失败'
  if (result.value.status !== 2) return '判题中…'
  const msg = result.value.judgeInfo?.message || '判题完成'
  return MESSAGE_CN[msg] || msg
})
const resultScore = computed(() => result.value?.judgeInfo?.score)
const resultPassed = computed(() => result.value?.judgeInfo)

/** 判题结果弹窗 */
const verdictOpen = ref(false)
const judgeMsg = computed(() => result.value?.judgeInfo?.message || '')
const verdictKind = computed<'ac' | 'partial' | 'wa' | 'ce' | 're' | 'tle' | 'fail'>(() => {
  if (!result.value) return 'fail'
  if (result.value.status === 3) return 'fail'
  if (judgeMsg.value === 'Accepted') return 'ac'
  if (judgeMsg.value === 'Compile Error') return 'ce'
  if (judgeMsg.value === 'Runtime Error') return 're'
  if (judgeMsg.value === 'Time Limit Exceeded') return 'tle'
  const p = result.value.judgeInfo
  if (p?.score != null && p.score > 0 && p.score < 100) return 'partial'
  return 'wa'
})
const verdictTitle = computed(() => {
  if (judgeMsg.value.includes('判题超时')) return '判题超时'
  switch (verdictKind.value) {
    case 'ac': return '通过！'
    case 'partial': return '半对 · 部分通过'
    case 'wa': return '未通过'
    case 'ce': return '编译错误'
    case 're': return '运行错误'
    case 'tle': return '运行超时'
    default: return '判题失败'
  }
})
const verdictEmoji = computed(() => {
  if (judgeMsg.value.includes('判题超时')) return '⏳'
  switch (verdictKind.value) {
    case 'ac': return '🌱'
    case 'partial': return '🧺'
    case 'wa': return '🍂'
    case 'ce': return '🪵'
    case 're': return '💥'
    case 'tle': return '⏳'
    default: return '☁️'
  }
})
const scoreClass = computed(() => {
  const s = resultScore.value
  if (s == null) return ''
  if (s >= 100) return 'full'
  if (s >= 60) return 'part'
  return 'zero'
})

const load = async () => {
  loading.value = true
  try {
    question.value = await questionApi.getVo(qid)
    const saved = localStorage.getItem(`code-${qid}-${language.value}`)
    code.value = saved || templates[language.value]
    comments.value = await questionApi.comments(qid)
    if (auth.isAuthed) {
      favours.value = await questionApi.favourIds()
      const page = await questionApi.submissions({ current: 1, pageSize: 8, questionId: qid, userId: auth.user?.id })
      submissions.value = page.records || []
    }
  } catch (e: any) {
    Message.error(errorMessage(e))
  } finally {
    loading.value = false
  }
}

const switchLang = (lang: string) => {
  localStorage.setItem(`code-${qid}-${language.value}`, code.value)
  language.value = lang
  code.value = localStorage.getItem(`code-${qid}-${lang}`) || templates[lang]
}

const delay = (ms: number) => new Promise((r) => setTimeout(r, ms))

const submit = async () => {
  if (!auth.isAuthed) {
    Message.info('登录后才能提交代码')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  if (!code.value.trim()) return Message.warning('代码不能为空')
  submitting.value = true
  result.value = null
  localStorage.setItem(`code-${qid}-${language.value}`, code.value)
  try {
    const id = await questionApi.submit({ questionId: qid, language: language.value, code: code.value })
    Message.success('已提交，正在评测')
    for (let i = 0; i < 45; i++) {
      await delay(i < 8 ? 600 : 1000)
      const page = await questionApi.submissions({
        current: 1, pageSize: 20, questionId: qid, userId: auth.user?.id,
        sortField: 'createTime', sortOrder: 'descend',
      })
      const found = (page.records || []).find((r: any) => String(r.id) === String(id))
      if (found && (found.status === 2 || found.status === 3)) {
        result.value = found
        verdictOpen.value = true
        break
      }
    }
    if (!result.value) result.value = { status: 3, judgeInfo: { message: '判题超时，请稍后在提交记录中查看' } }
    if (!result.value.id || result.value.status === 3) verdictOpen.value = true
    const page = await questionApi.submissions({ current: 1, pageSize: 8, questionId: qid, userId: auth.user?.id })
    submissions.value = page.records || []
  } catch (e: any) {
    Message.error(errorMessage(e))
  } finally {
    submitting.value = false
  }
}

const analyze = async () => {
  if (!result.value?.id) return
  analyzeOpen.value = true
  if (analysis.value) return
  analyzing.value = true
  try {
    analysis.value = await questionApi.analyze(result.value.id)
  } catch (e: any) {
    analysis.value = errorMessage(e)
  } finally {
    analyzing.value = false
  }
}

const toggleFavour = async () => {
  if (!auth.isAuthed) return Message.info('登录后可收藏题目')
  try {
    const res = await questionApi.toggleFavour(qid)
    favours.value = res.favour ? [...favours.value, qid] : favours.value.filter((x) => String(x) !== String(qid))
    Message.success(res.favour ? '已收藏，可在个人中心查看' : '已取消收藏')
  } catch (e: any) {
    Message.error(errorMessage(e))
  }
}

const addComment = async () => {
  if (!auth.isAuthed) return Message.info('登录后参与讨论')
  if (!commentText.value.trim()) return
  commentLoading.value = true
  try {
    await questionApi.addComment({ questionId: qid, content: commentText.value.trim() })
    commentText.value = ''
    comments.value = await questionApi.comments(qid)
    Message.success('评论成功')
  } catch (e: any) {
    Message.error(errorMessage(e))
  } finally {
    commentLoading.value = false
  }
}

const resetCode = () => {
  code.value = templates[language.value]
  localStorage.removeItem(`code-${qid}-${language.value}`)
}

onMounted(load)
</script>

<template>
  <div class="page" style="padding-top: 20px">
    <div class="container">
      <div class="row between wrap gap12" style="margin-bottom: 14px">
        <div class="row gap12">
          <button class="btn-ghost" @click="router.back()"><Icon name="arrow-left" :size="15" /> 返回</button>
          <a-button size="small" @click="router.push('/submissions')">提交记录</a-button>
        </div>
        <div class="row gap8">
          <a-button size="small" @click="toggleFavour">
            <Icon name="star" :size="14" :style="{ color: hasFavour ? 'var(--wheat-deep)' : undefined }" />
            {{ hasFavour ? '已收藏' : '收藏' }}
          </a-button>
          <a-button size="small" @click="router.push('/forum/new')"><Icon name="chat" :size="14" /> 发帖讨论</a-button>
        </div>
      </div>

      <div v-if="loading" class="card card-pad">
        <div class="skeleton" style="height: 28px; width: 40%; margin-bottom: 16px" />
        <div v-for="i in 8" :key="i" class="skeleton" style="height: 14px; margin-bottom: 10px" />
      </div>

      <div v-else-if="question" class="problem-layout">
        <div class="problem-pane">
          <div class="problem-pane-head">
            <div style="min-width: 0">
              <div class="row gap8">
                <span class="h3" style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis">{{ question.title }}</span>
                <span class="tag" :class="levelColor">{{ level }}</span>
              </div>
              <div class="row wrap gap8" style="margin-top: 6px">
                <span v-for="t in tags" :key="t" class="tag">{{ t }}</span>
                <span class="small muted">通过率 {{ acceptance(question.acceptedNum, question.submitNum) }} · {{ question.submitNum || 0 }} 次提交</span>
              </div>
            </div>
          </div>

          <div style="padding: 0 20px">
            <a-tabs v-model:active-key="tab" size="small" style="margin-top: 6px">
              <a-tab-pane key="statement" title="题目">
                <div class="problem-scroll" style="padding: 8px 0 24px">
                  <MarkdownView :content="question.content" />
                  <div class="card card-pad" style="margin-top: 18px; background: #fdfaf4">
                    <div class="row gap16 wrap small muted">
                      <span>时间限制：{{ question.judgeConfig?.timeLimit || 2 }} 秒</span>
                      <span>内存限制：{{ Math.round((question.judgeConfig?.memoryLimit || 262144) / 1024) }} MB</span>
                      <span>支持语言：Java / Python / C++</span>
                    </div>
                  </div>
                </div>
              </a-tab-pane>
              <a-tab-pane key="comments" :title="`讨论 ${comments.length}`">
                <div style="padding: 10px 0 24px; max-height: calc(100vh - 330px); overflow-y: auto">
                  <div style="display: flex; gap: 10px; margin-bottom: 16px">
                    <div class="avatar">{{ auth.initial }}</div>
                    <div style="flex: 1">
                      <a-textarea v-model="commentText" placeholder="分享思路、提问或补充边界条件…" :auto-size="{ minRows: 2 }" />
                      <div class="row between" style="margin-top: 8px">
                        <span class="small muted">友善交流，共同进步</span>
                        <a-button type="primary" size="small" :loading="commentLoading" @click="addComment">发表评论</a-button>
                      </div>
                    </div>
                  </div>
                  <div v-for="c in comments" :key="c.id" style="padding: 14px 0; border-top: 1px solid #f4eee2">
                    <div class="row gap8">
                      <div class="avatar sm">{{ (c.userName || '研').slice(0, 1) }}</div>
                      <span class="h4">{{ c.userName || '研友' }}</span>
                      <span class="small muted">{{ fromNow(c.gmtCreate) }}</span>
                    </div>
                    <div style="margin: 8px 0 0 32px; color: var(--ink-2); white-space: pre-wrap">{{ c.content }}</div>
                    <div v-for="r in c.reply || []" :key="r.id" style="margin: 10px 0 0 32px; padding: 10px 12px; background: #fdfaf4; border-radius: 9px">
                      <span class="h4" style="font-size: 13px">{{ r.userName }}</span>
                      <span class="small muted" style="margin-left: 8px">{{ r.content }}</span>
                    </div>
                  </div>
                  <div v-if="!comments.length" class="empty small">还没有讨论，来做第一个分享思路的人。</div>
                </div>
              </a-tab-pane>
              <a-tab-pane key="subs" :title="`我的提交 ${submissions.length}`">
                <div style="padding: 12px 0 24px; max-height: calc(100vh - 330px); overflow-y: auto">
                  <div v-for="s in submissions" :key="s.id" class="row between" style="padding: 11px 0; border-bottom: 1px solid #f4eee2">
                    <div>
                      <span class="status-dot" :class="s.judgeInfo?.message === 'Accepted' ? 'ok' : 'bad'" />
                      <span class="h4">{{ s.judgeInfo?.message || '判题中' }}</span>
                      <span class="small muted" style="margin-left: 8px">{{ s.language }}</span>
                    </div>
                    <div class="small muted">{{ dayjs(s.createTime).format('MM-DD HH:mm') }}</div>
                  </div>
                  <div v-if="!submissions.length" class="empty small">还没有提交记录，写一版试试。</div>
                </div>
              </a-tab-pane>
            </a-tabs>
          </div>
        </div>

        <div class="problem-pane">
          <div class="editor-toolbar">
            <a-select v-model="language" size="small" style="width: 120px" @change="switchLang">
              <a-option value="java">Java</a-option>
              <a-option value="python">Python</a-option>
              <a-option value="cpp">C++</a-option>
            </a-select>
            <a-button size="small" @click="resetCode">重置模板</a-button>
            <div style="flex: 1" />
            <a-button type="primary" size="small" :loading="submitting" @click="submit">
              <Icon name="send" :size="14" /> 提交评测
            </a-button>
          </div>
          <div class="editor-wrap"><CodeEditor v-model="code" :language="language" /></div>

          <div v-if="result" class="result-panel">
            <div class="row between">
              <div>
                <div class="row gap8">
                  <span :class="resultOk ? 'result-accepted' : 'result-failed'" class="h3">
                    {{ resultOk ? '✓ ' : '× ' }}{{ statusText }}
                  </span>
                  <span v-if="resultScore != null" class="score-badge" :class="scoreClass">{{ resultScore }} 分</span>
                  <span class="tag" :class="resultOk ? 'green' : 'rose'">{{ result.language }}</span>
                </div>
                <div class="small muted" style="margin-top: 4px">
                  <template v-if="resultPassed?.total">通过 {{ resultPassed.passed || 0 }}/{{ resultPassed.total }} 个用例 · </template>
                  耗时 {{ result.judgeInfo?.time || 0 }} ms · 内存 {{ Math.round((result.judgeInfo?.memory || 0) / 1024) }} MB
                </div>
                <pre v-if="result.judgeInfo?.detail" class="fail-detail">{{ result.judgeInfo.detail }}</pre>
              </div>
              <a-button size="small" type="outline" :loading="analyzing" @click="analyze">
                <Icon name="sparkles" :size="14" /> AI 分析代码
              </a-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <a-modal v-model:visible="verdictOpen" :footer="false" :width="440" unmount-on-close>
      <div class="verdict-modal">
        <div class="verdict-emoji">{{ verdictEmoji }}</div>
        <div class="verdict-title" :class="'v-' + verdictKind">{{ verdictTitle }}</div>
        <div v-if="resultScore != null && verdictKind !== 'fail'" class="verdict-score" :class="scoreClass">
          {{ resultScore }}<span class="unit">分</span>
        </div>
        <div v-if="resultPassed?.total" class="small muted">
          通过 {{ resultPassed.passed || 0 }} / {{ resultPassed.total }} 个测试用例
          <template v-if="result.judgeInfo?.time"> · 耗时 {{ result.judgeInfo.time }} ms</template>
        </div>
        <div v-else class="small muted">{{ statusText }}</div>
        <div v-if="verdictKind === 'ce'" class="small" style="margin-top: 8px; color: var(--timber)">
          提示：C++ 代码请选 C++ 提交；Java 的主类必须是 <code>Main</code>
        </div>
        <pre v-if="result.judgeInfo?.detail" class="fail-detail">{{ result.judgeInfo.detail }}</pre>
        <div class="row gap8" style="margin-top: 14px; justify-content: center">
          <a-button v-if="verdictKind !== 'ac'" type="primary" @click="verdictOpen = false">再改改</a-button>
          <a-button v-else type="primary" @click="verdictOpen = false">继续下一题</a-button>
          <a-button @click="verdictOpen = false; analyze()">AI 分析代码</a-button>
        </div>
      </div>
    </a-modal>

    <a-modal v-model:visible="analyzeOpen" title="AI 代码分析" :footer="false" :width="720">
      <div v-if="analyzing" style="padding: 30px; text-align: center" class="muted">正在阅读你的代码，请稍候…</div>
      <div v-else style="max-height: 60vh; overflow-y: auto">
        <MarkdownView :content="analysis" />
      </div>
    </a-modal>
  </div>
</template>
