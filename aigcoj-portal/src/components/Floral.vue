<script setup lang="ts">
/**
 * 手绘花卉装饰：叶片、小花、蘑菇、麦穗。
 * 田园核风里「不可省略」的身份标记，放在卡片角落或标题旁。
 */
withDefaults(
  defineProps<{
    variant?: 'sprig' | 'flower' | 'mushroom' | 'wheat' | 'leaf'
    size?: number
    color?: string
    stroke?: number
  }>(),
  { variant: 'sprig', size: 56, color: 'currentColor', stroke: 1.6 },
)
</script>

<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 64 64"
    fill="none"
    :stroke="color"
    :stroke-width="stroke"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
    focusable="false"
  >
    <!-- 枝条：一条斜生的茎，两侧交错叶片 -->
    <template v-if="variant === 'sprig'">
      <path d="M12 54C18 46 24 38 30 30C36 22 43 15 52 10" />
      <path d="M21 45C17 40 15.5 36 15.5 32C19.5 33 23 36 25 40C24.2 42.2 22.8 43.8 21 45Z" fill="currentColor" fill-opacity="0.16" />
      <path d="M30 34C28 29 28 25 29 21C32.5 23 35 26.5 36 31C34.6 32.4 32.5 33.5 30 34Z" fill="currentColor" fill-opacity="0.16" />
      <path d="M25 40C29 39 33 39.5 36 41.5C33.5 44.5 30 46.5 26 47C25.2 44.8 24.9 42.5 25 40Z" fill="currentColor" fill-opacity="0.12" />
      <path d="M36 26C40 24 44 23.5 48 24.5C46 27.8 43 30 39 30.8C37.8 29.4 36.8 27.8 36 26Z" fill="currentColor" fill-opacity="0.12" />
      <path d="M40 20C42 17 45 15 48 14C48.5 17 47.5 20 45 22C43.2 21.6 41.5 21 40 20Z" fill="currentColor" fill-opacity="0.16" />
      <circle cx="52" cy="10" r="2.2" fill="currentColor" fill-opacity="0.55" stroke="none" />
    </template>

    <!-- 小花：五瓣 + 麦黄花蕊 -->
    <template v-else-if="variant === 'flower'">
      <path d="M32 40V57" />
      <path d="M32 48C28 46 25 43 23 39C27 39 30 41 32 44" fill="currentColor" fill-opacity="0.14" />
      <g fill="currentColor" fill-opacity="0.13">
        <ellipse cx="32" cy="15" rx="5.4" ry="7.2" />
        <ellipse cx="32" cy="15" rx="5.4" ry="7.2" transform="rotate(72 32 24)" />
        <ellipse cx="32" cy="15" rx="5.4" ry="7.2" transform="rotate(144 32 24)" />
        <ellipse cx="32" cy="15" rx="5.4" ry="7.2" transform="rotate(216 32 24)" />
        <ellipse cx="32" cy="15" rx="5.4" ry="7.2" transform="rotate(288 32 24)" />
      </g>
      <g fill="currentColor" fill-opacity="0.4">
        <ellipse cx="32" cy="15" rx="5.4" ry="7.2" />
        <ellipse cx="32" cy="15" rx="5.4" ry="7.2" transform="rotate(72 32 24)" />
        <ellipse cx="32" cy="15" rx="5.4" ry="7.2" transform="rotate(144 32 24)" />
        <ellipse cx="32" cy="15" rx="5.4" ry="7.2" transform="rotate(216 32 24)" />
        <ellipse cx="32" cy="15" rx="5.4" ry="7.2" transform="rotate(288 32 24)" />
      </g>
      <circle cx="32" cy="24" r="4.4" fill="#f5d75f" stroke="none" />
      <circle cx="30.4" cy="23" r="0.9" fill="#c9a227" stroke="none" />
      <circle cx="33.4" cy="24.6" r="0.9" fill="#c9a227" stroke="none" />
    </template>

    <!-- 蘑菇：伞盖 + 斑点 + 菌柄 -->
    <template v-else-if="variant === 'mushroom'">
      <path d="M12 30C12 19 21 11 32 11C43 11 52 19 52 30C52 32 50 33 48 33H16C14 33 12 32 12 30Z" fill="currentColor" fill-opacity="0.18" />
      <path d="M27 33V47C27 50 29 52 32 52C35 52 37 50 37 47V33" fill="currentColor" fill-opacity="0.08" />
      <circle cx="24" cy="22" r="2.4" fill="currentColor" fill-opacity="0.42" stroke="none" />
      <circle cx="36" cy="19" r="1.8" fill="currentColor" fill-opacity="0.42" stroke="none" />
      <circle cx="43" cy="26" r="2.1" fill="currentColor" fill-opacity="0.42" stroke="none" />
      <path d="M14 40C18 38 22 37 26 37" stroke-opacity="0.5" />
    </template>

    <!-- 麦穗 -->
    <template v-else-if="variant === 'wheat'">
      <path d="M32 56V16" />
      <g fill="currentColor" fill-opacity="0.16">
        <path d="M32 20C28 19 25 16 24 12C28 12 31 14 32 17" />
        <path d="M32 20C36 19 39 16 40 12C36 12 33 14 32 17" />
        <path d="M32 29C28 28 25 25 24 21C28 21 31 23 32 26" />
        <path d="M32 29C36 28 39 25 40 21C36 21 33 23 32 26" />
        <path d="M32 38C28 37 25 34 24 30C28 30 31 32 32 35" />
        <path d="M32 38C36 37 39 34 40 30C36 30 33 32 32 35" />
      </g>
      <g fill="currentColor" fill-opacity="0.38">
        <path d="M32 20C28 19 25 16 24 12C28 12 31 14 32 17" />
        <path d="M32 20C36 19 39 16 40 12C36 12 33 14 32 17" />
        <path d="M32 29C28 28 25 25 24 21C28 21 31 23 32 26" />
        <path d="M32 29C36 28 39 25 40 21C36 21 33 23 32 26" />
      </g>
    </template>

    <!-- 单片叶子 -->
    <template v-else>
      <path d="M14 50C14 32 26 18 46 14C50 34 38 48 20 50" fill="currentColor" fill-opacity="0.14" />
      <path d="M14 50C22 42 30 34 40 24" stroke-opacity="0.7" />
      <path d="M24 40C24 36 25 32 27 29" stroke-opacity="0.45" />
      <path d="M32 32C33 28 35 25 38 22" stroke-opacity="0.45" />
    </template>
  </svg>
</template>
