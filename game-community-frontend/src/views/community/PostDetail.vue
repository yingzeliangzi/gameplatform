<template>
  <div class="post-detail-container">
    <el-card v-loading="loading">
      <!-- 帖子内容 -->
      <div class="post-header">
        <h1 class="post-title">{{ post.title }}</h1>
        <div class="post-meta">
          <div class="author-info">
            <el-avatar :src="post.author?.avatar" :size="40" />
            <span class="author-name">{{ post.author?.nickname }}</span>
          </div>
          <div class="post-info">
            <span class="post-time">发布于 {{ formatTime(post.createdAt) }}</span>
            <span v-if="post.gameName" class="game-tag">
              <el-tag size="small">{{ post.gameName }}</el-tag>
            </span>
          </div>
        </div>
      </div>

      <div class="post-content">{{ post.content }}</div>

      <div class="post-actions">
        <el-button type="primary" plain :icon="ThumbsUp" @click="handleLike">
          {{ post.likeCount }} 点赞
        </el-button>
        <el-button type="danger" plain :icon="Warning" @click="showReportDialog">
          举报
        </el-button>
      </div>

      <!-- 评论区 -->
      <div class="comments-section">
        <h2>评论 ({{ post.commentCount }})</h2>

        <!-- 评论输入框 -->
        <div class="comment-input">
          <el-input
              v-model="commentContent"
              type="textarea"
              :rows="3"
              placeholder="写下你的评论..."
          />
          <el-button
              type="primary"
              :loading="commenting"
              @click="submitComment"
          >
            发表评论
          </el-button>
        </div>

        <!-- 评论列表 -->
        <div class="comment-list">
          <div
              v-for="comment in comments"
              :key="comment.id"
              class="comment-item"
          >
            <div class="comment-header">
              <div class="commenter-info">
                <el-avatar :src="comment.author?.avatar" :size="32" />
                <span class="commenter-name">{{ comment.author?.nickname }}</span>
                <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
              </div>
              <div class="comment-actions">
                <el-button
                    text
                    type="primary"
                    @click="showReplyInput(comment)"
                >
                  回复
                </el-button>
                <el-button
                    text
                    type="danger"
                    @click="showReportDialog('comment', comment.id)"
                >
                  举报
                </el-button>
              </div>
            </div>

            <div class="comment-content">{{ comment.content }}</div>

            <!-- 回复列表 -->
            <div v-if="comment.replies?.length" class="reply-list">
              <div
                  v-for="reply in comment.replies"
                  :key="reply.id"
                  class="reply-item"
              >
                <div class="reply-header">
                  <el-avatar :src="reply.author?.avatar" :size="24" />
                  <span class="replier-name">{{ reply.author?.nickname }}</span>
                  <span class="reply-time">{{ formatTime(reply.createdAt) }}</span>
                </div>
                <div class="reply-content">{{ reply.content }}</div>
              </div>
            </div>

            <!-- 回复输入框 -->
            <div v-if="activeReplyId === comment.id" class="reply-input">
              <el-input
                  v-model="replyContent"
                  type="textarea"
                  :rows="2"
                  placeholder="回复评论..."
              />
              <div class="reply-actions">
                <el-button @click="cancelReply">取消</el-button>
                <el-button
                    type="primary"
                    :loading="replying"
                    @click="submitReply(comment.id)"
                >
                  回复
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 举报对话框 -->
    <el-dialog
        v-model="reportDialogVisible"
        title="举报内容"
        width="500px"
    >
      <el-form
          ref="reportForm"
          :model="reportForm"
          :rules="reportRules"
          label-width="80px"
      >
        <el-form-item label="举报原因" prop="reason">
          <el-input
              v-model="reportForm.reason"
              type="textarea"
              :rows="4"
              placeholder="请详细描述举报原因..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="reportDialogVisible = false">取消</el-button>
          <el-button
              type="primary"
              :loading="reporting"
              @click="submitReport"
          >
            提交
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ThumbsUp, Warning } from '@element-plus/icons-vue'
import {
  getPostDetail,
  addComment,
  replyComment,
  reportContent,
  likePost
} from '@/api/post'
import { formatDistanceToNow } from 'date-fns'
import { zhCN } from 'date-fns/locale'

export default {
  name: 'PostDetail',
  components: {
    ThumbsUp,
    Warning
  },

  setup() {
    const route = useRoute()
    const loading = ref(false)
    const commenting = ref(false)
    const replying = ref(false)
    const reporting = ref(false)
    const post = ref({})
    const comments = ref([])
    const commentContent = ref('')
    const replyContent = ref('')
    const activeReplyId = ref(null)
    const reportDialogVisible = ref(false)
    const reportForm = ref({
      type: 'post',
      targetId: null,
      reason: ''
    })

    const reportRules = {
      reason: [
        { required: true, message: '请输入举报原因', trigger: 'blur' },
        { min: 10, message: '举报原因至少10个字符', trigger: 'blur' }
      ]
    }

    // 获取帖子详情
    const fetchPostDetail = async () => {
      loading.value = true
      try {
        const res = await getPostDetail(route.params.id)
        post.value = res.data
        comments.value = res.data.comments || []
      } catch (error) {
        ElMessage.error('获取帖子详情失败')
      } finally {
        loading.value = false
      }
    }

    // 提交评论
    const submitComment = async () => {
      if (!commentContent.value.trim()) {
        ElMessage.warning('请输入评论内容')
        return
      }

      commenting.value = true
      try {
        const res = await addComment(post.value.id, {
          content: commentContent.value
        })
        comments.value.unshift(res.data)
        commentContent.value = ''
        ElMessage.success('评论成功')
      } catch (error) {
        ElMessage.error('评论失败')
      } finally {
        commenting.value = false
      }
    }

    // 回复评论
    const submitReply = async (commentId) => {
      if (!replyContent.value.trim()) {
        ElMessage.warning('请输入回复内容')
        return
      }

      replying.value = true
      try {
        const res = await replyComment(commentId, {
          content: replyContent.value
        })
        // 更新评论列表中的回复
        const comment = comments.value.find(c => c.id === commentId)
        if (comment) {
          if (!comment.replies) comment.replies = []
          comment.replies.push(res.data)
        }
        replyContent.value = ''
        activeReplyId.value = null
        ElMessage.success('回复成功')
      } catch (error) {
        ElMessage.error('回复失败')
      } finally {
        replying.value = false
      }
    }

    // 举报功能
    const showReportDialog = (type = 'post', targetId = null) => {
      reportForm.value.type = type
      reportForm.value.targetId = targetId || post.value.id
      reportDialogVisible.value = true
    }

    const submitReport = async () => {
      reporting.value = true
      try {
        await reportContent(reportForm.value)
        reportDialogVisible.value = false
        ElMessage.success('举报已提交')
      } catch (error) {
        ElMessage.error('举报失败')
      } finally {
        reporting.value = false
      }
    }

    // 点赞功能
    const handleLike = async () => {
      try {
        await likePost(post.value.id)
        post.value.likeCount++
        ElMessage.success('点赞成功')
      } catch (error) {
        ElMessage.error('点赞失败')
      }
    }

    const formatTime = (time) => {
      return formatDistanceToNow(new Date(time), { addSuffix: true, locale: zhCN })
    }

    onMounted(() => {
      fetchPostDetail()
    })

    return {
      loading,
      commenting,
      replying,
      reporting,
      post,
      comments,
      commentContent,
      replyContent,
      activeReplyId,
      reportDialogVisible,
      reportForm,
      reportRules,
      ThumbsUp,
      Warning,
      formatTime,
      submitComment,
      submitReply,
      showReportDialog,
      submitReport,
      handleLike,
      showReplyInput: (comment) => {
        activeReplyId.value = comment.id
      },
      cancelReply: () => {
        activeReplyId.value = null
        replyContent.value = ''
      }
    }
  }
}
</script>

<style scoped>
.post-detail-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.post-header {
  margin-bottom: 30px;
}

.post-title {
  margin: 0 0 20px 0;
  font-size: 24px;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-name {
  font-weight: bold;
}

.post-info {
  color: #909399;
  font-size: 14px;
}

.post-content {
  font-size: 16px;
  line-height: 1.8;
  margin: 30px 0;
  white-space: pre-wrap;
}

.post-actions {
  display: flex;
  gap: 15px;
  margin: 20px 0;
  padding: 20px 0;
  border-top: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
}

.comments-section {
  margin-top: 30px;
}

.comment-input {
  margin: 20px 0;
}

.comment-input .el-button {
  margin-top: 10px;
  float: right;
}

.comment-item {
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.commenter-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.comment-time {
  color: #909399;
  font-size: 12px;
}

.comment-content {
  margin: 10px 0;
  line-height: 1.6;
}

.reply-list {
  margin: 15px 0 15px 40px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.reply-item {
  padding: 10px 0;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 5px;
}

.reply-time {
  color: #909399;
  font-size: 12px;
}

.reply-input {
  margin: 15px 0 15px 40px;
}

.reply-actions {
  margin-top: 10px;
  text-align: right;
}
</style>