<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPost, getComments, postComment } from '../api'
import { marked } from 'marked'
import CommentNode from '../components/CommentNode.vue'

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
const commentSuccess = ref('')
const replyingTo = ref(null)
const replyContent = ref('')
const replyAuthor = ref('')

const html = computed(() => post.value?.content ? marked(post.value.content) : '')

/** 将扁平评论列表转为树形结构：顶层评论 + 嵌套回复 */
const commentTree = computed(() => {
  const list = comments.value || []
  const byParent = {}
  list.forEach(c => {
    const pid = c.parentId || 0
    if (!byParent[pid]) byParent[pid] = []
    byParent[pid].push(c)
  })
  function buildReplies(parentId) {
    return (byParent[parentId] || []).map(c => ({
      ...c,
      replies: buildReplies(c.id)
    }))
  }
  return buildReplies(0)
})

const totalCommentCount = computed(() => comments.value?.length || 0)

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

async function submitComment(parentId = null) {
  const author = (parentId ? replyAuthor : commentAuthor).value?.trim()
  const content = (parentId ? replyContent : commentContent).value?.trim()
  if (!author || !content) {
    commentError.value = '请填写昵称和评论内容'
    commentSuccess.value = ''
    return
  }
  commentError.value = ''
  commentSuccess.value = ''
  commentSubmitting.value = true
  try {
    const data = { author, content }
    if (parentId) data.parentId = parentId
    await postComment(route.params.id, data)
    commentContent.value = ''
    replyingTo.value = null
    replyContent.value = ''
    replyAuthor.value = ''
    commentError.value = ''
    commentSuccess.value = '已提交，审核通过后会显示'
    setTimeout(() => { commentSuccess.value = '' }, 3000)
  } catch (e) {
    commentError.value = '发布失败，请重试'
  } finally {
    commentSubmitting.value = false
  }
}

function startReply(comment) {
  replyingTo.value = comment.id
  replyAuthor.value = commentAuthor.value || ''
  replyContent.value = ''
}

function cancelReply() {
  replyingTo.value = null
  replyContent.value = ''
  replyAuthor.value = ''
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
        <h3 class="comments-title">评论 ({{ totalCommentCount }})</h3>
        <div class="comment-form">
          <input v-model="commentAuthor" type="text" placeholder="昵称" maxlength="50" />
          <textarea v-model="commentContent" placeholder="写下你的评论…" rows="3"></textarea>
          <p v-if="commentError && !replyingTo" class="error">{{ commentError }}</p>
          <p v-if="commentSuccess" class="success-msg">{{ commentSuccess }}</p>
          <button type="button" class="btn btn-comment" :disabled="commentSubmitting" @click="submitComment()">
            {{ commentSubmitting ? '提交中…' : '发布评论' }}
          </button>
        </div>
        <ul class="comment-list">
          <CommentNode
            v-for="node in commentTree"
            :key="node.id"
            :node="node"
            :replying-to="replyingTo"
            :comment-error="commentError"
            :comment-submitting="commentSubmitting"
            v-model:reply-author="replyAuthor"
            v-model:reply-content="replyContent"
            @reply="startReply"
            @submit-reply="submitComment"
            @cancel-reply="cancelReply"
          />
        </ul>
      </section>
    </template>
  </div>
</template>
