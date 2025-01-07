<template>
  <el-card class="event-card" :class="cardStatusClass" :body-style="{ padding: 0 }">
    <!-- 活动封面 -->
    <div class="event-cover">
      <el-image
          :src="event.coverImage"
          :alt="event.title"
          fit="cover"
      >
        <template #placeholder>
          <div class="image-placeholder">
            <el-icon><Picture /></el-icon>
          </div>
        </template>
      </el-image>

      <!-- 活动状态标签 -->
      <div class="event-status-tag" :class="statusClass">
        {{ statusText }}
      </div>
    </div>

    <!-- 活动内容 -->
    <div class="event-content">
      <h3 class="event-title" :title="event.title">{{ event.title }}</h3>

      <!-- 活动信息 -->
      <div class="event-info">
        <div class="info-item">
          <el-icon><Calendar /></el-icon>
          <span>{{ formatEventTime }}</span>
        </div>

        <div class="info-item">
          <el-icon><Location /></el-icon>
          <span>{{ event.isOnline ? '线上活动' : event.location }}</span>
        </div>

        <div class="info-item">
          <el-icon><User /></el-icon>
          <span>{{ event.currentParticipants }}/{{ event.maxParticipants || '不限' }}</span>
        </div>

        <div v-if="event.gameId" class="info-item">
          <el-icon><Platform /></el-icon>
          <span>{{ event.gameName }}</span>
        </div>
      </div>

      <!-- 活动类型标签 -->
      <div class="event-tags">
        <el-tag :type="tagType" size="small">{{ typeText }}</el-tag>
        <el-tag
            v-if="event.isRegistered"
            type="success"
            size="small"
        >
          已报名
        </el-tag>
      </div>

      <!-- 活动操作 -->
      <div class="event-actions">
        <el-button
            v-if="canRegister"
            type="primary"
            :disabled="isFullOrEnded"
            @click="handleRegister"
        >
          {{ registerButtonText }}
        </el-button>
        <el-button
            v-if="event.isRegistered && canCancel"
            type="danger"
            @click="handleCancel"
        >
          取消报名
        </el-button>
        <el-button
            v-if="showDetails"
            text
            @click="handleViewDetails"
        >
          查看详情
        </el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Calendar,
  Location,
  User,
  Platform,
  Picture
} from '@element-plus/icons-vue'
import { eventApi } from '@/api'
import { useFormatTime } from '@/hooks/useFormatTime'

const props = defineProps({
  event: {
    type: Object,
    required: true
  },
  showDetails: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update', 'register', 'cancel'])
const router = useRouter()
const { formatDateTime } = useFormatTime()

// 格式化活动时间
const formatEventTime = computed(() => {
  const start = formatDateTime(props.event.startTime, 'MM-DD HH:mm')
  const end = formatDateTime(props.event.endTime, 'MM-DD HH:mm')
  return `${start} ~ ${end}`
})

// 活动状态相关
const statusClass = computed(() => {
  switch(props.event.status) {
    case 'UPCOMING':
      return 'status-upcoming'
    case 'ONGOING':
      return 'status-ongoing'
    case 'ENDED':
      return 'status-ended'
    case 'CANCELLED':
      return 'status-cancelled'
    default:
      return ''
  }
})

const statusText = computed(() => {
  switch(props.event.status) {
    case 'UPCOMING':
      return '即将开始'
    case 'ONGOING':
      return '进行中'
    case 'ENDED':
      return '已结束'
    case 'CANCELLED':
      return '已取消'
    default:
      return '未知状态'
  }
})

const cardStatusClass = computed(() => {
  if (props.event.status === 'CANCELLED' || props.event.status === 'ENDED') {
    return 'event-card--disabled'
  }
  return ''
})

// 活动类型相关
const typeText = computed(() => {
  switch(props.event.type) {
    case 'TOURNAMENT':
      return '比赛'
    case 'MEETUP':
      return '聚会'
    case 'EXHIBITION':
      return '展会'
    case 'WORKSHOP':
      return '工作坊'
    default:
      return '其他'
  }
})

const tagType = computed(() => {
  switch(props.event.type) {
    case 'TOURNAMENT':
      return 'danger'
    case 'MEETUP':
      return 'success'
    case 'EXHIBITION':
      return 'warning'
    case 'WORKSHOP':
      return 'info'
    default:
      return 'default'
  }
})

// 报名相关状态
const canRegister = computed(() => {
  return props.event.status === 'UPCOMING' && !props.event.isRegistered
})

const isFullOrEnded = computed(() => {
  return props.event.currentParticipants >= props.event.maxParticipants
})

const registerButtonText = computed(() => {
  if (isFullOrEnded.value) {
    return '名额已满'
  }
  return '立即报名'
})

const canCancel = computed(() => {
  return props.event.status === 'UPCOMING'
})

// 处理报名
const handleRegister = async () => {
  try {
    await eventApi.joinEvent(props.event.id)
    emit('update', {
      ...props.event,
      isRegistered: true,
      currentParticipants: props.event.currentParticipants + 1
    })
    emit('register', props.event)
    ElMessage.success('报名成功')
  } catch (error) {
    ElMessage.error(error?.message || '报名失败，请重试')
  }
}

// 处理取消报名
const handleCancel = async () => {
  try {
    await ElMessageBox.confirm(
        '确定要取消报名吗？',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    await eventApi.leaveEvent(props.event.id)
    emit('update', {
      ...props.event,
      isRegistered: false,
      currentParticipants: props.event.currentParticipants - 1
    })
    emit('cancel', props.event)
    ElMessage.success('已取消报名')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消报名失败，请重试')
    }
  }
}

// 查看详情
const handleViewDetails = () => {
  router.push(`/events/${props.event.id}`)
}
</script>

<style lang="scss" scoped>
.event-card {
  transition: all 0.3s;

  &:hover {
    transform: translateY(-4px);
  }

  &.event-card--disabled {
    opacity: 0.6;
  }

  .event-cover {
    position: relative;
    height: 200px;
    overflow: hidden;

    .el-image {
      width: 100%;
      height: 100%;
    }

    .image-placeholder {
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: var(--el-fill-color-light);
      color: var(--el-text-color-secondary);
      font-size: 24px;
    }

    .event-status-tag {
      position: absolute;
      top: 16px;
      right: 16px;
      padding: 4px 12px;
      border-radius: 12px;
      color: #fff;
      font-size: 12px;
      font-weight: 500;

      &.status-upcoming {
        background-color: var(--el-color-primary);
      }

      &.status-ongoing {
        background-color: var(--el-color-success);
      }

      &.status-ended {
        background-color: var(--el-color-info);
      }

      &.status-cancelled {
        background-color: var(--el-color-danger);
      }
    }
  }

  .event-content {
    padding: 16px;

    .event-title {
      margin: 0 0 16px;
      font-size: 18px;
      font-weight: 500;
      line-height: 1.4;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .event-info {
      margin-bottom: 16px;

      .info-item {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;
        color: var(--el-text-color-regular);
        font-size: 14px;

        .el-icon {
          flex-shrink: 0;
          font-size: 16px;
        }

        span {
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }

    .event-tags {
      margin-bottom: 16px;
      display: flex;
      gap: 8px;
    }

    .event-actions {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
    }
  }
}
</style>