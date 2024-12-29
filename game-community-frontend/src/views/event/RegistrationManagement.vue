<template>
  <div class="registration-management-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>{{ event.title }} - 报名管理</h2>
          <div class="header-actions">
            <el-button type="primary" @click="exportRegistrations">
              导出报名表
            </el-button>
          </div>
        </div>
      </template>

      <!-- 报名统计 -->
      <div class="registration-stats">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-value">{{ stats.total }}</div>
              <div class="stat-label">总报名人数</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-value">{{ stats.registered }}</div>
              <div class="stat-label">已报名</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-value">{{ stats.attended }}</div>
              <div class="stat-label">已签到</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-value">{{ stats.cancelled }}</div>
              <div class="stat-label">已取消</div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 筛选器 -->
      <div class="filter-section">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
              <el-option
                  v-for="status in registrationStatuses"
                  :key="status.value"
                  :label="status.label"
                  :value="status.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="报名时间">
            <el-date-picker
                v-model="searchForm.timeRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 报名列表 -->
      <el-table
          v-loading="loading"
          :data="registrations"
          border
          style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="用户信息">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="32" :src="row.user.avatar" />
              <div class="user-details">
                <div>{{ row.user.nickname }}</div>
                <div class="user-id">ID: {{ row.user.id }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="contactInfo" label="联系方式" />
        <el-table-column label="报名时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.registeredAt) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button
                v-if="row.remark"
                link
                type="primary"
                @click="showRemark(row)"
            >
              查看备注
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
                v-if="row.status === 'REGISTERED'"
                link
                type="primary"
                @click="handleAttendance(row, true)"
            >
              签到
            </el-button>
            <el-button
                v-if="row.status === 'REGISTERED'"
                link
                type="danger"
                @click="handleAttendance(row, false)"
            >
              缺席
            </el-button>
            <el-button
                v-if="row.status === 'REGISTERED'"
                link
                type="warning"
                @click="handleCancelRegistration(row)"
            >
              取消报名
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 备注查看对话框 -->
    <el-dialog
        v-model="remarkDialogVisible"
        title="报名备注"
        width="500px"
    >
      <p class="remark-content">{{ currentRemark }}</p>
      <template #footer>
        <el-button @click="remarkDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { format } from 'date-fns'
import {
  getEventRegistrations,
  updateRegistrationStatus,
  getRegistrationStats,
  exportRegistrationList
} from '@/api/event'

export default {
  name: 'RegistrationManagement',

  setup() {
    const route = useRoute()
    const router = useRouter()
    const loading = ref(false)
    const registrations = ref([])
    const total = ref(0)
    const currentPage = ref(1)
    const pageSize = ref(20)
    const remarkDialogVisible = ref(false)
    const currentRemark = ref('')
    const event = ref({})
    const stats = ref({
      total: 0,
      registered: 0,
      attended: 0,
      cancelled: 0
    })

    const searchForm = reactive({
      status: '',
      timeRange: []
    })

    const registrationStatuses = [
      { label: '已报名', value: 'REGISTERED' },
      { label: '已签到', value: 'ATTENDED' },
      { label: '已取消', value: 'CANCELLED' },
      { label: '缺席', value: 'ABSENT' }
    ]

    // 获取报名列表
    const fetchRegistrations = async () => {
      loading.value = true
      try {
        const params = {
          page: currentPage.value - 1,
          size: pageSize.value,
          status: searchForm.status,
          startTime: searchForm.timeRange?.[0],
          endTime: searchForm.timeRange?.[1]
        }
        const res = await getEventRegistrations(route.params.id, params)
        registrations.value = res.data.content
        total.value = res.data.totalElements
      } catch (error) {
        ElMessage.error('获取报名列表失败')
      } finally {
        loading.value = false
      }
    }

    // 获取统计数据
    const fetchStats = async () => {
      try {
        const res = await getRegistrationStats(route.params.id)
        stats.value = res.data
      } catch (error) {
        ElMessage.error('获取统计数据失败')
      }
    }

    // 处理签到/缺席
    const handleAttendance = async (registration, isAttend) => {
      const status = isAttend ? 'ATTENDED' : 'ABSENT'
      const action = isAttend ? '签到' : '标记缺席'

      try {
        await ElMessageBox.confirm(`确定要将该用户标记为${action}吗？`)
        await updateRegistrationStatus(registration.id, status)
        ElMessage.success(`${action}成功`)
        fetchRegistrations()
        fetchStats()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error(`${action}失败`)
        }
      }
    }

    // 处理取消报名
    const handleCancelRegistration = async (registration) => {
      try {
        await ElMessageBox.confirm('确定要取消该用户的报名吗？')
        await updateRegistrationStatus(registration.id, 'CANCELLED')
        ElMessage.success('取消报名成功')
        fetchRegistrations()
        fetchStats()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('取消报名失败')
        }
      }
    }

    // 导出报名表
    const exportRegistrations = async () => {
      try {
        const res = await exportRegistrationList(route.params.id)
        const blob = new Blob([res.data], { type: 'text/csv' })
        const link = document.createElement('a')
        link.href = window.URL.createObjectURL(blob)
        link.download = `报名表-${event.value.title}.csv`
        link.click()
        window.URL.revokeObjectURL(link.href)
      } catch (error) {
        ElMessage.error('导出失败')
      }
    }

    // 显示备注
    const showRemark = (registration) => {
      currentRemark.value = registration.remark
      remarkDialogVisible.value = true
    }

    // 处理搜索
    const handleSearch = () => {
      currentPage.value = 1
      fetchRegistrations()
    }

    const resetSearch = () => {
      searchForm.status = ''
      searchForm.timeRange = []
      handleSearch()
    }

    const handleSizeChange = (val) => {
      pageSize.value = val
      fetchRegistrations()
    }

    const handleCurrentChange = (val) => {
      currentPage.value = val
      fetchRegistrations()
    }

    const getStatusType = (status) => {
      const typeMap = {
        'REGISTERED': 'info',
        'ATTENDED': 'success',
        'CANCELLED': 'danger',
        'ABSENT': 'warning'
      }
      return typeMap[status]
    }

    const getStatusLabel = (status) => {
      const labelMap = {
        'REGISTERED': '已报名',
        'ATTENDED': '已签到',
        'CANCELLED': '已取消',
        'ABSENT': '缺席'
      }
      return labelMap[status]
    }

    const formatDateTime = (time) => {
      return format(new Date(time), 'yyyy-MM-dd HH:mm:ss')
    }

    onMounted(() => {
      fetchRegistrations()
      fetchStats()
    })

    return {
      loading,
      registrations,
      total,
      currentPage,
      pageSize,
      searchForm,
      registrationStatuses,
      remarkDialogVisible,
      currentRemark,
      event,
      stats,
      handleAttendance,
      handleCancelRegistration,
      exportRegistrations,
      showRemark,
      handleSearch,
      resetSearch,
      handleSizeChange,
      handleCurrentChange,
      getStatusType,
      getStatusLabel,
      formatDateTime
    }
  }
}
</script>

<style scoped>
.registration-management-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.registration-stats {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  padding: 20px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.stat-label {
  margin-top: 10px;
  color: #666;
}

.filter-section {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.user-id {
  font-size: 12px;
  color: #999;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.remark-content {
  white-space: pre-wrap;
  line-height: 1.6;
}
</style>