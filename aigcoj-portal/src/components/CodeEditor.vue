<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as monaco from 'monaco-editor'

const props = withDefaults(defineProps<{ modelValue: string; language?: string; readonly?: boolean }>(), {
  language: 'java',
  readonly: false,
})
const emit = defineEmits<{ (e: 'update:modelValue', value: string): void }>()

const el = ref<HTMLDivElement>()
let editor: monaco.editor.IStandaloneCodeEditor | null = null

const monacoLang = (lang: string) => {
  if (lang === 'cpp' || lang === 'c') return 'cpp'
  if (lang === 'python' || lang === 'python3') return 'python'
  return 'java'
}

/** 亚麻暖色代码主题，避免纯白与冷色高亮 */
function defineLinenTheme() {
  monaco.editor.defineTheme('linen', {
    base: 'vs',
    inherit: true,
    rules: [
      { token: 'comment', foreground: 'a89b86', fontStyle: 'italic' },
      { token: 'keyword', foreground: '8a5a6b' },
      { token: 'string', foreground: '5a8f5a' },
      { token: 'number', foreground: 'b07d3a' },
      { token: 'type', foreground: '7a6a4f' },
      { token: 'identifier', foreground: '3d3a34' },
      { token: 'delimiter', foreground: '8b7355' },
      { token: 'operator', foreground: '8b7355' },
    ],
    colors: {
      'editor.background': '#fffdf8',
      'editor.foreground': '#3d3a34',
      'editorLineNumber.foreground': '#c3b7a1',
      'editorLineNumber.activeForeground': '#5a8f5a',
      'editor.lineHighlightBackground': '#f6f1e5',
      'editor.selectionBackground': '#e6dfcb',
      'editorCursor.foreground': '#5a8f5a',
      'editorIndentGuide.background': '#efe7d9',
      'editorIndentGuide.activeBackground': '#d8ccb6',
      'editorWidget.background': '#fffdf8',
      'editorSuggestWidget.background': '#fffdf8',
      'editorSuggestWidget.selectedBackground': '#f0ead9',
    },
  })
}

onMounted(() => {
  if (!el.value) return
  defineLinenTheme()
  editor = monaco.editor.create(el.value, {
    value: props.modelValue,
    language: monacoLang(props.language),
    theme: 'linen',
    fontSize: 14,
    lineHeight: 23,
    fontFamily: '"JetBrains Mono", "Cascadia Code", Consolas, monospace',
    minimap: { enabled: false },
    scrollBeyondLastLine: false,
    automaticLayout: true,
    readOnly: props.readonly,
    padding: { top: 14, bottom: 14 },
    roundedSelection: true,
    renderLineHighlight: 'line',
    tabSize: 2,
  })
  editor.onDidChangeModelContent(() => emit('update:modelValue', editor!.getValue()))
})

watch(() => props.modelValue, (value) => {
  if (editor && editor.getValue() !== value) editor.setValue(value || '')
})

watch(() => props.language, (lang) => {
  if (editor) monaco.editor.setModelLanguage(editor.getModel()!, monacoLang(lang))
})

onBeforeUnmount(() => editor?.dispose())
</script>

<template>
  <div ref="el" class="editor-host" />
</template>

<style scoped>
.editor-host { width: 100%; height: 100%; background: #fffdf8; }
</style>
