<script setup lang="ts">
import { computed } from 'vue'
import dayjs from 'dayjs'

const props = withDefaults(defineProps<{ dates: string[]; weeks?: number }>(), { weeks: 16 })

const cells = computed(() => {
  const set = new Set(props.dates)
  const cols: { date: string; level: number; label: string }[][] = []
  const end = dayjs().endOf('week')
  const start = end.subtract(props.weeks - 1, 'week').startOf('week')
  for (let w = 0; w < props.weeks; w++) {
    const col: { date: string; level: number; label: string }[] = []
    for (let d = 0; d < 7; d++) {
      const date = start.add(w, 'week').add(d, 'day')
      const key = date.format('YYYY-MM-DD')
      const active = set.has(key)
      const level = active ? (date.isAfter(dayjs().subtract(7, 'day')) ? 4 : 3) : 0
      col.push({ date: key, level, label: `${key}${active ? ' 已学习' : ' 未打卡'}` })
    }
    cols.push(col)
  }
  return cols
})
</script>

<template>
  <div class="heatmap">
    <div v-for="(col, i) in cells" :key="i" class="heatmap-col">
      <div v-for="cell in col" :key="cell.date" class="heat-cell" :class="cell.level ? `l${cell.level}` : ''" :title="cell.label" />
    </div>
  </div>
</template>
