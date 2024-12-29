<template>
  <el-header class="app-header">
    <div class="header-left">
      <div class="logo">游戏社区平台</div>
      <el-menu
          mode="horizontal"
          :default-active="activeMenu"
          router
          class="header-menu"
      >
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/games">游戏库</el-menu-item>
        <el-menu-item index="/community">社区</el-menu-item>
        <el-menu-item index="/events">活动</el-menu-item>
      </el-menu>
    </div>

    <div class="header-right">
      <el-input
          v-model="searchText"
          placeholder="搜索..."
          prefix-icon="Search"
          class="search-input"
      />

      <el-badge :value="unreadCount" class="notification-badge" v-if="isLoggedIn">
        <el-button circle icon="Bell" @click="showNotifications" />
      </el-badge>

      <template v-if="isLoggedIn">
        <el-dropdown @command="handleCommand">
          <div class="user-info">
            <el-avatar :size="32" :src="userAvatar" />
            <span class="username">{{ username }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="settings">设置</el-dropdown-item>
              <el-dropdown-item
                  v-if="isAdmin"
                  command="admin"
                  divided
              >管理后台</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
      <template v-else>
        <el-button type="primary" @click="$router.push('/login')">登录</el-button>
      </template>
    </div>
  </el-header>
</template>

<script>
import { computed, ref } from 'vue'
import { useStore } from 'vuex'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

export default {
  name: 'AppHeader',

  setup() {
    const store = useStore()
    const router = useRouter()
    const route = useRoute()

    const searchText = ref('')
    const unreadCount = ref(0)

    const isLoggedIn = computed(() => store.state.auth.token)
    const username = computed(() => store.state.auth.userInfo.username)
    const userAvatar = computed(() => store.state.auth.userInfo.avatar || '')
    const isAdmin = computed(() => store.state.auth.roles.includes('admin'))

    const activeMenu = computed(() => route.path)

    const handleCommand = async (command) => {
      switch (command) {
        case 'profile':
          router.push('/profile')
          break
        case 'settings':
          router.push('/settings')
          break
        case 'admin':
          router.push('/admin')
          break
        case 'logout':
          try {
            await store.dispatch('auth/logout')
            ElMessage.success('退出成功')
            router.push('/login')
          } catch (error) {
            ElMessage.error('退出失败')
          }
          break
      }
    }

    const showNotifications = () => {
      // 显示通知面板的逻辑
    }

    return {
      searchText,
      unreadCount,
      isLoggedIn,
      username,
      userAvatar,
      isAdmin,
      activeMenu,
      handleCommand,
      showNotifications
    }
  }
}
</script>

<style scoped>
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 20px;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  margin-right: 40px;
  color: #409EFF;
}

.header-menu {
  border-bottom: none;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.search-input {
  width: 200px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.username {
  margin-left: 8px;
}

.notification-badge {
  margin-right: 10px;
}
</style>