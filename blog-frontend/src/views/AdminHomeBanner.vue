<script setup>
import { ref, onMounted } from 'vue'
import { getAdminHomeBanners, createHomeBanner, updateHomeBanner, deleteHomeBanner, uploadCover } from '../api'

const list = ref([])
const loading = ref(false)
const error = ref('')
const editing = ref(null)
const form = ref({ imageUrl: '', title: '', subtitle: '', sortOrder: 0 })

const token = localStorage.getItem('blog_token')

async function load() {
  loading.value = true
  try {
    list.value = await getAdminHomeBanners(token)
  } catch (e) {
    error.value = '加载失败'
  } finally {
    loading.value = false
  }
}

function resetForm() {
  editing.value = null
  form.value = { imageUrl: '', title: '', subtitle: '', sortOrder: 0 }
}

function editItem(b) {
  editing.value = b.id
  form.value = {
    imageUrl: b.imageUrl || '',
    title: b.title || '',
    subtitle: b.subtitle || '',
    sortOrder: b.sortOrder ?? 0
  }
}

async function submit() {
  error.value = ''
  try {
    if (editing.value) {
      await updateHomeBanner(editing.value, form.value, token)
    } else {
      await createHomeBanner(form.value, token)
    }
    resetForm()
    await load()
  } catch (e) {
    error.value = '保存失败'
  }
}

async function remove(id) {
  if (!confirm('确定删除该条背景图配置？')) return
  try {
    await deleteHomeBanner(id, token)
    await load()
  } catch (_) {
    alert('删除失败')
  }
}

async function onCoverChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  try {
    form.value.imageUrl = await uploadCover(file, token)
  } catch (_) {
    alert('上传失败')
  }
  e.target.value = ''
}

onMounted(load)
</script>

<template>
  <div class="page">
    <h2 style="margin: 0 0 20px;">首页背景图配置</h2>
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
      <h3>{{ editing ? '编辑' : '新增' }}背景图</h3>
      <label>背景图 URL <input v-model="form.imageUrl" type="text" placeholder="图片地址" /></label>
      <label>上传图片 <input type="file" accept="image/*" @change="onCoverChange" /></label>
      <label>主标题（显示在背景图上） <input v-model="form.title" type="text" placeholder="如：欢迎来到我的博客" /></label>
      <label>副标题 <input v-model="form.subtitle" type="text" placeholder="如：记录 · 分享 · 交流" /></label>
      <label>排序（数字越小越靠前） <input v-model.number="form.sortOrder" type="number" /></label>
      <button class="btn" @click="submit">保存</button>
      <button class="btn btn-ghost" @click="resetForm">取消</button>
    </div>

    <section class="form-section">
      <h3>当前配置列表</h3>
      <table>
        <thead>
          <tr><th>预览</th><th>主标题</th><th>副标题</th><th>排序</th><th>操作</th></tr>
        </thead>
        <tbody>
          <tr v-for="b in list" :key="b.id">
            <td>
              <img v-if="b.imageUrl" :src="b.imageUrl" alt="" style="width: 120px; height: 48px; object-fit: cover; border-radius: 6px;" />
              <span v-else class="muted">—</span>
            </td>
            <td>{{ b.title || '—' }}</td>
            <td>{{ b.subtitle || '—' }}</td>
            <td>{{ b.sortOrder }}</td>
            <td class="table-actions">
              <button @click="editItem(b)">编辑</button>
              <button @click="remove(b.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-if="!loading && list.length === 0" class="muted">暂无配置，请在上方添加。首页将显示默认欢迎文案。</p>
    </section>
  </div>
</template>
