<template>
  <div class="notification-list">
    <!-- 工具栏 -->
    <div class="notification-toolbar">
      <div class="notification-filter">
        <el-radio-group v-model="currentType" size="small">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button
              v-for="type in notificationTypes"
              :key="type.value"
              :label="type.value"
          >
            {{ type.label }}
          </el-radio-button>
        </el-radio-group>
      </div>

      <div class="notification-actions">
        <el-button
            v-if="hasUnread"
            type="primary"
            text
            @click="handleMarkAllRead"
        >
          全部标为已读
        </el-button>
        <el-popconfirm
            title="确定要清空所有通知吗？"
            @confirm="handleClearAll"
        >
          <template #reference>
            <el-button type="danger" text>清空全部</el-button>
          </template>
        </el-popconfirm>
      </div>
    </div>

    <!-- 通知列表 -->
    <div class="notification-content">
      <el-scrollbar v-loading="loading">
        <template v-if="notifications.length">
          <notification-item
              v-for="notification in notifications"
              :key="notification.id"
              :notification="notification"
              @update="handleNotificationUpdate"
              @delete="handleNotificationDelete"
          />
        </template>
        <el-empty
            v-else
            :description="loading ? '加载中...' : '暂无通知'"
        />
      </el-scrollbar>
    </div>

    <!-- 加载更多 -->
    <div
        v-if="hasMore"
        class="notification-load-more"
        :class="{ loading }"
    >
      <el-button
          :loading="loading"
          text
          @click="loadMore"
      >
        加载更多
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watchEffect } from 'vue'
import { ElMessage } from 'element-plus'
import NotificationItem from './NotificationItem.vue'
import { notificationApi } from '@/api'

const notificationTypes = [
  { label: '系统', value: 'SYSTEM' },
  { label: '消息', value: 'MESSAGE' },
  { label: '评论', value: 'COMMENT' },
  { label: '活动', value: 'EVENT' },
  { label: '游戏', value: 'GAME' }
]

// 状态变量
const loading = ref(false)
const notifications = ref([])
const currentType = ref('')
const currentPage = ref(1)
const hasMore = ref(false)
const total = ref(0)

// 计算属性
const hasUnread = computed(() => {
  return notifications.value.some(n => !n.isRead)
})

// 加载通知
const loadNotifications = async (page = 1) => {
  loading.value = true
  try {
    const res = await notificationApi.getNotifications({
      page,
      size: 20,
      type: currentType.value || undefined
    })

    if (page === 1) {
      notifications.value = res.data
    } else {
      notifications.value.push(...res.data)
    }

    total.value = res.total
    hasMore.value = notifications.value.length < total.value
    currentPage.value = page
  } catch (error) {
    ElMessage.error('加载通知失败')
  } finally {
    loading.value = false
  }
}

// 监听类型变化，重新加载
watchEffect(() => {
  currentType.value && loadNotifications(1)
})

// 加载更多
const loadMore = () => {
  if (loading.value || !hasMore.value) return
  loadNotifications(currentPage.value + 1)
}

// 更新通知
const handleNotificationUpdate = (updatedNotification) => {
  const index = notifications.value.findIndex(n => n.id === updatedNotification.id)
  if (index !== -1) {
    notifications.value[index] = updatedNotification
  }
}

// 删除通知
const handleNotificationDelete = (id) => {
  notifications.value = notifications.value.filter(n => n.id !== id)
  total.value--
}

// 标记所有为已读
const handleMarkAllRead = async () => {
  try {
    await notificationApi.markAllAsRead()
    notifications.value = notifications.value.map(n => ({
      ...n,
      isRead: true
    }))
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    ElMessage.error('操作失败，请重试')
  }
}

// 清空所有通知
const handleClearAll = async () => {
  try {
    await notificationApi.deleteAllNotifications()
    notifications.value = []
    total.value = 0
    ElMessage.success('已清空所有通知')
  } catch (error) {
    ElMessage.error('操作失败，请重试')
  }
}

// 初始加载
loadNotifications(1)
</script>

<style lang="scss" scoped>
.notification-list {
  height: 100%;
  display: flex;
  flex-direction: column;

  .notification-toolbar {
    padding: 16px;
    border-bottom: 1px solid var(--el-border-color-light);
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .notification-content {
    flex: 1;
    overflow: hidden;
  }

  .notification-load-more {
    padding: 16px;
    text-align: center;
    border-top: 1px solid var(--el-border-color-light);
  }
}
</style>