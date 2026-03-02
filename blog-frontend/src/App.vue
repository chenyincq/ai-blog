<script setup>
import { ref, onMounted, provide } from 'vue'

const isDark = ref(true)
const weather = ref(null)
const weatherLoading = ref(false)
const weatherError = ref('')

const WEATHER_CODE_MAP = {
  0: '晴', 1: '少云', 2: '多云', 3: '阴', 45: '雾', 48: '雾',
  51: '毛毛雨', 53: '毛毛雨', 55: '毛毛雨', 61: '小雨', 63: '中雨', 65: '大雨',
  71: '小雪', 73: '雪', 75: '大雪', 77: '雪粒', 80: '阵雨', 81: '阵雨', 82: '强阵雨',
  85: '阵雪', 86: '阵雪', 95: '雷雨', 96: '雷暴', 99: '雷暴'
}

function getWeatherDesc(code) {
  return WEATHER_CODE_MAP[code] ?? '—'
}

function fetchWeather(lat, lon, locationLabel) {
  weatherLoading.value = true
  weatherError.value = ''
  const weatherUrl = `https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}&current=temperature_2m,relative_humidity_2m,weather_code&timezone=auto`
  fetch(weatherUrl)
    .then(r => r.json())
    .then((weatherData) => {
      const cur = weatherData?.current
      if (cur) {
        weather.value = {
          location: locationLabel || `${lat.toFixed(2)}°, ${lon.toFixed(2)}°`,
          temp: Math.round(cur.temperature_2m),
          humidity: cur.relative_humidity_2m,
          code: cur.weather_code,
          desc: getWeatherDesc(cur.weather_code)
        }
      }
    })
    .catch(() => { weatherError.value = '' })
    .finally(() => { weatherLoading.value = false })
}

function parseLocationFromGeo(geoData) {
  if (!geoData?.address) return ''
  const a = geoData.address
  const name = a.city || a.town || a.village || a.municipality || a.county || a.state
  const country = a.country
  return name && country ? `${name} · ${country}` : (name || country || '')
}

function tryFetchByCoords(lat, lon, thenReverseGeocode = true) {
  if (thenReverseGeocode) {
    weatherLoading.value = true
    weatherError.value = ''
    const headers = { 'Accept': 'application/json', 'Accept-Language': 'zh-CN,zh;q=0.9,en;q=0.8', 'User-Agent': 'BlogApp/1.0' }
    const geoUrl = `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lon}&format=json`
    fetch(geoUrl, { headers })
      .then(r => r.ok ? r.json() : null)
      .then(geoData => {
        const location = parseLocationFromGeo(geoData) || `${lat.toFixed(2)}°, ${lon.toFixed(2)}°`
        fetchWeather(lat, lon, location)
      })
      .catch(() => fetchWeather(lat, lon, `${lat.toFixed(2)}°, ${lon.toFixed(2)}°`))
    return
  }
  fetchWeather(lat, lon, '')
}

function loadWeatherFromIP() {
  weatherLoading.value = true
  weatherError.value = ''
  fetch('https://ip-api.com/json/?lang=zh-CN&fields=lat,lon,city,countryName')
    .then(r => r.json())
    .then(data => {
      if (data?.lat != null && data?.lon != null) {
        const loc = [data.city, data.countryName].filter(Boolean).join(' · ')
        fetchWeather(data.lat, data.lon, loc || undefined)
      } else {
        weatherError.value = ''
        weatherLoading.value = false
      }
    })
    .catch(() => {
      weatherError.value = ''
      weatherLoading.value = false
    })
}

function loadWeather() {
  // 仅在此处触发：打开页面时一次，或用户点击「天气」/「点击重新获取」时。不自动轮询刷新。
  if (!navigator.geolocation) {
    loadWeatherFromIP()
    return
  }
  navigator.geolocation.getCurrentPosition(
    pos => tryFetchByCoords(pos.coords.latitude, pos.coords.longitude, true),
    () => loadWeatherFromIP(),
    { timeout: 10000, maximumAge: 0 }
  )
}

function toggleTheme() {
  isDark.value = !isDark.value
  localStorage.setItem('blog-theme', isDark.value ? 'dark' : 'light')
  document.documentElement.classList.toggle('theme-light', !isDark.value)
}

function getDarkByTime() {
  const hour = new Date().getHours()
  return hour < 6 || hour >= 18
}

function applyTheme(dark) {
  isDark.value = dark
  document.documentElement.classList.toggle('theme-light', !dark)
}

onMounted(() => {
  const saved = localStorage.getItem('blog-theme')
  if (saved === 'light') {
    applyTheme(false)
  } else if (saved === 'dark') {
    applyTheme(true)
  } else {
    applyTheme(getDarkByTime())
  }
  setInterval(() => {
    if (localStorage.getItem('blog-theme') == null) {
      applyTheme(getDarkByTime())
    }
  }, 60 * 1000)
  loadWeather()
})

provide('theme', { isDark, toggleTheme })
provide('weather', { weather, weatherLoading, loadWeather })
</script>

<template>
  <div class="layout">
    <header v-if="$route.name !== 'home'" class="page-header">
      <div class="page-header-inner">
        <div class="header-main">
          <div class="header-brand" @click="$router.push('/')">
            <span class="site-title">博客</span>
          </div>
          <nav class="site-nav">
            <router-link to="/">首页</router-link>
            <router-link to="/posts">文章</router-link>
            <router-link to="/popular">热门</router-link>
            <router-link to="/friend-links">友情链接</router-link>
            <router-link to="/admin/login">管理</router-link>
          </nav>
          <div class="header-actions">
            <div class="weather-widget">
              <span v-if="weatherLoading" class="weather-loading">天气…</span>
              <span v-else-if="weather" class="weather-info" title="本地天气（打开时获取，点击可重新获取）" @click="loadWeather">
                <span v-if="weather.location" class="weather-location">{{ weather.location }}</span>
                <span class="weather-temp">{{ weather.temp }}°C</span>
                <span class="weather-desc">{{ weather.desc }}</span>
                <span v-if="weather.humidity != null" class="weather-humidity">湿度 {{ weather.humidity }}%</span>
              </span>
              <button v-else type="button" class="weather-refresh" title="获取本地天气" @click="loadWeather">天气</button>
            </div>
            <button type="button" class="theme-toggle" :title="isDark ? '切换到日间模式' : '切换到夜间模式'" @click="toggleTheme">
              <span v-if="isDark" class="theme-icon">☀️</span>
              <span v-else class="theme-icon">🌙</span>
            </button>
          </div>
        </div>
      </div>
    </header>

    <main class="main">
      <router-view />
    </main>

    <footer class="site-footer">
      © {{ new Date().getFullYear() }} 博客 · 前后端分离 · Vue + 微服务
    </footer>
  </div>
</template>
