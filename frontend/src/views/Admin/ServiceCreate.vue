<template>
  <div class="service-create">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title-with-icon">
            <el-icon><Service /></el-icon>
            {{ isEdit ? '编辑服务' : '创建服务' }}
          </span>
          <el-button @click="$router.go(-1)">
            <el-icon><Back /></el-icon>
            返回
          </el-button>
        </div>
      </template>
      
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="服务名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入服务名称" />
        </el-form-item>
        
        <el-form-item label="服务描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入服务描述" />
        </el-form-item>
        
        <el-form-item label="服务价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="10" />
        </el-form-item>
        
        <el-form-item label="服务时长" prop="duration">
          <el-input-number v-model="form.duration" :min="15" :step="15" />
          <span style="margin-left: 10px;">分钟</span>
        </el-form-item>
        
        <el-form-item label="服务分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择服务分类">
            <el-option label="外观美容" value="exterior" />
            <el-option label="内饰清洁" value="interior" />
            <el-option label="深度养护" value="maintenance" />
            <el-option label="维修服务" value="repair" />
            <el-option label="套餐服务" value="package" />
          </el-select>
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
            <img v-if="form.imageUrl" :src="form.imageUrl" class="service-image" />
            <div v-else class="image-placeholder">
              <el-icon class="image-uploader-icon"><Plus /></el-icon>
              <div class="upload-text">点击上传服务图片</div>
            </div>
          </el-upload>
          <div class="upload-tip">建议上传 800x600 像素的图片，展示服务效果</div>
        </el-form-item>
        
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            <el-icon><Check /></el-icon>
            {{ isEdit ? '更新' : '创建' }}
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Service, Back, Check, RefreshLeft } from '@element-plus/icons-vue'
import { createService, updateService, getServiceById } from '@/api/service'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const loading = ref(false)
const isEdit = ref(false)

const form = reactive({
  name: '',
  description: '',
  price: 0,
  duration: 60,
  category: '',
  imageUrl: '',
  sortOrder: 0,
  status: 1
})

const userStore = useUserStore()
const uploadUrl = `${process.env.VUE_APP_API_BASE_URL}/upload/service`
const uploadHeaders = {
  'Authorization': `Bearer ${userStore.token}`
}

const rules = {
  name: [{ required: true, message: '请输入服务名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入服务描述', trigger: 'blur' }],
  price: [{ required: true, message: '请输入服务价格', trigger: 'blur' }],
  duration: [{ required: true, message: '请输入服务时长', trigger: 'blur' }],
  category: [{ required: true, message: '请选择服务分类', trigger: 'change' }]
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    
    if (isEdit.value) {
      await updateService(route.params.id, form)
      ElMessage.success('服务更新成功')
    } else {
      await createService(form)
      ElMessage.success('服务创建成功')
    }
    
    router.push('/admin/services')
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  formRef.value.resetFields()
}

const fetchServiceDetail = async () => {
  try {
    const response = await getServiceById(route.params.id)
    Object.assign(form, response.data)
  } catch (error) {
    console.error('获取服务详情失败:', error)
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
    form.imageUrl = response.data
    ElMessage.success('服务图片上传成功')
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

onMounted(() => {
  if (route.params.id) {
    isEdit.value = true
    fetchServiceDetail()
  }
})
</script>

<style scoped>
.service-create {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .title-with-icon {
    display: inline-flex;
    align-items: center;
    gap: 8px;
  }
}

.service-image-uploader {
  display: inline-block;
}

.service-image-uploader .service-image {
  width: 300px;
  height: 200px;
  display: block;
  border-radius: 8px;
  object-fit: cover;
  cursor: pointer;
  transition: all 0.3s;
}

.service-image-uploader .service-image:hover {
  opacity: 0.8;
  transform: scale(1.02);
}

.image-placeholder {
  width: 300px;
  height: 200px;
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
  font-size: 48px;
  color: #8c939d;
}

.upload-text {
  margin-top: 12px;
  font-size: 14px;
  color: #8c939d;
  font-weight: 500;
}

.upload-tip {
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}
</style>
