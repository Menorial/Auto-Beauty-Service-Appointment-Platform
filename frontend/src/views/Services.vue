<template>
  <div class="services-page">
    <Header />
    
    <div class="container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1>服务项目</h1>
        <p>为您的爱车提供专业的美容护理服务</p>
      </div>
      
      <!-- 搜索和筛选 -->
      <div class="search-filter">
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索服务项目..."
            size="large"
            prefix-icon="Search"
            @input="handleSearch"
            clearable
          />
        </div>
        
        <div class="filter-tabs">
          <el-tabs v-model="activeCategory" @tab-change="handleCategoryChange">
            <el-tab-pane label="全部" name="all" />
            <el-tab-pane label="外观美容" name="exterior" />
            <el-tab-pane label="内饰清洁" name="interior" />
            <el-tab-pane label="深度养护" name="maintenance" />
            <el-tab-pane label="维修服务" name="repair" />
            <el-tab-pane label="套餐服务" name="package" />
          </el-tabs>
        </div>
        
        <div class="sort-options">
          <el-select v-model="sortBy" placeholder="排序方式" @change="handleSort">
            <el-option label="默认排序" value="default" />
            <el-option label="价格从低到高" value="price_asc" />
            <el-option label="价格从高到低" value="price_desc" />
            <el-option label="评分最高" value="rating_desc" />
            <el-option label="最受欢迎" value="popular" />
          </el-select>
        </div>
      </div>
      
      <!-- 服务列表 -->
      <div class="services-content" v-loading="loading">
        <div class="services-grid" v-if="services.length > 0">
          <div 
            class="service-card" 
            v-for="service in services" 
            :key="service.id"
            @click="viewServiceDetail(service.id)"
          >
            <div class="service-image">
              <img :src="service.imageUrl || '/images/default-service.jpg'" :alt="service.name">
              <div class="service-badge" v-if="service.isPopular">
                <el-tag type="danger" size="small">热门</el-tag>
              </div>
            </div>
            
            <div class="service-info">
              <h3>{{ service.name }}</h3>
              <p class="service-desc">{{ service.description }}</p>
              
              <div class="service-meta">
                <div class="price-duration">
                  <span class="price">¥{{ service.price }}</span>
                  <span class="duration">{{ service.duration }}分钟</span>
                </div>
                
                <div class="rating" v-if="serviceRatings[service.id]">
                  <el-rate
                    v-model="serviceRatings[service.id].rating"
                    disabled
                    size="small"
                  />
                  <span class="rating-text">
                    {{ serviceRatings[service.id].rating.toFixed(1) }}
                    ({{ serviceRatings[service.id].count }})
                  </span>
                </div>
              </div>
              
              <div class="service-actions">
                <el-button 
                  type="primary" 
                  @click.stop="bookService(service)"
                  :disabled="!userStore.isLoggedIn"
                >
                  {{ userStore.isLoggedIn ? '立即预约' : '请先登录' }}
                </el-button>
                <el-button @click.stop="viewServiceDetail(service.id)">
                  查看详情
                </el-button>
              </div>
            </div>
          </div>
        </div>
        
        <el-empty v-else description="暂无服务项目" />
        
        <!-- 分页 -->
        <div class="pagination" v-if="total > 0">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[12, 24, 36, 48]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
    
    <Footer />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import Header from '@/components/Layout/Header.vue'
import Footer from '@/components/Layout/Footer.vue'
import { getServices, searchServices } from '@/api/services'
import { getServiceRating } from '@/api/reviews'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const searchKeyword = ref('')
const activeCategory = ref('all')
const sortBy = ref('default')
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

const services = ref([])
const serviceRatings = ref({})

const categoryValueMap = {
  all: null,
  exterior: '外观美容',
  interior: '内饰清洁',
  maintenance: '深度养护',
  repair: '维修服务',
  package: '套餐服务'
}

const loadServices = async () => {
  loading.value = true
  try {
    const mappedCategory = Object.prototype.hasOwnProperty.call(categoryValueMap, activeCategory.value)
      ? categoryValueMap[activeCategory.value]
      : activeCategory.value
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      category: mappedCategory,
      keyword: searchKeyword.value || null,
      sort: sortBy.value
    }
    
    const response = searchKeyword.value 
      ? await searchServices(searchKeyword.value, params)
      : await getServices(params)
    
    if (response.success) {
      services.value = response.data.content || response.data
      total.value = response.data.totalElements || response.data.length
      
      // 加载服务评分
      await loadServiceRatings()
    }
  } catch (error) {
    console.error('加载服务列表失败:', error)
    ElMessage.error('加载服务列表失败')
  } finally {
    loading.value = false
  }
}

const loadServiceRatings = async () => {
  for (const service of services.value) {
    try {
      const response = await getServiceRating(service.id)
      if (response.success) {
        serviceRatings.value[service.id] = {
          rating: response.data.averageRating || 0,
          count: response.data.reviewCount || 0
        }
      }
    } catch (error) {
      console.error(`加载服务${service.id}评分失败:`, error)
    }
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadServices()
}

const handleCategoryChange = () => {
  currentPage.value = 1
  loadServices()
}

const handleSort = () => {
  currentPage.value = 1
  loadServices()
}

const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1
  loadServices()
}

const handleCurrentChange = (newPage) => {
  currentPage.value = newPage
  loadServices()
}

const viewServiceDetail = (serviceId) => {
  router.push(`/services/${serviceId}`)
}

const bookService = (service) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  router.push({
    path: '/appointment',
    query: { serviceId: service.id }
  })
}

// 监听搜索关键词变化
watch(searchKeyword, (newVal, oldVal) => {
  if (newVal !== oldVal) {
    // 防抖处理
    clearTimeout(searchKeyword.timer)
    searchKeyword.timer = setTimeout(() => {
      handleSearch()
    }, 500)
  }
})

onMounted(() => {
  loadServices()
})
</script>

<style lang="scss" scoped>
.services-page {
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
    font-weight: 600;
    color: #FFFFFF;
    margin-bottom: 16px;
    position: relative;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
  
  p {
    font-size: 18px;
    color: rgba(255, 255, 255, 0.9);
    position: relative;
  }
}

.search-filter {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 32px;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: $box-shadow-base;
  
  .search-box {
    flex: 1;
    max-width: 400px;
  }
  
  .filter-tabs {
    flex: 2;
    
    :deep(.el-tabs__header) {
      margin: 0;
    }
    
    :deep(.el-tabs__nav-wrap::after) {
      display: none;
    }
  }
  
  .sort-options {
    width: 150px;
  }
}

.services-content {
  flex: 1;
  margin-bottom: 40px;
}

.services-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 40px;
}

.service-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: $box-shadow-base;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  height: 100%;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: $box-shadow-light;
  }
  
  .service-image {
    height: 200px;
    position: relative;
    overflow: hidden;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }
    
    &:hover img {
      transform: scale(1.05);
    }
    
    .service-badge {
      position: absolute;
      top: 12px;
      right: 12px;
    }
  }
  
  .service-info {
    padding: 20px;
    display: flex;
    flex-direction: column;
    flex: 1;
    
    h3 {
      font-size: 18px;
      font-weight: 600;
      margin-bottom: 8px;
      color: $text-primary;
    }
    
    .service-desc {
      color: $text-secondary;
      margin-bottom: 16px;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      line-height: 1.5;
      height: 3em; // 固定高度，确保一致性
    }
    
    .service-meta {
      margin-bottom: 16px;
      
      .price-duration {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;
        
        .price {
          font-size: 20px;
          font-weight: 600;
          color: $danger-color;
        }
        
        .duration {
          color: $text-secondary;
          font-size: 14px;
        }
      }
      
      .rating {
        display: flex;
        align-items: center;
        gap: 8px;
        height: 20px; // 固定评分区域高度
        
        .rating-text {
          color: $text-secondary;
          font-size: 14px;
        }
      }
    }
    
    .service-actions {
      display: flex;
      gap: 12px;
      margin-top: auto; // 将按钮推到底部
      
      .el-button {
        flex: 1;
      }
    }
  }
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

@media (max-width: 768px) {
  .search-filter {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
    
    .search-box,
    .sort-options {
      max-width: none;
      width: 100%;
    }
  }
  
  .services-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .page-header {
    padding: 24px 0;
    margin-bottom: 24px;
    
    h1 {
      font-size: 28px;
    }
    
    p {
      font-size: 16px;
    }
  }
}
</style>
