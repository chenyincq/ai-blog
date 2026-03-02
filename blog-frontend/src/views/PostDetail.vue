<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPost, getComments, postComment } from '../api'
import { marked } from 'marked'

const route = useRoute()
const router = useRouter()
const post = ref(null)
const loading = ref(true)
const error = ref('')
const comments = ref([])
const commentAuthor = ref('')
const commentContent = ref('')
const commentSubmitting = ref(false)
const commentError = ref('')

const html = computed(() => post.value?.content ? marked(post.value.content) : '')

async function loadPost() {
  try {
    post.value = await getPost(route.params.id)
  } catch (e) {
    error.value = '文章不存在'
  } finally {
    loading.value = false
  }
}

async function loadComments() {
  try {
    comments.value = await getComments(route.params.id)
  } catch (_) {
    comments.value = []
  }
}

async function submitComment() {
  const author = commentAuthor.value?.trim()
  const content = commentContent.value?.trim()
  if (!author || !content) {
    commentError.value = '请填写昵称和评论内容'
    return
  }
  commentError.value = ''
  commentSubmitting.value = true
  try {
    const newComment = await postComment(route.params.id, { author, content })
    comments.value = [...comments.value, newComment]
    commentContent.value = ''
  } catch (e) {
    commentError.value = '发布失败，请重试'
  } finally {
    commentSubmitting.value = false
  }
}

onMounted(() => {
  loadPost().then(() => {
    if (!error.value) loadComments()
  })
})
</script>

<template>
  <div class="page">
    <p><a href="javascript:;" @click="router.back()">← 返回</a></p>
    <p v-if="loading">加载中...</p>
    <p v-else-if="error" class="error">{{ error }}</p>
    <template v-else>
      <article>
        <header class="article-header">
          <h1>{{ post.title }}</h1>
          <p class="article-meta">
            {{ post.author }} · {{ post.createdAt?.slice(0, 10) }}
            <span v-if="post.category"> · {{ post.category }}</span>
          </p>
          <div class="article-stats">
            <span class="stat-item">阅读 {{ post.viewCount ?? 0 }}</span>
          </div>
          <div v-if="post.tags" class="post-card-tags" style="margin-top: 8px;">
            <span v-for="t in (post.tags || '').split(',').filter(Boolean)" :key="t" class="tag-pill">{{ t.trim() }}</span>
          </div>
        </header>
        <img v-if="post.coverUrl" :src="post.coverUrl" alt="" class="article-cover" />
        <div class="article-content" v-html="html"></div>
      </article>

      <section class="comments-section">
        <h3 class="comments-title">评论 ({{ comments.length }})</h3>
        <div class="comment-form">
          <input v-model="commentAuthor" type="text" placeholder="昵称" maxlength="50" />
          <textarea v-model="commentContent" placeholder="写下你的评论…" rows="3"></textarea>
          <p v-if="commentError" class="error">{{ commentError }}</p>
          <button type="button" class="btn btn-comment" :disabled="commentSubmitting" @click="submitComment">
            {{ commentSubmitting ? '提交中…' : '发布评论' }}
          </button>
        </div>
        <ul class="comment-list">
          <li v-for="c in comments" :key="c.id" class="comment-item">
            <div class="comment-meta">{{ c.author }} · {{ c.createdAt?.slice(0, 16)?.replace('T', ' ') }}</div>
            <div class="comment-content">{{ c.content }}</div>
          </li>
        </ul>
      </section>
    </template>
  </div>
</template>
