<template>
  <div class="comment-list">
    <!-- 评论输入框 -->
    <div v-if="isLoggedIn" class="comment-editor">
      <comment-editor
          :submitting="submitting"
          @submit="submitComment"
      />
    </div>

    <!-- 评论筛选 -->
    <comment-filters
        @filter-change="handleFilterChange"
        ref="filtersRef"
    />

    <!-- 评论列表 -->
    <div class="comment-items">
      <template v-if="comments.length">
        <comment-item
            v-for="comment in comments"
            :key="comment.id"
            :comment="comment"
            :can-manage="canManageComment(comment)"
            @like="handleLike"
            @reply="showReplyInput"
            @edit="editComment"
            @delete="deleteComment"
        >
          <!-- 回复列表 -->
          <template v-if="comment.replies?.length">
            <comment-item
                v-for="reply in comment.replies"
                :key="reply.id"
                :comment="reply"
                :is-reply="true"
                :can-manage="canManageComment(reply)"
                @like="handleLike"
                @reply="showReplyInput"
                @edit="editComment"
                @delete="deleteComment"
            />
          </template>

          <!-- 回复输入框 -->
          <comment-editor
              v-if="replyingTo === comment.id"
              :is-reply="true"
              :submitting="submittingReply"
              @submit="content => submitReply(comment, content)"
              @cancel="cancelReply"
          />
        </comment-item>
      </template>
      <el-empty v-else description="暂无评论" />
    </div>

    <!-- 加载更多 -->
    <div
        v-if="hasMore"
        class="load-more"
        v-loading="loading"
    >
      <el-button link @click="loadMore">加载更多</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import CommentEditor from './CommentEditor.vue'
import CommentFilters from './CommentFilters.vue'
import CommentItem from './CommentItem.vue'
import { useFormatTime } from '@/hooks/useFormatTime'

const props = defineProps({
  postId: {
    type: [Number, String],
    required: true
  }
})

const store = useStore()
const { formatTime } = useFormatTime()

// 响应式状态
const comments = ref([])
const replyingTo = ref(null)
const submitting = ref(false)
const submittingReply = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)
const filtersRef = ref(null)

// 计算属性
const isLoggedIn = computed(() => store.getters['auth/isLoggedIn'])
const currentUser = computed(() => store.state.auth.userInfo)
const hasMore = computed(() => comments.value.length < total.value)

// 加载评论列表
const fetchComments = async () => {
  if (loading.value) return
  loading.value = true

  try {
    const response = await store.dispatch('comment/getComments', {
      postId: props.postId,
      page: page.value - 1,
      size: pageSize.value,
      ...filtersRef.value?.filters
    })

    const { content, totalElements } = response
    comments.value = page.value === 1
        ? content
        : [...comments.value, ...content]
    total.value = totalElements
  } catch (error) {
    ElMessage.error('获取评论失败')
  } finally {
    loading.value = false
  }
}

// 提交评论
const submitComment = async (content) => {
  submitting.value = true
  try {
    const comment = await store.dispatch('comment/createComment', {
      postId: props.postId,
      content
    })
    comments.value.unshift(comment)
    ElMessage.success('评论成功')
  } catch (error) {
    ElMessage.error('发表评论失败')
  } finally {
    submitting.value = false
  }
}

// 点赞处理
const handleLike = async (comment) => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    if (comment.isLiked) {
      await store.dispatch('comment/unlikeComment', comment.id)
      comment.likeCount--
    } else {
      await store.dispatch('comment/likeComment', comment.id)
      comment.likeCount++
    }
    comment.isLiked = !comment.isLiked
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 回复相关
const showReplyInput = (comment) => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    return
  }
  replyingTo.value = comment.id
}

const submitReply = async (parentComment, content) => {
  submittingReply.value = true
  try {
    const reply = await store.dispatch('comment/createComment', {
      postId: props.postId,
      content,
      parentId: parentComment.id
    })

    if (!parentComment.replies) {
      parentComment.replies = []
    }
    parentComment.replies.push(reply)
    replyingTo.value = null
    ElMessage.success('回复成功')
  } catch (error) {
    ElMessage.error('回复失败')
  } finally {
    submittingReply.value = false
  }
}

const cancelReply = () => {
  replyingTo.value = null
}

// 评论管理
const canManageComment = (comment) => {
  return isLoggedIn.value && (
      comment.authorId === currentUser.value?.id ||
      currentUser.value?.roles?.includes('ADMIN')
  )
}

const editComment = async (comment) => {
  try {
    const { value } = await ElMessageBox.prompt('编辑评论', '提示', {
      inputValue: comment.content,
      inputValidator: value => value.trim().length > 0,
      inputErrorMessage: '评论内容不能为空'
    })

    const updatedComment = await store.dispatch('comment/updateComment', {
      id: comment.id,
      content: value
    })
    Object.assign(comment, updatedComment)
    ElMessage.success('编辑成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('编辑失败')
    }
  }
}

const deleteComment = async (comment) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      type: 'warning'
    })

    await store.dispatch('comment/deleteComment', comment.id)

    if (comment.parentId) {
      // 删除回复
      const parentComment = comments.value.find(c => c.id === comment.parentId)
      if (parentComment) {
        const index = parentComment.replies.indexOf(comment)
        parentComment.replies.splice(index, 1)
      }
    } else {
      // 删除主评论
      const index = comments.value.indexOf(comment)
      comments.value.splice(index, 1)
    }
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 筛选处理
const handleFilterChange = () => {
  page.value = 1
  fetchComments()
}

// 加载更多
const loadMore = () => {
  page.value++
  fetchComments()
}

// 生命周期钩子
onMounted(() => {
  fetchComments()
})
</script>

<style lang="scss" scoped>
.comment-list {
  margin-top: 20px;

  .comment-editor {
    margin-bottom: 20px;
  }

  .load-more {
    text-align: center;
    margin-top: 20px;
  }
}
</style>