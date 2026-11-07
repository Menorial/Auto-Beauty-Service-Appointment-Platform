import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import '@/styles/index.scss'

// 页面加载时检查localStorage状态
console.log('=== 页面加载时localStorage状态检查 ===')
console.log('Token存在:', !!localStorage.getItem('token'))
console.log('Token值:', localStorage.getItem('token') ? localStorage.getItem('token').substring(0, 50) + '...' : 'No token')
console.log('User存在:', !!localStorage.getItem('user'))
console.log('所有localStorage keys:', Object.keys(localStorage))

// 监听localStorage变化
const originalSetItem = localStorage.setItem
const originalRemoveItem = localStorage.removeItem
const originalClear = localStorage.clear

localStorage.setItem = function(key, value) {
  console.log(`localStorage.setItem called: ${key} = ${value ? value.substring(0, 50) + '...' : value}`)
  console.trace('setItem call stack:')
  return originalSetItem.apply(this, arguments)
}

localStorage.removeItem = function(key) {
  console.log(`localStorage.removeItem called: ${key}`)
  console.trace('removeItem call stack:')
  return originalRemoveItem.apply(this, arguments)
}

localStorage.clear = function() {
  console.log('localStorage.clear called')
  console.trace('clear call stack:')
  return originalClear.apply(this, arguments)
}

// 监听页面卸载事件
window.addEventListener('beforeunload', () => {
  console.log('=== 页面卸载前localStorage状态 ===')
  console.log('Token存在:', !!localStorage.getItem('token'))
  console.log('User存在:', !!localStorage.getItem('user'))
})

// 监听页面可见性变化
document.addEventListener('visibilitychange', () => {
  console.log('=== 页面可见性变化localStorage状态 ===')
  console.log('页面可见:', !document.hidden)
  console.log('Token存在:', !!localStorage.getItem('token'))
  console.log('User存在:', !!localStorage.getItem('user'))
})

const app = createApp(App)
const pinia = createPinia()

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus, {
  locale: zhCn,
})

app.mount('#app')
