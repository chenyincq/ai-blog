<script setup>
import { ref, onMounted, onUnmounted, inject, computed } from 'vue'
import { getPosts, getHomeBanners } from '../api'
import { useRouter } from 'vue-router'

const router = useRouter()
const theme = inject('theme', { isDark: ref(true), toggleTheme: () => {} })
const weatherApi = inject('weather', {
  weather: ref(null),
  weatherLoading: ref(false),
  loadWeather: () => {}
})
const { isDark, toggleTheme } = theme
const { weather, weatherLoading, loadWeather } = weatherApi

const posts = ref([])
const loading = ref(true)
const banners = ref([])
const currentBannerIndex = ref(0)
let bannerTimer = null

const DEFAULT_BANNER = {
  id: 'default',
  imageUrl: 'https://images.unsplash.com/photo-1499750310107-5fef28a66643?w=1920&q=80',
  title: '',
  subtitle: ''
}
const displayBanners = computed(() => {
  const list = banners.value || []
  return list.length ? [...list, DEFAULT_BANNER] : [DEFAULT_BANNER]
})

onMounted(async () => {
  try {
    const [data, list] = await Promise.all([
      getPosts({ page: 0, size: 6 }),
      getHomeBanners()
    ])
    posts.value = data.content || []
    banners.value = list || []
    if (displayBanners.value.length > 1) {
      bannerTimer = setInterval(() => {
        currentBannerIndex.value = (currentBannerIndex.value + 1) % displayBanners.value.length
      }, 5000)
    }
  } finally {
    loading.value = false
  }
})

onUnmounted(() => {
  if (bannerTimer) clearInterval(bannerTimer)
})

function setBannerIndex(i) {
  currentBannerIndex.value = i
  if (bannerTimer) {
    clearInterval(bannerTimer)
    bannerTimer = setInterval(() => {
      currentBannerIndex.value = (currentBannerIndex.value + 1) % displayBanners.value.length
    }, 5000)
  }
}

function summaryText(post) {
  return post.summary || (post.content ? post.content.slice(0, 200).trim() + (post.content.length > 200 ? '…' : '') : '')
}
function summaryShort(post) {
  const s = post.summary || post.content || ''
  return s.length > 80 ? s.slice(0, 80).trim() + '…' : s
}
</script>

<template>
  <div class="page">
    <section v-if="displayBanners.length" class="home-banner">
      <div
        v-for="(b, i) in displayBanners"
        :key="b.id"
        class="banner-slide"
        :class="{ active: i === currentBannerIndex }"
        :style="{ backgroundImage: `url(${b.imageUrl})` }"
      >
        <div class="banner-overlay">
          <div class="banner-nav">
            <span class="banner-site-title" @click="router.push('/')">博客</span>
            <nav class="banner-links">
              <router-link to="/">首页</router-link>
              <router-link to="/posts">文章</router-link>
              <router-link to="/popular">热门</router-link>
              <router-link to="/friend-links">友情链接</router-link>
              <router-link to="/admin/login">管理</router-link>
            </nav>
            <div class="banner-actions">
              <span v-if="weatherLoading" class="weather-loading">天气…</span>
              <span v-else-if="weather" class="weather-info" title="点击刷新" @click="loadWeather">
                <span v-if="weather.location" class="weather-location">{{ weather.location }}</span>
                <span class="weather-temp">{{ weather.temp }}°C</span>
                <span class="weather-desc">{{ weather.desc }}</span>
              </span>
              <button v-else type="button" class="weather-refresh" @click="loadWeather">天气</button>
              <button type="button" class="theme-toggle" :title="isDark ? '日间' : '夜间'" @click="toggleTheme">
                <span v-if="isDark" class="theme-icon">☀️</span>
                <span v-else class="theme-icon">🌙</span>
              </button>
            </div>
          </div>
          <div class="banner-text">
            <h2 v-if="b.title" class="banner-title">{{ b.title }}</h2>
            <p v-if="b.subtitle" class="banner-subtitle">{{ b.subtitle }}</p>
          </div>
        </div>
      </div>
      <div class="banner-dots">
        <button
          v-for="(_, i) in displayBanners"
          :key="i"
          type="button"
          class="banner-dot"
          :class="{ active: i === currentBannerIndex }"
          :aria-label="`切换到第 ${i + 1} 张`"
          @click="setBannerIndex(i)"
        />
      </div>
    </section>

    <template v-else>
      <header class="home-masthead-slim">
        <div class="home-masthead-inner">
          <span class="banner-site-title" @click="router.push('/')">博客</span>
            <nav class="banner-links">
              <router-link to="/">首页</router-link>
              <router-link to="/posts">文章</router-link>
              <router-link to="/popular">热门</router-link>
              <router-link to="/friend-links">友情链接</router-link>
              <router-link to="/admin/login">管理</router-link>
            </nav>
            <div class="banner-actions">
              <span v-if="weatherLoading" class="weather-loading">天气…</span>
            <span v-else-if="weather" class="weather-info" @click="loadWeather">
              <span v-if="weather.location" class="weather-location">{{ weather.location }}</span>
              <span class="weather-temp">{{ weather.temp }}°C</span> {{ weather.desc }}
            </span>
            <button v-else type="button" class="weather-refresh" @click="loadWeather">天气</button>
            <button type="button" class="theme-toggle" @click="toggleTheme">
              <span v-if="isDark" class="theme-icon">☀️</span>
              <span v-else class="theme-icon">🌙</span>
            </button>
          </div>
        </div>
      </header>
      <p style="font-family: var(--serif); font-size: 1.1rem; color: var(--text-muted); margin-bottom: 24px;">
        欢迎来到博客，这里记录文字与思考。
      </p>
    </template>

    <p v-if="loading">加载中...</p>

    <div v-else-if="posts.length" class="home-post-grid">
      <article
        v-for="post in posts"
        :key="post.id"
        class="post-card post-card-home"
        @click="router.push(`/post/${post.id}`)"
      >
        <img v-if="post.coverUrl" :src="post.coverUrl" alt="" class="post-card-cover" />
        <div v-else class="post-card-cover" style="background: var(--border);" />
        <div class="post-card-body">
          <h3 :title="post.title">{{ post.title }}</h3>
          <p class="meta">{{ post.author }} · {{ post.createdAt?.slice(0, 10) }}</p>
          <p class="summary" :title="summaryText(post)">{{ summaryShort(post) }}</p>
          <div class="post-card-stats post-card-stats-home">
            <span class="stat-views" title="阅读量">
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
              {{ post.viewCount ?? 0 }}
            </span>
            <span v-if="(post.viewCount ?? 0) > 5" class="stat-flame" title="热度">🔥</span>
          </div>
        </div>
      </article>
    </div>

    <p v-else style="color: var(--text-muted);">暂无文章。</p>

    <p style="margin-top: 24px;">
      <router-link to="/posts">查看全部文章 →</router-link>
    </p>
  </div>
</template>
