<template>
  <div class="admin-reviews">
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><ChatDotRound /></el-icon>
        评价管理
      </h2>
      <div class="header-actions">
        <el-button type="success" size="large" @click="exportReviews" :icon="Download">
          导出Excel数据
        </el-button>
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ stats.total }}</div>
          <div class="stat-label">总评价数</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ stats.averageRating }}</div>
          <div class="stat-label">平均评分</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ stats.pendingReply }}</div>
          <div class="stat-label">待回复</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ stats.todayCount }}</div>
          <div class="stat-label">今日新增</div>
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
        
        <el-form-item label="服务项目">
          <el-select v-model="searchForm.serviceId" placeholder="请选择服务" clearable>
            <el-option
              v-for="service in services"
              :key="service.id"
              :label="service.name"
              :value="service.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="评分">
          <el-select v-model="searchForm.rating" placeholder="请选择评分" clearable>
            <el-option label="5星" :value="5" />
            <el-option label="4星" :value="4" />
            <el-option label="3星" :value="3" />
            <el-option label="2星" :value="2" />
            <el-option label="1星" :value="1" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="回复状态">
          <el-select v-model="searchForm.replyStatus" placeholder="请选择状态" clearable>
            <el-option label="已回复" value="REPLIED" />
            <el-option label="未回复" value="NOT_REPLIED" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="评价时间">
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
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 评价列表 -->
    <el-card>
      <el-table :data="reviews" v-loading="loading" stripe>
        <el-table-column type="selection" width="55" />
        
        <el-table-column label="用户" width="120">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="32" :src="row.user?.avatarUrl">
                {{ row.user?.username?.charAt(0) }}
              </el-avatar>
              <span>{{ row.user?.username }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="service.name" label="服务项目" min-width="150" />
        
        <el-table-column label="评分" width="120">
          <template #default="{ row }">
            <el-rate
              v-model="row.rating"
              disabled
              show-score
              text-color="#ff9900"
            />
          </template>
        </el-table-column>
        
        <el-table-column label="评价内容" min-width="200">
          <template #default="{ row }">
            <div class="review-content">
              <p>{{ row.comment || '无文字评价' }}</p>
              <div v-if="row.images && row.images.length" class="review-images">
                <el-image
                  v-for="(image, index) in row.images.slice(0, 3)"
                  :key="index"
                  :src="image"
                  :preview-src-list="row.images"
                  class="review-image"
                />
                <span v-if="row.images.length > 3" class="more-images">
                  +{{ row.images.length - 3 }}
                </span>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="回复状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.reply ? 'success' : 'warning'">
              {{ row.reply ? '已回复' : '未回复' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="createdAt" label="评价时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewReview(row)">
              查看
            </el-button>
            <el-button 
              size="small" 
              type="primary" 
              @click="replyReview(row)"
              v-if="!row.reply"
            >
              回复
            </el-button>
            <el-button 
              size="small" 
              type="warning" 
              @click="editReply(row)"
              v-else
            >
              编辑回复
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
          @size-change="loadReviews"
          @current-change="loadReviews"
        />
      </div>
    </el-card>
    
    <!-- 评价详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="评价详情" width="700px">
      <div v-if="selectedReview">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户">
            <div class="user-info">
              <el-avatar :size="32" :src="selectedReview.user?.avatarUrl">
                {{ selectedReview.user?.username?.charAt(0) }}
              </el-avatar>
              <span>{{ selectedReview.user?.username }}</span>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="服务项目">
            {{ selectedReview.service?.name }}
          </el-descriptions-item>
          <el-descriptions-item label="评分">
            <el-rate
              v-model="selectedReview.rating"
              disabled
              show-score
              text-color="#ff9900"
            />
          </el-descriptions-item>
          <el-descriptions-item label="评价时间">
            {{ formatDateTime(selectedReview.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="评价内容" :span="2">
            {{ selectedReview.comment || '无文字评价' }}
          </el-descriptions-item>
          <el-descriptions-item label="评价图片" :span="2" v-if="selectedReview.images?.length">
            <div class="review-images-detail">
              <el-image
                v-for="(image, index) in selectedReview.images"
                :key="index"
                :src="image"
                :preview-src-list="selectedReview.images"
                class="review-image-large"
              />
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="商家回复" :span="2" v-if="selectedReview.reply">
            <div class="reply-content">
              <p>{{ selectedReview.reply }}</p>
              <small class="reply-time">
                回复时间：{{ formatDateTime(selectedReview.replyTime) }}
              </small>
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
    
    <!-- 回复评价对话框 -->
    <el-dialog v-model="showReplyDialog" title="回复评价" width="600px">
      <div v-if="selectedReview">
        <div class="original-review">
          <h4>原评价内容：</h4>
          <div class="review-info">
            <el-rate
              v-model="selectedReview.rating"
              disabled
              show-score
              text-color="#ff9900"
            />
            <p>{{ selectedReview.comment || '无文字评价' }}</p>
          </div>
        </div>
        
        <el-divider />
        
        <el-form :model="replyForm" label-width="80px">
          <el-form-item label="回复内容" required>
            <el-input
              v-model="replyForm.content"
              type="textarea"
              :rows="4"
              placeholder="请输入回复内容"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <el-button @click="showReplyDialog = false">取消</el-button>
        <el-button type="primary" @click="submitReply" :loading="replying">
          提交回复
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { 
  getAllReviews,
  getAdminStats,
  replyToReview,
  getAllServices
} from '@/api/admin'
import { ElMessage } from 'element-plus'
import { Download, ChatDotRound, Search, RefreshLeft } from '@element-plus/icons-vue'
import { formatDateTime } from '@/utils/dateUtils'

const loading = ref(false)
const replying = ref(false)
const showDetailDialog = ref(false)
const showReplyDialog = ref(false)

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const reviews = ref([])
const services = ref([])
const selectedReview = ref(null)

const stats = ref({
  total: 0,
  averageRating: 0,
  pendingReply: 0,
  todayCount: 0
})

const searchForm = reactive({
  username: '',
  serviceId: '',
  rating: '',
  replyStatus: '',
  dateRange: []
})

const replyForm = reactive({
  content: ''
})

// formatDateTime 函数已从 @/utils/dateUtils 导入

const loadReviews = async () => {
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
    
    const response = await getAllReviews(params)
    if (response.success) {
      const reviewsList = response.data.content || response.data
      
      // 处理评价图片：将逗号分隔的字符串转换为URL数组
      reviews.value = reviewsList.map(review => {
        if (review.images && typeof review.images === 'string') {
          // 将逗号分隔的路径字符串转换为数组
          const imagePaths = review.images.split(',').filter(path => path.trim())
          // 拼接完整URL
          review.images = imagePaths.map(path => {
            // 如果是相对路径，拼接API基础URL
            if (path && !path.startsWith('http') && !path.startsWith('data:')) {
              return `${process.env.VUE_APP_API_BASE_URL}${path}`
            }
            return path
          })
        } else if (!review.images) {
          review.images = []
        }
        return review
      })
      
      total.value = response.data.totalElements || response.data.length
      
      // 加载真实的统计数据
      await loadStats()
    }
  } catch (error) {
    console.error('加载评价列表失败:', error)
    ElMessage.error('加载评价列表失败')
  } finally {
    loading.value = false
  }
}

const loadServices = async () => {
  try {
    const response = await getAllServices({ status: 'ACTIVE' })
    if (response.success) {
      services.value = response.data.content || response.data
    }
  } catch (error) {
    console.error('加载服务列表失败:', error)
  }
}

// 加载真实的统计数据（从数据库查询）
const loadStats = async () => {
  try {
    const response = await getAdminStats()
    if (response && response.success && response.data) {
      stats.value = {
        total: response.data.totalReviews || 0,
        averageRating: response.data.averageRating || 0,
        pendingReply: response.data.pendingReviews || 0,
        todayCount: response.data.todayNewReviews || 0
      }
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadReviews()
}

const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = key === 'dateRange' ? [] : ''
  })
  handleSearch()
}

const viewReview = (review) => {
  selectedReview.value = review
  showDetailDialog.value = true
}

const replyReview = (review) => {
  selectedReview.value = review
  replyForm.content = ''
  showReplyDialog.value = true
}

const editReply = (review) => {
  selectedReview.value = review
  replyForm.content = review.reply || ''
  showReplyDialog.value = true
}

const submitReply = async () => {
  if (!replyForm.content.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  try {
    replying.value = true
    
    const response = await replyToReview(selectedReview.value.id, replyForm.content)
    
    if (response.success) {
      ElMessage.success('回复成功')
      showReplyDialog.value = false
      await loadReviews()
    }
  } catch (error) {
    console.error('回复失败:', error)
    ElMessage.error('回复失败')
  } finally {
    replying.value = false
  }
}

const exportReviews = () => {
  try {
    // 检查是否有数据
    if (!reviews.value || reviews.value.length === 0) {
      ElMessage.warning('暂无数据可导出')
      return
    }
    
    // 准备导出数据
    const exportList = reviews.value.map((review, index) => {
      // 格式化时间
      let createdAt = '-'
      if (review.createdAt) {
        const timeStr = formatDateTime(review.createdAt)
        createdAt = '\t' + timeStr
      }
      
      return {
        '序号': index + 1,
        '用户': review.user?.realName || review.user?.username || '-',
        '服务项目': review.service?.name || '-',
        '评分': review.rating || 0,
        '评论内容': review.comment || '-',
        '图片数量': review.images?.length || 0,
        '商家回复': review.reply || '-',
        '评价时间': createdAt
      }
    })
    
    // 转换为CSV格式
    const headers = Object.keys(exportList[0])
    const csvContent = [
      headers.join(','),
      ...exportList.map(row => headers.map(header => {
        let value = String(row[header] || '')
        // 处理包含逗号、换行或引号的值
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
    const fileName = `评价数据_${new Date().toISOString().split('T')[0]}.csv`
    link.download = fileName
    link.click()
    
    // 清理URL对象
    URL.revokeObjectURL(link.href)
    
    ElMessage.success(`已导出 ${exportList.length} 条评价数据`)
  } catch (error) {
    console.error('导出评价数据失败:', error)
    ElMessage.error('导出评价数据失败：' + error.message)
  }
}

onMounted(() => {
  loadReviews()
  loadServices()
})
</script>

<style lang="scss" scoped>
.admin-reviews {
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
  
  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;
  }
  
  .review-content {
    p {
      margin: 0 0 8px 0;
      line-height: 1.4;
    }
  }
  
  .review-images {
    display: flex;
    gap: 4px;
    align-items: center;
    
    .review-image {
      width: 40px;
      height: 40px;
      border-radius: 4px;
    }
    
    .more-images {
      font-size: 12px;
      color: $text-secondary;
    }
  }
  
  .review-images-detail {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
    
    .review-image-large {
      width: 80px;
      height: 80px;
      border-radius: 4px;
    }
  }
  
  .original-review {
    background: #f5f7fa;
    padding: 16px;
    border-radius: 8px;
    
    h4 {
      margin: 0 0 12px 0;
      color: $text-primary;
    }
    
    .review-info {
      p {
        margin: 8px 0 0 0;
        color: $text-regular;
      }
    }
  }
  
  .reply-content {
    p {
      margin: 0 0 8px 0;
      line-height: 1.4;
    }
    
    .reply-time {
      color: $text-secondary;
      font-size: 12px;
    }
  }
  
  .pagination {
    display: flex;
    justify-content: center;
    margin-top: 24px;
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
