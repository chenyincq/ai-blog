const getApiHost = () => {
  if (typeof window === 'undefined' || !window.location?.hostname) {
    return { base: 'http://localhost', useProxy: false }
  }
  const { protocol, hostname, origin } = window.location
  const isProdHttps = protocol === 'https:' && hostname !== 'localhost' && hostname !== '127.0.0.1'
  return isProdHttps
    ? { base: origin, useProxy: true }
    : { base: `${protocol}//${hostname}`, useProxy: false }
}

const { base: apiBase, useProxy } = getApiHost()
const GATEWAY = useProxy ? `${apiBase}` : `${apiBase}:8080`
const POST_API = `${GATEWAY}/api/posts`
const AUTH_API = `${GATEWAY}/api/auth`
const FRIEND_LINKS_API = `${GATEWAY}/api/friend-links`
const HOME_BANNERS_API = `${GATEWAY}/api/home-banners`

export function getPosts(params = {}) {
  return fetch(`${POST_API}?${new URLSearchParams(params)}`).then(r => r.json())
}

export function getPost(id) {
  return fetch(`${POST_API}/${id}`).then(r => {
    if (!r.ok) throw new Error('Not found')
    return r.json()
  })
}

export function getPostsPopular(params = {}) {
  return fetch(`${POST_API}/popular?${new URLSearchParams(params)}`).then(r => r.json())
}

export function getLikeStatus(postId, visitorId) {
  return fetch(`${POST_API}/${postId}/like/status?${new URLSearchParams({ visitorId })}`).then(r => r.json())
}

export function addLike(postId, visitorId) {
  return fetch(`${POST_API}/${postId}/like`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ visitorId })
  }).then(r => {
    if (!r.ok) throw new Error('Like failed')
    return r.json()
  })
}

export function removeLike(postId, visitorId) {
  return fetch(`${POST_API}/${postId}/like?${new URLSearchParams({ visitorId })}`, {
    method: 'DELETE'
  }).then(r => {
    if (!r.ok) throw new Error('Unlike failed')
    return r.json()
  })
}

export function getComments(postId) {
  return fetch(`${POST_API}/${postId}/comments`).then(r => r.json())
}

export function postComment(postId, data) {
  return fetch(`${POST_API}/${postId}/comments`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  }).then(r => {
    if (!r.ok) throw new Error('Comment failed')
    return r.json()
  })
}

export function getAdminComments(params, token) {
  const q = new URLSearchParams(params)
  return fetch(`${POST_API}/admin/comments?${q}`, {
    headers: { Authorization: `Bearer ${token}` }
  }).then(r => {
    if (!r.ok) throw new Error('Unauthorized')
    return r.json()
  })
}

export function deleteComment(id, token) {
  return fetch(`${POST_API}/admin/comments/${id}`, {
    method: 'DELETE',
    headers: { Authorization: `Bearer ${token}` }
  }).then(r => { if (!r.ok) throw new Error('Delete failed') })
}

export function approveComment(id, token) {
  return fetch(`${POST_API}/admin/comments/${id}/approve`, {
    method: 'PUT',
    headers: { Authorization: `Bearer ${token}` }
  }).then(r => {
    if (!r.ok) throw new Error('Approve failed')
    return r.json()
  })
}

export function getTags() {
  return fetch(`${POST_API}/tags`).then(r => r.json())
}

export function getCategories() {
  return fetch(`${POST_API}/categories`).then(r => r.json())
}

export function login(username, password) {
  return fetch(`${AUTH_API}/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  }).then(r => {
    if (!r.ok) throw new Error('Login failed')
    return r.json()
  })
}

export function getWechatAuthorizeUrl() {
  return fetch(`${AUTH_API}/wechat/authorize`).then(r => r.json()).then(d => d.url)
}

export function getAdminPosts(params = {}, token) {
  return fetch(`${POST_API}/admin/all?${new URLSearchParams(params)}`, {
    headers: { Authorization: `Bearer ${token}` }
  }).then(r => {
    if (!r.ok) throw new Error('Unauthorized')
    return r.json()
  })
}

export function createPost(data, token) {
  return fetch(`${POST_API}/admin`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
    body: JSON.stringify(data)
  }).then(r => r.json())
}

export function updatePost(id, data, token) {
  return fetch(`${POST_API}/admin/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
    body: JSON.stringify(data)
  }).then(r => r.json())
}

export function deletePost(id, token) {
  return fetch(`${POST_API}/admin/${id}`, {
    method: 'DELETE',
    headers: { Authorization: `Bearer ${token}` }
  }).then(r => { if (!r.ok) throw new Error('Delete failed') })
}

export function uploadCover(file, token) {
  const form = new FormData()
  form.append('file', file)
  return fetch(`${POST_API}/admin/cover`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${token}` },
    body: form
  }).then(r => r.json()).then(d => d.url)
}

export function getFriendLinks() {
  return fetch(FRIEND_LINKS_API).then(r => r.json())
}

export function getAdminFriendLinks(token) {
  return fetch(`${FRIEND_LINKS_API}/admin/all`, {
    headers: { Authorization: `Bearer ${token}` }
  }).then(r => {
    if (!r.ok) throw new Error('Unauthorized')
    return r.json()
  })
}

export function createFriendLink(data, token) {
  return fetch(`${FRIEND_LINKS_API}/admin`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
    body: JSON.stringify(data)
  }).then(r => r.json())
}

export function updateFriendLink(id, data, token) {
  return fetch(`${FRIEND_LINKS_API}/admin/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
    body: JSON.stringify(data)
  }).then(r => r.json())
}

export function deleteFriendLink(id, token) {
  return fetch(`${FRIEND_LINKS_API}/admin/${id}`, {
    method: 'DELETE',
    headers: { Authorization: `Bearer ${token}` }
  }).then(r => { if (!r.ok) throw new Error('Delete failed') })
}

export function getHomeBanners() {
  return fetch(HOME_BANNERS_API).then(r => r.json())
}

export function getAdminHomeBanners(token) {
  return fetch(`${HOME_BANNERS_API}/admin/all`, {
    headers: { Authorization: `Bearer ${token}` }
  }).then(r => {
    if (!r.ok) throw new Error('Unauthorized')
    return r.json()
  })
}

export function createHomeBanner(data, token) {
  return fetch(`${HOME_BANNERS_API}/admin`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
    body: JSON.stringify(data)
  }).then(r => r.json())
}

export function updateHomeBanner(id, data, token) {
  return fetch(`${HOME_BANNERS_API}/admin/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
    body: JSON.stringify(data)
  }).then(r => r.json())
}

export function deleteHomeBanner(id, token) {
  return fetch(`${HOME_BANNERS_API}/admin/${id}`, {
    method: 'DELETE',
    headers: { Authorization: `Bearer ${token}` }
  }).then(r => { if (!r.ok) throw new Error('Delete failed') })
}
