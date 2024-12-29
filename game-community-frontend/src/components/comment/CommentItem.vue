<template>
  <div :class="['comment-item', { 'is-reply': isReply }]">
    <div class="comment-header">
      <div class="author-info">
        <el-avatar :src="comment.authorAvatar" :size="32" />
        <span class="author-name">{{ comment.authorName }}</span>
        <el-tag
            v-if="comment.authorRoles?.includes('ADMIN')"
            size="small"
            type="danger"
        >管理员</el-tag>
        <el-tag
            v-else-if="comment.authorRoles?.includes('MODERATOR')"
            size="small"
            type="warning"
        >版主</el-tag>
      </div>
      <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
    </div>

    <div class="comment-content">{{ comment.content }}</div>

    <div class="comment-actions">
      <el-button
          type="text"
          @click="$emit('like', comment)"
          :class="{ 'is-liked': comment.isLiked }"
      >
        <el-icon><StarFilled v-if="comment.isLiked" /><Star v-else /></el-icon>
        {{ comment.likeCount || 0 }}
      </el-button>

      <el-button
          type="text"
          @click="$emit('reply', comment)"
      >
        <el-icon><ChatDotRound /></el-icon>
        回复
      </el-button>

      <el-dropdown
          v-if="canManage"
          trigger="click"
      >
        <el-button type="text">
          <el-icon><More /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="$emit('edit', comment)">
              编辑
            </el-dropdown-item>
            <el-dropdown-item @click="$emit('delete', comment)">
              删除
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <slot></slot>
  </div>
</template>

<script setup>
import {
  Star,
  StarFilled,
  ChatDotRound,
  More
} from '@element-plus/icons-vue'
import { useFormatTime } from '@/hooks/useFormatTime'

const props = defineProps({
  comment: {
    type: Object,
    required: true
  },
  isReply: {
    type: Boolean,
    default: false
  },
  canManage: {
    type: Boolean,
    default: false
  }
})

defineEmits(['like', 'reply', 'edit', 'delete'])

const { formatTime } = useFormatTime()
</script>

<style lang="scss" scoped>
.comment-item {
  padding: 15px 0;
  border-bottom: 1px solid #eee;

  &:last-child {
    border-bottom: none;
  }

  &.is-reply {
    margin-left: 40px;
    padding: 10px 0;
    border-bottom: none;
    background: #f8f9fa;
    border-radius: 4px;
  }

  .comment-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;

    .author-info {
      display: flex;
      align-items: center;
      gap: 10px;

      .author-name {
        font-weight: 500;
        color: #303133;
      }
    }

    .comment-time {
      font-size: 12px;
      color: #909399;
    }
  }

  .comment-content {
    margin: 10px 0;
    line-height: 1.6;
    color: #606266;
    white-space: pre-wrap;
    word-break: break-all;
  }

  .comment-actions {
    display: flex;
    align-items: center;
    gap: 15px;

    .el-button {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      height: auto;
      padding: 0;

      &.is-liked {
        color: #409EFF;
      }
    }
  }
}
</style>