<script setup>
import { ref, onMounted, computed } from 'vue'
import { getAdminPosts, createPost, updatePost, deletePost, uploadCover } from '../api'
import { marked } from 'marked'

const posts = ref([])
const totalElements = ref(0)
const page = ref(0)
const size = ref(10)
const loading = ref(false)
const error = ref('')
const editing = ref(null)
const form = ref({
  title: '', summary: '', content: '', author: '', category: '', tags: '', coverUrl: '', published: true
})

const token = localStorage.getItem('blog_token')
const previewHtml = computed(() => form.value.content ? marked(form.value.content) : '')

async function load() {
  loading.value = true
  try {
    const data = await getAdminPosts({ page: page.value, size: size.value }, token)
    posts.value = data.content || []
    totalElements.value = data.totalElements || 0
  } catch (e) {
    error.value = '加载失败'
  } finally {
    loading.value = false
  }
}

function resetForm() {
  editing.value = null
  form.value = { title: '', summary: '', content: '', author: '', category: '', tags: '', coverUrl: '', published: true }
}

function editPost(p) {
  editing.value = p.id
  form.value = { ...p }
}

async function submit() {
  error.value = ''
  try {
    if (editing.value) {
      await updatePost(editing.value, form.value, token)
    } else {
      await createPost(form.value, token)
    }
    resetForm()
    await load()
  } catch (e) {
    error.value = '保存失败'
  }
}

async function remove(id) {
  if (!confirm('确定删除？')) return
  try {
    await deletePost(id, token)
    await load()
  } catch (_) {
    alert('删除失败')
  }
}

async function onCoverChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  try {
    form.value.coverUrl = await uploadCover(file, token)
  } catch (_) {
    alert('上传失败')
  }
  e.target.value = ''
}

const totalPages = () => Math.ceil(totalElements.value / size.value) || 1

onMounted(load)
</script>

<template>
  <div class="page">
    <h2 style="margin: 0 0 20px;">文章管理</h2>
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

    <div class="form-section" style="display: grid; grid-template-columns: 1fr 1fr; gap: 24px;">
      <div>
        <h3>{{ editing ? '编辑文章' : '新建文章' }}</h3>
        <label>标题 <input v-model="form.title" type="text" /></label>
        <label>摘要 <input v-model="form.summary" type="text" /></label>
        <label>作者 <input v-model="form.author" type="text" /></label>
        <label>分类 <input v-model="form.category" type="text" /></label>
        <label>标签（逗号分隔） <input v-model="form.tags" type="text" placeholder="Vue, 前端" /></label>
        <label>封面 URL <input v-model="form.coverUrl" type="text" placeholder="或上传文件" /></label>
        <label>上传封面 <input type="file" accept="image/*" @change="onCoverChange" /></label>
        <label>内容 (Markdown) <textarea v-model="form.content" rows="12" /></label>
        <label><input v-model="form.published" type="checkbox" /> 发布</label>
        <button class="btn" @click="submit">保存</button>
        <button class="btn btn-ghost" @click="resetForm">重置</button>
      </div>
      <div style="border-left: 1px solid var(--border); padding-left: 16px;">
        <h3>预览</h3>
        <div class="article-content" v-html="previewHtml"></div>
      </div>
    </div>

    <section class="form-section">
      <h3>文章列表</h3>
      <table>
        <thead>
          <tr><th>ID</th><th>标题</th><th>作者</th><th>分类</th><th>发布</th><th>操作</th></tr>
        </thead>
        <tbody>
          <tr v-for="p in posts" :key="p.id">
            <td>{{ p.id }}</td>
            <td>{{ p.title }}</td>
            <td>{{ p.author }}</td>
            <td>{{ p.category }}</td>
            <td>{{ p.published ? '是' : '否' }}</td>
            <td class="table-actions">
              <button @click="editPost(p)">编辑</button>
              <button @click="remove(p.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div class="pagination">
        <button :disabled="page === 0" @click="page--; load()">上一页</button>
        <span>第 {{ page + 1 }} / {{ totalPages() }} 页</span>
        <button :disabled="page >= totalPages() - 1" @click="page++; load()">下一页</button>
      </div>
    </section>
  </div>
</template>
