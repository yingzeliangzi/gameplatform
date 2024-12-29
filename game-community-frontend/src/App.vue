<template>
  <div id="app">
    <!-- 全局loading -->
    <div v-if="loading" class="global-loading">
      <el-loading></el-loading>
    </div>

    <!-- 主内容 -->
    <router-view v-slot="{ Component }">
      <transition name="fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>

    <!-- 回到顶部按钮 -->
    <el-backtop></el-backtop>

    <!-- 全局提示组件 -->
    <el-notification></el-notification>
  </div>
</template>

<script>
import { computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useStore } from 'vuex'
import { useRoute } from 'vue-router'
import { wsClient } from '@/utils/websocket'

export default {
  name: 'App',

  setup() {
    const store = useStore()
    const route = useRoute()

    const loading = computed(() => store.state.loading)
    const isLoggedIn = computed(() => store.state.auth.token)

    // 初始化用户信息
    const initUserInfo = async () => {
      if (isLoggedIn.value) {
        try {
          await store.dispatch('user/getUserInfo')
          // 连接WebSocket
          wsClient.connect()
        } catch (error) {
          // token可能过期，清除登录状态
          store.dispatch('auth/logout')
        }
      }
    }

    // 监听路由变化以更新页面标题
    watch(() => route.meta.title, (title) => {
      if (title) {
        document.title = `${title} - 游戏社区`
      } else {
        document.title = '游戏社区'
      }
    }, { immediate: true })

    // 监听登录状态变化
    watch(isLoggedIn, (newVal) => {
      if (newVal) {
        initUserInfo()
      } else {
        // 断开WebSocket连接
        wsClient.disconnect()
      }
    })

    // 防止页面关闭时意外退出
    const handleBeforeUnload = (e) => {
      if (store.state.hasUnsavedChanges) {
        e.preventDefault()
        e.returnValue = ''
      }
    }

    onMounted(() => {
      // 初始化用户信息
      initUserInfo()
      // 注册页面关闭事件
      window.addEventListener('beforeunload', handleBeforeUnload)
      // 设置主题
      document.body.setAttribute('data-theme', store.state.theme)
    })

    onBeforeUnmount(() => {
      window.removeEventListener('beforeunload', handleBeforeUnload)
      wsClient.disconnect()
    })

    return {
      loading
    }
  }
}
</script>

<style>
#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
  'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: var(--el-text-color-primary);
}

/* 页面切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 全局loading */
.global-loading {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.9);
  z-index: 9999;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-thumb {
  background-color: var(--el-border-color-lighter);
  border-radius: 3px;
}

::-webkit-scrollbar-track {
  background-color: transparent;
}

/* 主题相关 */
[data-theme='dark'] {
  --el-bg-color: #1a1a1a;
  --el-text-color-primary: #ffffff;
  /* 其他深色主题变量 */
}

/* 响应式布局 */
@media screen and (max-width: 768px) {
  .el-message {
    width: 90% !important;
    min-width: unset !important;
  }
}

/* 通用样式 */
.page-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.text-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.clickable {
  cursor: pointer;
}

.disabled {
  cursor: not-allowed;
  opacity: 0.6;
}
</style>