<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-code">500</div>
      <div class="error-title">服务器错误</div>
      <div class="error-desc">抱歉，服务器出现了一些问题</div>
      <div class="error-detail" v-if="errorMessage">
        错误信息：{{ errorMessage }}
      </div>
      <div class="error-actions">
        <el-button type="primary" @click="reload">重新加载</el-button>
        <el-button @click="goHome">返回首页</el-button>
      </div>
      <div class="error-contact">
        <p>如果问题持续存在，请联系管理员</p>
        <p>联系邮箱：support@gameplatform.com</p>
      </div>
      <div class="error-image">
        <img src="@/assets/images/500.svg" alt="500" />
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'

export default {
  name: 'ServerError',

  setup() {
    const router = useRouter()
    const route = useRoute()
    const errorMessage = ref(route.query.message || '')

    const reload = () => {
      window.location.reload()
    }

    const goHome = () => {
      router.push('/')
    }

    return {
      errorMessage,
      reload,
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
  color: #e6a23c;
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
  margin-bottom: 16px;
}

.error-detail {
  font-size: 14px;
  color: #f56c6c;
  margin-bottom: 30px;
  padding: 10px;
  background-color: #fef0f0;
  border-radius: 4px;
}

.error-actions {
  margin-bottom: 20px;
}

.error-actions .el-button {
  margin: 0 10px;
}

.error-contact {
  margin: 20px 0;
  color: #666;
  font-size: 14px;
}

.error-contact p {
  margin: 5px 0;
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