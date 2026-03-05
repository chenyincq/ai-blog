<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getAdminComments, deleteComment, approveComment } from '../api'

const router = useRouter()
const comments = ref([])
const totalElements = ref(0)
const page = ref(0)
const size = ref(20)
const postIdFilter = ref('')
const loading = ref(false)
const error = ref('')

const token = localStorage.getItem('blog_token')

async function load() {
  loading.value = true
  error.value = ''
  try {
  const params = { page: page.value, size: size.value }
  if (postIdFilter.value) params.postId = postIdFilter.value
  const data = await getAdminComments(params, token)
  comments.value = data.content || []
  totalElements.value = data.totalElements ?? 0
  } catch (e) {
    error.value = '加载失败'
  } finally {
    loading.value = false
  }
}

function totalPages() {
  return Math.ceil(totalElements.value / size.value) || 1
}

async function remove(c) {
  if (!confirm('确定删除该评论？删除后不可恢复。')) return
  try {
    await deleteComment(c.id, token)
    await load()
  } catch (_) {
    alert('删除失败')
  }
}

async function approve(c) {
  try {
    await approveComment(c.id, token)
    await load()
  } catch (_) {
    alert('通过失败')
  }
}

function goToPost(postId) {
  router.push(`/post/${postId}`)
}

onMounted(load)
watch([page, postIdFilter], () => { page.value = 0; load() })
</script>

<template>
  <div class="page">
    <h2 style="margin: 0 0 20px;">评论管理</h2>
    <p style="margin-bottom: 12px;">
      <router-link to="/admin/posts">文章</router-link>
      ·
      <router-link to="/admin/comments">评论</router-link>
      ·
      <router-link to="/admin/friend-links">友情链接</router-link>
      ·
      <router-link to="/admin/home-banner">首页轮播</router-link>
    </p>
    <p v-if="error" class="error">{{ error }}</p>

    <div class="form-section">
      <h3>评论列表</h3>
      <div class="filters" style="margin-bottom: 16px;">
        <label style="display: inline-flex; align-items: center; gap: 8px;">
          文章 ID
          <input v-model.number="postIdFilter" type="number" placeholder="留空为全部" style="width: 120px;" />
        </label>
        <button class="btn tag-pill" @click="page = 0; load()">筛选</button>
      </div>
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>文章</th>
            <th>类型</th>
            <th>昵称</th>
            <th>地址</th>
            <th>IP</th>
            <th>内容</th>
            <th>状态</th>
            <th>时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in comments" :key="c.id">
            <td>{{ c.id }}</td>
            <td>
              <button type="button" class="link-btn" @click="goToPost(c.postId)">{{ c.postId }}</button>
            </td>
            <td>
              <span v-if="c.parentId" class="muted">回复 #{{ c.parentId }}</span>
              <span v-else class="muted">评论</span>
            </td>
            <td>{{ c.author }}</td>
            <td class="muted">{{ c.address || '未知地址' }}</td>
            <td class="muted">{{ c.ip || '—' }}</td>
            <td class="comment-content-cell">{{ (c.content || '').slice(0, 80) }}{{ (c.content && c.content.length > 80) ? '…' : '' }}</td>
            <td>
              <span :class="c.approved ? 'status-done' : 'status-pending'">{{ c.approved ? '已通过' : '待审核' }}</span>
            </td>
            <td class="muted">{{ c.createdAt ? c.createdAt.slice(0, 16) : '—' }}</td>
            <td class="table-actions">
              <button v-if="!c.approved" @click="approve(c)">通过</button>
              <button @click="remove(c)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-if="!loading && comments.length === 0" class="muted">暂无评论。</p>
      <div v-else class="pagination" style="margin-top: 16px;">
        <button :disabled="page === 0" @click="page--; load()">上一页</button>
        <span>第 {{ page + 1 }} / {{ totalPages() }} 页（共 {{ totalElements }} 条）</span>
        <button :disabled="page >= totalPages() - 1" @click="page++; load()">下一页</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.link-btn {
  background: none;
  border: none;
  color: var(--accent);
  cursor: pointer;
  padding: 0;
  font-size: inherit;
}
.link-btn:hover { text-decoration: underline; }
.comment-content-cell { max-width: 240px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.status-pending { color: var(--text-muted); }
.status-done { color: #22c55e; }
</style>
