<template>
  <div class="dashboard-container">
    <!-- 数据卡片 -->
    <el-row :gutter="20">
      <el-col :span="6" v-for="(item, index) in statCards" :key="index">
        <el-card class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-content">
            <div class="stat-value" :style="{ color: item.color }">
              {{ item.value }}
              <span class="stat-trend" v-if="item.trend !== 0">
                <el-icon :color="item.trend > 0 ? '#67C23A' : '#F56C6C'">
                  <component :is="item.trend > 0 ? 'ArrowUp' : 'ArrowDown'" />
                </el-icon>
                {{ Math.abs(item.trend) }}%
              </span>
            </div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
          <el-icon class="stat-icon" :size="40" :color="item.color">
            <component :is="item.icon" />
          </el-icon>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>用户增长趋势</span>
              <el-radio-group v-model="timeRange" size="small">
                <el-radio-button label="week">周</el-radio-button>
                <el-radio-button label="month">月</el-radio-button>
                <el-radio-button label="year">年</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-container">
            <div ref="userGrowthChart" class="chart"></div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>用户分布</span>
            </div>
          </template>
          <div class="chart-container">
            <div ref="userDistributionChart" class="chart"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 内容数据 -->
    <el-row :gutter="20" class="data-row">
      <el-col :span="12">
        <el-card class="data-card">
          <template #header>
            <div class="card-header">
              <span>热门游戏</span>
              <el-button type="primary" link @click="viewMore('games')">
                查看更多
              </el-button>
            </div>
          </template>
          <el-table :data="hotGames" stripe style="width: 100%">
            <el-table-column prop="rank" label="排名" width="80" />
            <el-table-column prop="title" label="游戏名称" />
            <el-table-column prop="rating" label="评分" width="100">
              <template #default="{ row }">
                <el-rate v-model="row.rating" disabled text-color="#ff9900" />
              </template>
            </el-table-column>
            <el-table-column prop="playerCount" label="玩家数" width="100">
              <template #default="{ row }">
                {{ formatNumber(row.playerCount) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="data-card">
          <template #header>
            <div class="card-header">
              <span>活跃用户</span>
              <el-button type="primary" link @click="viewMore('users')">
                查看更多
              </el-button>
            </div>
          </template>
          <el-table :data="activeUsers" stripe style="width: 100%">
            <el-table-column width="60">
              <template #default="{ row }">
                <el-avatar :size="32" :src="row.avatar" />
              </template>
            </el-table-column>
            <el-table-column prop="nickname" label="用户名" />
            <el-table-column prop="lastActive" label="最后活跃" width="180">
              <template #default="{ row }">
                {{ formatTime(row.lastActive) }}
              </template>
            </el-table-column>
            <el-table-column prop="activityScore" label="活跃度" width="120">
              <template #default="{ row }">
                <el-progress
                    :percentage="row.activityScore"
                    :color="getActivityColor(row.activityScore)"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 系统日志 -->
    <el-card class="log-card">
      <template #header>
        <div class="card-header">
          <span>系统日志</span>
          <div class="header-actions">
            <el-select v-model="logType" style="width: 120px">
              <el-option label="全部日志" value="all" />
              <el-option label="错误日志" value="error" />
              <el-option label="警告日志" value="warn" />
              <el-option label="操作日志" value="operation" />
            </el-select>
            <el-button type="primary" link @click="clearLogs">
              清空日志
            </el-button>
          </div>
        </div>
      </template>
      <el-timeline>
        <el-timeline-item
            v-for="(log, index) in systemLogs"
            :key="index"
            :type="getLogType(log.level)"
            :timestamp="formatDateTime(log.time)"
        >
          <div class="log-content">
            <span :class="['log-level', log.level]">
              {{ log.level.toUpperCase() }}
            </span>
            <span class="log-message">{{ log.message }}</span>
          </div>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>


<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { formatDistanceToNow, format } from 'date-fns'
import { zhCN } from 'date-fns/locale'
import {
  User,
  Memo,
  ChatDotRound,
  Trophy,
  ArrowUp,
  ArrowDown
} from '@element-plus/icons-vue'

const router = useRouter()
const userGrowthChart = ref(null)
const userDistributionChart = ref(null)
const chartInstances = ref([])

// 数据统计卡片
const statCards = ref([
  {
    label: '总用户数',
    value: '1,234',
    trend: 12.5,
    icon: 'User',
    color: '#409EFF'
  },
  {
    label: '发帖数',
    value: '352',
    trend: -5.2,
    icon: 'Memo',
    color: '#67C23A'
  },
  {
    label: '评论数',
    value: '1,586',
    trend: 8.4,
    icon: 'ChatDotRound',
    color: '#E6A23C'
  },
  {
    label: '活动数',
    value: '45',
    trend: 15.8,
    icon: 'Trophy',
    color: '#F56C6C'
  }
])

// 时间范围
const timeRange = ref('week')

// 热门游戏
const hotGames = ref([
  { rank: 1, title: '游戏1', rating: 4.5, playerCount: 12500 },
  { rank: 2, title: '游戏2', rating: 4.3, playerCount: 10200 },
  { rank: 3, title: '游戏3', rating: 4.2, playerCount: 9800 },
  { rank: 4, title: '游戏4', rating: 4.0, playerCount: 8600 },
  { rank: 5, title: '游戏5', rating: 3.9, playerCount: 7400 }
])

// 活跃用户
const activeUsers = ref([
  {
    nickname: '用户1',
    avatar: '',
    lastActive: new Date(),
    activityScore: 95
  },
  {
    nickname: '用户2',
    avatar: '',
    lastActive: new Date(Date.now() - 3600000),
    activityScore: 88
  },
  {
    nickname: '用户3',
    avatar: '',
    lastActive: new Date(Date.now() - 7200000),
    activityScore: 76
  },
  {
    nickname: '用户4',
    avatar: '',
    lastActive: new Date(Date.now() - 10800000),
    activityScore: 65
  },
  {
    nickname: '用户5',
    avatar: '',
    lastActive: new Date(Date.now() - 14400000),
    activityScore: 52
  }
])

// 系统日志
const logType = ref('all')
const systemLogs = ref([
  {
    level: 'error',
    time: new Date(),
    message: '系统错误: 数据库连接失败'
  },
  {
    level: 'warn',
    time: new Date(Date.now() - 1800000),
    message: '警告: 系统负载过高'
  },
  {
    level: 'info',
    time: new Date(Date.now() - 3600000),
    message: '系统更新完成'
  }
])

// 初始化图表
const initCharts = () => {
  const userGrowthOption = {
    title: {
      text: '用户增长趋势'
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        data: [820, 932, 901, 934, 1290, 1330, 1320],
        type: 'line',
        smooth: true
      }
    ]
  }

  const userDistributionOption = {
    title: {
      text: '用户分布'
    },
    tooltip: {
      trigger: 'item'
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        data: [
          { value: 1048, name: '新用户' },
          { value: 735, name: '活跃用户' },
          { value: 580, name: '沉睡用户' },
          { value: 484, name: '流失用户' }
        ]
      }
    ]
  }

  // 初始化用户增长图表
  const growthChart = echarts.init(userGrowthChart.value)
  growthChart.setOption(userGrowthOption)
  chartInstances.value.push(growthChart)

  // 初始化用户分布图表
  const distributionChart = echarts.init(userDistributionChart.value)
  distributionChart.setOption(userDistributionOption)
  chartInstances.value.push(distributionChart)
}

// 更多按钮处理
const viewMore = (type) => {
  switch (type) {
    case 'games':
      router.push('/admin/games')
      break
    case 'users':
      router.push('/admin/users')
      break
  }
}

// 清空日志
const clearLogs = () => {
  systemLogs.value = []
}

// 格式化时间
const formatTime = (time) => {
  return formatDistanceToNow(new Date(time), { addSuffix: true, locale: zhCN })
}

const formatDateTime = (time) => {
  return format(new Date(time), 'yyyy-MM-dd HH:mm:ss')
}

// 格式化数字
const formatNumber = (num) => {
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

// 获取活跃度颜色
const getActivityColor = (score) => {
  if (score >= 90) return '#67C23A'
  if (score >= 70) return '#E6A23C'
  if (score >= 50) return '#F56C6C'
  return '#909399'
}

// 获取日志类型
const getLogType = (level) => {
  const typeMap = {
    error: 'danger',
    warn: 'warning',
    info: 'info'
  }
  return typeMap[level] || 'info'
}

// 监听窗口大小变化
const handleResize = () => {
  chartInstances.value.forEach(chart => chart.resize())
}

onMounted(() => {
  initCharts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstances.value.forEach(chart => chart.dispose())
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 20px;

  .stat-card {
    margin-bottom: 20px;

    .stat-content {
      position: relative;
    }

    .stat-value {
      font-size: 24px;
      font-weight: bold;

      .stat-trend {
        font-size: 14px;
        margin-left: 8px;
      }
    }

    .stat-label {
      margin-top: 8px;
      color: #909399;
    }

    .stat-icon {
      position: absolute;
      top: 50%;
      right: 0;
      transform: translateY(-50%);
      opacity: 0.2;
    }
  }

  .chart-row {
    margin-bottom: 20px;

    .chart-card {
      height: 400px;

      .chart-container {
        height: 100%;

        .chart {
          height: 100%;
        }
      }
    }
  }

  .data-row {
    margin-bottom: 20px;

    .data-card {
      margin-bottom: 20px;
    }
  }

  .log-card {
    .log-content {
      display: flex;
      align-items: center;
      gap: 10px;

      .log-level {
        padding: 2px 6px;
        border-radius: 4px;
        font-size: 12px;
        color: #fff;

        &.error {
          background-color: #F56C6C;
        }

        &.warn {
          background-color: #E6A23C;
        }

        &.info {
          background-color: #909399;
        }
      }
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-actions {
      display: flex;
      align-items: center;
      gap: 10px;
    }
  }
}
</style>