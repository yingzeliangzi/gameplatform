<template>
  <div
      class="notification-item"
      :class="{ unread: !notification.isRead }"
      @click="handleClick"
  >
    <div class="notification-icon">
      <el-icon :size="24" :color="iconColor">
        <component :is="iconType" />
      </el-icon>
    </div>

    <div class="notification-content">
      <div class="notification-title">
        {{ notification.title }}
      </div>
      <div class="notification-message">
        {{ notification.content }}
      </div>
      <div class="notification-time">
        {{ formatTime(notification.createdAt) }}
      </div>
    </div>

    <div class="notification-actions">
      <el-button
          v-if="!notification.isRead"
          type="primary"
          text
          size="small"
          @click.stop="handleMarkAsRead"
      >
        标为已读
      </el-button>
      <el-button
          type="danger"
          text
          size="small"
          @click.stop="handleDelete"
      >
        删除
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useFormatTime } from '@/hooks/useFormatTime'
import {
  Bell,
  Message,
  ChatDotRound,
  Calendar,
  Star
} from '@element-plus/icons-vue'

const props = defineProps({
  notification: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['update', 'delete'])
const router = useRouter()
const { formatTime } = useFormatTime()

// 根据不同类型显示不同图标和颜色
const iconType = computed(() => {
  switch (props.notification.type) {
    case 'SYSTEM':
      return Bell
    case 'MESSAGE':
      return Message
    case 'COMMENT':
      return ChatDotRound
    case 'EVENT':
      return Calendar
    case 'GAME':
      return Star
    default:
      return Bell
  }
})

const iconColor = computed(() => {
  switch (props.notification.type) {
    case 'SYSTEM':
      return '#409EFF'
    case 'MESSAGE':
      return '#67C23A'
    case 'COMMENT':
      return '#E6A23C'
    case 'EVENT':
      return '#F56C6C'
    case 'GAME':
      return '#909399'
    default:
      return '#409EFF'
  }
})

// 处理点击通知
const handleClick = () => {
  if (props.notification.targetUrl) {
    router.push(props.notification.targetUrl)
  }
  if (!props.notification.isRead) {
    handleMarkAsRead()
  }
}

// 标记为已读
const handleMarkAsRead = async () => {
  try {
    await notificationApi.markAsRead(props.notification.id)
    emit('update', {
      ...props.notification,
      isRead: true
    })
  } catch (error) {
    ElMessage.error('操作失败，请重试')
  }
}

// 删除通知
const handleDelete = async () => {
  try {
    await notificationApi.deleteNotification(props.notification.id)
    emit('delete', props.notification.id)
    ElMessage.success('删除成功')
  } catch (error) {
    ElMessage.error('删除失败，请重试')
  }
}
</script>

<style lang="scss" scoped>
.notification-item {
  position: relative;
  padding: 16px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  cursor: pointer;
  transition: background-color 0.3s;

  &:hover {
    background-color: var(--el-fill-color-light);

    .notification-actions {
      opacity: 1;
    }
  }

  &.unread {
    background-color: var(--el-color-primary-light-9);

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 4px;
      height: 16px;
      background-color: var(--el-color-primary);
      border-radius: 0 2px 2px 0;
    }
  }

  .notification-icon {
    flex-shrink: 0;
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    background-color: var(--el-fill-color-light);
  }

  .notification-content {
    flex: 1;
    min-width: 0;

    .notification-title {
      font-weight: 500;
      margin-bottom: 4px;
      color: var(--el-text-color-primary);
    }

    .notification-message {
      font-size: 14px;
      color: var(--el-text-color-regular);
      margin-bottom: 8px;
    }

    .notification-time {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }

  .notification-actions {
    opacity: 0;
    transition: opacity 0.3s;
  }
}
</style>