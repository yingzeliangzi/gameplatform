<template>
  <div class="event-list-container">
    <!-- 搜索和筛选 -->
    <el-card class="filter-card">
      <div class="filter-header">
        <div class="search-filters">
          <el-input
              v-model="searchForm.keyword"
              placeholder="搜索活动..."
              prefix-icon="Search"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch"
          />
          <el-select
              v-model="searchForm.type"
              placeholder="活动类型"
              clearable
              @change="handleSearch"
          >
            <el-option
                v-for="type in eventTypes"
                :key="type.value"
                :label="type.label"
                :value="type.value"
            />
          </el-select>
          <el-select
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
        <el-button
            v-if="isAdmin"
            type="primary"
            @click="createEvent"
        >
          创建活动
        </el-button>
      </div>
    </el-card>

    <!-- 活动展示区域 -->
    <el-tabs v-model="activeTab" @tab-click="handleTabChange">
      <el-tab-pane label="即将开始" name="upcoming">
        <el-row :gutter="20">
          <el-col
              v-for="event in eventList"
              :key="event.id"
              :xs="24"
              :sm="12"
              :md="8"
              :lg="6"
          >
            <el-card class="event-card" :body-style="{ padding: '0px' }">
              <el-image
                  :src="event.coverImage"
                  class="event-image"
                  fit="cover"
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>

              <div class="event-info">
                <div class="event-type">
                  <el-tag :type="getEventTypeTag(event.type)">
                    {{ getEventTypeLabel(event.type) }}
                  </el-tag>
                </div>
                <h3 class="event-title" @click="goToEventDetail(event.id)">
                  {{ event.title }}
                </h3>
                <div class="event-meta">
                  <div class="time-info">
                    <el-icon><Calendar /></el-icon>
                    {{ formatDateTime(event.startTime) }}
                  </div>
                  <div class="location-info">
                    <el-icon><Location /></el-icon>
                    {{ event.isOnline ? '线上活动' : event.location }}
                  </div>
                </div>
                <div class="event-status">
                  <div class="participants">
                    <span>{{ event.currentParticipants }}/{{ event.maxParticipants || '∞' }}</span>
                    <span class="label">已报名</span>
                  </div>
                  <el-button
                      :type="event.isRegistered ? 'info' : 'primary'"
                      :disabled="isEventFull(event)"
                      @click="handleRegister(event)"
                  >
                    {{ getRegisterButtonText(event) }}
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane label="进行中" name="ongoing">
        <!-- 与上面相同的活动卡片布局 -->
      </el-tab-pane>

      <el-tab-pane label="已结束" name="ended">
        <!-- 与上面相同的活动卡片布局 -->
      </el-tab-pane>
    </el-tabs>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[12, 24, 36, 48]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </div>

    <!-- 报名对话框 -->
    <el-dialog
        v-model="registerDialogVisible"
        title="活动报名"
        width="500px"
    >
      <el-form
          ref="registerForm"
          :model="registerForm"
          :rules="registerRules"
          label-width="80px"
      >
        <el-form-item label="联系方式" prop="contactInfo">
          <el-input v-model="registerForm.contactInfo" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
              v-model="registerForm.remark"
              type="textarea"
              :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="registerDialogVisible = false">取消</el-button>
          <el-button
              type="primary"
              :loading="submitting"
              @click="submitRegistration"
          >
            确认报名
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture, Calendar, Location } from '@element-plus/icons-vue'
import { format } from 'date-fns'
import { registerEvent, cancelRegistration } from '@/api/event'

export default {
  name: 'EventList',
  components: { Picture, Calendar, Location },

  setup() {
    const store = useStore()
    const router = useRouter()
    const currentPage = ref(1)
    const pageSize = ref(12)
    const activeTab = ref('upcoming')
    const registerDialogVisible = ref(false)
    const submitting = ref(false)
    const selectedEventId = ref(null)

    const searchForm = reactive({
      keyword: '',
      type: '',
      gameId: null
    })

    const registerForm = reactive({
      contactInfo: '',
      remark: ''
    })

    const registerRules = {
      contactInfo: [
        { required: true, message: '请输入联系方式', trigger: 'blur' }
      ]
    }

    const eventTypes = [
      { label: '比赛', value: 'TOURNAMENT' },
      { label: '线下聚会', value: 'MEETUP' },
      { label: '展会', value: 'EXHIBITION' },
      { label: '工作坊', value: 'WORKSHOP' }
    ]

    const isAdmin = computed(() => {
      return store.state.user.roles.includes('ADMIN')
    })

    const fetchEvents = async () => {
      const params = {
        page: currentPage.value - 1,
        size: pageSize.value,
        ...searchForm,
        status: activeTab.value.toUpperCase()
      }
      await store.dispatch('event/fetchEvents', params)
    }

    // ... 其他方法实现
    // 处理报名
    const handleRegister = (event) => {
      if (event.isRegistered) {
        cancelEventRegistration(event.id)
      } else {
        selectedEventId.value = event.id
        registerDialogVisible.value = true
      }
    }

    // 提交报名
    const submitRegistration = async () => {
      submitting.value = true
      try {
        await registerEvent(selectedEventId.value, registerForm)
        ElMessage.success('报名成功')
        registerDialogVisible.value = false
        fetchEvents()
      } catch (error) {
        ElMessage.error('报名失败')
      } finally {
        submitting.value = false
      }
    }

    // 取消报名
    const cancelEventRegistration = async (eventId) => {
      try {
        await cancelRegistration(eventId)
        ElMessage.success('已取消报名')
        fetchEvents()
      } catch (error) {
        ElMessage.error('取消报名失败')
      }
    }

    onMounted(() => {
      fetchEvents()
    })

    return {
      currentPage,
      pageSize,
      activeTab,
      searchForm,
      registerForm,
      registerRules,
      registerDialogVisible,
      submitting,
      eventTypes,
      isAdmin,
      handleRegister,
      submitRegistration,
      // ... 其他返回值
    }
  }
}
</script>

<style scoped>
.event-list-container {
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

.search-filters {
  display: flex;
  gap: 15px;
  flex: 1;
  max-width: 800px;
}

.event-card {
  margin-bottom: 20px;
  transition: transform 0.3s;
}

.event-card:hover {
  transform: translateY(-5px);
}

.event-image {
  height: 200px;
  width: 100%;
}

.event-info {
  padding: 14px;
}

.event-type {
  margin-bottom: 10px;
}

.event-title {
  margin: 10px 0;
  font-size: 16px;
  cursor: pointer;
}

.event-meta {
  font-size: 14px;
  color: #666;
  margin-bottom: 15px;
}

.time-info,
.location-info {
  display: flex;
  align-items: center;
  gap: 5px;
  margin: 5px 0;
}

.event-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
}

.participants {
  display: flex;
  flex-direction: column;
  font-size: 14px;
}

.participants .label {
  color: #999;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>