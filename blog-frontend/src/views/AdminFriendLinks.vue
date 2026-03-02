<script setup>
import { ref, onMounted } from 'vue'
import { getAdminFriendLinks, createFriendLink, updateFriendLink, deleteFriendLink } from '../api'

const links = ref([])
const loading = ref(false)
const error = ref('')
const editing = ref(null)
const form = ref({ name: '', url: '', description: '', sortOrder: 0 })

const token = localStorage.getItem('blog_token')

async function load() {
  loading.value = true
  try {
    links.value = await getAdminFriendLinks(token)
  } catch (e) {
    error.value = '加载失败'
  } finally {
    loading.value = false
  }
}

function resetForm() {
  editing.value = null
  form.value = { name: '', url: '', description: '', sortOrder: 0 }
}

function editLink(link) {
  editing.value = link.id
  form.value = { name: link.name, url: link.url, description: link.description || '', sortOrder: link.sortOrder ?? 0 }
}

async function submit() {
  error.value = ''
  try {
    if (editing.value) {
      await updateFriendLink(editing.value, form.value, token)
    } else {
      await createFriendLink(form.value, token)
    }
    resetForm()
    await load()
  } catch (e) {
    error.value = '保存失败'
  }
}

async function remove(id) {
  if (!confirm('确定删除该链接？')) return
  try {
    await deleteFriendLink(id, token)
    await load()
  } catch (_) {
    alert('删除失败')
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <h2 style="margin: 0 0 20px;">友情链接管理</h2>
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
      <h3>{{ editing ? '编辑链接' : '新建链接' }}</h3>
      <label>名称 <input v-model="form.name" type="text" placeholder="如：Vue 官网" /></label>
      <label>URL <input v-model="form.url" type="url" placeholder="https://..." /></label>
      <label>描述（可选） <input v-model="form.description" type="text" /></label>
      <label>排序（数字越小越靠前） <input v-model.number="form.sortOrder" type="number" /></label>
      <button class="btn" @click="submit">保存</button>
      <button class="btn btn-ghost" @click="resetForm">取消</button>
    </div>

    <section class="form-section">
      <h3>链接列表</h3>
      <table>
        <thead>
          <tr><th>名称</th><th>URL</th><th>描述</th><th>排序</th><th>操作</th></tr>
        </thead>
        <tbody>
          <tr v-for="link in links" :key="link.id">
            <td>{{ link.name }}</td>
            <td><a :href="link.url" target="_blank" rel="noopener">{{ link.url }}</a></td>
            <td>{{ link.description || '—' }}</td>
            <td>{{ link.sortOrder }}</td>
            <td class="table-actions">
              <button @click="editLink(link)">编辑</button>
              <button @click="remove(link.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-if="!loading && links.length === 0" class="muted">暂无友情链接，请在上方添加。</p>
    </section>
  </div>
</template>
