<template>
  <div class="app-container">
    <header class="app-header">
      <div class="header-left">
        <div class="logo">游戏社区平台</div>
        <el-menu
            mode="horizontal"
            :default-active="activeMenu"
            router
        >
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/games">游戏库</el-menu-item>
          <el-menu-item index="/community">社区</el-menu-item>
          <el-menu-item index="/events">活动</el-menu-item>
        </el-menu>
      </div>

      <div class="header-right">
        <template v-if="isLoggedIn">
          <notification-bell />
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
    </header>

    <main class="app-main">
      <router-view v-slot="{ Component }">
        <transition name="fade-transform" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script>
import {ref, computed, onMounted, onBeforeUnmount} from 'vue'
import {useStore} from 'vuex'
import {useRoute, useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import NotificationBell from '@/components/NotificationBell.vue'
import {wsClient} from '@/utils/websocket'

export default {
  name: 'MainLayout',
  components: {
    NotificationBell
  },

  setup() {
    const store = useStore()
    const route = useRoute()
    const router = useRouter()

    const activeMenu = computed(() => route.path)
    const isLoggedIn = computed(() => store.state.auth.token)
    const username = computed(() => store.state.auth.userInfo.username)
    const userAvatar = computed(() => store.state.auth.userInfo.avatar)
    const isAdmin = computed(() => store.state.auth.roles.includes('admin'))

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

    onMounted(() => {
      if (isLoggedIn.value) {
        wsClient.connect()
      }
    })

    onBeforeUnmount(() => {
      wsClient.disconnect()
    })

    return {
      activeMenu,
      isLoggedIn,
      username,
      userAvatar,
      isAdmin,
      handleCommand
    }
  }
}
</script>

<style scoped>
.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}
</style>