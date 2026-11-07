<template>
  <footer class="app-footer">
    <div class="footer-container">
      <div class="footer-content">
        <div class="footer-section">
          <h4>汽车美容预约平台</h4>
          <p>专业的汽车美容服务预约平台，为您提供便捷、优质的汽车美容服务。</p>
        </div>
        
        <div class="footer-section">
          <h4>服务项目</h4>
          <ul>
            <li>外观美容</li>
            <li>内饰清洁</li>
            <li>深度养护</li>
            <li>套餐服务</li>
          </ul>
        </div>
        
        <div class="footer-section">
          <h4>联系我们</h4>
          <ul>
            <li><el-icon><Phone /></el-icon> 400-123-4567</li>
            <li><el-icon><Message /></el-icon> service@autobeauty.com</li>
            <li><el-icon><Location /></el-icon> 北京市朝阳区xxx街道xxx号</li>
          </ul>
        </div>
        
        <div class="footer-section">
          <h4>关注我们</h4>
          <div class="social-links">
            <el-button circle><el-icon><ChatDotRound /></el-icon></el-button>
            <el-button circle><el-icon><ChatLineRound /></el-icon></el-button>
            <el-button circle><el-icon><Share /></el-icon></el-button>
          </div>
        </div>
      </div>
      
      <div class="footer-bottom">
        <p>&copy; 2025 汽车美容在线预约平台.</p>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { Phone, Message, Location, ChatDotRound, ChatLineRound, Share } from '@element-plus/icons-vue'
</script>

<style lang="scss" scoped>
.app-footer {
  background: #2c3e50;
  color: #ffffff;
  margin-top: auto;
  width: 100vw;
  position: relative;
  left: 50%;
  right: 50%;
  margin-left: -50vw;
  margin-right: -50vw;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  width: 100%;
}

.footer-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 40px;
  padding: 40px 0;
}

.footer-section {
  h4 {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 16px;
    color: #ffffff;
  }
  
  p {
    line-height: 1.6;
    color: #ffffff;
  }
  
  ul {
    list-style: none;
    
    li {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;
      color: #ffffff;
      
      .el-icon {
        color: #74b9ff;
      }
    }
  }
}

.social-links {
  display: flex;
  gap: 12px;
  
  .el-button {
    background: transparent;
    border-color: #ffffff;
    color: #ffffff;
    
    &:hover {
      background: #74b9ff;
      border-color: #74b9ff;
      color: #fff;
    }
  }
}

.footer-bottom {
  border-top: 1px solid #34495e;
  padding: 20px 0;
  text-align: center;
  color: #ffffff;
  font-size: 14px;
  background: #2c3e50;
  
  p {
    margin: 4px 0;
    font-weight: 500;
    color: #ffffff;
  }
}

@media (max-width: 768px) {
  .footer-content {
    grid-template-columns: 1fr;
    gap: 24px;
    padding: 24px 0;
  }
}
</style>
