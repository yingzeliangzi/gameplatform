<template>
  <div class="report-management-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>举报管理</span>
          <el-radio-group v-model="currentStatus" @change="handleStatusChange">
            <el-radio-button label="PENDING">待处理</el-radio-button>
            <el-radio-button label="APPROVED">已通过</el-radio-button>
            <el-radio-button label="REJECTED">已驳回</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-table
          v-loading="loading"
          :data="reports"
          style="width: 100%"
          border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="举报类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getReportTypeTag(row.type)">
              {{ getReportTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="被举报内容" min-width="200">
          <template #default="{ row }">
            <el-link @click="viewContent(row)">
              {{ row.targetContent }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="reporter.nickname" label="举报人" width="120" />
        <el-table-column prop="reason" label="举报原因" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="举报时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button
                  link
                  type="primary"
                  @click="handleReport(row, true)"
              >
                通过
              </el-button>
              <el-button
                  link
                  type="danger"
                  @click="handleReport(row, false)"
              >
                驳回
              </el-button>
            </template>
            <template v-else>
              <el-tag :type="getStatusTag(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { format } from 'date-fns'
import { getReports, handleReport } from '@/api/report'

export default {
  name: 'ReportManagement',

  setup() {
    const router = useRouter()
    const loading = ref(false)
    const reports = ref([])
    const total = ref(0)
    const currentPage = ref(1)
    const pageSize = ref(10)
    const currentStatus = ref('PENDING')

    const fetchReports = async () => {
      loading.value = true
      try {
        const params = {
          page: currentPage.value - 1,
          size: pageSize.value,
          status: currentStatus.value
        }
        const res = await getReports(params)
        reports.value = res.data.content
        total.value = res.data.totalElements
      } catch (error) {
        ElMessage.error('获取举报列表失败')
      } finally {
        loading.value = false
      }
    }

    const handleReport = async (report, isApproved) => {
      const action = isApproved ? '通过' : '驳回'
      ElMessageBox.confirm(`确定要${action}这个举报吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await handleReport(report.id, isApproved)
          ElMessage.success('操作成功')
          fetchReports()
        } catch (error) {
          ElMessage.error('操作失败')
        }
      })
    }

    const viewContent = (report) => {
      if (report.type === 'POST') {
        router.push(`/community/posts/${report.targetId}`)
      }
      // 处理其他类型的内容查看
    }

    const handleStatusChange = () => {
      currentPage.value = 1
      fetchReports()
    }

    const getReportTypeTag = (type) => {
      const typeMap = {
        'POST': 'primary',
        'COMMENT': 'success'
      }
      return typeMap[type] || 'info'
    }

    const getReportTypeText = (type) => {
      const typeMap = {
        'POST': '帖子',
        'COMMENT': '评论'
      }
      return typeMap[type] || type
    }

    const getStatusTag = (status) => {
      const statusMap = {
        'APPROVED': 'success',
        'REJECTED': 'danger',
        'PENDING': 'warning'
      }
      return statusMap[status] || 'info'
    }

    const getStatusText = (status) => {
      const statusMap = {
        'APPROVED': '已通过',
        'REJECTED': '已驳回',
        'PENDING': '待处理'
      }
      return statusMap[status] || status
    }

    const formatDateTime = (time) => {
      return format(new Date(time), 'yyyy-MM-dd HH:mm:ss')
    }

    const handleSizeChange = (val) => {
      pageSize.value = val
      fetchReports()
    }

    const handleCurrentChange = (val) => {
      currentPage.value = val
      fetchReports()
    }

    onMounted(() => {
      fetchReports()
    })

    return {
      loading,
      reports,
      total,
      currentPage,
      pageSize,
      currentStatus,
      handleStatusChange,
      handleSizeChange,
      handleCurrentChange,
      handleReport,
      viewContent,
      getReportTypeTag,
      getReportTypeText,
      getStatusTag,
      getStatusText,
      formatDateTime
    }
  }
}
</script>

<style scoped>
.report-management-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-table) {
  margin-top: 20px;
}

:deep(.el-tag) {
  text-align: center;
  min-width: 60px;
}
</style>