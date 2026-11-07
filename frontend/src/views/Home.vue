<template>
  <div class="home">
    <Header />
    
    <!-- 轮播图 -->
    <section class="hero-section">
      <el-carousel height="400px" indicator-position="outside">
        <el-carousel-item v-for="item in banners" :key="item.id">
          <div class="carousel-item" :style="{ backgroundImage: `url(${item.image})` }">
            <div class="carousel-content">
              <h2>{{ item.title }}</h2>
              <p>{{ item.description }}</p>
              <el-button type="primary" size="large" @click="$router.push('/services')">
                立即预约
              </el-button>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>
    
    <!-- 服务特色 -->
    <section class="features-section">
      <div class="container">
        <h2 class="section-title">为什么选择我们</h2>
        <div class="features-grid">
          <div class="feature-item" v-for="feature in features" :key="feature.id">
            <div class="feature-icon">
              <span v-if="feature.icon === 'Star'" class="icon-symbol">⭐</span>
              <span v-else-if="feature.icon === 'CircleCheck'" class="icon-symbol">🛡️</span>
              <span v-else-if="feature.icon === 'Clock'" class="icon-symbol">⏰</span>
              <span v-else-if="feature.icon === 'Headset'" class="icon-symbol">🎧</span>
            </div>
            <h3>{{ feature.title }}</h3>
            <p>{{ feature.description }}</p>
          </div>
        </div>
      </div>
    </section>
    
    <!-- 热门服务 -->
    <section class="popular-services">
      <div class="container">
        <h2 class="section-title">热门服务</h2>
        <div class="services-grid">
          <div class="service-card" v-for="service in popularServices" :key="service.id" @click="viewService(service.id)">
            <div class="service-image">
              <img :src="service.imageUrl || '/images/default-service.jpg'" :alt="service.name">
            </div>
            <div class="service-info">
              <h3>{{ service.name }}</h3>
              <p class="service-desc">{{ service.description }}</p>
              <div class="service-meta">
                <span class="price">¥{{ service.price }}</span>
                <span class="duration">{{ service.duration }}分钟</span>
              </div>
              <div class="service-rating" v-if="serviceRatings[service.id]">
                <el-rate
                  v-model="serviceRatings[service.id].rating"
                  disabled
                  show-score
                  text-color="#ff9900"
                  score-template="{value}"
                />
                <span class="review-count">({{ serviceRatings[service.id].count }}条评价)</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
    
    <!-- 用户评价 -->
    <section class="reviews-section">
      <div class="container">
        <h2 class="section-title">用户评价</h2>
        <div class="reviews-grid">
          <div class="review-card" v-for="review in latestReviews" :key="review.id">
            <div class="review-header">
              <el-avatar :src="review.user?.avatarUrl">{{ review.user?.username?.charAt(0) }}</el-avatar>
              <div class="review-user">
                <h4>{{ review.user?.realName || review.user?.username }}</h4>
                <el-rate v-model="review.rating" disabled />
              </div>
            </div>
            <p class="review-content">{{ review.comment }}</p>
            <div class="review-service">
              <span>服务项目：{{ review.service?.name }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>
    
    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Header from '@/components/Layout/Header.vue'
import Footer from '@/components/Layout/Footer.vue'
import { getPopularServices } from '@/api/services'
import { getLatestReviews, getServiceRating } from '@/api/reviews'
import { Star, CircleCheck, Clock, Headset } from '@element-plus/icons-vue'

const router = useRouter()

const banners = ref([
  {
    id: 1,
    title: '专业汽车美容服务',
    description: '为您的爱车提供最专业的美容护理服务',
    image: '/images/1.jpg'
  },
  {
    id: 2,
    title: '便捷在线预约',
    description: '随时随地预约，享受优质服务',
    image: '/images/2.jpg'
  },
  {
    id: 3,
    title: '品质保证',
    description: '专业技师，优质产品，让您放心',
    image: '/images/3.jpeg'
  }
])

const features = ref([
  {
    id: 1,
    icon: 'Star',
    title: '专业技师',
    description: '拥有多年经验的专业技师团队，为您提供优质服务'
  },
  {
    id: 2,
    icon: 'CircleCheck',
    title: '品质保证',
    description: '使用优质产品和先进设备，确保服务质量'
  },
  {
    id: 3,
    icon: 'Clock',
    title: '快速便捷',
    description: '在线预约，快速响应，节省您的宝贵时间'
  },
  {
    id: 4,
    icon: 'Headset',
    title: '贴心服务',
    description: '全程贴心服务，让您享受舒适的服务体验'
  }
])

const popularServices = ref([])
const latestReviews = ref([])
const serviceRatings = ref({})

const viewService = (id) => {
  router.push(`/services/${id}`)
}

const loadPopularServices = async () => {
  try {
    const response = await getPopularServices()
    if (response.success) {
      popularServices.value = response.data.slice(0, 6) // 只显示前6个
      
      // 加载每个服务的评分
      for (const service of popularServices.value) {
        try {
          const ratingResponse = await getServiceRating(service.id)
          if (ratingResponse.success) {
            serviceRatings.value[service.id] = {
              rating: ratingResponse.data.averageRating || 0,
              count: ratingResponse.data.reviewCount || 0
            }
          }
        } catch (error) {
          console.error('加载服务评分失败:', error)
        }
      }
    }
  } catch (error) {
    console.error('加载热门服务失败:', error)
  }
}

const loadLatestReviews = async () => {
  try {
    const response = await getLatestReviews()
    if (response.success) {
      latestReviews.value = response.data.slice(0, 6) // 只显示前6个
    }
  } catch (error) {
    console.error('加载最新评价失败:', error)
  }
}

onMounted(() => {
  loadPopularServices()
  loadLatestReviews()
})
</script>

<style lang="scss" scoped>
.home {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.hero-section {
  .carousel-item {
    height: 400px;
    background-size: cover;
    background-position: center;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.4);
    }
  }
  
  .carousel-content {
    text-align: center;
    color: white;
    z-index: 1;
    
    h2 {
      font-size: 48px;
      font-weight: 700;
      margin-bottom: 16px;
    }
    
    p {
      font-size: 20px;
      margin-bottom: 32px;
      opacity: 0.9;
    }
  }
}

.section-title {
  text-align: center;
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 64px;
  color: $text-primary;
  position: relative;
  padding-bottom: 20px;
  
  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 80px;
    height: 4px;
    background: linear-gradient(90deg, $primary-color, $primary-light);
    border-radius: 2px;
  }
}

.features-section {
  padding: 80px 0;
  background: transparent;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 40px;
}

.feature-item {
  text-align: center;
  padding: 32px 24px;
  border-radius: 12px;
  transition: transform 0.3s ease;
  
  &:hover {
    transform: translateY(-8px);
  }
  
  .feature-icon {
    width: 80px;
    height: 80px;
    background: linear-gradient(135deg, $primary-color, $primary-light);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 24px;
    
    .el-icon {
      font-size: 36px;
      color: white;
    }
    
    .icon-symbol {
      font-size: 36px;
      color: white;
      display: block;
    }
  }
  
  h3 {
    font-size: 20px;
    font-weight: 600;
    margin-bottom: 16px;
    color: $text-primary;
  }
  
  p {
    color: $text-secondary;
    line-height: 1.6;
  }
}

.popular-services {
  padding: 100px 0;
  background: transparent;
}

.services-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 40px;
}

.service-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
  
  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 12px 32px rgba(37, 99, 235, 0.15);
    border-color: rgba(37, 99, 235, 0.2);
  }
  
  .service-image {
    height: 240px;
    overflow: hidden;
    position: relative;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }
    
    &:hover img {
      transform: scale(1.1);
    }
  }
  
  .service-info {
    padding: 28px;
    
    h3 {
      font-size: 22px;
      font-weight: 700;
      margin-bottom: 12px;
      color: $text-primary;
    }
    
    .service-desc {
      color: $text-secondary;
      margin-bottom: 20px;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      line-height: 1.6;
      font-size: 15px;
    }
    
    .service-meta {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      padding-top: 16px;
      border-top: 1px solid #f0f0f0;
      
      .price {
        font-size: 28px;
        font-weight: 700;
        background: linear-gradient(135deg, #f56565, #fc8181);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
      }
      
      .duration {
        color: $text-secondary;
        font-size: 15px;
        font-weight: 500;
      }
    }
    
    .service-rating {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .review-count {
        color: $text-secondary;
        font-size: 13px;
        font-weight: 500;
      }
    }
  }
}

.reviews-section {
  padding: 100px 0;
  background: transparent;
}

.reviews-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 32px;
}

.review-card {
  background: $background-light;
  padding: 28px;
  border-radius: 16px;
  border-left: 4px solid $primary-color;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }
  
  .review-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 20px;
    
    .review-user h4 {
      margin-bottom: 6px;
      color: $text-primary;
      font-size: 16px;
      font-weight: 600;
    }
  }
  
  .review-content {
    color: $text-regular;
    line-height: 1.7;
    margin-bottom: 16px;
    font-size: 15px;
  }
  
  .review-service {
    color: $text-secondary;
    font-size: 14px;
    font-weight: 500;
  }
}

@media (max-width: 768px) {
  .hero-section .carousel-content {
    h2 {
      font-size: 28px;
    }
    
    p {
      font-size: 16px;
    }
  }
  
  .section-title {
    font-size: 24px;
  }
  
  .features-section,
  .popular-services,
  .reviews-section {
    padding: 40px 0;
  }
  
  .services-grid,
  .reviews-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
}
</style>
