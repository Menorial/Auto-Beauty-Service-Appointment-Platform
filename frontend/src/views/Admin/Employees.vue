<template>
  <div class="admin-employees">
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><UserFilled /></el-icon>
        员工管理
      </h2>
      <el-button type="primary" @click="$router.push('/admin/employees/create')">
        <el-icon><Plus /></el-icon>
        添加员工
      </el-button>
    </div>
    
    <!-- 搜索和筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item>
          <template #label>
            <span class="label-with-icon">
              <el-icon><User /></el-icon>
              员工姓名
            </span>
          </template>
          <el-input
            v-model="searchForm.name"
            placeholder="请输入员工姓名"
            @keyup.enter="handleSearch"
            clearable
          />
        </el-form-item>
        
        <el-form-item>
          <template #label>
            <span class="label-with-icon">
              <el-icon><Phone /></el-icon>
              联系电话
            </span>
          </template>
          <el-input
            v-model="searchForm.phone"
            placeholder="请输入联系电话"
            @keyup.enter="handleSearch"
            clearable
          />
        </el-form-item>
        
        <el-form-item>
          <template #label>
            <span class="label-with-icon">
              <el-icon><SwitchButton /></el-icon>
              员工状态
            </span>
          </template>
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="在职" :value="1" />
            <el-option label="离职" :value="0" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 员工列表 -->
    <el-card>
      <el-table :data="employees" v-loading="loading" stripe>
        <el-table-column type="selection" width="55" />
        
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :src="row.avatarUrl">
              {{ row.name?.charAt(0) }}
            </el-avatar>
          </template>
        </el-table-column>
        
        <el-table-column prop="name" label="姓名" width="120" />
        
        <el-table-column prop="phone" label="联系电话" width="140" />
        
        <el-table-column prop="skills" label="技能专长" min-width="150" />
        
        <el-table-column prop="workSchedule" label="工作时间" width="180">
          <template #default="{ row }">
            {{ row.workSchedule || '09:00-18:00' }}
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '在职' : '离职' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="当前预约" width="100">
          <template #default="{ row }">
            {{ row.currentAppointments || 0 }}
          </template>
        </el-table-column>
        
        <el-table-column prop="createdAt" label="入职时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="editEmployee(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button 
              size="small" 
              type="primary"
              @click="createAccount(row)"
            >
              <el-icon><Plus /></el-icon>
              创建账号
            </el-button>
            <el-button 
              size="small" 
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleStatus(row)"
            >
              <el-icon><SwitchButton /></el-icon>
              {{ row.status === 1 ? '离职' : '复职' }}
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="deleteEmployee(row)"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
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
          @size-change="loadEmployees"
          @current-change="loadEmployees"
        />
      </div>
    </el-card>
    
    <!-- 编辑员工对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑员工" width="600px">
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
      >
        <el-form-item label="员工姓名" prop="name">
          <el-input v-model="editForm.name" />
        </el-form-item>
        
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        
        <el-form-item label="技能专长" prop="skills">
          <el-input
            v-model="editForm.skills"
            type="textarea"
            :rows="3"
            placeholder="请输入技能专长，多个技能用逗号分隔"
          />
        </el-form-item>
        
        <el-form-item label="工作时间" prop="workSchedule">
          <el-input
            v-model="editForm.workSchedule"
            placeholder="例如：09:00-18:00"
          />
        </el-form-item>
        
        <el-form-item label="员工状态">
          <el-radio-group v-model="editForm.status">
            <el-radio :label="1">在职</el-radio>
            <el-radio :label="0">离职</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="头像">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
            accept="image/*"
          >
            <el-avatar :size="80" :src="editForm.avatarUrl">
              {{ editForm.name?.charAt(0) }}
            </el-avatar>
          </el-upload>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="updateEmployee" :loading="updating">
          保存
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 创建员工账号对话框 -->
    <el-dialog v-model="showAccountDialog" title="创建员工账号" width="500px">
      <el-form :model="accountForm" label-width="100px">
        <el-form-item label="员工姓名">
          <el-input :value="currentEmployee?.name" disabled />
        </el-form-item>
        
        <el-form-item label="用户名" required>
          <el-input 
            v-model="accountForm.username" 
            placeholder="请输入用户名"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="密码" required>
          <el-input 
            v-model="accountForm.password" 
            type="password"
            placeholder="请输入密码"
            show-password
            clearable
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showAccountDialog = false">取消</el-button>
        <el-button type="primary" @click="submitCreateAccount" :loading="creatingAccount">
          创建账号
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getAllEmployees, updateEmployee as apiUpdateEmployee, deleteEmployee as apiDeleteEmployee } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, UserFilled, User, Phone, SwitchButton, RefreshLeft } from '@element-plus/icons-vue'
import { formatDateTime } from '@/utils/dateUtils'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const updating = ref(false)
const showEditDialog = ref(false)

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const employees = ref([])
const editingEmployee = ref(null)

const searchForm = reactive({
  name: '',
  phone: '',
  status: ''
})

const editFormRef = ref()
const editForm = reactive({
  name: '',
  phone: '',
  skills: '',
  workSchedule: '',
  status: 1,
  avatarUrl: ''
})

const uploadUrl = `${process.env.VUE_APP_API_BASE_URL}/upload/avatar`
const uploadHeaders = {
  'Authorization': `Bearer ${userStore.token}`
}

const editRules = {
  name: [
    { required: true, message: '请输入员工姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  skills: [
    { required: true, message: '请输入技能专长', trigger: 'blur' }
  ]
}

const loadEmployees = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      ...searchForm
    }
    
    const response = await getAllEmployees(params)
    if (response.success) {
      employees.value = response.data.content || response.data
      total.value = response.data.totalElements || response.data.length
    }
  } catch (error) {
    console.error('加载员工列表失败:', error)
    ElMessage.error('加载员工列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadEmployees()
}

const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  handleSearch()
}

const editEmployee = (employee) => {
  editingEmployee.value = employee
  Object.keys(editForm).forEach(key => {
    editForm[key] = employee[key] || ''
  })
  showEditDialog.value = true
}

const updateEmployee = async () => {
  if (!editFormRef.value) return
  
  try {
    await editFormRef.value.validate()
    updating.value = true
    
    const response = await apiUpdateEmployee(editingEmployee.value.id, editForm)
    if (response.success) {
      ElMessage.success('员工信息更新成功')
      showEditDialog.value = false
      await loadEmployees()
    }
  } catch (error) {
    console.error('更新员工信息失败:', error)
    ElMessage.error(error.message || '更新员工信息失败')
  } finally {
    updating.value = false
  }
}

const toggleStatus = async (employee) => {
  try {
    const newStatus = employee.status === 1 ? 0 : 1
    const action = newStatus === 1 ? '复职' : '离职'
    
    await ElMessageBox.confirm(
      `确定要将员工 ${employee.name} 设为${action}状态吗？`,
      `${action}确认`,
      {
        confirmButtonText: `确定${action}`,
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await apiUpdateEmployee(employee.id, { status: newStatus })
    if (response.success) {
      ElMessage.success(`员工已${action}`)
      await loadEmployees()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新员工状态失败:', error)
      ElMessage.error('更新员工状态失败')
    }
  }
}

const deleteEmployee = async (employee) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除员工 "${employee.name}" 吗？此操作不可恢复。`,
      '删除员工',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await apiDeleteEmployee(employee.id)
    if (response.success) {
      ElMessage.success('员工删除成功')
      await loadEmployees()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除员工失败:', error)
      ElMessage.error(error.message || '删除员工失败')
    }
  }
}

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleAvatarSuccess = (response) => {
  if (response.success) {
    // 直接使用Base64数据
    editForm.avatarUrl = response.data
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 创建员工账号相关
const showAccountDialog = ref(false)
const accountForm = reactive({
  username: '',
  password: ''
})
const currentEmployee = ref(null)
const creatingAccount = ref(false)

const createAccount = (employee) => {
  currentEmployee.value = employee
  accountForm.username = `employee${employee.id}`
  accountForm.password = '123456'
  showAccountDialog.value = true
}

const submitCreateAccount = async () => {
  try {
    if (!accountForm.username) {
      ElMessage.error('请输入用户名')
      return
    }
    if (!accountForm.password) {
      ElMessage.error('请输入密码')
      return
    }
    
    creatingAccount.value = true
    const response = await fetch(`${process.env.VUE_APP_API_BASE_URL}/admin/employees/${currentEmployee.value.id}/create-account`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${userStore.token}`
      },
      body: JSON.stringify({
        username: accountForm.username,
        password: accountForm.password
      })
    })
    
    const data = await response.json()
    if (data.success) {
      ElMessage.success('员工账号创建成功')
      showAccountDialog.value = false
    } else {
      ElMessage.error(data.message || '创建账号失败')
    }
  } catch (error) {
    console.error('创建账号失败:', error)
    ElMessage.error('创建账号失败')
  } finally {
    creatingAccount.value = false
  }
}

onMounted(() => {
  loadEmployees()
})
</script>

<style lang="scss" scoped>
.admin-employees {
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
  }
  
  .search-card {
    margin-bottom: 24px;
  }
  
  .pagination {
    display: flex;
    justify-content: center;
    margin-top: 24px;
  }
}

.label-with-icon {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.el-button :deep(.el-icon) {
  margin-right: 6px;
}

</style>
