<template>
  <div class="dashboard-container">
    <!-- 数据统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statistics" :key="stat.title">
        <el-card class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="24" :color="stat.color">
                <component :is="stat.icon" />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-title">{{ stat.title }}</div>
            </div>
          </div>
          <div class="stat-footer">
            <span :style="{ color: stat.trending >= 0 ? '#67C23A' : '#F56C6C' }">
              <el-icon>
                <component :is="stat.trending >= 0 ? 'ArrowUp' : 'ArrowDown'" />
              </el-icon>
              {{ Math.abs(stat.trending) }}%
            </span>
            <span class="stat-period">较上月</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 活动图表 -->
    <el-row :gutter="20" class="dashboard-row">
      <el-col :span="16">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>活动统计</span>
              <el-radio-group v-model="chartPeriod" size="small">
                <el-radio-button label="week">周</el-radio-button>
                <el-radio-button label="month">月</el-radio-button>
                <el-radio-button label="year">年</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-container">
            <el-skeleton :rows="8" animated v-if="loading" />
            <div ref="activityChart" class="activity-chart" v-else></div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="todo-card">
          <template #header>
            <div class="card-header">
              <span>待办事项</span>
              <el-button type="primary" link @click="showAddTodo">
                添加待办
              </el-button>
            </div>
          </template>
          <el-empty v-if="!todos.length" description="暂无待办事项" />
          <div v-else class="todo-list">
            <div
                v-for="todo in todos"
                :key="todo.id"
                class="todo-item"
            >
              <el-checkbox
                  v-model="todo.completed"
                  @change="handleTodoStatusChange(todo)"
              >
                <span :class="{ completed: todo.completed }">
                  {{ todo.content }}
                </span>
              </el-checkbox>
              <div class="todo-actions">
                <el-tag size="small" :type="getTodoTypeTag(todo.type)">
                  {{ todo.type }}
                </el-tag>
                <el-button
                    type="danger"
                    link
                    @click="deleteTodo(todo)"
                >
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近活动和通知 -->
    <el-row :gutter="20" class="dashboard-row">
      <el-col :span="12">
        <el-card class="activity-card">
          <template #header>
            <div class="card-header">
              <span>最近活动</span>
              <el-button type="primary" link @click="viewAllActivities">
                查看全部
              </el-button>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item
                v-for="activity in activities"
                :key="activity.id"
                :timestamp="formatDateTime(activity.time)"
                :type="getActivityType(activity.type)"
            >
              <div class="activity-content">
                <div class="activity-title">{{ activity.title }}</div>
                <div class="activity-desc">{{ activity.description }}</div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="notification-card">
          <template #header>
            <div class="card-header">
              <span>系统通知</span>
              <el-button type="primary" link @click="viewAllNotifications">
                查看全部
              </el-button>
            </div>
          </template>
          <div class="notification-list">
            <div
                v-for="notification in notifications"
                :key="notification.id"
                class="notification-item"
            >
              <div class="notification-icon">
                <el-icon :size="24" :color="getNotificationColor(notification.type)">
                  <component :is="getNotificationIcon(notification.type)" />
                </el-icon>
              </div>
              <div class="notification-content">
                <div class="notification-title">
                  {{ notification.title }}
                  <el-tag
                      v-if="!notification.read"
                      size="small"
                      type="danger"
                  >
                    新
                  </el-tag>
                </div>
                <div class="notification-time">
                  {{ formatTime(notification.time) }}
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 添加待办对话框 -->
    <el-dialog
        v-model="todoDialogVisible"
        title="添加待办"
        width="500px"
    >
      <el-form
          ref="todoForm"
          :model="todoForm"
          :rules="todoRules"
          label-width="80px"
      >
        <el-form-item label="内容" prop="content">
          <el-input
              v-model="todoForm.content"
              placeholder="请输入待办事项内容"
          />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="todoForm.type" placeholder="选择类型">
            <el-option label="游戏" value="game" />
            <el-option label="活动" value="event" />
            <el-option label="社区" value="community" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="todoDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitTodo">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { formatDistanceToNow } from 'date-fns'
import { zhCN } from 'date-fns/locale'
import {
  getStatistics,
  getActivities,
  getNotifications,
  getTodos,
  addTodo,
  updateTodo,
  deleteTodo
} from '@/api/dashboard'

export default {
  name: 'Dashboard',

  setup() {
    const router = useRouter()
    const loading = ref(false)
    const chartPeriod = ref('week')
    const activityChart = ref(null)
    const chartInstance = ref(null)
    const todoDialogVisible = ref(false)

    // 统计数据
    const statistics = reactive([
      {
        title: '游戏总数',
        value: 0,
        icon: 'Game',
        color: '#409EFF',
        trending: 0
      },
      {
        title: '发帖数',
        value: 0,
        icon: 'ChatDotRound',
        color: '#67C23A',
        trending: 0
      },
      {
        title: '活动参与',
        value: 0,
        icon: 'Calendar',
        color: '#E6A23C',
        trending: 0
      },
      {
        title: '获赞数',
        value: 0,
        icon: 'ThumbsUp',
        color: '#F56C6C',
        trending: 0
      }
    ])

    // 活动记录
    const activities = ref([])

    // 通知列表
    const notifications = ref([])

    // 待办事项
    const todos = ref([])
    const todoForm = reactive({
      content: '',
      type: ''
    })

    const todoRules = {
      content: [
        { required: true, message: '请输入待办事项内容', trigger: 'blur' }
      ],
      type: [
        { required: true, message: '请选择类型', trigger: 'change' }
      ]
    }

    // 初始化数据
    const initData = async () => {
      loading.value = true
      try {
        const [statsRes, activitiesRes, notificationsRes, todosRes] = await Promise.all([
          getStatistics(),
          getActivities(),
          getNotifications(),
          getTodos()
        ])

        // 更新统计数据
        statistics.forEach((stat, index) => {
          Object.assign(stat, statsRes.data[index])
        })

        activities.value = activitiesRes.data
        notifications.value = notificationsRes.data
        todos.value = todosRes.data

        // 初始化图表
        initChart()
      } catch (error) {
        ElMessage.error('数据加载失败')
      } finally {
        loading.value = false
      }
    }

    // 初始化图表
    const initChart = () => {
      if (!activityChart.value) return

      if (chartInstance.value) {
        chartInstance.value.dispose()
      }

      chartInstance.value = echarts.init(activityChart.value)
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['发帖', '评论', '活动参与']
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '发帖',
            type: 'line',
            smooth: true,
            data: [10, 15, 8, 12, 18, 20, 16]
          },
          {
            name: '评论',
            type: 'line',
            smooth: true,
            data: [5, 8, 12, 6, 9, 15, 10]
          },
          {
            name: '活动参与',
            type: 'line',
            smooth: true,
            data: [2, 4, 3, 5, 6, 8, 7]
          }
        ]
      }
      chartInstance.value.setOption(option)
    }

    // 待办事项相关方法
    const showAddTodo = () => {
      todoForm.content = ''
      todoForm.type = ''
      todoDialogVisible.value = true
    }

    const submitTodo = async () => {
      try {
        const res = await addTodo(todoForm)
        todos.value.unshift(res.data)
        todoDialogVisible.value = false
        ElMessage.success('添加成功')
      } catch (error) {
        ElMessage.error('添加失败')
      }
    }

    const handleTodoStatusChange = async (todo) => {
      try {
        await updateTodo(todo.id, { completed: todo.completed })
      } catch (error) {
        todo.completed = !todo.completed
        ElMessage.error('更新失败')
      }
    }

    const deleteTodo = async (todo) => {
      try {
        await ElMessageBox.confirm('确定要删除这个待办事项吗？')
        await deleteTodo(todo.id)
        const index = todos.value.findIndex(item => item.id === todo.id)
        todos.value.splice(index, 1)
        ElMessage.success('删除成功')
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败')
        }
      }
    }

    // 工具方法
    const formatTime = (time) => {
      return formatDistanceToNow(new Date(time), {
        addSuffix: true,
        locale: zhCN
      })
    }

    const getActivityType = (type) => {
      const typeMap = {
        post: 'primary',
        comment: 'success',
        event: 'warning'
      }
      return typeMap[type] || 'info'
    }

    const getNotificationIcon = (type) => {
      const iconMap = {
        system: 'Bell',
        game: 'Game',
        event: 'Calendar',
        social: 'ChatDotRound'
      }
      return iconMap[type] || 'Info'
    }

    const getNotificationColor = (type) => {
      const colorMap = {
        system: '#409EFF',
        game: '#67C23A',
        event: '#E6A23C',
        social: '#F56C6C'
      }
      return colorMap[type] || '#909399'
    }

    const getTodoTypeTag = (type) => {
      const tagMap = {
        game: '',
        event: 'success',
        community: 'warning',
        other: 'info'
      }
      return tagMap[type] || 'info'
    }

    // 页面跳转
    const viewAllActivities = () => {
      router.push('/activities')
    }

    const viewAllNotifications = () => {
      router.push('/notifications')
    }

    // 生命周期钩子
    onMounted(() => {
      initData()
      window.addEventListener('resize', initChart)
    })

    onUnmounted(() => {
      if (chartInstance.value) {
        chartInstance.value.dispose()
      }
      window.removeEventListener('resize', initChart)
    })

    return {
      loading,
      chartPeriod,
      activityChart,
      statistics,
      activities,
      notifications,
      todos,
      todoDialogVisible,
      todoForm,
      todoRules,
      showAddTodo,
      submitTodo,
      handleTodoStatusChange,
      deleteTodo,
      formatTime,
      getActivityType,
      getNotificationIcon,
      getNotificationColor,
      getTodoTypeTag,
      viewAllActivities,
      viewAllNotifications
    }
  }
}
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.dashboard-row {
  margin-top: 20px;
}

/* 统计卡片 */
.stat-card {
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  display: flex;
  align-items: flex-start;
  gap: 15px;
}

.stat-icon {
  padding: 12px;
  border-radius: 8px;
  background-color: #f0f2f5;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  line-height: 1.2;
}

.stat-title {
  margin-top: 5px;
  color: #666;
  font-size: 14px;
}

.stat-footer {
  margin-top: 15px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.stat-period {
  color: #909399;
}

/* 图表卡片 */
.chart-card {
  height: 400px;
}

.chart-container {
  height: 320px;
}

.activity-chart {
  width: 100%;
  height: 100%;
}

/* 待办事项 */
.todo-list {
  max-height: 320px;
  overflow-y: auto;
}

.todo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}

.todo-item:last-child {
  border-bottom: none;
}

.completed {
  text-decoration: line-through;
  color: #909399;
}

.todo-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 活动时间线 */
.activity-content {
  margin-bottom: 20px;
}

.activity-title {
  font-weight: 500;
  margin-bottom: 5px;
}

.activity-desc {
  color: #666;
  font-size: 14px;
}

/* 通知列表 */
.notification-list {
  max-height: 400px;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 15px;
  padding: 15px 0;
  border-bottom: 1px solid #ebeef5;
}

.notification-item:last-child {
  border-bottom: none;
}

.notification-content {
  flex: 1;
}

.notification-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 5px;
}

.notification-time {
  font-size: 12px;
  color: #909399;
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-thumb {
  background-color: #dcdfe6;
  border-radius: 3px;
}

::-webkit-scrollbar-track {
  background-color: #f5f7fa;
}
</style>