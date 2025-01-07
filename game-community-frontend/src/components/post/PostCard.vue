// src/components/post/PostCard.vue
<template>
  <div class="post-card">
    <!-- 作者信息 -->
    <div class="post-author">
      <el-avatar
          :size="40"
          :src="post.author.avatar"
          @click="handleAuthorClick"
      >
        {{ post.author.nickname?.charAt(0) }}
      </el-avatar>
      <div class="author-info">
        <div class="author-name" @click="handleAuthorClick">
          {{ post.author.nickname }}
        </div>
        <div class="post-time">
          {{ formatTime(post.createdAt) }}
        </div>
      </div>
    </div>

    <!-- 帖子内容 -->
    <div class="post-content" @click="handlePostClick">
      <h2 class="post-title">{{ post.title }}</h2>
      <div v-if="post.gameId" class="post-game">
        <el-tag size="small" type="success">{{ post.gameName }}</el-tag>
      </div>
      <div class="post-text">{{ post.content }}</div>
    </div>

    <!-- 帖子统计 -->
    <div class="post-stats">
      <div class="stat-item">
        <el-icon><View /></el-icon>
        <span>{{ post.viewCount }}</span>
      </div>
      <div class="stat-item">
        <el-icon><ChatDotRound /></el-icon>
        <span>{{ post.commentCount }}</span>
      </div>
      <div class="stat-item">
        <el-icon><Star /></el-icon>
        <span>{{ post.collectCount }}</span>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="post-actions">
      <el-button
          :type="post.isLiked ? 'danger' : 'default'"
          :icon="post.isLiked ? ThumbUpFilled : ThumbUp"
          text
          @click="handleLike"
      >
        {{ post.likeCount }}
      </el-button>
      <el-button
          :type="post.isCollected ? 'danger' : 'default'"
          :icon="post.isCollected ? StarFilled : Star"
          text
          @click="handleCollect"
      >
        收藏
      </el-button>
      <el-button
          icon="Share"
          text
          @click="handleShare"
      >
        分享
      </el-button>
      <el-dropdown trigger="click" @command="handleCommand">
        <el-button icon="More" text />
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="report">举报</el-dropdown-item>
            <el-dropdown-item
                v-if="isAuthor"
                command="edit"
            >
              编辑
            </el-dropdown-item>
            <el-dropdown-item
                v-if="isAuthor"
                command="delete"
                divided
                type="danger"
            >
              删除
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <!-- 举报对话框 -->
    <el-dialog
        v-model="reportDialogVisible"
        title="举报内容"
        width="500px"
    >
      <el-form
          ref="reportFormRef"
          :model="reportForm"
          :rules="reportRules"
          label-width="80px"
      >
        <el-form-item label="举报原因" prop="reason">
          <el-select v-model="reportForm.reason" placeholder="请选择举报原因">
            <el-option
                v-for="item in reportReasons"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="详细说明" prop="description">
          <el-input
              v-model="reportForm.description"
              type="textarea"
              :rows="4"
              placeholder="请详细描述问题"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReport">提交</el-button>
      </template>
    </el-dialog>

    <!-- 删除确认对话框 -->
    <el-dialog
        v-model="deleteDialogVisible"
        title="确认删除"
        width="400px"
    >
      <p>确定要删除这篇帖子吗？删除后将无法恢复。</p>
      <template #footer>
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmDelete">确定删除</el-button>
      </template>
    </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  View,
  ChatDotRound,
  Star,
  StarFilled,
  ThumbUp,
  ThumbUpFilled,
  Share
} from '@element-plus/icons-vue'
import { postApi } from '@/api'
import { useFormatTime } from '@/hooks/useFormatTime'

const props = defineProps({
  post: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['update:post', 'delete'])

const router = useRouter()
const { formatTime } = useFormatTime()

// 是否是作者
const isAuthor = computed(() => {
  return props.post.author.id === store.state.user.id
})

// 举报相关
const reportDialogVisible = ref(false)
const reportFormRef = ref(null)
const reportForm = ref({
  reason: '',
  description: ''
})

const reportReasons = [
  { label: '垃圾广告', value: 'spam' },
  { label: '违法内容', value: 'illegal' },
  { label: '侵犯权益', value: 'rights' },
  { label: '不当内容', value: 'inappropriate' },
  { label: '其他原因', value: 'other' }
]

const reportRules = {
  reason: [{ required: true, message: '请选择举报原因', trigger: 'change' }],
  description: [{ required: true, message: '请填写详细说明', trigger: 'blur' }]
}

// 删除相关
const deleteDialogVisible = ref(false)

// 处理点击作者
const handleAuthorClick = () => {
  router.push(`/user/${props.post.author.id}`)
}

// 处理点击帖子
const handlePostClick = () => {
  router.push(`/post/${props.post.id}`)
}

// 处理点赞
const handleLike = async () => {
  try {
    if (props.post.isLiked) {
      await postApi.unlikePost(props.post.id)
      emit('update:post', {
        ...props.post,
        isLiked: false,
        likeCount: props.post.likeCount - 1
      })
      ElMessage.success('已取消点赞')
    } else {
      await postApi.likePost(props.post.id)
      emit('update:post', {
        ...props.post,
        isLiked: true,
        likeCount: props.post.likeCount + 1
      })
      ElMessage.success('点赞成功')
    }
  } catch (error) {
    ElMessage.error('操作失败，请重试')
  }
}

// 处理收藏
const handleCollect = async () => {
  try {
    if (props.post.isCollected) {
      await postApi.uncollectPost(props.post.id)
      emit('update:post', {
        ...props.post,
        isCollected: false,
        collectCount: props.post.collectCount - 1
      })
      ElMessage.success('已取消收藏')
    } else {
      await postApi.collectPost(props.post.id)
      emit('update:post', {
        ...props.post,
        isCollected: true,
        collectCount: props.post.collectCount + 1
      })
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    ElMessage.error('操作失败，请重试')
  }
}

// 处理分享
const handleShare = () => {
  const shareUrl = `${window.location.origin}/post/${props.post.id}`
  navigator.clipboard.writeText(shareUrl).then(() => {
    ElMessage.success('链接已复制到剪贴板')
  })
}

// 处理下拉菜单命令
const handleCommand = (command) => {
  switch (command) {
    case 'report':
      reportDialogVisible.value = true
      break
    case 'edit':
      router.push(`/post/edit/${props.post.id}`)
      break
    case 'delete':
      deleteDialogVisible.value = true
      break
  }
}

// 提交举报
const submitReport = async () => {
  if (!reportFormRef.value) return

  try {
    await reportFormRef.value.validate()
    await postApi.reportPost(props.post.id, reportForm.value)
    ElMessage.success('举报已提交')
    reportDialogVisible.value = false
    reportForm.value = { reason: '', description: '' }
  } catch (error) {
    if (error?.message) {
      ElMessage.error(error.message)
    }
  }
}

// 确认删除
const confirmDelete = async () => {
  try {
    await postApi.deletePost(props.post.id)
    ElMessage.success('删除成功')
    deleteDialogVisible.value = false
    emit('delete', props.post.id)
  } catch (error) {
    ElMessage.error('删除失败，请重试')
  }
}
</script>

<style lang="scss" scoped>
.post-card {
  padding: 20px;
  border-radius: 8px;
  background: var(--el-bg-color);
  box-shadow: var(--el-box-shadow-light);

  .post-author {
    display: flex;
    align-items: center;
    margin-bottom: 16px;

    .el-avatar {
      cursor: pointer;
      margin-right: 12px;
    }

    .author-info {
      .author-name {
        font-weight: 500;
        cursor: pointer;
        &:hover {
          color: var(--el-color-primary);
        }
      }

      .post-time {
        font-size: 12px;
        color: var(--el-text-color-secondary);
        margin-top: 4px;
      }
    }
  }

  .post-content {
    cursor: pointer;
    margin-bottom: 16px;

    .post-title {
      margin: 0 0 12px;
      font-size: 18px;
      font-weight: 500;
      line-height: 1.4;
    }

    .post-game {
      margin-bottom: 8px;
    }

    .post-text {
      color: var(--el-text-color-regular);
      line-height: 1.6;
      max-height: 4.8em;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 3;
      -webkit-box-orient: vertical;
    }
  }

  .post-stats {
    display: flex;
    gap: 16px;
    padding: 12px 0;
    border-top: 1px solid var(--el-border-color-lighter);
    border-bottom: 1px solid var(--el-border-color-lighter);
    margin-bottom: 12px;

    .stat-item {
      display: flex;
      align-items: center;
      gap: 4px;
      color: var(--el-text-color-secondary);
      font-size: 14px;
    }
  }

  .post-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }
}
</style>