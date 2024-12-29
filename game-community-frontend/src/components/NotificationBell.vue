<template>
  <div class="notification-bell">
    <el-badge :value="unreadCount" :hidden="!hasUnread">
      <el-button
          circle
          :icon="Bell"
          @click="toggleNotificationPanel"
      />
    </el-badge>

    <!-- 通知面板 -->
    <el-drawer
        v-model="drawerVisible"
        title="消息通知"
        :size="400"
        :with-header="false"
    >
      <div class="notification-drawer">
        <div class="drawer-header">
          <h3>消息通知</h3>
          <el-button
              v-if="hasUnread"
              type="primary"
              link
              @click="handleMarkAllRead"
          >
            全部标为已读
          </el-button>
        </div>

        <!-- 通知类型切换 -->
        <el-tabs v-model="activeTab" @tab-click="handleTabChange">
          <el-tab-pane label="全部" name="all">
            <notification-list
                :notifications="notifications"
                @read="handleMarkRead"
                @delete="handleDelete"
            />
          </el-tab-pane>
          <el-tab-pane label="系统通知" name="SYSTEM">
            <notification-list
                :notifications="notifications"
                @read="handleMarkRead"
                @delete="handleDelete"
            />
          </el-tab-pane>
          <el-tab-pane label="游戏折扣" name="GAME_DISCOUNT">
            <notification-list
                :notifications="notifications"
                @read="handleMarkRead"
                @delete="handleDelete"
            />
          </el-tab-pane>
          <el-tab-pane label="活动提醒" name="EVENT_REMINDER">
            <notification-list
                :notifications="notifications"
                @read="handleMarkRead"
                @delete="handleDelete"
            />
          </el-tab-pane>
          <el-tab-pane label="回复通知" name="POST_REPLY">
            <notification-list
                :notifications="notifications"
                @read="handleMarkRead"
                @delete="handleDelete"
            />
          </el-tab-pane>
        </el-tabs>

        <!-- 加载更多 -->
        <div
            v-if="hasMore"
            class="load-more"
            v-loading="loading"
        >
          <el-button link @click="loadMore">加载更多</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { ref, computed, watch, onMounted } from 'vue'
import { useStore } from 'vuex'
import { Bell } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import NotificationList from './NotificationList.vue'

export default {
  name: 'NotificationBell',
  components: { NotificationList },

  setup() {
    const store = useStore()
    const drawerVisible = ref(false)
    const activeTab = ref('all')
    const currentPage = ref(1)
    const pageSize = ref(10)
    const loading = ref(false)

    const unreadCount = computed(() => store.getters['notification/unreadTotal'])
    const hasUnread = computed(() => store.getters['notification/hasUnread'])
    const notifications = computed(() => store.state.notification.notifications)
    const total = computed(() => store.state.notification.total)
    const hasMore = computed(() => notifications.value.length < total.value)

    // 加载通知数据
    const loadNotifications = async (page = 1) => {
      loading.value = true
      try {
        await store.dispatch('notification/fetchNotifications', {
          page: page - 1,
          size: pageSize.value,
          type: activeTab.value === 'all' ? null : activeTab.value
        })
      } finally {
        loading.value = false
      }
    }

    // 加载更多
    const loadMore = () => {
      currentPage.value++
      loadNotifications(currentPage.value)
    }

    // 切换通知面板
    const toggleNotificationPanel = () => {
      drawerVisible.value = !drawerVisible.value
      if (drawerVisible.value) {
        loadNotifications()
      }
    }

    // 标记已读
    const handleMarkRead = async (id) => {
      await store.dispatch('notification/markAsRead', id)
    }

    // 全部标记已读
    const handleMarkAllRead = async () => {
      await store.dispatch('notification/markAllAsRead')
      ElMessage.success('已全部标记为已读')
    }

    // 删除通知
    const handleDelete = async (id) => {
      await store.dispatch('notification/deleteNotification', id)
    }

    // 切换标签页
    const handleTabChange = () => {
      currentPage.value = 1
      loadNotifications()
    }

    // 监听面板显示状态
    watch(drawerVisible, (newVal) => {
      if (!newVal) {
        activeTab.value = 'all'
        currentPage.value = 1
      }
    })

    onMounted(() => {
      store.dispatch('notification/fetchUnreadCount')
    })

    return {
      drawerVisible,
      activeTab,
      loading,
      unreadCount,
      hasUnread,
      notifications,
      hasMore,
      Bell,
      toggleNotificationPanel,
      handleMarkRead,
      handleMarkAllRead,
      handleDelete,
      handleTabChange,
      loadMore
    }
  }
}
</script>

<style scoped>
.notification-bell {
  position: relative;
}

.notification-drawer {
  padding: 20px;
}

.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.drawer-header h3 {
  margin: 0;
}

.load-more {
  text-align: center;
  margin-top: 20px;
}
</style>