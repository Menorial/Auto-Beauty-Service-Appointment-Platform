<template>
  <el-header class="app-header">
    <div class="container">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <el-icon><Car /></el-icon>
          <span>汽车美容预约平台</span>
        </div>
        
        <el-menu
          :default-active="activeIndex"
          class="header-menu"
          mode="horizontal"
          @select="handleSelect"
        >
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/services">服务项目</el-menu-item>
          <el-menu-item index="/appointment" v-if="userStore.isLoggedIn">预约服务</el-menu-item>
        </el-menu>
        
        <div class="header-actions">
          <template v-if="userStore.isLoggedIn">
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="32" :src="userStore.user?.avatarUrl">
                  <el-icon><User /></el-icon>
                </el-avatar>
                <span class="username">{{ userStore.user?.realName || userStore.user?.username }}</span>
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                  <el-dropdown-item command="vehicles">我的车辆</el-dropdown-item>
                  <el-dropdown-item command="appointments">我的预约</el-dropdown-item>
                  <el-dropdown-item command="reviews">我的评价</el-dropdown-item>
                  <el-dropdown-item divided command="admin" v-if="userStore.isAdmin">管理后台</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" @click="$router.push('/login')">登录</el-button>
            <el-button @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </div>
  </el-header>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Car, User, ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeIndex = computed(() => route.path)

const handleSelect = (key) => {
  router.push(key)
}

const handleCommand = async (command) => {
  try {
    switch (command) {
      case 'profile':
        await router.push('/profile')
        break
      case 'vehicles':
        await router.push('/vehicles')
        break
      case 'appointments':
        await router.push('/appointments')
        break
      case 'reviews':
        await router.push('/reviews')
        break
      case 'admin':
        await router.push('/admin')
        break
      case 'logout':
        try {
          await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          })
          userStore.logout()
          ElMessage.success('退出登录成功')
          await router.push('/')
        } catch {
          // 用户取消
        }
        break
    }
  } catch (error) {
    console.error('导航错误:', error)
    ElMessage.error('页面跳转失败，请稍后重试')
  }
}
</script>

<style lang="scss" scoped>
.app-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #000;
  cursor: pointer;
  
  .el-icon {
    font-size: 24px;
    color: #000;
  }
}

.header-menu {
  flex: 1;
  margin: 0 40px;
  border-bottom: none;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
  
  &:hover {
    background-color: #f5f7fa;
  }
  
  .username {
    font-size: 14px;
    color: $text-primary;
  }
}

@media (max-width: 768px) {
  .header-menu {
    display: none;
  }
  
  .logo span {
    display: none;
  }
}
</style>
