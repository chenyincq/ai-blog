<script setup>
import { ref, onMounted } from 'vue'
import { getFriendLinks } from '../api'

const links = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    links.value = await getFriendLinks()
  } catch (_) {
    links.value = []
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="page page-friend-links">
    <h1 class="page-title">友情链接</h1>
    <p v-if="loading" class="muted">加载中...</p>
    <ul v-else-if="links.length === 0" class="link-list">
      <li class="muted">暂无友情链接</li>
    </ul>
    <ul v-else class="link-list">
      <li v-for="link in links" :key="link.id" class="link-item">
        <a :href="link.url" target="_blank" rel="noopener noreferrer" class="link-name">{{ link.name }}</a>
        <span v-if="link.description" class="link-desc"> — {{ link.description }}</span>
      </li>
    </ul>
  </div>
</template>
