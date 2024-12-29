<template>
  <div class="post-list-container">
    <el-card class="filter-card">
      <div class="filter-header">
        <div class="left">
          <el-input
              v-model="searchForm.keyword"
              placeholder="搜索帖子..."
              prefix-icon="Search"
              clearable
              @keyup.enter="handleSearch"
          />
          <el-select
              v-if="games.length"
              v-model="searchForm.gameId"
              placeholder="选择游戏"
              clearable
              @change="handleSearch"
          >
            <el-option
                v-for="game in games"
                :key="game.id"
                :label="game.title"
                :value="game.id"
            />
          </el-select>
        </div>
        <el-button type="primary" @click="handleCreatePost">发帖</el-button>
      </div>
    </el-card>

    <el-card v-loading="loading">
      <div class="post-list">
        <div v-for="post in posts" :key="post.id" class="post-item" @click="goToPost(post.id)">
          <el-avatar :src="post.author.avatar" :size="40" />
          <div class="post-content">
            <h3 class="post-title">{{ post.title }}</h3>
            <div class="post-meta">
              <span class="author">{{ post.author.nickname }}</span>
              <span class="time">{{ formatTime(post.createdAt) }}</span>
              <span v-if="post.gameName" class="game-tag">
                <el-tag size="small">{{ post.gameName }}</el-tag>
              </span>
            </div>
            <p class="post-summary">{{ post.content.substring(0, 200) }}...</p>
            <div class="post-stats">
              <span class="stat-item">
                <el-icon><View /></el-icon>
                {{ post.viewCount }}
              </span>
              <span class="stat-item">
                <el-icon><ChatDotRound /></el-icon>
                {{ post.commentCount }}
              </span>
              <span class="stat-item">
                <el-icon><ThumbsUp /></el-icon>
                {{ post.likeCount }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <div class="pagination">
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 30, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 发帖对话框 -->
    <el-dialog
        v-model="dialogVisible"
        title="发布帖子"
        width="60%"
        :before-close="handleDialogClose"
    >
      <el-form
          ref="postForm"
          :model="postForm"
          :rules="postRules"
          label-width="80px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="postForm.title" placeholder="请输入标题" />
        </el-form-item>

        <el-form-item label="关联游戏" prop="gameId">
          <el-select v-model="postForm.gameId" placeholder="选择关联游戏" clearable>
            <el-option
                v-for="game in games"
                :key="game.id"
                :label="game.title"
                :value="game.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <el-input
              v-model="postForm.content"
              type="textarea"
              :rows="6"
              placeholder="请输入内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPost" :loading="submitting">
            发布
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { View, ChatDotRound, ThumbsUp } from '@element-plus/icons-vue'
import { getPosts, createPost } from '@/api/post'
import { formatDistanceToNow } from 'date-fns'
import { zhCN } from 'date-fns/locale'

export default {
  name: 'PostList',
  components: { View, ChatDotRound, ThumbsUp },

  setup() {
    const router = useRouter()
    const loading = ref(false)
    const submitting = ref(false)
    const dialogVisible = ref(false)
    const posts = ref([])
    const games = ref([])
    const total = ref(0)
    const currentPage = ref(1)
    const pageSize = ref(10)

    const searchForm = reactive({
      keyword: '',
      gameId: null
    })

    const postForm = reactive({
      title: '',
      content: '',
      gameId: null
    })

    const postRules = {
      title: [
        { required: true, message: '请输入标题', trigger: 'blur' },
        { min: 5, max: 100, message: '标题长度在5-100个字符之间', trigger: 'blur' }
      ],
      content: [
        { required: true, message: '请输入内容', trigger: 'blur' },
        { min: 10, message: '内容至少10个字符', trigger: 'blur' }
      ]
    }

    // 获取帖子列表
    const fetchPosts = async () => {
      loading.value = true
      try {
        const params = {
          page: currentPage.value - 1,
          size: pageSize.value,
          ...searchForm
        }
        const res = await getPosts(params)
        posts.value = res.data.content
        total.value = res.data.totalElements
      } catch (error) {
        ElMessage.error('获取帖子列表失败')
      } finally {
        loading.value = false
      }
    }

    // 格式化时间
    const formatTime = (time) => {
      return formatDistanceToNow(new Date(time), { addSuffix: true, locale: zhCN })
    }

    // 搜索处理
    const handleSearch = () => {
      currentPage.value = 1
      fetchPosts()
    }

    // 分页处理
    const handleSizeChange = (val) => {
      pageSize.value = val
      fetchPosts()
    }

    const handleCurrentChange = (val) => {
      currentPage.value = val
      fetchPosts()
    }

    // 发帖处理
    const handleCreatePost = () => {
      dialogVisible.value = true
    }

    const submitPost = async () => {
      submitting.value = true
      try {
        await createPost(postForm)
        ElMessage.success('发布成功')
        dialogVisible.value = false
        fetchPosts()
      } catch (error) {
        ElMessage.error('发布失败')
      } finally {
        submitting.value = false
      }
    }

    // 跳转到帖子详情
    const goToPost = (id) => {
      router.push(`/community/posts/${id}`)
    }

    onMounted(() => {
      fetchPosts()
    })

    return {
      loading,
      submitting,
      dialogVisible,
      posts,
      games,
      total,
      currentPage,
      pageSize,
      searchForm,
      postForm,
      postRules,
      formatTime,
      handleSearch,
      handleSizeChange,
      handleCurrentChange,
      handleCreatePost,
      submitPost,
      goToPost
    }
  }
}
</script>

<style scoped>
.post-list-container {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.left {
  display: flex;
  gap: 15px;
  flex: 1;
  max-width: 600px;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.post-item {
  display: flex;
  gap: 15px;
  padding: 15px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: background-color 0.3s;
}

.post-item:hover {
  background-color: #f5f7fa;
}

.post-content {
  flex: 1;
}

.post-title {
  margin: 0 0 10px 0;
  font-size: 18px;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
  color: #909399;
  font-size: 14px;
}

.post-summary {
  color: #606266;
  margin: 0;
  line-height: 1.6;
}

.post-stats {
  display: flex;
  gap: 20px;
  margin-top: 10px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #909399;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}
</style>