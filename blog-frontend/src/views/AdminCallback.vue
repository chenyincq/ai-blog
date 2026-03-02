<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

onMounted(() => {
  const token = route.query.token
  const nickname = route.query.nickname
  if (token) {
    localStorage.setItem('blog_token', token)
    if (nickname) localStorage.setItem('blog_nickname', decodeURIComponent(nickname))
    router.replace('/admin/posts')
  } else {
    router.replace('/admin/login')
  }
})
</script>

<template>
  <div class="page" style="text-align: center; padding: 48px 20px;">
    <p>登录成功，正在跳转…</p>
  </div>
</template>
