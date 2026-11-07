<template>
  <div class="admin-layout">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="250px" class="admin-sidebar">
        <div class="sidebar-header">
          <div class="logo">
            <el-icon><Van /></el-icon>
            <span>管理后台</span>
          </div>
        </div>
        
        <el-menu
          :default-active="activeMenu"
          class="admin-menu"
          router
          unique-opened
        >
          <el-menu-item index="/admin">
            <el-icon><Odometer /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          
          <el-sub-menu index="appointments">
            <template #title>
              <el-icon><Calendar /></el-icon>
              <span>预约管理</span>
            </template>
            <el-menu-item index="/admin/appointments">预约列表</el-menu-item>
            <el-menu-item index="/admin/appointments/calendar">预约日历</el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="services">
            <template #title>
              <el-icon><Service /></el-icon>
              <span>服务管理</span>
            </template>
            <el-menu-item index="/admin/services">服务列表</el-menu-item>
            <el-menu-item index="/admin/services/create">添加服务</el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="employees">
            <template #title>
              <el-icon><UserFilled /></el-icon>
              <span>员工管理</span>
            </template>
            <el-menu-item index="/admin/employees">员工列表</el-menu-item>
            <el-menu-item index="/admin/employees/create">添加员工</el-menu-item>
          </el-sub-menu>
          
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          
          <el-menu-item index="/admin/reviews">
            <el-icon><Star /></el-icon>
            <span>评价管理</span>
          </el-menu-item>
          
          <el-menu-item index="/admin/reports">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据报表</span>
          </el-menu-item>
          
          <el-menu-item index="/admin/settings">
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <!-- 主内容区 -->
      <el-container>
        <!-- 顶部导航 -->
        <el-header class="admin-header">
          <div class="header-left">
            <el-button 
              type="text" 
              @click="toggleSidebar"
              class="sidebar-toggle"
            >
              <el-icon><Fold v-if="!sidebarCollapsed" /><Expand v-else /></el-icon>
            </el-button>
            
            <el-breadcrumb separator="/">
              <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
                {{ item.title }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          
          <div class="header-right">
            <el-button type="primary" @click="$router.push('/')">
              <el-icon><House /></el-icon>
              返回前台
            </el-button>
            
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
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <!-- 主内容 -->
        <el-main class="admin-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Van, Odometer, Calendar, Service, UserFilled, User, Star,
  DataAnalysis, Setting, Fold, Expand, House, ArrowDown
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const sidebarCollapsed = ref(false)

const activeMenu = computed(() => route.path)

const breadcrumbs = computed(() => {
  const pathMap = {
    '/admin': [{ title: '仪表盘', path: '/admin' }],
    '/admin/appointments': [
      { title: '仪表盘', path: '/admin' },
      { title: '预约管理', path: '/admin/appointments' }
    ],
    '/admin/appointments/calendar': [
      { title: '仪表盘', path: '/admin' },
      { title: '预约管理', path: '/admin/appointments' },
      { title: '预约日历', path: '/admin/appointments/calendar' }
    ],
    '/admin/services': [
      { title: '仪表盘', path: '/admin' },
      { title: '服务管理', path: '/admin/services' }
    ],
    '/admin/services/create': [
      { title: '仪表盘', path: '/admin' },
      { title: '服务管理', path: '/admin/services' },
      { title: '添加服务', path: '/admin/services/create' }
    ],
    '/admin/employees': [
      { title: '仪表盘', path: '/admin' },
      { title: '员工管理', path: '/admin/employees' }
    ],
    '/admin/employees/create': [
      { title: '仪表盘', path: '/admin' },
      { title: '员工管理', path: '/admin/employees' },
      { title: '添加员工', path: '/admin/employees/create' }
    ],
    '/admin/users': [
      { title: '仪表盘', path: '/admin' },
      { title: '用户管理', path: '/admin/users' }
    ],
    '/admin/reviews': [
      { title: '仪表盘', path: '/admin' },
      { title: '评价管理', path: '/admin/reviews' }
    ],
    '/admin/reports': [
      { title: '仪表盘', path: '/admin' },
      { title: '数据报表', path: '/admin/reports' }
    ],
    '/admin/settings': [
      { title: '仪表盘', path: '/admin' },
      { title: '系统设置', path: '/admin/settings' }
    ]
  }
  
  return pathMap[route.path] || [{ title: '仪表盘', path: '/admin' }]
})

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
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
        router.push('/')
      } catch {
        // 用户取消
      }
      break
  }
}
</script>

<style lang="scss" scoped>
.admin-layout {
  height: 100vh;
  
  .el-container {
    height: 100%;
  }
}

.admin-sidebar {
  background: #001529;
  
  .sidebar-header {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-bottom: 1px solid #1f1f1f;
    
    .logo {
      display: flex;
      align-items: center;
      gap: 8px;
      color: white;
      font-size: 18px;
      font-weight: 600;
      
      .el-icon {
        font-size: 24px;
      }
    }
  }
  
  .admin-menu {
    border: none;
    background: #001529;
    
    :deep(.el-menu-item) {
      color: rgba(255, 255, 255, 0.65);
      
      &:hover {
        background-color: #1890ff;
        color: white;
      }
      
      &.is-active {
        background-color: #1890ff;
        color: white;
      }
    }
    
    :deep(.el-sub-menu__title) {
      color: rgba(255, 255, 255, 0.65);
      
      &:hover {
        background-color: #1f1f1f;
        color: white;
      }
    }
    
    :deep(.el-sub-menu .el-menu-item) {
      background-color: #000c17;
      
      &:hover {
        background-color: #1890ff;
      }
      
      &.is-active {
        background-color: #1890ff;
      }
    }
  }
}

.admin-header {
  background: white;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .sidebar-toggle {
      font-size: 16px;
    }
  }
  
  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;
    
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
  }
}

.admin-main {
  background: #fff;
  overflow-y: auto;
}

@media (max-width: 768px) {
  .admin-sidebar {
    width: 200px !important;
  }
  
  .header-left {
    .el-breadcrumb {
      display: none;
    }
  }
}
</style>
