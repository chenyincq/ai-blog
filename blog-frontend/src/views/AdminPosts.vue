<script setup>
import { ref, onMounted, computed } from 'vue'
import { getAdminPosts, createPost, updatePost, deletePost, uploadCover, uploadImage } from '../api'
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

const contentTextarea = ref(null)
const showPreviewModal = ref(false)
function openPreview() {
  showPreviewModal.value = true
}
function closePreview() {
  showPreviewModal.value = false
}

async function onImageChange(e) {
  const file = e.target.files?.[0]
  if (!file || !file.type.startsWith('image/')) return
  try {
    const url = await uploadImage(file, token)
    const insert = `\n![图片](${url})\n`
    const textarea = contentTextarea.value
    if (textarea) {
      const start = textarea.selectionStart
      const end = textarea.selectionEnd
      const before = form.value.content.slice(0, start)
      const after = form.value.content.slice(end)
      form.value.content = before + insert + after
    } else {
      form.value.content += insert
    }
  } catch (_) {
    alert('图片上传失败')
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

    <div class="form-section editor-section">
      <div class="editor-form">
        <h3>{{ editing ? '编辑文章' : '新建文章' }}</h3>
        <label>标题 <input v-model="form.title" type="text" /></label>
        <label>摘要 <input v-model="form.summary" type="text" /></label>
        <label>作者 <input v-model="form.author" type="text" /></label>
        <label>分类 <input v-model="form.category" type="text" /></label>
        <label>标签（逗号分隔） <input v-model="form.tags" type="text" placeholder="Vue, 前端" /></label>
        <label>封面 URL <input v-model="form.coverUrl" type="text" placeholder="或上传文件" /></label>
        <label>上传封面 <input type="file" accept="image/*" @change="onCoverChange" /></label>
        <div class="content-editor-wrap">
          <div class="content-editor-header">
            <span>内容 (Markdown)</span>
            <button type="button" class="btn btn-ghost btn-sm" @click="openPreview">预览</button>
          </div>
          <textarea ref="contentTextarea" v-model="form.content" class="content-textarea" />
          <p class="insert-image-row">
            <label class="inline-label">插入图片 <input type="file" accept="image/*" @change="onImageChange" /></label>
          </p>
        </div>
        <label><input v-model="form.published" type="checkbox" /> 发布</label>
        <button class="btn" @click="submit">保存</button>
        <button class="btn btn-ghost" @click="resetForm">重置</button>
      </div>
    </div>

    <!-- 预览弹窗 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="showPreviewModal" class="preview-modal-overlay" @click.self="closePreview">
          <div class="preview-modal">
            <div class="preview-modal-header">
              <h3>预览</h3>
              <button type="button" class="preview-close" @click="closePreview">×</button>
            </div>
            <div class="preview-modal-body">
              <div class="article-content" v-html="previewHtml"></div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

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

<style scoped>
.editor-section {
  max-width: 100%;
}
.editor-section .content-editor-wrap {
  margin-bottom: 12px;
}
.content-editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  font-size: 0.9rem;
  color: var(--text-muted);
}
.content-editor-header .btn-sm {
  padding: 6px 12px;
  font-size: 0.85rem;
}
.content-textarea {
  width: 100%;
  max-width: 100%;
  min-height: 420px;
  resize: vertical;
  display: block;
}
.insert-image-row {
  margin: 8px 0 0;
}
.preview-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}
.preview-modal {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 12px;
  max-width: 720px;
  width: 100%;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.4);
}
.preview-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border);
}
.preview-modal-header h3 { margin: 0; font-size: 1rem; }
.preview-close {
  background: none;
  border: none;
  color: var(--text-muted);
  font-size: 1.5rem;
  cursor: pointer;
  line-height: 1;
  padding: 0 8px;
}
.preview-close:hover { color: var(--text); }
.preview-modal-body {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}
.modal-enter-active, .modal-leave-active {
  transition: opacity 0.2s ease;
}
.modal-enter-from, .modal-leave-to { opacity: 0; }
</style>
