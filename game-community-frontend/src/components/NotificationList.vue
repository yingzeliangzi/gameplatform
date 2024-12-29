<template>
  <div class="notification-list">
    <template v-if="notifications.length">
      <div
          v-for="notification in notifications"
          :key="notification.id"
          :class="['notification-item', { unread: !notification.isRead }]"
          @click="handleClick(notification)"
      >
        <div class="notification-icon">
          <el-icon :size="24" :color="getIconColor(notification.type)">
            <component :is="getIcon(notification.type)" />
          </el-icon>
        </div>

        <div class="notification-content">
          <div class="notification-header">
            <span class="notification-title">{{ notification.title }}</span>
            <span class="notification-time">{{ formatTime(notification.createdAt) }}</span>
          </div>
          <div class="notification-body">{{ notification.content }}</div>
        </div>

        <div class="notification-actions">
          <el-button
              v-if="!notification.isRead"
              link
              type="primary"
              @click.stop="$emit('read', notification.id)"
          >
            标记已读
          </el-button>
          <el-button
              link
              type="danger"
              @click.stop="handleDelete(notification)"
          >
            删除
          </el-button>
        </div>
      </div>
    </template>
    <el-empty
        v-else
        description="暂无通知"
    />
  </div>
</template>

<script>
import {
  Bell,
  Message,
  Discount,
  Calendar,
  ChatDotRound
} from '@element-plus/icons-vue'
import { formatDistanceToNow } from 'date-fns'
import { zhCN } from 'date-fns/locale'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'

export default {
  name: 'NotificationList',
  components: {
    Bell,
    Message,
    Discount,
    Calendar,
    ChatDotRound
  },

  props: {
    notifications: {
      type: Array,
      default: () => []
    }
  },

  emits: ['read', 'delete'],

  setup(props, { emit }) {
    const router = useRouter()

    // 获取通知图标
    const getIcon = (type) => {
      const iconMap = {
        'SYSTEM': 'Bell',
        'GAME_DISCOUNT': 'Discount',
        'EVENT_REMINDER': 'Calendar',
        'POST_REPLY': 'ChatDotRound',
        'POST_LIKE': 'ChatDotRound'
      }
      return iconMap[type] || 'Message'
    }

    // 获取图标颜色
    const getIconColor = (type) => {
      const colorMap = {
        'SYSTEM': '#409EFF',
        'GAME_DISCOUNT': '#F56C6C',
        'EVENT_REMINDER': '#67C23A',
        'POST_REPLY': '#E6A23C',
        'POST_LIKE': '#E6A23C'
      }
      return colorMap[type] || '#909399'
    }

    // 格式化时间
    const formatTime = (time) => {
      return formatDistanceToNow(new Date(time), {
        addSuffix: true,
        locale: zhCN
      })
    }

    // 处理通知点击
    const handleClick = (notification) => {
      if (!notification.isRead) {
        emit('read', notification.id)
      }
      if (notification.targetUrl) {
        router.push(notification.targetUrl)
      }
    }

    // 处理删除
    const handleDelete = async (notification) => {
      try {
        await ElMessageBox.confirm('确定要删除这条通知吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        emit('delete', notification.id)
      } catch {
        // 用户取消删除
      }
    }

    return {
      getIcon,
      getIconColor,
      formatTime,
      handleClick,
      handleDelete
    }
  }
}
</script>

<style scoped>
.notification-list {
  min-height: 200px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 15px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: background-color 0.3s;
}

.notification-item:hover {
  background-color: #f5f7fa;
}

.notification-item.unread {
  background-color: #edf5ff;
}

.notification-icon {
  flex-shrink: 0;
  margin-right: 15px;
  margin-top: 2px;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

.notification-title {
  font-weight: bold;
  color: #303133;
}

.notification-time {
  font-size: 12px;
  color: #909399;
}

.notification-body {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}

.notification-actions {
  flex-shrink: 0;
  margin-left: 15px;
  opacity: 0;
  transition: opacity 0.3s;
}

.notification-item:hover .notification-actions {
  opacity: 1;
}
</style>