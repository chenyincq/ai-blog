import { createRouter, createWebHashHistory } from 'vue-router'
import Home from '../views/Home.vue'
import PostList from '../views/PostList.vue'
import PostDetail from '../views/PostDetail.vue'
import AdminLogin from '../views/AdminLogin.vue'
import AdminCallback from '../views/AdminCallback.vue'
import AdminPosts from '../views/AdminPosts.vue'
import AdminFriendLinks from '../views/AdminFriendLinks.vue'
import FriendLinks from '../views/FriendLinks.vue'
import AdminComments from '../views/AdminComments.vue'
import AdminHomeBanner from '../views/AdminHomeBanner.vue'
import Popular from '../views/Popular.vue'

const routes = [
  { path: '/', name: 'home', component: Home },
  { path: '/posts', name: 'posts', component: PostList },
  { path: '/popular', name: 'popular', component: Popular },
  { path: '/post/:id', name: 'post-detail', component: PostDetail, props: true },
  { path: '/friend-links', name: 'friend-links', component: FriendLinks },
  { path: '/admin/login', name: 'admin-login', component: AdminLogin },
  { path: '/admin/callback', name: 'admin-callback', component: AdminCallback },
  { path: '/admin/posts', name: 'admin-posts', component: AdminPosts },
  { path: '/admin/comments', name: 'admin-comments', component: AdminComments },
  { path: '/admin/friend-links', name: 'admin-friend-links', component: AdminFriendLinks },
  { path: '/admin/home-banner', name: 'admin-home-banner', component: AdminHomeBanner }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('blog_token')
  const adminNames = ['admin-posts', 'admin-friend-links', 'admin-comments', 'admin-home-banner']
  if (adminNames.includes(to.name) && !token) {
    next({ name: 'admin-login' })
  } else {
    next()
  }
})

export default router
