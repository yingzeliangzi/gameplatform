<template>
  <div class="post-management-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input
              v-model="searchForm.keyword"
              placeholder="搜索标题或内容"
              clearable
              @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
            <el-option label="正常" value="NORMAL" />
            <el-option label="已隐藏" value="HIDDEN" />
            <el-option label="已删除" value="DELETED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table
          v-loading="loading"
          :data="posts"
          style="width: 100%"
          border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" show-overflow-tooltip />
        <el-table-column prop="author.nickname" label="作者" width="120" />
        <el-table-column prop="gameName" label="关联游戏" width="120" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="统计" width="200">
          <template #default="{ row }">
            <div class="post-stats">
              <span>浏览: {{ row.viewCount }}</span>
              <span>评论: {{ row.commentCount }}</span>
              <span>点赞: {{ row.likeCount }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
                link
                type="primary"
                @click="viewPost(row)"
            >
              查看
            </el-button>
            <el-button
                v-if="row.status === 'NORMAL'"
                link
                type="warning"
                @click="hidePost(row)"
            >
              隐藏
            </el-button>
            <el-button
                v-if="row.status === 'HIDDEN'"
                link
                type="primary"
                @click="restorePost(row)"
            >
              恢复
            </el-button>
            <el-button
                link
                type="danger"
                @click="deletePost(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { format } from 'date-fns'
import { getPosts, updatePostStatus } from '@/api/post'

export default {
  name: 'PostManagement',

  setup() {
    const router = useRouter()
    const loading = ref(false)
    const posts = ref([])
    const total = ref(0)
    const currentPage = ref(1)
    const pageSize = ref(10)

    const searchForm = reactive({
      keyword: '',
      status: ''
    })

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

    // 更新帖子状态
    const updateStatus = async (post, status) => {
      try {
        await updatePostStatus(post.id, status)
        await fetchPosts()
        ElMessage.success('操作成功')
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }

    // 隐藏帖子
    const hidePost = async (post) => {
      ElMessageBox.confirm('确定要隐藏这篇帖子吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        updateStatus(post, 'HIDDEN')
      })
    }

    // 恢复帖子
    const restorePost = (post) => {
      ElMessageBox.confirm('确定要恢复这篇帖子吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        updateStatus(post, 'NORMAL')
      })
    }

    // 删除帖子
    const deletePost = (post) => {
      ElMessageBox.confirm('确定要删除这篇帖子吗？此操作不可恢复！', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }).then(() => {
        updateStatus(post, 'DELETED')
      })
    }

    const handleSearch = () => {
      currentPage.value = 1
      fetchPosts()
    }

    const resetSearch = () => {
      searchForm.keyword = ''
      searchForm.status = ''
      handleSearch()
    }

    const handleSizeChange = (val) => {
      pageSize.value = val
      fetchPosts()
    }

    const handleCurrentChange = (val) => {
      currentPage.value = val
      fetchPosts()
    }

    const viewPost = (post) => {
      router.push(`/community/posts/${post.id}`)
    }

    const formatDateTime = (time) => {
      return format(new Date(time), 'yyyy-MM-dd HH:mm:ss')
    }

    const getStatusType = (status) => {
      const statusMap = {
        'NORMAL': 'success',
        'HIDDEN': 'warning',
        'DELETED': 'danger'
      }
      return statusMap[status] || 'info'
    }

    const getStatusText = (status) => {
      const statusMap = {
        'NORMAL': '正常',
        'HIDDEN': '已隐藏',
        'DELETED': '已删除'
      }
      return statusMap[status] || status
    }

    onMounted(() => {
      fetchPosts()
    })

    return {
      loading,
      posts,
      total,
      currentPage,
      pageSize,
      searchForm,
      handleSearch,
      resetSearch,
      handleSizeChange,
      handleCurrentChange,
      hidePost,
      restorePost,
      deletePost,
      viewPost,
      formatDateTime,
      getStatusType,
      getStatusText
    }
  }
}
</script>

<style scoped>
.post-management-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.post-stats {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: #666;
}

.post-stats span {
  display: flex;
  align-items: center;
}

.table-operations {
  display: flex;
  gap: 8px;
}

:deep(.el-table) {
  margin-top: 20px;
}

:deep(.el-tag) {
  text-align: center;
  min-width: 60px;
}
</style>