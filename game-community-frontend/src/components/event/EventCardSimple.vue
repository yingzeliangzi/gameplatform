<template>
  <div class="event-card-simple" @click="handleClick">
    <!-- 活动日期 -->
    <div class="event-date">
      <div class="date-month">{{ month }}</div>
      <div class="date-day">{{ day }}</div>
    </div>

    <!-- 活动信息 -->
    <div class="event-info">
      <h4 class="event-title" :title="event.title">
        <el-tag
            v-if="event.isRegistered"
            size="small"
            type="success"
            effect="plain"
        >
          已报名
        </el-tag>
        {{ event.title }}
      </h4>

      <div class="event-meta">
        <span class="meta-item">
          <el-icon><Timer /></el-icon>
          {{ startTime }}
        </span>
        <span class="meta-item">
          <el-icon><Location /></el-icon>
          {{ event.isOnline ? '线上活动' : event.location }}
        </span>
      </div>

      <!-- 活动状态 -->
      <div class="event-status">
        <el-tag
            :type="statusType"
            :effect="statusEffect"
            size="small"
        >
          {{ statusText }}
        </el-tag>
        <span class="participants-count">
          {{ event.currentParticipants }}/{{ event.maxParticipants || '不限' }}
        </span>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="event-actions">
      <template v-if="!hideActions">
        <el-button
            v-if="canRegister"
            type="primary"
            link
            :disabled="isFullOrEnded"
            @click.stop="handleRegister"
        >
          {{ registerButtonText }}
        </el-button>
        <el-button
            v-if="event.isRegistered && canCancel"
            type="danger"
            link
            @click.stop="handleCancel"
        >
          取消报名
        </el-button>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Timer, Location } from '@element-plus/icons-vue'
import { eventApi } from '@/api'
import { useFormatTime } from '@/hooks/useFormatTime'

const props = defineProps({
  event: {
    type: Object,
    required: true
  },
  hideActions: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update', 'register', 'cancel'])
const router = useRouter()
const { formatDateTime } = useFormatTime()

// 日期相关
const startDate = computed(() => new Date(props.event.startTime))
const month = computed(() => startDate.value.toLocaleString('zh-CN', { month: 'short' }))
const day = computed(() => startDate.value.getDate())
const startTime = computed(() => formatDateTime(props.event.startTime, 'HH:mm'))

// 状态相关
const statusType = computed(() => {
  switch(props.event.status) {
    case 'UPCOMING':
      return 'primary'
    case 'ONGOING':
      return 'success'
    case 'ENDED':
      return 'info'
    case 'CANCELLED':
      return 'danger'
    default:
      return 'info'
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

const statusEffect = computed(() => {
  return props.event.status === 'ONGOING' ? 'dark' : 'plain'
})

// 报名相关
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

// 处理点击事件
const handleClick = () => {
  router.push(`/events/${props.event.id}`)
}

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
    await ElMessageBox.confirm('确定要取消报名吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

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
</script>

<style lang="scss" scoped>
.event-card-simple {
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 20px;
  cursor: pointer;
  transition: background-color 0.3s;
  border-radius: 8px;

  &:hover {
    background-color: var(--el-fill-color-light);
  }

  .event-date {
    flex-shrink: 0;
    width: 60px;
    text-align: center;

    .date-month {
      font-size: 14px;
      color: var(--el-text-color-secondary);
      margin-bottom: 4px;
    }

    .date-day {
      font-size: 24px;
      font-weight: bold;
      color: var(--el-text-color-primary);
    }
  }

  .event-info {
    flex: 1;
    min-width: 0;

    .event-title {
      margin: 0 0 8px;
      font-size: 16px;
      font-weight: 500;
      line-height: 1.4;
      display: flex;
      align-items: center;
      gap: 8px;

      .el-tag {
        flex-shrink: 0;
      }

      span {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    .event-meta {
      margin-bottom: 8px;
      display: flex;
      gap: 16px;
      color: var(--el-text-color-regular);
      font-size: 14px;

      .meta-item {
        display: flex;
        align-items: center;
        gap: 4px;

        .el-icon {
          flex-shrink: 0;
        }
      }
    }

    .event-status {
      display: flex;
      align-items: center;
      gap: 12px;

      .participants-count {
        font-size: 14px;
        color: var(--el-text-color-secondary);
      }
    }
  }

  .event-actions {
    flex-shrink: 0;
    opacity: 0;
    transition: opacity 0.3s;
    display: flex;
    gap: 8px;
  }

  &:hover .event-actions {
    opacity: 1;
  }
}
</style>