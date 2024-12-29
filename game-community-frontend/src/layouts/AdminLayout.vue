<template>
  <el-container class="admin-container">
    <el-aside width="auto">
      <app-sidebar />
    </el-aside>

    <el-container>
      <el-header height="60px">
        <div class="admin-header">
          <div class="left">
            <el-button
                type="text"
                :icon="isCollapse ? Expand : Fold"
                @click="toggleSidebar"
            />
            <el-breadcrumb>
              <el-breadcrumb-item
                  v-for="(item, index) in breadcrumbs"
                  :key="index"
                  :to="item.path"
              >
                {{ item.title }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>

          <div class="right">
            <notification-bell />
            <el-dropdown>
              <div class="admin-user">
                <el-avatar :size="32" :src="userAvatar" />
                <span class="username">{{ username }}</span>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="goToProfile">个人中心</el-dropdown-item>
                  <el-dropdown-item @click="goToSettings">设置</el-dropdown-item>
                  <el-dropdown-item
                      divided
                      @click="handleLogout"
                  >退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>

      <el-main>
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { Expand, Fold } from '@element-plus/icons-vue'
import AppSidebar from '@/components/Sidebar.vue'
import NotificationBell from '@/components/NotificationBell.vue'

const store = useStore()
const router = useRouter()
const route = useRoute()

const isCollapse = ref(false)
const username = computed(() => store.state.auth.userInfo.username)
const userAvatar = computed(() => store.state.auth.userInfo.avatar || '')

// 面包屑导航
const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta?.title)
  return matched.map(item => ({
    title: item.meta.title,
    path: item.path
  }))
})

// 切换侧边栏
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

// 导航方法
const goToProfile = () => router.push('/profile')
const goToSettings = () => router.push('/settings')

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      type: 'warning'
    })
    await store.dispatch('auth/logout')
    router.push('/login')
  } catch {
    // 用户取消操作
  }
}
</script>

<style lang="scss" scoped>
.admin-container {
  min-height: 100vh;

  .admin-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 100%;
    padding: 0 20px;
    background-color: #fff;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);

    .left {
      display: flex;
      align-items: center;
      gap: 20px;
    }

    .right {
      display: flex;
      align-items: center;
      gap: 20px;
    }

    .admin-user {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;

      .username {
        color: #606266;
      }
    }
  }

  .el-main {
    padding: 20px;
    background-color: #f5f7fa;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>