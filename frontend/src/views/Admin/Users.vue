<template>
  <div class="admin-users">
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><UserFilled /></el-icon>
        用户管理
      </h2>
      <div class="header-actions">
        <el-button type="success" size="large" @click="exportUsers" :icon="Download">
          导出Excel数据
        </el-button>
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ stats.total }}</div>
          <div class="stat-label">总用户数</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ stats.active }}</div>
          <div class="stat-label">活跃用户</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ stats.newToday }}</div>
          <div class="stat-label">今日新增</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ stats.totalPoints }}</div>
          <div class="stat-label">总积分</div>
        </div>
      </el-card>
    </div>
    
    <!-- 搜索和筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="用户名">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入用户名"
            @keyup.enter="handleSearch"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="真实姓名">
          <el-input
            v-model="searchForm.realName"
            placeholder="请输入真实姓名"
            @keyup.enter="handleSearch"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="联系电话">
          <el-input
            v-model="searchForm.phone"
            placeholder="请输入联系电话"
            @keyup.enter="handleSearch"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="用户状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="正常" value="ACTIVE" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="注册时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 用户列表 -->
    <el-card>
      <el-table :data="users" v-loading="loading" stripe>
        <el-table-column type="selection" width="55" />
        
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :src="row.avatarUrl">
              {{ row.username?.charAt(0)?.toUpperCase() }}
            </el-avatar>
          </template>
        </el-table-column>
        
        <el-table-column prop="username" label="用户名" width="120" />
        
        <el-table-column prop="realName" label="真实姓名" width="120">
          <template #default="{ row }">
            {{ row.realName || '-' }}
          </template>
        </el-table-column>
        
        <el-table-column prop="email" label="邮箱" min-width="180">
          <template #default="{ row }">
            {{ row.email || '-' }}
          </template>
        </el-table-column>
        
        <el-table-column prop="phone" label="联系电话" width="140">
          <template #default="{ row }">
            {{ row.phone || '-' }}
          </template>
        </el-table-column>
        
        <el-table-column prop="points" label="积分" width="100">
          <template #default="{ row }">
            {{ row.points || 0 }}
          </template>
        </el-table-column>
        
        <el-table-column label="车辆数" width="80">
          <template #default="{ row }">
            {{ row.vehicleCount || 0 }}
          </template>
        </el-table-column>
        
        <el-table-column label="预约数" width="80">
          <template #default="{ row }">
            {{ row.appointmentCount || 0 }}
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ row.status === 'ACTIVE' ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="createdAt" label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-dropdown @command="(command) => handleAction(command, row)">
              <el-button size="small">
                操作 <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="view">查看详情</el-dropdown-item>
                  <el-dropdown-item command="edit">编辑信息</el-dropdown-item>
                  <el-dropdown-item command="points">积分管理</el-dropdown-item>
                  <el-dropdown-item 
                    :command="row.status === 'ACTIVE' ? 'disable' : 'enable'"
                    divided
                  >
                    {{ row.status === 'ACTIVE' ? '禁用用户' : '启用用户' }}
                  </el-dropdown-item>
                  <el-dropdown-item command="resetPassword">重置密码</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadUsers"
          @current-change="loadUsers"
        />
      </div>
    </el-card>
    
    <!-- 用户/员工详情对话框 -->
    <el-dialog v-model="showDetailDialog" :title="detailTitle" width="800px">
      <div v-if="selectedUser">
        <!-- 员工详情：users(role=EMPLOYEE) -> employees 表 -->
        <div v-if="selectedUser.role === 'EMPLOYEE' && selectedEmployee">
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="user-avatar">
                <el-avatar
                  :size="120"
                  :src="selectedEmployee.avatarUrl || selectedUser.avatarUrl"
                >
                  {{ selectedEmployee.name?.charAt(0)?.toUpperCase() }}
                </el-avatar>
              </div>
            </el-col>
            <el-col :span="16">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="员工姓名">
                  {{ selectedEmployee.name || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="联系电话">
                  {{ selectedEmployee.phone || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="技能专长">
                  {{ selectedEmployee.skills || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="工作时间">
                  {{ selectedEmployee.workSchedule || selectedEmployee.work_schedule || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="员工状态">
                  <el-tag :type="selectedEmployee.status === 1 ? 'success' : 'danger'">
                    {{ selectedEmployee.status === 1 ? '在职' : '离职' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="入职时间">
                  {{ formatDateTime(selectedEmployee.createdAt) }}
                </el-descriptions-item>
              </el-descriptions>
            </el-col>
          </el-row>
        </div>

        <!-- 用户详情：普通用户/管理员 -->
        <div v-else>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="user-avatar">
                <el-avatar :size="120" :src="selectedUser.avatarUrl">
                  {{ selectedUser.username?.charAt(0)?.toUpperCase() }}
                </el-avatar>
              </div>
            </el-col>
            <el-col :span="16">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="用户名">
                  {{ selectedUser.username }}
                </el-descriptions-item>
                <el-descriptions-item label="真实姓名">
                  {{ selectedUser.realName || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="邮箱">
                  {{ selectedUser.email || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="联系电话">
                  {{ selectedUser.phone || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="积分">
                  {{ selectedUser.points || 0 }}
                </el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag :type="selectedUser.status === 'ACTIVE' ? 'success' : 'danger'">
                    {{ selectedUser.status === 'ACTIVE' ? '正常' : '禁用' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="注册时间">
                  {{ formatDateTime(selectedUser.createdAt) }}
                </el-descriptions-item>
                <el-descriptions-item label="最后登录">
                  {{ formatDateTime(selectedUser.lastLoginAt) || '-' }}
                </el-descriptions-item>
              </el-descriptions>
            </el-col>
          </el-row>
          
          <!-- 用户统计信息 -->
          <el-divider>统计信息</el-divider>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-statistic title="车辆数量" :value="selectedUser.vehicleCount || 0" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="预约次数" :value="selectedUser.appointmentCount || 0" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="评价数量" :value="selectedUser.reviewCount || 0" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="消费金额" :value="selectedUser.totalAmount || 0" prefix="¥" />
            </el-col>
          </el-row>
        </div>
      </div>
    </el-dialog>
    
    <!-- 编辑用户对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑用户" width="600px">
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" disabled />
        </el-form-item>
        
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="editForm.realName" />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" />
        </el-form-item>
        
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        
        <el-form-item label="用户状态">
          <el-radio-group v-model="editForm.status">
            <el-radio label="ACTIVE">正常</el-radio>
            <el-radio label="DISABLED">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="updateUser" :loading="updating">
          保存
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 积分管理对话框 -->
    <el-dialog v-model="showPointsDialog" title="积分管理" width="500px">
      <el-form :model="pointsForm" label-width="100px">
        <el-form-item label="当前积分">
          <el-input :value="selectedUser?.points || 0" disabled />
        </el-form-item>
        
        <el-form-item label="操作类型">
          <el-radio-group v-model="pointsForm.type">
            <el-radio label="ADD">增加积分</el-radio>
            <el-radio label="SUBTRACT">扣除积分</el-radio>
            <el-radio label="SET">设置积分</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="积分数量">
          <el-input-number
            v-model="pointsForm.amount"
            :min="0"
            :max="99999"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="操作原因">
          <el-input
            v-model="pointsForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入操作原因"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showPointsDialog = false">取消</el-button>
        <el-button type="primary" @click="updatePoints" :loading="updatingPoints">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { 
  getAllUsers,
  getUserById,
  getAdminStats,
  updateUser as apiUpdateUser,
  updateUserStatus, 
  resetUserPassword, 
  updateUserPoints
} from '@/api/admin'
import { getEmployeeById } from '@/api/employee'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Edit, Delete, Refresh, Lock, Trophy, UserFilled } from '@element-plus/icons-vue'
import { formatDateTime } from '@/utils/dateUtils'

const loading = ref(false)
const updating = ref(false)
const updatingPoints = ref(false)
const showDetailDialog = ref(false)
const showEditDialog = ref(false)
const showPointsDialog = ref(false)

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const users = ref([])
const selectedUser = ref(null)
const selectedEmployee = ref(null)

const stats = ref({
  total: 0,
  active: 0,
  newToday: 0,
  totalPoints: 0
})

const searchForm = reactive({
  username: '',
  realName: '',
  phone: '',
  status: '',
  dateRange: []
})

const editFormRef = ref()
const editForm = reactive({
  username: '',
  realName: '',
  email: '',
  phone: '',
  status: 'ACTIVE'
})

const pointsForm = reactive({
  type: 'ADD',
  amount: 0,
  reason: ''
})

const detailTitle = computed(() => {
  if (!selectedUser.value) return '详情'
  return selectedUser.value.role === 'EMPLOYEE' ? '员工详情' : '用户详情'
})

const editRules = {
  realName: [
    { max: 50, message: '真实姓名不能超过50个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ]
}

// formatDateTime 函数已从 @/utils/dateUtils 导入

const loadUsers = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      ...searchForm
    }
    
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    
    const response = await getAllUsers(params)
    if (response.success) {
      users.value = response.data.content || response.data
      total.value = response.data.totalElements || response.data.length
      
      // 加载真实的统计数据
      await loadStats()
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

// 加载真实的统计数据（从数据库查询）
const loadStats = async () => {
  try {
    const response = await getAdminStats()
    if (response && response.success && response.data) {
      stats.value = {
        total: response.data.totalUsers || 0,
        active: response.data.activeUsers || 0,
        newToday: response.data.todayNewUsers || 0,
        totalPoints: response.data.totalPoints || 0
      }
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadUsers()
}

const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = key === 'dateRange' ? [] : ''
  })
  handleSearch()
}

const handleAction = async (command, user) => {
  selectedUser.value = user
  
  switch (command) {
    case 'view':
      // 获取完整的用户详情（包含统计信息）
      try {
        const response = await getUserById(user.id)
        if (response.success) {
          selectedUser.value = response.data
        }
      } catch (error) {
        console.error('获取用户详情失败:', error)
        ElMessage.error('获取用户详情失败')
      }

      // 员工：拉取 employees 表详情，展示员工字段
      selectedEmployee.value = null
      if (selectedUser.value?.role === 'EMPLOYEE' && selectedUser.value?.employeeId) {
        try {
          const empResp = await getEmployeeById(selectedUser.value.employeeId)
          if (empResp.success) {
            selectedEmployee.value = empResp.data
          } else {
            ElMessage.error(empResp.message || '获取员工详情失败')
          }
        } catch (e) {
          console.error('获取员工详情失败:', e)
          ElMessage.error('获取员工详情失败')
        }
      }

      showDetailDialog.value = true
      break
    case 'edit':
      Object.keys(editForm).forEach(key => {
        editForm[key] = user[key] || ''
      })
      showEditDialog.value = true
      break
    case 'points':
      pointsForm.type = 'ADD'
      pointsForm.amount = 0
      pointsForm.reason = ''
      showPointsDialog.value = true
      break
    case 'enable':
    case 'disable':
      await toggleUserStatus(user, command === 'enable' ? 'ACTIVE' : 'DISABLED')
      break
    case 'resetPassword':
      await resetPassword(user)
      break
  }
}

const updateUser = async () => {
  if (!editFormRef.value) return
  
  try {
    await editFormRef.value.validate()
    updating.value = true
    
    const response = await apiUpdateUser(selectedUser.value.id, editForm)
    if (response.success) {
      ElMessage.success('用户信息更新成功')
      showEditDialog.value = false
      await loadUsers()
    }
  } catch (error) {
    console.error('更新用户信息失败:', error)
    ElMessage.error(error.message || '更新用户信息失败')
  } finally {
    updating.value = false
  }
}

const toggleUserStatus = async (user, status) => {
  try {
    const action = status === 'ACTIVE' ? '启用' : '禁用'
    
    await ElMessageBox.confirm(
      `确定要${action}用户 "${user.username}" 吗？`,
      `${action}用户`,
      {
        confirmButtonText: `确定${action}`,
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await updateUserStatus(user.id, status)
    if (response.success) {
      ElMessage.success(`用户已${action}`)
      await loadUsers()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新用户状态失败:', error)
      ElMessage.error('更新用户状态失败')
    }
  }
}

const resetPassword = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要重置用户 "${user.username}" 的密码吗？系统将自动生成一个新密码`,
      '重置密码',
      {
        confirmButtonText: '确定重置',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await resetUserPassword(user.id, '123456')
    if (response.success) {
      const newPassword = response.data?.password || '123456'
      await ElMessageBox.alert(
        `密码重置成功！\n\n新密码为：${newPassword}\n\n请复制并告知用户，用户首次登录后建议立即修改密码。`,
        '密码重置成功',
        {
          confirmButtonText: '我已复制',
          type: 'success',
          dangerouslyUseHTMLString: false
        }
      )
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置密码失败:', error)
      ElMessage.error('重置密码失败')
    }
  }
}

const updatePoints = async () => {
  if (!pointsForm.amount || !pointsForm.reason) {
    ElMessage.warning('请填写积分数量和操作原因')
    return
  }
  
  try {
    updatingPoints.value = true
    
    const response = await updateUserPoints(selectedUser.value.id, {
      type: pointsForm.type,
      amount: pointsForm.amount,
      reason: pointsForm.reason
    })
    
    if (response.success) {
      ElMessage.success('积分更新成功')
      showPointsDialog.value = false
      await loadUsers()
    }
  } catch (error) {
    console.error('更新积分失败:', error)
    ElMessage.error('更新积分失败')
  } finally {
    updatingPoints.value = false
  }
}

const exportUsers = () => {
  try {
    // 检查是否有数据
    if (!users.value || users.value.length === 0) {
      ElMessage.warning('暂无数据可导出')
      return
    }
    
    // 准备导出数据
    const exportList = users.value.map((user, index) => {
      // 格式化注册时间
      let createdAt = '-'
      if (user.createdAt) {
        const timeStr = formatDateTime(user.createdAt)
        // 添加制表符前缀，让Excel识别为文本
        createdAt = '\t' + timeStr
      }
      
      return {
        '序号': index + 1,
        '用户名': user.username || '-',
        '真实姓名': user.realName || '-',
        '邮箱': user.email || '-',
        '联系电话': user.phone || '-',
        '性别': user.gender === 1 ? '男' : user.gender === 2 ? '女' : '未知',
        '地址': user.address || '-',
        '积分': user.points || 0,
        '车辆数量': user.vehicleCount || 0,
        '预约次数': user.appointmentCount || 0,
        '用户状态': user.status === 'ACTIVE' ? '正常' : '禁用',
        '角色': user.role === 'ADMIN' ? '管理员' : '普通用户',
        '注册时间': createdAt
      }
    })
    
    // 转换为CSV格式
    const headers = Object.keys(exportList[0])
    const csvContent = [
      headers.join(','),
      ...exportList.map(row => headers.map(header => {
        let value = String(row[header] || '')
        // 处理包含逗号、换行或引号的值，用引号包裹并转义引号
        if (value.includes(',') || value.includes('\n') || value.includes('"')) {
          value = '"' + value.replace(/"/g, '""') + '"'
        }
        return value
      }).join(','))
    ].join('\n')
    
    // 添加BOM以支持中文
    const BOM = '\uFEFF'
    const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
    
    // 创建下载链接
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    const fileName = `用户数据_${new Date().toISOString().split('T')[0]}.csv`
    link.download = fileName
    link.click()
    
    // 清理URL对象
    URL.revokeObjectURL(link.href)
    
    ElMessage.success(`已导出 ${exportList.length} 条用户数据`)
  } catch (error) {
    console.error('导出用户数据失败:', error)
    ElMessage.error('导出用户数据失败：' + error.message)
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style lang="scss" scoped>
.admin-users {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: $text-primary;
      margin: 0;
      display: flex;
      align-items: center;
      gap: 8px;
    }
    
    .header-actions {
      display: flex;
      gap: 12px;
    }
  }
  
  .stats-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 16px;
    margin-bottom: 24px;
    
    .stat-card {
      text-align: center;
      
      .stat-content {
        .stat-number {
          font-size: 32px;
          font-weight: 700;
          color: $primary-color;
          margin-bottom: 8px;
        }
        
        .stat-label {
          color: $text-secondary;
          font-size: 14px;
        }
      }
    }
  }
  
  .search-card {
    margin-bottom: 24px;
  }
  
  .pagination {
    display: flex;
    justify-content: center;
    margin-top: 24px;
  }
  
  .user-avatar {
    text-align: center;
    margin-bottom: 20px;
  }
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
}
</style>
