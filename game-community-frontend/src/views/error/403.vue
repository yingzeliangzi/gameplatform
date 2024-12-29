<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-code">403</div>
      <div class="error-title">访问被拒绝</div>
      <div class="error-desc">抱歉，您没有权限访问此页面</div>
      <div class="error-actions">
        <el-button type="primary" @click="goBack">返回上一页</el-button>
        <el-button @click="goHome">返回首页</el-button>
      </div>
      <div v-if="!isLogin" class="error-login">
        <p>还未登录？</p>
        <el-button type="primary" link @click="goLogin">立即登录</el-button>
      </div>
      <div class="error-image">
        <img src="@/assets/images/403.svg" alt="403" />
      </div>
    </div>
  </div>
</template>

<script>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'

export default {
  name: 'Forbidden',

  setup() {
    const router = useRouter()
    const store = useStore()

    const isLogin = computed(() => store.state.auth.token)

    const goBack = () => {
      router.back()
    }

    const goHome = () => {
      router.push('/')
    }

    const goLogin = () => {
      router.push({
        path: '/login',
        query: { redirect: router.currentRoute.value.fullPath }
      })
    }

    return {
      isLogin,
      goBack,
      goHome,
      goLogin
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
  color: #f56c6c;
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
  margin-bottom: 20px;
}

.error-actions .el-button {
  margin: 0 10px;
}

.error-login {
  margin: 20px 0;
  color: #666;
}

.error-login p {
  margin-bottom: 10px;
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