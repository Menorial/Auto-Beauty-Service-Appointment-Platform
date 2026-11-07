<template>
  <div class="admin-services">
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Service /></el-icon>
        服务管理
      </h2>
      <el-button type="primary" @click="$router.push('/admin/services/create')">
        <el-icon><Plus /></el-icon>
        添加服务
      </el-button>
    </div>
    
    <!-- 搜索和筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="服务名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入服务名称"
            @keyup.enter="handleSearch"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="服务分类">
          <el-select v-model="searchForm.category" placeholder="请选择分类" clearable>
            <el-option label="外观美容" value="外观美容" />
            <el-option label="内饰清洁" value="内饰清洁" />
            <el-option label="深度养护" value="深度养护" />
            <el-option label="维修服务" value="维修服务" />
            <el-option label="套餐服务" value="套餐服务" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="服务状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
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
    
    <!-- 服务列表 -->
    <el-card>
      <el-table :data="services" v-loading="loading" stripe>
        <el-table-column type="selection" width="55" />
        
        <el-table-column label="服务图片" width="100">
          <template #default="{ row }">
            <el-image
              :src="row.imageUrl || '/images/default-service.jpg'"
              :preview-src-list="[row.imageUrl || '/images/default-service.jpg']"
              fit="cover"
              style="width: 60px; height: 60px; border-radius: 8px;"
            />
          </template>
        </el-table-column>
        
        <el-table-column prop="name" label="服务名称" min-width="150" />
        
        <el-table-column prop="category" label="分类" width="120">
          <template #default="{ row }">
            {{ getCategoryText(row.category) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        
        <el-table-column prop="duration" label="时长" width="100">
          <template #default="{ row }">
            {{ row.duration }}分钟
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="editService(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button 
              size="small" 
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleStatus(row)"
            >
              <el-icon><SwitchButton /></el-icon>
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="deleteService(row)"
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
          @size-change="loadServices"
          @current-change="loadServices"
        />
      </div>
    </el-card>
    
    <!-- 编辑服务对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑服务" width="600px">
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
      >
        <el-form-item label="服务名称" prop="name">
          <el-input v-model="editForm.name" />
        </el-form-item>
        
        <el-form-item label="服务分类" prop="category">
          <el-select v-model="editForm.category" style="width: 100%">
            <el-option label="外观美容" value="外观美容" />
            <el-option label="内饰清洁" value="内饰清洁" />
            <el-option label="深度养护" value="深度养护" />
            <el-option label="套餐服务" value="套餐服务" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="服务价格" prop="price">
          <el-input-number v-model="editForm.price" :min="0" :precision="2" />
        </el-form-item>
        
        <el-form-item label="服务时长" prop="duration">
          <el-input-number v-model="editForm.duration" :min="1" />
          <span style="margin-left: 8px;">分钟</span>
        </el-form-item>
        
        <el-form-item label="服务描述" prop="description">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="4"
          />
        </el-form-item>
        
        <el-form-item label="服务图片">
          <el-upload
            class="service-image-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleImageSuccess"
            :before-upload="beforeImageUpload"
            accept="image/*"
          >
            <img v-if="editForm.imageUrl" :src="editForm.imageUrl" class="service-image" />
            <div v-else class="image-placeholder">
              <el-icon class="image-uploader-icon"><Plus /></el-icon>
              <div class="upload-text">点击上传图片</div>
            </div>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="服务状态">
          <el-radio-group v-model="editForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="updateService" :loading="updating">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAllServices, updateService as apiUpdateService, deleteService as apiDeleteService } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, Service, RefreshLeft, SwitchButton } from '@element-plus/icons-vue'
import { formatDateTime } from '@/utils/dateUtils'
import { useUserStore } from '@/stores/user'

const router = useRouter()

const loading = ref(false)
const updating = ref(false)
const showEditDialog = ref(false)

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const services = ref([])
const editingService = ref(null)

const searchForm = reactive({
  name: '',
  category: '',
  status: ''
})

const editFormRef = ref()
const editForm = reactive({
  name: '',
  category: '',
  price: 0,
  duration: 60,
  description: '',
  imageUrl: '',
  status: 1
})

const userStore = useUserStore()
const uploadUrl = `${process.env.VUE_APP_API_BASE_URL}/upload/service`
const uploadHeaders = {
  'Authorization': `Bearer ${userStore.token}`
}

const editRules = {
  name: [
    { required: true, message: '请输入服务名称', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择服务分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入服务价格', trigger: 'blur' }
  ],
  duration: [
    { required: true, message: '请输入服务时长', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入服务描述', trigger: 'blur' }
  ]
}

const getCategoryText = (category) => {
  const categoryMap = {
    'exterior': '外观美容',
    'interior': '内饰清洁',
    'maintenance': '深度养护',
    'repair': '维修服务',
    'package': '套餐服务'
  }
  return categoryMap[category] || category
}

// formatDateTime 函数已从 @/utils/dateUtils 导入

const loadServices = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      ...searchForm
    }
    
    const response = await getAllServices(params)
    if (response.success) {
      services.value = response.data.content || response.data
      total.value = response.data.totalElements || response.data.length
    }
  } catch (error) {
    console.error('加载服务列表失败:', error)
    ElMessage.error('加载服务列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadServices()
}

const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  // 删除是软删除（status=0），重置后仍可能把禁用项查出来。
  // 这里重置时默认只展示启用项，避免“删除成功但重置又看见”。
  searchForm.status = 1
  handleSearch()
}

const editService = (service) => {
  editingService.value = service
  Object.keys(editForm).forEach(key => {
    editForm[key] = service[key] || ''
  })
  showEditDialog.value = true
}

const updateService = async () => {
  if (!editFormRef.value) return
  
  try {
    await editFormRef.value.validate()
    updating.value = true
    
    const response = await apiUpdateService(editingService.value.id, editForm)
    if (response.success) {
      ElMessage.success('服务更新成功')
      showEditDialog.value = false
      await loadServices()
    }
  } catch (error) {
    console.error('更新服务失败:', error)
    ElMessage.error(error.message || '更新服务失败')
  } finally {
    updating.value = false
  }
}

const toggleStatus = async (service) => {
  try {
    const newStatus = service.status === 1 ? 0 : 1
    const response = await apiUpdateService(service.id, { status: newStatus })
    
    if (response.success) {
      ElMessage.success(`服务已${newStatus === 1 ? '启用' : '禁用'}`)
      await loadServices()
    }
  } catch (error) {
    console.error('更新服务状态失败:', error)
    ElMessage.error('更新服务状态失败')
  }
}

const deleteService = async (service) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除服务 "${service.name}" 吗？此操作不可恢复。`,
      '删除服务',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await apiDeleteService(service.id)
    if (response.success) {
      ElMessage.success('服务删除成功')
      await loadServices()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除服务失败:', error)
      ElMessage.error(error.message || '删除服务失败')
    }
  }
}

// 图片上传相关
const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

const handleImageSuccess = (response) => {
  if (response.success) {
    // 直接使用Base64数据
    editForm.imageUrl = response.data
    ElMessage.success('服务图片上传成功')
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

onMounted(() => {
  loadServices()
})
</script>

<style lang="scss" scoped>
.admin-services {
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

.el-button :deep(.el-icon) {
  margin-right: 6px;
}

.service-image-uploader {
  display: inline-block;
}

.service-image-uploader .service-image {
  width: 200px;
  height: 150px;
  display: block;
  border-radius: 8px;
  object-fit: cover;
  cursor: pointer;
  transition: all 0.3s;
}

.service-image-uploader .service-image:hover {
  opacity: 0.8;
}

.image-placeholder {
  width: 200px;
  height: 150px;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
  background-color: #fafafa;
}

.image-placeholder:hover {
  border-color: #409eff;
  background-color: #f5f7fa;
}

.image-uploader-icon {
  font-size: 36px;
  color: #8c939d;
}

.upload-text {
  margin-top: 8px;
  font-size: 13px;
  color: #8c939d;
}
</style>
