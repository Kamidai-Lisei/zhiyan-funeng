<script setup lang="ts">
withDefaults(defineProps<{ name: string; size?: number }>(), { size: 18 })
</script>

<template>
  <svg :width="size" :height="size" viewBox="0 0 24 24" fill="none" stroke="currentColor"
       stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
    <template v-if="name === 'home'"><path d="M3 10.5 12 3l9 7.5" /><path d="M5 9.5V21h14V9.5" /><path d="M9 21v-7h6v7" /></template>
    <template v-else-if="name === 'book'"><path d="M4 5.5A2.5 2.5 0 0 1 6.5 3H20v16H6.5A2.5 2.5 0 0 0 4 21.5z" /><path d="M4 5.5v16" /></template>
    <template v-else-if="name === 'code'"><path d="m8 8-4 4 4 4" /><path d="m16 8 4 4-4 4" /><path d="m13 5-2 14" /></template>
    <template v-else-if="name === 'grid'"><rect x="3" y="3" width="7" height="7" rx="1.5" /><rect x="14" y="3" width="7" height="7" rx="1.5" /><rect x="3" y="14" width="7" height="7" rx="1.5" /><rect x="14" y="14" width="7" height="7" rx="1.5" /></template>
    <template v-else-if="name === 'chat'"><path d="M21 12a8 8 0 0 1-8 8H7l-4 2 1.5-4.5A8 8 0 1 1 21 12z" /><path d="M8 11h8M8 14h5" /></template>
    <template v-else-if="name === 'briefcase'"><rect x="3" y="7" width="18" height="13" rx="2" /><path d="M8 7V5a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" /><path d="M3 12h18" /></template>
    <template v-else-if="name === 'user'"><circle cx="12" cy="8" r="4" /><path d="M4.5 21a7.5 7.5 0 0 1 15 0" /></template>
    <template v-else-if="name === 'search'"><circle cx="11" cy="11" r="7" /><path d="m20 20-3.5-3.5" /></template>
    <template v-else-if="name === 'arrow-right'"><path d="M5 12h14" /><path d="m13 6 6 6-6 6" /></template>
    <template v-else-if="name === 'arrow-left'"><path d="M19 12H5" /><path d="m11 18-6-6 6-6" /></template>
    <template v-else-if="name === 'fire'"><path d="M12 3c3 4 5 6 5 9a5 5 0 0 1-10 0c0-1.5.5-2.5 1.5-4 .5 2 1.5 2.5 2.5 2.5C11 8 11 5.5 12 3z" /></template>
    <template v-else-if="name === 'clock'"><circle cx="12" cy="12" r="9" /><path d="M12 7v5l3 2" /></template>
    <template v-else-if="name === 'check'"><path d="m5 12 5 5L20 7" /></template>
    <template v-else-if="name === 'check-circle'"><circle cx="12" cy="12" r="9" /><path d="m8 12 3 3 5-6" /></template>
    <template v-else-if="name === 'plus'"><path d="M12 5v14M5 12h14" /></template>
    <template v-else-if="name === 'download'"><path d="M12 4v11" /><path d="m7 11 5 5 5-5" /><path d="M5 20h14" /></template>
    <template v-else-if="name === 'heart'"><path d="M20.8 5.6a5 5 0 0 0-7.1 0L12 7.3l-1.7-1.7a5 5 0 1 0-7.1 7.1L12 21l8.8-8.3a5 5 0 0 0 0-7.1z" /></template>
    <template v-else-if="name === 'star'"><path d="m12 3 2.8 5.6 6.2.9-4.5 4.4 1.1 6.2L12 17.2 6.4 20l1.1-6.2L3 9.5l6.2-.9z" /></template>
    <template v-else-if="name === 'eye'"><path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6S2 12 2 12z" /><circle cx="12" cy="12" r="2.5" /></template>
    <template v-else-if="name === 'file'"><path d="M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8z" /><path d="M14 3v5h5" /></template>
    <template v-else-if="name === 'trophy'"><path d="M8 4h8v6a4 4 0 0 1-8 0z" /><path d="M8 6H4v2a4 4 0 0 0 4 4M16 6h4v2a4 4 0 0 1-4 4" /><path d="M12 14v4M8 21h8" /></template>
    <template v-else-if="name === 'calendar'"><rect x="3" y="5" width="18" height="16" rx="2" /><path d="M8 3v4M16 3v4M3 10h18" /></template>
    <template v-else-if="name === 'target'"><circle cx="12" cy="12" r="9" /><circle cx="12" cy="12" r="5" /><circle cx="12" cy="12" r="1" /></template>
    <template v-else-if="name === 'sparkles'"><path d="m12 3 1.6 5.4L19 10l-5.4 1.6L12 17l-1.6-5.4L5 10l5.4-1.6z" /><path d="M19 16l.8 2.2L22 19l-2.2.8L19 22l-.8-2.2L16 19l2.2-.8z" /></template>
    <template v-else-if="name === 'close'"><path d="M6 6l12 12M18 6 6 18" /></template>
    <template v-else-if="name === 'edit'"><path d="M4 20h4L19 9a2.8 2.8 0 0 0-4-4L4 16z" /><path d="m14 6 4 4" /></template>
    <template v-else-if="name === 'trash'"><path d="M4 7h16M9 7V4h6v3M6 7l1 13h10l1-13" /></template>
    <template v-else-if="name === 'menu'"><path d="M4 7h16M4 12h16M4 17h16" /></template>
    <template v-else-if="name === 'logout'"><path d="M10 4H5v16h5" /><path d="M14 8l4 4-4 4M9 12h9" /></template>
    <template v-else-if="name === 'send'"><path d="m4 12 16-8-6 16-2.5-6.5z" /></template>
    <template v-else-if="name === 'filter'"><path d="M4 6h16M7 12h10M10 18h4" /></template>
    <template v-else-if="name === 'chart'"><path d="M4 20V10M10 20V4M16 20v-7M22 20H2" /></template>
    <template v-else-if="name === 'shield'"><path d="M12 3 5 6v6c0 4 3 7 7 9 4-2 7-5 7-9V6z" /><path d="m9 12 2 2 4-4" /></template>
    <template v-else-if="name === 'link'"><path d="M10 14a4 4 0 0 1 0-6l2-2a4 4 0 0 1 6 6l-1 1" /><path d="M14 10a4 4 0 0 1 0 6l-2 2a4 4 0 0 1-6-6l1-1" /></template>
    <template v-else-if="name === 'compass'"><circle cx="12" cy="12" r="9" /><path d="m15.5 8.5-2 5-5 2 2-5z" /></template>
    <template v-else-if="name === 'layers'"><path d="m12 3 9 5-9 5-9-5z" /><path d="m3 13 9 5 9-5" /></template>
    <template v-else-if="name === 'zap'"><path d="M13 2 4 14h6l-1 8 9-12h-6z" /></template>
    <template v-else><circle cx="12" cy="12" r="9" /></template>
  </svg>
</template>
