<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPostsPopular } from '../api'

const router = useRouter()
const posts = ref([])
const totalElements = ref(0)
const page = ref(0)
const size = ref(20)
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const data = await getPostsPopular({ page: page.value, size: size.value })
    posts.value = data.content || []
    totalElements.value = data.totalElements || 0
  } finally {
    loading.value = false
  }
}

onMounted(load)

const totalPages = () => Math.ceil(totalElements.value / size.value) || 1
</script>

<template>
  <div class="page">
    <h1 class="page-title">热门文章</h1>
    <p class="page-desc">按阅读量排序</p>

    <p v-if="loading">加载中...</p>
    <template v-else>
      <div class="popular-list">
        <article
          v-for="(post, index) in posts"
          :key="post.id"
          class="post-card popular-card"
          @click="router.push(`/post/${post.id}`)"
        >
          <span class="popular-rank">{{ index + 1 + page * size }}</span>
          <img v-if="post.coverUrl" :src="post.coverUrl" alt="" class="post-card-cover" />
          <div v-else class="post-card-cover" style="background: var(--border);" />
          <div class="post-card-body">
            <h3>{{ post.title }}</h3>
            <p class="meta">{{ post.author }} · {{ post.createdAt?.slice(0, 10) }}</p>
            <p class="summary">{{ post.summary || (post.content && post.content.slice(0, 100) + '…') }}</p>
            <div class="post-card-stats">
              <span class="stat-views" title="阅读量">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                {{ post.viewCount ?? 0 }}
              </span>
            </div>
          </div>
        </article>
      </div>

      <div class="pagination">
        <button :disabled="page === 0" @click="page--; load()">上一页</button>
        <span>第 {{ page + 1 }} / {{ totalPages() }} 页</span>
        <button :disabled="page >= totalPages() - 1" @click="page++; load()">下一页</button>
      </div>
    </template>
  </div>
</template>
