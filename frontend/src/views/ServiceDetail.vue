<template>
  <div class="service-detail-page">
    <Header />
    
    <div class="service-detail" v-loading="loading">
      <div class="container" v-if="service">
        <!-- 服务基本信息 -->
        <div class="service-main">
          <div class="service-images">
            <el-carousel height="400px" indicator-position="outside">
              <el-carousel-item v-for="(image, index) in serviceImages" :key="index">
                <img :src="image" :alt="service.name" />
              </el-carousel-item>
            </el-carousel>
          </div>
          
          <div class="service-info">
            <h1>{{ service.name }}</h1>
            <div class="service-rating" v-if="serviceRating">
              <el-rate
                v-model="serviceRating.rating"
                disabled
                show-score
                text-color="#ff9900"
                score-template="{value}"
              />
              <span class="review-count">({{ serviceRating.count }}条评价)</span>
            </div>
            
            <div class="service-price">
              <span class="current-price">¥{{ service.price }}</span>
              <span class="original-price" v-if="service.originalPrice">¥{{ service.originalPrice }}</span>
            </div>
            
            <div class="service-meta">
              <div class="meta-item">
                <el-icon><Clock /></el-icon>
                <span>服务时长：{{ service.duration }}分钟</span>
              </div>
              <div class="meta-item">
                <el-icon><Location /></el-icon>
                <span>服务类型：{{ getCategoryText(service.category) }}</span>
              </div>
              <div class="meta-item">
                <el-icon><Star /></el-icon>
                <span>状态：{{ service.status === 'ACTIVE' ? '可预约' : '暂停服务' }}</span>
              </div>
            </div>
            
            <div class="service-actions">
              <el-button 
                type="primary" 
                size="large"
                @click="bookService"
                :disabled="!userStore.isLoggedIn || service.status !== 'ACTIVE'"
              >
                {{ userStore.isLoggedIn ? '立即预约' : '请先登录' }}
              </el-button>
              <el-button size="large" @click="addToFavorites">
                <el-icon><Star /></el-icon>
                收藏
              </el-button>
            </div>
          </div>
        </div>
        
        <!-- 服务详情 -->
        <div class="service-tabs">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="服务详情" name="details">
              <div class="service-description">
                <h3>服务介绍</h3>
                <div class="description-content" v-html="service.description"></div>
                
                <h3>服务流程</h3>
                <div class="service-process">
                  <div class="process-step" v-for="(step, index) in serviceProcess" :key="index">
                    <div class="step-number">{{ index + 1 }}</div>
                    <div class="step-content">
                      <h4>{{ step.title }}</h4>
                      <p>{{ step.description }}</p>
                    </div>
                  </div>
                </div>
                
                <h3>注意事项</h3>
                <ul class="service-notes">
                  <li v-for="note in serviceNotes" :key="note">{{ note }}</li>
                </ul>
              </div>
            </el-tab-pane>
            
            <el-tab-pane :label="`用户评价(${reviews.length})`" name="reviews">
              <div class="reviews-section">
                <div class="reviews-summary" v-if="serviceRating">
                  <div class="rating-overview">
                    <div class="rating-score">
                      <span class="score">{{ serviceRating.rating.toFixed(1) }}</span>
                      <el-rate v-model="serviceRating.rating" disabled />
                      <span class="total">共{{ serviceRating.count }}条评价</span>
                    </div>
                  </div>
                </div>
                
                <div class="reviews-list">
                  <div class="review-item" v-for="review in reviews" :key="review.id">
                    <div class="review-header">
                      <el-avatar :src="review.user?.avatarUrl">
                        {{ review.user?.username?.charAt(0) }}
                      </el-avatar>
                      <div class="review-user">
                        <h4>{{ review.user?.realName || review.user?.username }}</h4>
                        <el-rate v-model="review.rating" disabled size="small" />
                        <span class="review-date">{{ formatDate(review.createdAt) }}</span>
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
                    </div>
                  </div>
                  
                  <div class="reviews-pagination" v-if="reviewsTotal > 0">
                    <el-pagination
                      v-model:current-page="reviewsPage"
                      v-model:page-size="reviewsSize"
                      :total="reviewsTotal"
                      layout="prev, pager, next"
                      @current-change="loadReviews"
                    />
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>
    
    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import Header from '@/components/Layout/Header.vue'
import Footer from '@/components/Layout/Footer.vue'
import { getServiceById } from '@/api/services'
import { getServiceReviews, getServiceRating } from '@/api/reviews'
import { ElMessage } from 'element-plus'
import { Clock, Location, Star, Service } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const activeTab = ref('details')

const service = ref(null)
const serviceRating = ref(null)
const reviews = ref([])
const reviewsPage = ref(1)
const reviewsSize = ref(10)
const reviewsTotal = ref(0)

const serviceImages = ref([])
const serviceProcess = ref([
  { title: '接车检查', description: '专业技师检查车辆状况，确认服务项目' },
  { title: '清洁准备', description: '准备专用清洁用品和工具设备' },
  { title: '服务实施', description: '按照标准流程进行专业服务' },
  { title: '质量检验', description: '服务完成后进行质量检查' },
  { title: '交车确认', description: '客户验收确认，完成服务交付' }
])

const serviceNotes = ref([
  '请提前预约，确保服务时间安排',
  '服务过程中请配合技师工作',
  '如有特殊要求请提前说明',
  '服务完成后请及时验收确认'
])

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

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

const loadService = async () => {
  loading.value = true
  try {
    const response = await getServiceById(route.params.id)
    if (response.success) {
      service.value = response.data
      
      // 设置服务图片
      serviceImages.value = service.value.imageUrl 
        ? [service.value.imageUrl]
        : ['/images/default-service.jpg']
      
      // 加载评分
      await loadServiceRating()
    }
  } catch (error) {
    console.error('加载服务详情失败:', error)
    ElMessage.error('加载服务详情失败')
  } finally {
    loading.value = false
  }
}

const loadServiceRating = async () => {
  try {
    const response = await getServiceRating(route.params.id)
    if (response.success) {
      serviceRating.value = {
        rating: response.data.averageRating || 0,
        count: response.data.reviewCount || 0
      }
    }
  } catch (error) {
    console.error('加载服务评分失败:', error)
  }
}

const loadReviews = async () => {
  try {
    const response = await getServiceReviews(route.params.id, {
      page: reviewsPage.value - 1,
      size: reviewsSize.value
    })
    
    if (response.success) {
      reviews.value = response.data.content || response.data
      reviewsTotal.value = response.data.totalElements || response.data.length
    }
  } catch (error) {
    console.error('加载评价列表失败:', error)
  }
}

const bookService = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  router.push({
    path: '/appointment',
    query: { serviceId: service.value.id }
  })
}

const addToFavorites = () => {
  ElMessage.info('收藏功能待开发')
}

const previewImage = (image) => {
  // 图片预览功能
  window.open(image, '_blank')
}

onMounted(() => {
  loadService()
  loadReviews()
})
</script>

<style lang="scss" scoped>
.service-detail-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.service-detail {
  padding: 60px 0;
  background: $background-light;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.service-main {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  margin-bottom: 60px;
  background: white;
  padding: 40px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.service-images {
  .el-carousel {
    border-radius: 12px;
    overflow: hidden;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
}

.service-info {
  h1 {
    font-size: 32px;
    font-weight: 600;
    color: $text-primary;
    margin-bottom: 16px;
  }
  
  .service-rating {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 20px;
    
    .review-count {
      color: $text-secondary;
      font-size: 14px;
    }
  }
  
  .service-price {
    margin-bottom: 24px;
    
    .current-price {
      font-size: 36px;
      font-weight: 700;
      color: $danger-color;
      margin-right: 12px;
    }
    
    .original-price {
      font-size: 20px;
      color: $text-secondary;
      text-decoration: line-through;
    }
  }
  
  .service-meta {
    margin-bottom: 32px;
    
    .meta-item {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 12px;
      color: $text-regular;
      
      .el-icon {
        color: $primary-color;
      }
    }
  }
  
  .service-actions {
    display: flex;
    gap: 16px;
    
    .el-button {
      flex: 1;
      height: 48px;
      font-size: 16px;
    }
  }
}

.service-tabs {
  background: white;
  padding: 40px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  
  .service-description {
    h3 {
      font-size: 20px;
      font-weight: 600;
      color: $text-primary;
      margin: 32px 0 16px;
      
      &:first-child {
        margin-top: 0;
      }
    }
    
    .description-content {
      line-height: 1.8;
      color: $text-regular;
      margin-bottom: 24px;
    }
    
    .service-process {
      .process-step {
        display: flex;
        gap: 16px;
        margin-bottom: 24px;
        
        .step-number {
          width: 32px;
          height: 32px;
          background: $primary-color;
          color: white;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: 600;
          flex-shrink: 0;
        }
        
        .step-content {
          flex: 1;
          
          h4 {
            font-size: 16px;
            font-weight: 600;
            color: $text-primary;
            margin-bottom: 4px;
          }
          
          p {
            color: $text-secondary;
            line-height: 1.6;
          }
        }
      }
    }
    
    .service-notes {
      list-style: none;
      
      li {
        position: relative;
        padding-left: 20px;
        margin-bottom: 8px;
        color: $text-regular;
        
        &::before {
          content: '•';
          position: absolute;
          left: 0;
          color: $primary-color;
          font-weight: bold;
        }
      }
    }
  }
}

.reviews-section {
  .reviews-summary {
    background: $background-light;
    padding: 24px;
    border-radius: 12px;
    margin-bottom: 24px;
    
    .rating-overview {
      .rating-score {
        display: flex;
        align-items: center;
        gap: 16px;
        
        .score {
          font-size: 48px;
          font-weight: 700;
          color: $primary-color;
        }
        
        .total {
          color: $text-secondary;
        }
      }
    }
  }
  
  .reviews-list {
    .review-item {
      border-bottom: 1px solid #f0f0f0;
      padding: 24px 0;
      
      &:last-child {
        border-bottom: none;
      }
      
      .review-header {
        display: flex;
        gap: 12px;
        margin-bottom: 16px;
        
        .review-user {
          flex: 1;
          
          h4 {
            font-size: 16px;
            font-weight: 600;
            color: $text-primary;
            margin-bottom: 4px;
          }
          
          .review-date {
            color: $text-secondary;
            font-size: 12px;
            margin-left: 12px;
          }
        }
      }
      
      .review-content {
        margin-bottom: 16px;
        
        p {
          color: $text-regular;
          line-height: 1.6;
          margin-bottom: 12px;
        }
        
        .review-images {
          display: flex;
          gap: 8px;
          flex-wrap: wrap;
          
          .review-image {
            width: 80px;
            height: 80px;
            object-fit: cover;
            border-radius: 8px;
            cursor: pointer;
            transition: transform 0.3s;
            
            &:hover {
              transform: scale(1.05);
            }
          }
        }
      }
      
      .review-reply {
        background: $background-light;
        padding: 16px;
        border-radius: 8px;
        border-left: 4px solid $primary-color;
        
        .reply-header {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 8px;
          font-weight: 600;
          color: $text-primary;
          
          .el-icon {
            color: $primary-color;
          }
        }
        
        p {
          color: $text-regular;
          margin: 0;
        }
      }
    }
    
    .reviews-pagination {
      display: flex;
      justify-content: center;
      margin-top: 32px;
    }
  }
}

@media (max-width: 768px) {
  .container {
    padding: 0 16px;
  }
  
  .service-detail {
    padding: 40px 0;
  }
  
  .service-main {
    grid-template-columns: 1fr;
    gap: 32px;
    padding: 24px;
    margin-bottom: 32px;
  }
  
  .service-tabs {
    padding: 24px;
  }
  
  .service-info h1 {
    font-size: 24px;
  }
  
  .service-price .current-price {
    font-size: 28px;
  }
  
  .service-actions {
    flex-direction: column;
    
    .el-button {
      width: 100%;
    }
  }
}
</style>
