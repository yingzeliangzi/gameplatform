<template>
  <div class="notification-bell">
    <el-badge :value="unreadCount" :hidden="!hasUnread">
      <el-button
          circle
          :icon="Bell"
          @click="toggleNotificationPanel"
      />
    </el-badge>

    <el-drawer
        v-model="drawerVisible"
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

        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="全部" name="all">
            <notification-list
                :notifications="notifications"
                @read="handleMarkRead"
                @delete="handleDelete"
            />
          </el-tab-pane>
          <el-tab-pane label="系统通知" name="SYSTEM">
            <notification-list
                :notifications="filteredNotifications"
                @read="handleMarkRead"
                @delete="handleDelete"
            />
          </el-tab-pane>
          <el-tab-pane label="游戏折扣" name="GAME_DISCOUNT">
            <notification-list
                :notifications="filteredNotifications"
                @read="handleMarkRead"
                @delete="handleDelete"
            />
          </el-tab-pane>
          <el-tab-pane label="活动提醒" name="EVENT_REMINDER">
            <notification-list
                :notifications="filteredNotifications"
                @read="handleMarkRead"
                @delete="handleDelete"
            />
          </el-tab-pane>
          <el-tab-pane label="社区互动" name="POST_REPLY">
            <notification-list
                :notifications="filteredNotifications"
                @read="handleMarkRead"
                @delete="handleDelete"
            />
          </el-tab-pane>
        </el-tabs>

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

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useStore } from 'vuex'
import { Bell } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import NotificationList from './NotificationList.vue'

const store = useStore()
const drawerVisible = ref(false)
const activeTab = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

// 计算属性
const unreadCount = computed(() => store.getters['notification/unreadCount'])
const hasUnread = computed(() => unreadCount.value > 0)
const notifications = computed(() => store.state.notification.notifications)
const total = computed(() => store.state.notification.total)
const hasMore = computed(() => notifications.value.length < total.value)

const filteredNotifications = computed(() => {
  if (activeTab.value === 'all') {
    return notifications.value
  }
  return notifications.value.filter(n => n.type === activeTab.value)
})

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
  try {
    await store.dispatch('notification/markAllAsRead')
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    ElMessage.error('操作失败，请重试')
  }
}

// 删除通知
const handleDelete = async (id) => {
  try {
    await store.dispatch('notification/deleteNotification', id)
    ElMessage.success('删除成功')
  } catch (error) {
    ElMessage.error('删除失败，请重试')
  }
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

// 生命周期钩子
onMounted(() => {
  store.dispatch('notification/getUnreadCount')
})
</script>

<style lang="scss" scoped>
.notification-bell {
  position: relative;
}

.notification-drawer {
  padding: 20px;

  .drawer-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h3 {
      margin: 0;
      font-size: 18px;
    }
  }

  .load-more {
    text-align: center;
    margin-top: 20px;
  }
}
</style>