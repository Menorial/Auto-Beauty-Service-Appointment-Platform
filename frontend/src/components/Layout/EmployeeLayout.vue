<template>
  <div class="employee-layout">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="250px" class="employee-sidebar">
        <div class="sidebar-header">
          <div class="logo">
            <el-icon><UserFilled /></el-icon>
            <span>员工工作台</span>
          </div>
        </div>
        
        <el-menu
          :default-active="activeMenu"
          class="employee-menu"
          router
          unique-opened
        >
          <el-menu-item index="/employee">
            <el-icon><Odometer /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          
          <el-menu-item index="/employee/appointments">
            <el-icon><Calendar /></el-icon>
            <span>我的预约</span>
          </el-menu-item>
          
          <el-menu-item index="/employee/schedule">
            <el-icon><Clock /></el-icon>
            <span>工作排班</span>
          </el-menu-item>
          
          <el-menu-item index="/employee/profile">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
          
          <el-menu-item index="/employee/change-password">
            <el-icon><Lock /></el-icon>
            <span>修改密码</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <!-- 主内容区 -->
      <el-container>
        <!-- 顶部导航 -->
        <el-header class="employee-header">
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
                <el-avatar :src="userStore.user?.avatarUrl" :size="32" />
                {{ userStore.user?.realName || userStore.user?.username }}
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                  <el-dropdown-item command="changePassword">修改密码</el-dropdown-item>
                  <el-dropdown-divider />
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <!-- 主内容 -->
        <el-main class="employee-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Odometer, Calendar, Clock, User, House, ArrowDown, Fold, Expand, UserFilled, Lock } from '@element-plus/icons-vue'
import { getCurrentEmployeeInfo } from '@/api/employee'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const sidebarCollapsed = ref(false)

const activeMenu = computed(() => {
  return route.path
})

const breadcrumbs = computed(() => {
  const breadcrumbMap = {
    '/employee': [{ path: '/employee', title: '仪表盘' }],
    '/employee/appointments': [{ path: '/employee', title: '员工工作台' }, { path: '/employee/appointments', title: '我的预约' }],
    '/employee/schedule': [{ path: '/employee', title: '员工工作台' }, { path: '/employee/schedule', title: '工作排班' }],
    '/employee/profile': [{ path: '/employee', title: '员工工作台' }, { path: '/employee/profile', title: '个人信息' }],
    '/employee/change-password': [{ path: '/employee', title: '员工工作台' }, { path: '/employee/change-password', title: '修改密码' }]
  }
  
  return breadcrumbMap[route.path] || [{ path: route.path, title: '页面' }]
})

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/employee/profile')
  } else if (command === 'changePassword') {
    router.push('/employee/change-password')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      router.push('/login')
      ElMessage.success('已退出登录')
    }).catch(() => {})
  }
}

// 员工头像下拉框使用 userStore.user.avatarUrl，但员工头像实际存储在 employees 表。
// 进入员工后台时若 avatarUrl 为空，则拉取 /employee/info 并同步到 store，保证右上角头像显示。
const syncEmployeeAvatarToStore = async () => {
  try {
    if (!userStore.user) return
    if (userStore.user.role !== 'EMPLOYEE') return

    // avatarUrl 已有则不重复请求
    if (userStore.user.avatarUrl) return

    const resp = await getCurrentEmployeeInfo()
    if (resp?.success && resp.data?.avatarUrl) {
      userStore.updateUserInfo({ avatarUrl: resp.data.avatarUrl })
    }
  } catch (e) {
    console.error('同步员工头像失败:', e)
  }
}

onMounted(() => {
  syncEmployeeAvatarToStore()
})

watch(
  () => userStore.user,
  () => {
    syncEmployeeAvatarToStore()
  }
)
</script>

<style lang="scss" scoped>
.employee-layout {
  height: 100vh;
  
  :deep(.el-container) {
    height: 100%;
  }
}

.employee-sidebar {
  background: #001529;
  overflow-y: auto;
  
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
  
  .employee-menu {
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

.employee-header {
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

.employee-main {
  background: #fff;
  overflow-y: auto;
}

@media (max-width: 768px) {
  .employee-sidebar {
    width: 200px !important;
  }
  
  .employee-header {
    padding: 0 16px;
    
    .header-left {
      gap: 12px;
    }
    
    .header-right {
      gap: 12px;
    }
  }
}
</style>
