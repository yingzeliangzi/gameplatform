<template>
  <div class="event-detail-container">
    <el-card v-loading="loading">
      <!-- 活动头部信息 -->
      <div class="event-header">
        <div class="event-title-section">
          <h1 class="event-title">{{ event.title }}</h1>
          <div class="event-tags">
            <el-tag :type="getEventTypeTag(event.type)">
              {{ getEventTypeLabel(event.type) }}
            </el-tag>
            <el-tag v-if="event.isOnline" type="info">线上活动</el-tag>
            <el-tag v-if="event.game" type="success">{{ event.gameName }}</el-tag>
          </div>
        </div>

        <div class="event-status-section">
          <el-button
              v-if="isAdmin"
              type="primary"
              plain
              @click="handleEdit"
          >
            编辑活动
          </el-button>
          <template v-else>
            <el-button
                v-if="!event.isRegistered"
                type="primary"
                :disabled="isEventFull || !isUpcoming"
                @click="showRegisterDialog"
            >
              {{ getRegisterButtonText }}
            </el-button>
            <el-button
                v-else
                type="danger"
                plain
                :disabled="!isUpcoming"
                @click="handleCancelRegistration"
            >
              取消报名
            </el-button>
          </template>
        </div>
      </div>

      <!-- 活动关键信息 -->
      <div class="event-meta">
        <div class="meta-item">
          <div class="meta-label">
            <el-icon><Calendar /></el-icon>
            活动时间
          </div>
          <div class="meta-content">
            {{ formatDateTime(event.startTime) }} - {{ formatDateTime(event.endTime) }}
          </div>
        </div>
        <div class="meta-item">
          <div class="meta-label">
            <el-icon><Location /></el-icon>
            活动地点
          </div>
          <div class="meta-content">
            {{ event.isOnline ? '线上活动' : event.location }}
          </div>
        </div>
        <div class="meta-item">
          <div class="meta-label">
            <el-icon><UserFilled /></el-icon>
            报名情况
          </div>
          <div class="meta-content">
            <el-progress
                :percentage="registrationPercentage"
                :format="format => `${event.currentParticipants}/${event.maxParticipants || '∞'}`"
            />
          </div>
        </div>
      </div>

      <!-- 活动详情 -->
      <el-tabs class="event-tabs">
        <el-tab-pane label="活动详情">
          <div class="event-description" v-html="formattedDescription"></div>

          <div v-if="event.images?.length" class="event-images">
            <h3>活动图片</h3>
            <el-image-viewer
                v-if="showViewer"
                :initial-index="imageIndex"
                :url-list="event.images"
                @close="showViewer = false"
            />
            <div class="image-list">
              <el-image
                  v-for="(image, index) in event.images"
                  :key="index"
                  :src="image"
                  fit="cover"
                  @click="previewImage(index)"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="参与者" v-if="isAdmin">
          <div class="participants-list">
            <el-table :data="participants" border>
              <el-table-column prop="user.nickname" label="用户名" />
              <el-table-column prop="contactInfo" label="联系方式" />
              <el-table-column prop="registeredAt" label="报名时间">
                <template #default="{ row }">
                  {{ formatDateTime(row.registeredAt) }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.status)">
                    {{ getStatusLabel(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="remark" label="备注" show-overflow-tooltip />
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

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
          <el-input v-model="registerForm.contactInfo" placeholder="请输入您的联系方式" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
              v-model="registerForm.remark"
              type="textarea"
              :rows="3"
              placeholder="有什么想告诉主办方的吗？"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="registerDialogVisible = false">取消</el-button>
          <el-button
              type="primary"
              :loading="submitting"
              @click="handleRegister"
          >
            确认报名
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar, Location, UserFilled, Picture } from '@element-plus/icons-vue'
import { format } from 'date-fns'
import { registerEvent, cancelRegistration } from '@/api/event'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

export default {
  name: 'EventDetail',
  components: { Calendar, Location, UserFilled, Picture },

  setup() {
    const store = useStore()
    const route = useRoute()
    const router = useRouter()
    const loading = ref(false)
    const registerDialogVisible = ref(false)
    const submitting = ref(false)
    const showViewer = ref(false)
    const imageIndex = ref(0)

    const registerForm = ref({
      contactInfo: '',
      remark: ''
    })

    const registerRules = {
      contactInfo: [
        { required: true, message: '请输入联系方式', trigger: 'blur' }
      ]
    }

    const event = computed(() => store.state.event.currentEvent || {})
    const isAdmin = computed(() => store.state.user.roles.includes('ADMIN'))
    const isUpcoming = computed(() => event.value.status === 'UPCOMING')
    const isEventFull = computed(() => {
      const { maxParticipants, currentParticipants } = event.value
      return maxParticipants && currentParticipants >= maxParticipants
    })

    const registrationPercentage = computed(() => {
      const { maxParticipants, currentParticipants } = event.value
      return maxParticipants ? (currentParticipants / maxParticipants) * 100 : 100
    })

    const formattedDescription = computed(() => {
      return DOMPurify.sanitize(marked(event.value.description || ''))
    })

    const getRegisterButtonText = computed(() => {
      if (!isUpcoming.value) return '活动已结束'
      if (isEventFull.value) return '报名已满'
      return '立即报名'
    })

    // ... 其他方法实现

    onMounted(async () => {
      const eventId = route.params.id
      if (eventId) {
        loading.value = true
        try {
          await store.dispatch('event/fetchEventDetail', eventId)
        } catch (error) {
          ElMessage.error('获取活动详情失败')
        } finally {
          loading.value = false
        }
      }
    })

    return {
      loading,
      event,
      registerDialogVisible,
      registerForm,
      registerRules,
      submitting,
      showViewer,
      imageIndex,
      isAdmin,
      isUpcoming,
      isEventFull,
      registrationPercentage,
      formattedDescription,
      getRegisterButtonText
      // ... 其他返回值
    }
  }
}
</script>

<style scoped>
.event-detail-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.event-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
}

.event-title {
  margin: 0 0 15px 0;
  font-size: 28px;
}

.event-tags {
  display: flex;
  gap: 10px;
}

.event-meta {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meta-label {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #666;
  font-size: 14px;
}

.meta-content {
  font-size: 16px;
}

.event-tabs {
  margin-top: 30px;
}

.event-description {
  line-height: 1.8;
}

.event-images {
  margin-top: 30px;
}

.image-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
  margin-top: 15px;
}

.image-list .el-image {
  height: 200px;
  border-radius: 4px;
  cursor: pointer;
}

.participants-list {
  margin-top: 20px;
}

.image-error {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 30px;
}

:deep(.el-progress-bar__outer) {
  background-color: #e9ecef;
}
</style>