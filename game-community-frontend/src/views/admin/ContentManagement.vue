<template>
  <div class="content-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>内容管理</span>
          <el-radio-group v-model="contentType">
            <el-radio-button label="posts">帖子</el-radio-button>
            <el-radio-button label="comments">评论</el-radio-button>
            <el-radio-button label="reports">举报</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <!-- 搜索过滤 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
              v-model="searchForm.keyword"
              placeholder="搜索内容..."
              clearable
              @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
            <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 内容列表 -->
      <el-table
          v-loading="loading"
          :data="contentList"
          border
          style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="内容" min-width="300">
          <template #default="{ row }">
            <div class="content-preview">
              <el-link @click="viewDetail(row)">
                {{ getContentPreview(row) }}
              </el-link>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="author.nickname" label="作者" width="120" />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
                v-if="row.status !== 'DELETED'"
                link
                type="primary"
                @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
                v-if="row.status === 'HIDDEN'"
                link
                type="success"
                @click="handleRestore(row)"
            >
              恢复
            </el-button>
            <el-button
                v-if="row.status !== 'HIDDEN' && row.status !== 'DELETED'"
                link
                type="warning"
                @click="handleHide(row)"
            >
              隐藏
            </el-button>
            <el-button
                link
                type="danger"
                @click="handleDelete(row)"
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

    <!-- 内容编辑对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="60%"
    >
      <el-form
          ref="contentForm"
          :model="formData"
          :rules="formRules"
          label-width="80px"
      >
        <el-form-item
            v-if="contentType === 'posts'"
            label="标题"
            prop="title"
        >
          <el-input v-model="formData.title" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
              v-model="formData.content"
              type="textarea"
              :rows="6"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="formData.status">
            <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { format } from 'date-fns'

export default {
  name: 'ContentManagement',

  setup() {
    const contentType = ref('posts')
    const loading = ref(false)
    const dialogVisible = ref(false)
    const currentPage = ref(1)
    const pageSize = ref(20)
    const total = ref(0)
    const contentList = ref([])

    const searchForm = reactive({
      keyword: '',
      status: ''
    })

    const formData = reactive({
      title: '',
      content: '',
      status: ''
    })

    const statusOptions = computed(() => {
      return [
        { label: '正常', value: 'NORMAL' },
        { label: '隐藏', value: 'HIDDEN' },
        { label: '已删除', value: 'DELETED' }
      ]
    })

    const dialogTitle = computed(() => {
      return contentType.value === 'posts' ? '编辑帖子' : '编辑评论'
    })

    const formRules = {
      title: [
        { required: true, message: '请输入标题', trigger: 'blur' }
      ],
      content: [
        { required: true, message: '请输入内容', trigger: 'blur' }
      ],
      status: [
        { required: true, message: '请选择状态', trigger: 'change' }
      ]
    }

    // 加载内容列表
    const fetchContentList = async () => {
      loading.value = true
      try {
        const params = {
          page: currentPage.value - 1,
          size: pageSize.value,
          ...searchForm
        }
        const res = await fetch(`/api/${contentType.value}`, { params })
        const data = await res.json()
        contentList.value = data.content
        total.value = data.totalElements
      } catch (error) {
        ElMessage.error('获取内容列表失败')
      } finally {
        loading.value = false
      }
    }

    // 处理内容编辑
    const handleEdit = (row) => {
      Object.assign(formData, row)
      dialogVisible.value = true
    }

    // 提交表单
    const submitForm = async () => {
      try {
        await fetch(`/api/${contentType.value}/${formData.id}`, {
          method: 'PUT',
          body: JSON.stringify(formData)
        })
        ElMessage.success('更新成功')
        dialogVisible.value = false
        fetchContentList()
      } catch (error) {
        ElMessage.error('更新失败')
      }
    }

    // 处理内容隐藏
    const handleHide = async (row) => {
      try {
        await ElMessageBox.confirm('确定要隐藏该内容吗？')
        await fetch(`/api/${contentType.value}/${row.id}/hide`, {
          method: 'PUT'
        })
        ElMessage.success('操作成功')
        fetchContentList()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('操作失败')
        }
      }
    }

    // 处理内容恢复
    const handleRestore = async (row) => {
      try {
        await ElMessageBox.confirm('确定要恢复该内容吗？')
        await fetch(`/api/${contentType.value}/${row.id}/restore`, {
          method: 'PUT'
        })
        ElMessage.success('操作成功')
        fetchContentList()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('操作失败')
        }
      }
    }

    // 处理内容删除
    const handleDelete = async (row) => {
      try {
        await ElMessageBox.confirm('确定要删除该内容吗？此操作不可恢复！', '警告', {
          type: 'warning'
        })
        await fetch(`/api/${contentType.value}/${row.id}`, {
          method: 'DELETE'
        })
        ElMessage.success('删除成功')
        fetchContentList()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败')
        }
      }
    }

    // 监听内容类型变化
    watch(contentType, () => {
      currentPage.value = 1
      fetchContentList()
    })

    // 格式化时间
    const formatDateTime = (time) => {
      return format(new Date(time), 'yyyy-MM-dd HH:mm:ss')
    }

    // 获取内容预览
    const getContentPreview = (row) => {
      const content = contentType.value === 'posts' ? row.title : row.content
      return content.length > 50 ? content.slice(0, 50) + '...' : content
    }

    return {
      contentType,
      loading,
      dialogVisible,
      currentPage,
      pageSize,
      total,
      contentList,
      searchForm,
      formData,
      statusOptions,
      dialogTitle,
      formRules,
      handleEdit,
      submitForm,
      handleHide,
      handleRestore,
      handleDelete,
      formatDateTime,
      getContentPreview
    }
  }
}
</script>

<style scoped>
.content-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.content-preview {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dialog-footer {
  text-align: right;
}
</style>