<template>
  <div class="reviews-page">
    <Header />
    
    <div class="container">
      <div class="page-header">
        <h1>我的评价</h1>
        <p>查看和管理您的服务评价</p>
      </div>
      
      <div class="reviews-content">
        <div class="reviews-list" v-loading="loading">
          <div v-if="reviews.length > 0" class="reviews-grid">
            <el-card 
              v-for="review in reviews" 
              :key="review.id"
              class="review-card"
            >
              <div class="review-header">
                <div class="service-info">
                  <h3>{{ review.service?.name }}</h3>
                  <p class="appointment-info">
                    预约时间：{{ formatDateTime(review.appointment?.appointmentTime) }}
                  </p>
                </div>
                <div class="review-rating">
                  <el-rate v-model="review.rating" disabled />
                  <span class="rating-text">{{ review.rating }}分</span>
                </div>
              </div>
              
              <div class="review-content">
                <p>{{ review.comment }}</p>
                <div class="review-images" v-if="review.images">
                  <img 
                    v-for="(image, index) in review.images.split(',')" 
                    :key="index"
                    :src="image" 
                    @click="previewImage(image)"
                    class="review-image"
                  />
                </div>
              </div>
              
              <div class="review-reply" v-if="review.reply">
                <div class="reply-header">
                  <el-icon><Service /></el-icon>
                  <span>商家回复：</span>
                </div>
                <p>{{ review.reply }}</p>
                <span class="reply-time">{{ formatDateTime(review.replyTime) }}</span>
              </div>
              
              <div class="review-footer">
                <span class="review-time">{{ formatDateTime(review.createdAt) }}</span>
                <div class="review-actions">
                  <el-button size="small" @click="editReview(review)">
                    编辑
                  </el-button>
                  <el-button 
                    type="danger" 
                    size="small" 
                    @click="deleteReview(review)"
                  >
                    删除
                  </el-button>
                </div>
              </div>
            </el-card>
          </div>
          
          <el-empty v-else description="暂无评价记录">
            <el-button type="primary" @click="$router.push('/appointments')">
              查看我的预约
            </el-button>
          </el-empty>
        </div>
        
        <!-- 分页 -->
        <div class="pagination" v-if="total > 0">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="loadReviews"
          />
        </div>
      </div>
    </div>
    
    <Footer />
    
    <!-- 编辑评价对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑评价" width="600px">
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="80px"
      >
        <el-form-item label="服务评分" prop="rating">
          <el-rate v-model="editForm.rating" show-text />
        </el-form-item>
        
        <el-form-item label="评价内容" prop="comment">
          <el-input
            v-model="editForm.comment"
            type="textarea"
            :rows="4"
            placeholder="请分享您的服务体验..."
          />
        </el-form-item>
        
        <el-form-item label="评价图片">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            list-type="picture-card"
            :file-list="editForm.imageList"
            :on-success="handleImageSuccess"
            :on-error="handleImageError"
            :on-remove="handleImageRemove"
            :before-upload="beforeImageUpload"
            accept="image/*"
            :limit="3"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="updateReview" :loading="updating">
          保存修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import Header from '@/components/Layout/Header.vue'
import Footer from '@/components/Layout/Footer.vue'
import { getUserReviews, updateReview as apiUpdateReview, deleteReview as apiDeleteReview } from '@/api/reviews'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Delete, Star } from '@element-plus/icons-vue'
import { formatDateTime } from '@/utils/dateUtils'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const updating = ref(false)
const showEditDialog = ref(false)

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const reviews = ref([])
const editingReview = ref(null)

const editFormRef = ref()
const editForm = reactive({
  rating: 5,
  comment: '',
  images: [],
  imageList: []
})

const uploadUrl = `${process.env.VUE_APP_API_BASE_URL}/upload/review`
const uploadHeaders = {
  'Authorization': `Bearer ${userStore.token}`
}

const editRules = {
  rating: [
    { required: true, message: '请选择评分', trigger: 'change' }
  ],
  comment: [
    { required: true, message: '请输入评价内容', trigger: 'blur' },
    { min: 10, message: '评价内容不能少于10个字符', trigger: 'blur' }
  ]
}

// formatDateTime 函数已从 @/utils/dateUtils 导入

const loadReviews = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    
    if (!userStore.user?.id) {
      ElMessage.error('无法获取用户信息，请重新登录')
      return
    }
    
    const response = await getUserReviews(userStore.user.id, currentPage.value - 1, pageSize.value)
    if (response.success) {
      reviews.value = response.data.content || response.data
      total.value = response.data.totalElements || response.data.length
    }
  } catch (error) {
    console.error('加载评价列表失败:', error)
    ElMessage.error('加载评价列表失败')
  } finally {
    loading.value = false
  }
}

const editReview = (review) => {
  editingReview.value = review
  editForm.rating = review.rating
  editForm.comment = review.comment
  editForm.images = review.images ? review.images.split(',') : []
  editForm.imageList = editForm.images.map((url, index) => ({
    uid: index,
    name: `image${index}`,
    status: 'done',
    url: url
  }))
  showEditDialog.value = true
}

const updateReview = async () => {
  if (!editFormRef.value) return
  
  try {
    await editFormRef.value.validate()
    updating.value = true
    
    const reviewData = {
      rating: editForm.rating,
      comment: editForm.comment,
      images: editForm.images.join(',')
    }
    
    const response = await apiUpdateReview(editingReview.value.id, reviewData)
    if (response.success) {
      ElMessage.success('评价更新成功')
      showEditDialog.value = false
      await loadReviews()
    }
  } catch (error) {
    console.error('更新评价失败:', error)
    ElMessage.error(error.message || '更新评价失败')
  } finally {
    updating.value = false
  }
}

const deleteReview = async (review) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条评价吗？删除后不可恢复。',
      '删除评价',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await apiDeleteReview(review.id)
    if (response.success) {
      ElMessage.success('评价删除成功')
      await loadReviews()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评价失败:', error)
      ElMessage.error(error.message || '删除评价失败')
    }
  }
}

const handleImageSuccess = (response, uploadFile, uploadFiles) => {
  console.log('Upload success callback:', { response, uploadFile, uploadFiles })
  if (response.success) {
    // 构建完整的图片URL
    const fullUrl = `${process.env.VUE_APP_API_BASE_URL}${response.data}?t=${Date.now()}`
    
    // 更新上传文件的URL
    uploadFile.url = fullUrl
    
    // 更新组件状态
    editForm.imageList = [...uploadFiles]
    editForm.images = editForm.imageList.map(file => {
      if (file.response && file.response.success) {
        return `${process.env.VUE_APP_API_BASE_URL}${file.response.data}`
      }
      return file.url
    })
    
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '图片上传失败')
    // If the server indicates failure, remove the file from the display list.
    const failedFileIndex = uploadFiles.findIndex(f => f.uid === uploadFile.uid)
    if (failedFileIndex > -1) {
      uploadFiles.splice(failedFileIndex, 1)
    }
    editForm.imageList = [...uploadFiles]
  }
}

const handleImageError = (error, uploadFile, uploadFiles) => {
  console.error('Upload error:', { error, uploadFile, uploadFiles })
  ElMessage.error('图片上传失败，请检查网络连接或文件格式')
}

const beforeImageUpload = (file) => {
  console.log('Before upload:', { file, uploadUrl, uploadHeaders })
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB!')
    return false
  }
  return true
}

const handleImageRemove = (uploadFile, uploadFiles) => {
  // When a file is removed, the component provides the updated list.
  // We sync our state to this new list.
  editForm.imageList = [...uploadFiles]
  editForm.images = editForm.imageList.map(file => (file.response ? file.response.data.url : file.url))
}

const previewImage = (image) => {
  window.open(image, '_blank')
}

onMounted(() => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  loadReviews()
})
</script>

<style lang="scss" scoped>
.reviews-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.page-header {
  text-align: center;
  padding: 64px 0;
  background: linear-gradient(135deg, #85CACF 0%, #A8DEE0 100%);
  margin-bottom: 48px;
  position: relative;
  overflow: hidden;
  border-radius: 16px;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: url('data:image/svg+xml,<svg width="100" height="100" xmlns="http://www.w3.org/2000/svg"><defs><pattern id="grid" width="100" height="100" patternUnits="userSpaceOnUse"><path d="M 100 0 L 0 0 0 100" fill="none" stroke="rgba(255,255,255,0.1)" stroke-width="1"/></pattern></defs><rect width="100%" height="100%" fill="url(%23grid)"/></svg>');
    opacity: 0.5;
  }
  
  h1 {
    font-size: 36px;
    font-weight: 700;
    color: #FFFFFF;
    margin-bottom: 16px;
    position: relative;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
  
  p {
    font-size: 20px;
    color: rgba(255, 255, 255, 0.9);
    position: relative;
  }
}

.reviews-content {
  flex: 1;
  margin-bottom: 64px;
}

.reviews-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
}

.review-card {
  margin-bottom: 0;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 32px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: white;
  height: 100%;
  display: flex;
  flex-direction: column;
  
  &:hover {
    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
    transform: translateY(-4px);
    border-color: $primary-light;
  }
  
  .review-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 24px;
    padding-bottom: 20px;
    border-bottom: 2px solid #f3f4f6;
    
    .service-info {
      flex: 1;
      
      h3 {
        font-size: 22px;
        font-weight: 700;
        color: $text-primary;
        margin-bottom: 8px;
      }
      
      .appointment-info {
        color: $text-secondary;
        font-size: 15px;
        font-weight: 500;
      }
    }
    
    .review-rating {
      display: flex;
      align-items: center;
      gap: 12px;
      
      :deep(.el-rate) {
        height: auto;
      }
      
      :deep(.el-rate__icon) {
        font-size: 24px;
      }
      
      .rating-text {
        color: $text-primary;
        font-size: 20px;
        font-weight: 600;
      }
    }
  }
  
  .review-content {
    margin-bottom: 24px;
    flex: 1;
    
    p {
      color: $text-regular;
      line-height: 1.8;
      margin-bottom: 16px;
      font-size: 16px;
    }
    
    .review-images {
      display: flex;
      gap: 12px;
      flex-wrap: wrap;
      
      .review-image {
        width: 100%;
        max-width: 120px;
        height: 120px;
        object-fit: cover;
        border-radius: 12px;
        cursor: pointer;
        transition: all 0.3s ease;
        box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
        
        &:hover {
          transform: scale(1.1);
          box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.2);
        }
      }
    }
  }
  
  .review-reply {
    background: linear-gradient(to right, rgba(133, 202, 207, 0.05), rgba(168, 222, 224, 0.05));
    padding: 24px;
    border-radius: 12px;
    border-left: 4px solid $primary-color;
    margin-bottom: 24px;
    
    .reply-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 12px;
      font-weight: 600;
      font-size: 16px;
      color: $text-primary;
      
      .el-icon {
        color: $primary-color;
        font-size: 20px;
      }
    }
    
    p {
      color: $text-regular;
      margin-bottom: 12px;
      line-height: 1.6;
      font-size: 15px;
    }
    
    .reply-time {
      color: $text-secondary;
      font-size: 14px;
      font-weight: 500;
    }
  }
  
  .review-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 20px;
    border-top: 1px solid #f3f4f6;
    
    .review-time {
      color: $text-secondary;
      font-size: 15px;
      font-weight: 500;
    }
    
    .review-actions {
      display: flex;
      gap: 12px;
    }
  }
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 64px;
}

@media (max-width: 768px) {
  .page-header {
    padding: 48px 0;
    
    h1 {
      font-size: 28px;
    }
    
    p {
      font-size: 16px;
    }
  }
  
  .reviews-grid {
    grid-template-columns: 1fr;
  }
  
  .review-card {
    padding: 24px;
    
    .review-header {
      flex-direction: column;
      gap: 16px;
      align-items: flex-start;
    }
    
    .review-footer {
      flex-direction: column;
      gap: 16px;
      align-items: flex-start;
    }
    
    .review-content {
      .review-images {
        .review-image {
          width: 100px;
          height: 100px;
        }
      }
    }
  }
}
</style>
