<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="logo">游戏社区</div>
      <el-menu
          :default-active="activeMenu"
          mode="horizontal"
          router
          background-color="#ffffff"
          text-color="#303133"
          active-text-color="#409EFF"
      >
        <el-menu-item index="/home">首页</el-menu-item>
        <el-menu-item index="/games">游戏</el-menu-item>
        <el-menu-item index="/posts">社区</el-menu-item>
        <el-menu-item index="/events">活动</el-menu-item>
      </el-menu>

      <div class="header-right">
        <template v-if="isLogin">
          <el-badge :value="unreadCount" :max="99" class="notice-badge">
            <el-icon @click="handleNoticeClick"><Bell /></el-icon>
          </el-badge>
          <el-dropdown @command="handleCommand">
            <div class="avatar-container">
              <el-avatar :size="32" :src="userAvatar">
                {{ userInfo?.nickname?.charAt(0)?.toUpperCase() }}
              </el-avatar>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="settings">设置</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" @click="$router.push('/login')">登录</el-button>
          <el-button @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </el-header>

    <el-main class="main">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </el-main>

    <el-footer class="footer">
      <div class="footer-content">
        <p>© 2025 游戏社区 All Rights Reserved.</p>
      </div>
    </el-footer>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { Bell } from '@element-plus/icons-vue'

const store = useStore()
const router = useRouter()
const route = useRoute()

const isLogin = computed(() => store.getters['auth/isAuthenticated'])
const userInfo = computed(() => store.getters['auth/userInfo'])
const userAvatar = computed(() => userInfo.value?.avatar || '')
const unreadCount = computed(() => store.getters['notification/unreadCount'])

const activeMenu = computed(() => route.path)

const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/user/profile')
      break
    case 'settings':
      router.push('/user/settings')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await store.dispatch('auth/logout')
        router.push('/login')
      } catch (error) {
        // 用户取消操作
      }
      break
  }
}

const handleNoticeClick = () => {
  router.push('/notifications')
}
</script>

<style lang="scss" scoped>
.layout-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  height: 60px;
  padding: 0 20px;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;

  .logo {
    font-size: 20px;
    font-weight: bold;
    color: #409EFF;
    margin-right: 40px;
  }

  :deep(.el-menu) {
    border-bottom: none;
    flex: 1;

    .el-menu-item {
      font-size: 15px;
      height: 60px;
      line-height: 60px;

      &.is-active {
        font-weight: bold;
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 20px;

    .notice-badge {
      cursor: pointer;
      font-size: 20px;
      margin-right: 10px;

      :deep(.el-badge__content) {
        right: -5px;
        top: 5px;
      }
    }

    .avatar-container {
      cursor: pointer;
      line-height: 1;
      display: flex;
      align-items: center;

      &:hover {
        opacity: 0.8;
      }

      :deep(.el-avatar) {
        background-color: #409EFF;
        font-weight: bold;
      }
    }

    .el-button {
      margin-left: 10px;
    }
  }
}

.main {
  margin-top: 60px;
  min-height: calc(100vh - 60px - 60px);
  padding: 20px;
  background-color: #f5f7fa;

  :deep(.el-card) {
    margin-bottom: 20px;
  }
}

.footer {
  height: 60px;
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 14px;

  .footer-content {
    text-align: center;

    p {
      margin: 0;
    }
  }
}

// 页面切换动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

// 响应式设计
@media screen and (max-width: 768px) {
  .header {
    padding: 0 10px;

    .logo {
      font-size: 18px;
      margin-right: 20px;
    }

    :deep(.el-menu-item) {
      padding: 0 10px;
    }

    .header-right {
      gap: 10px;

      .el-button {
        padding: 8px 15px;
      }
    }
  }

  .main {
    padding: 10px;
  }
}

// 暗色主题支持
:root[data-theme='dark'] {
  .header {
    background-color: #1f1f1f;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);

    .logo {
      color: #69b1ff;
    }
  }

  .footer {
    background-color: #1f1f1f;
    color: #a6a6a6;
  }

  :deep(.el-card) {
    background-color: #1f1f1f;
    border-color: #2f2f2f;
    color: #fff;
  }
}
</style>