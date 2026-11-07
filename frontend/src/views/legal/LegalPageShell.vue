<template>
  <div class="legal-shell">
    <header class="legal-shell__header">
      <div class="legal-shell__bar">
        <el-button text type="primary" @click="goBack">
          <el-icon class="el-icon--left"><ArrowLeft /></el-icon>
          返回
        </el-button>
      </div>
      <h1 class="legal-shell__title">{{ title }}</h1>
      <p class="legal-shell__meta">
        <span>更新日期：{{ updateDate }}</span>
        <span class="legal-shell__sep">|</span>
        <span>生效日期：{{ effectiveDate }}</span>
      </p>
    </header>
    <main class="legal-shell__body">
      <slot />
    </main>
  </div>
</template>

<script setup>
import { onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'

const props = defineProps({
  title: { type: String, required: true },
  updateDate: { type: String, default: '2026年3月20日' },
  effectiveDate: { type: String, default: '2026年3月20日' }
})

const router = useRouter()
const appTitle = process.env.VUE_APP_TITLE || '汽车美容服务预约平台'
let previousTitle = ''

onMounted(() => {
  previousTitle = document.title
  document.title = `${props.title} - ${appTitle}`
})

onBeforeUnmount(() => {
  document.title = previousTitle || appTitle
})

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}
</script>

<style lang="scss" scoped>
.legal-shell {
  min-height: 100vh;
  background: #fff;
  padding: 24px 16px 48px;
}

.legal-shell__header {
  max-width: 900px;
  margin: 0 auto 24px;
}

.legal-shell__bar {
  margin-bottom: 16px;
}

.legal-shell__title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px;
  line-height: 1.4;
}

.legal-shell__meta {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.legal-shell__sep {
  margin: 0 8px;
  opacity: 0.6;
}

.legal-shell__body {
  max-width: 900px;
  margin: 0 auto;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  padding: 28px 32px 36px;
}

@media (max-width: 768px) {
  .legal-shell__body {
    padding: 20px 16px 28px;
  }

  .legal-shell__title {
    font-size: 20px;
  }
}
</style>
