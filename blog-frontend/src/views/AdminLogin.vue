<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login, getWechatAuthorizeUrl } from '../api'

const router = useRouter()
const username = ref('admin')
const password = ref('admin123')
const loading = ref(false)
const error = ref('')

async function doLogin() {
  loading.value = true
  error.value = ''
  try {
    const res = await login(username.value, password.value)
    localStorage.setItem('blog_token', res.token)
    localStorage.setItem('blog_nickname', res.nickname || '')
    localStorage.setItem('blog_avatar', res.avatar || '')
    router.push('/admin/posts')
  } catch (e) {
    error.value = '用户名或密码错误'
  } finally {
    loading.value = false
  }
}

async function wechatLogin() {
  try {
    const url = await getWechatAuthorizeUrl()
    window.location.href = url
  } catch (e) {
    alert('获取微信登录地址失败，请检查后端配置')
  }
}
</script>

<template>
  <div class="page" style="max-width: 400px; margin: 0 auto;">
    <div class="form-section">
      <h3>管理后台登录</h3>
      <p v-if="error" class="error">{{ error }}</p>
      <label>用户名 <input v-model="username" type="text" /></label>
      <label>密码 <input v-model="password" type="password" /></label>
      <button class="btn" :disabled="loading" @click="doLogin">{{ loading ? '登录中…' : '登录' }}</button>
      <div style="margin-top: 16px; padding-top: 16px; border-top: 1px solid var(--border);">
        <p style="font-size: 0.9rem; color: var(--text-muted); margin-bottom: 8px;">或使用微信扫码登录</p>
        <button type="button" class="btn-wechat" @click="wechatLogin">微信登录</button>
      </div>
    </div>
  </div>
</template>
