<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-code">404</div>
      <div class="error-title">页面不存在</div>
      <div class="error-desc">抱歉，您访问的页面不存在或已被移除</div>
      <div class="error-actions">
        <el-button type="primary" @click="goBack">返回上一页</el-button>
        <el-button @click="goHome">返回首页</el-button>
      </div>
      <div class="error-image">
        <img src="@/assets/images/404.svg" alt="404" />
      </div>
    </div>
  </div>
</template>

<script>
import { useRouter } from 'vue-router'

export default {
  name: 'NotFound',

  setup() {
    const router = useRouter()

    const goBack = () => {
      router.back()
    }

    const goHome = () => {
      router.push('/')
    }

    return {
      goBack,
      goHome
    }
  }
}
</script>

<style scoped>
.error-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f0f2f5;
}

.error-content {
  text-align: center;
  padding: 40px;
}

.error-code {
  font-size: 120px;
  font-weight: bold;
  color: #434e59;
  margin-bottom: 20px;
}

.error-title {
  font-size: 24px;
  color: #434e59;
  margin-bottom: 16px;
}

.error-desc {
  font-size: 16px;
  color: #666;
  margin-bottom: 30px;
}

.error-actions {
  margin-bottom: 40px;
}

.error-actions .el-button {
  margin: 0 10px;
}

.error-image {
  max-width: 400px;
  margin: 0 auto;
}

.error-image img {
  width: 100%;
  height: auto;
}
</style>