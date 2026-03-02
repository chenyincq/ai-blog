<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPosts, getTags, getCategories, getFriendLinks } from '../api'

const route = useRoute()
const router = useRouter()
const posts = ref([])
const totalElements = ref(0)
const page = ref(0)
const size = ref(8)
const loading = ref(false)
const author = ref('')
const category = ref('')
const tag = ref('')
const tagsCloud = ref([])
const categories = ref([])
const friendLinks = ref([])

async function load() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (author.value) params.author = author.value
    if (category.value) params.category = category.value
    if (tag.value) params.tag = tag.value
    const data = await getPosts(params)
    posts.value = data.content || []
    totalElements.value = data.totalElements || 0
  } finally {
    loading.value = false
  }
}

async function loadMeta() {
  try {
    const [t, c, links] = await Promise.all([getTags(), getCategories(), getFriendLinks()])
    tagsCloud.value = t || []
    categories.value = c || []
    friendLinks.value = links || []
  } catch (_) {}
}

onMounted(() => { load(); loadMeta() })
watch([page, author, category, tag], () => { page.value = 0; load() })

const totalPages = () => Math.ceil(totalElements.value / size.value) || 1
function chooseTag(name) { tag.value = name; page.value = 0; load() }
function chooseCategory(name) { category.value = name; page.value = 0; load() }
function applyFilters() { page.value = 0; load() }
</script>

<template>
  <div class="page">
    <div class="list-layout">
      <div>
        <div class="filters">
          <input v-model="author" placeholder="作者" />
          <input v-model="tag" placeholder="标签" />
          <select v-model="category">
            <option value="">全部分类</option>
            <option v-for="c in categories" :key="c.name" :value="c.name">{{ c.name }} ({{ c.count }})</option>
          </select>
          <button class="btn tag-pill" @click="applyFilters">筛选</button>
        </div>

        <p v-if="loading">加载中...</p>
        <template v-else>
          <article
            v-for="post in posts"
            :key="post.id"
            class="post-card"
            @click="router.push(`/post/${post.id}`)"
          >
            <img v-if="post.coverUrl" :src="post.coverUrl" alt="" class="post-card-cover" />
            <div v-else class="post-card-cover" style="background: var(--border);" />
            <div class="post-card-body">
              <h3>{{ post.title }}</h3>
              <p class="meta">{{ post.author }} · {{ post.createdAt?.slice(0, 10) }}</p>
              <p class="summary">{{ post.summary || (post.content && post.content.slice(0, 100) + '…') }}</p>
              <div v-if="post.tags" class="post-card-tags">
                <button v-for="t in (post.tags || '').split(',').filter(Boolean)" :key="t" type="button" class="tag-pill" @click.stop="chooseTag(t.trim())">{{ t.trim() }}</button>
              </div>
              <div class="post-card-stats">
                <span class="stat-views" title="阅读量">
                  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                  {{ post.viewCount ?? 0 }}
                </span>
              </div>
            </div>
          </article>

          <div class="pagination">
            <button :disabled="page === 0" @click="page--; load()">上一页</button>
            <span>第 {{ page + 1 }} / {{ totalPages() }} 页</span>
            <button :disabled="page >= totalPages() - 1" @click="page++; load()">下一页</button>
          </div>
        </template>
      </div>

      <aside class="sidebar">
        <h3>标签云</h3>
        <div style="display: flex; flex-wrap: wrap; gap: 6px;">
          <button v-for="t in tagsCloud" :key="t.name" type="button" class="tag-pill" @click="chooseTag(t.name)">{{ t.name }} ({{ t.count }})</button>
        </div>
        <h3 style="margin-top: 20px;">分类归档</h3>
        <ul>
          <li v-for="c in categories" :key="c.name" @click="chooseCategory(c.name)">{{ c.name }} ({{ c.count }})</li>
        </ul>
        <h3 style="margin-top: 20px;">友情链接</h3>
        <ul class="friend-links">
          <li v-for="link in friendLinks" :key="link.id">
            <a :href="link.url" target="_blank" rel="noopener noreferrer">{{ link.name }}</a>
          </li>
        </ul>
      </aside>
    </div>
  </div>
</template>
