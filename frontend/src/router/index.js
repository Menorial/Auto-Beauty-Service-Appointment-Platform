import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import AdminLayout from '@/components/Layout/AdminLayout.vue'
// 登录/注册改为同步打包进主 chunk，避免懒加载 chunk 被 CDN/浏览器长期缓存导致界面仍是旧版
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/legal/user-agreement',
    name: 'UserAgreement',
    component: () => import('@/views/legal/UserAgreement.vue'),
    meta: { title: '用户服务协议' }
  },
  {
    path: '/legal/privacy-policy',
    name: 'PrivacyPolicy',
    component: () => import('@/views/legal/PrivacyPolicy.vue'),
    meta: { title: '隐私政策' }
  },
  {
    path: '/services',
    name: 'Services',
    component: () => import('@/views/Services.vue')
  },
  {
    path: '/services/:id',
    name: 'ServiceDetail',
    component: () => import('@/views/ServiceDetail.vue')
  },
  {
    path: '/appointment',
    name: 'Appointment',
    component: () => import('@/views/Appointment.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/vehicles',
    name: 'Vehicles',
    component: () => import('@/views/Vehicles.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/appointments',
    name: 'MyAppointments',
    component: () => import('@/views/Appointments.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/appointments/:id',
    name: 'AppointmentDetail',
    component: () => import('@/views/AppointmentDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/reviews',
    name: 'MyReviews',
    component: () => import('@/views/Reviews.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/payment/result',
    name: 'PaymentResult',
    component: () => import('@/views/PaymentResult.vue')
  },
  // 管理员路由
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('@/views/Admin/Dashboard.vue')
      },
      {
        path: 'services',
        name: 'AdminServices',
        component: () => import('@/views/Admin/Services.vue')
      },
      {
        path: 'services/create',
        name: 'AdminServiceCreate',
        component: () => import('@/views/Admin/ServiceCreate.vue')
      },
      {
        path: 'appointments',
        name: 'AdminAppointments',
        component: () => import('@/views/Admin/Appointments.vue')
      },
      {
        path: 'appointments/:id',
        name: 'AdminAppointmentDetail',
        component: () => import('@/views/Employee/AppointmentDetail.vue')
      },
      {
        path: 'appointments/calendar',
        name: 'AdminAppointmentCalendar',
        component: () => import('@/views/Admin/AppointmentCalendar.vue')
      },
      {
        path: 'employees',
        name: 'AdminEmployees',
        component: () => import('@/views/Admin/Employees.vue')
      },
      {
        path: 'employees/create',
        name: 'AdminEmployeeCreate',
        component: () => import('@/views/Admin/EmployeeCreate.vue')
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/Admin/Users.vue')
      },
      {
        path: 'reviews',
        name: 'AdminReviews',
        component: () => import('@/views/Admin/Reviews.vue')
      },
      {
        path: 'reports',
        name: 'AdminReports',
        component: () => import('@/views/Admin/Reports.vue')
      },
      {
        path: 'settings',
        name: 'AdminSettings',
        component: () => import('@/views/Admin/Settings.vue')
      }
    ]
  },
  // 员工路由
  {
    path: '/employee',
    component: () => import('@/components/Layout/EmployeeLayout.vue'),
    meta: { requiresAuth: true, requiresEmployee: true },
    children: [
      {
        path: '',
        name: 'EmployeeDashboard',
        component: () => import('@/views/Employee/Dashboard.vue')
      },
      {
        path: 'appointments',
        name: 'EmployeeAppointments',
        component: () => import('@/views/Employee/Appointments.vue')
      },
      {
        path: 'appointments/:id',
        name: 'EmployeeAppointmentDetail',
        component: () => import('@/views/Employee/AppointmentDetail.vue')
      },
      {
        path: 'schedule',
        name: 'EmployeeSchedule',
        component: () => import('@/views/Employee/Schedule.vue')
      },
      {
        path: 'profile',
        name: 'EmployeeProfile',
        component: () => import('@/views/Employee/Profile.vue')
      },
      {
        path: 'change-password',
        name: 'EmployeeChangePassword',
        component: () => import('@/views/Employee/ChangePassword.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth) {
    if (!userStore.isLoggedIn) {
      next('/login')
      return
    }
    
    if (to.meta.requiresAdmin && userStore.user?.role !== 'ADMIN') {
      next('/')
      return
    }
    
    if (to.meta.requiresEmployee && userStore.user?.role !== 'EMPLOYEE') {
      next('/')
      return
    }
  }
  
  next()
})

export default router
