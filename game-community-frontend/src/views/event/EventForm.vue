<template>
  <div class="event-form-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>{{ isEdit ? '编辑活动' : '创建活动' }}</h2>
        </div>
      </template>

      <el-form
          ref="eventForm"
          :model="eventData"
          :rules="rules"
          label-width="100px"
      >
        <el-form-item label="活动标题" prop="title">
          <el-input
              v-model="eventData.title"
              placeholder="请输入活动标题"
              maxlength="100"
              show-word-limit
          />
        </el-form-item>

        <el-form-item label="活动类型" prop="type">
          <el-select v-model="eventData.type" placeholder="请选择活动类型">
            <el-option
                v-for="type in eventTypes"
                :key="type.value"
                :label="type.label"
                :value="type.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="关联游戏" prop="gameId">
          <el-select
              v-model="eventData.gameId"
              placeholder="请选择关联游戏"
              clearable
              filterable
          >
            <el-option
                v-for="game in games"
                :key="game.id"
                :label="game.title"
                :value="game.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="活动时间" prop="timeRange">
          <el-date-picker
              v-model="eventData.timeRange"
              type="datetimerange"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              :default-time="defaultTime"
          />
        </el-form-item>

        <el-form-item label="活动形式">
          <el-radio-group v-model="eventData.isOnline">
            <el-radio :label="false">线下活动</el-radio>
            <el-radio :label="true">线上活动</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item
            label="活动地点"
            prop="location"
            v-if="!eventData.isOnline"
        >
          <el-input
              v-model="eventData.location"
              placeholder="请输入活动地点"
          />
        </el-form-item>

        <el-form-item label="参与人数" prop="maxParticipants">
          <el-input-number
              v-model="eventData.maxParticipants"
              :min="0"
              placeholder="不填则不限制人数"
          />
        </el-form-item>

        <el-form-item label="封面图片" prop="coverImage">
          <el-upload
              class="cover-uploader"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :before-upload="beforeUpload"
              :on-success="handleCoverSuccess"
              :on-error="handleUploadError"
          >
            <img
                v-if="eventData.coverImage"
                :src="eventData.coverImage"
                class="cover-image"
            />
            <el-icon v-else class="cover-uploader-icon">
              <Plus />
            </el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item label="活动图片">
          <el-upload
              class="image-uploader"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :file-list="fileList"
              list-type="picture-card"
              :on-preview="handlePreview"
              :on-remove="handleRemove"
              :on-success="handleImageSuccess"
              :on-error="handleUploadError"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>

          <el-dialog v-model="previewVisible">
            <img :src="previewUrl" alt="Preview" style="width: 100%" />
          </el-dialog>
        </el-form-item>

        <el-form-item label="活动详情" prop="description">
          <el-input
              v-model="eventData.description"
              type="textarea"
              :rows="10"
              placeholder="支持 Markdown 格式"
          />
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              @click="submitForm"
              :loading="submitting"
          >
            {{ isEdit ? '保存' : '创建' }}
          </el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { createEvent, updateEvent, getEventDetail } from '@/api/event'
import { getGameList } from '@/api/game'

export default {
  name: 'EventForm',
  components: { Plus },

  setup() {
    const store = useStore()
    const route = useRoute()
    const router = useRouter()
    const eventForm = ref(null)
    const loading = ref(false)
    const submitting = ref(false)
    const games = ref([])
    const previewVisible = ref(false)
    const previewUrl = ref('')
    const fileList = ref([])

    const eventData = reactive({
      title: '',
      type: '',
      gameId: null,
      timeRange: [],
      isOnline: false,
      location: '',
      maxParticipants: null,
      coverImage: '',
      images: [],
      description: ''
    })

    const isEdit = computed(() => !!route.params.id)

    const rules = {
      title: [
        { required: true, message: '请输入活动标题', trigger: 'blur' },
        { min: 5, max: 100, message: '标题长度在5-100个字符之间', trigger: 'blur' }
      ],
      type: [
        { required: true, message: '请选择活动类型', trigger: 'change' }
      ],
      timeRange: [
        { required: true, message: '请选择活动时间', trigger: 'change' }
      ],
      location: [
        { required: true, message: '请输入活动地点', trigger: 'blur' }
      ],
      description: [
        { required: true, message: '请输入活动详情', trigger: 'blur' }
      ]
    }

    const eventTypes = [
      { label: '比赛', value: 'TOURNAMENT' },
      { label: '线下聚会', value: 'MEETUP' },
      { label: '展会', value: 'EXHIBITION' },
      { label: '工作坊', value: 'WORKSHOP' }
    ]

    const defaultTime = [
      new Date(2000, 1, 1, 0, 0, 0),
      new Date(2000, 1, 1, 23, 59, 59)
    ]

    // 初始化数据
    const initData = async () => {
      if (isEdit.value) {
        loading.value = true
        try {
          const res = await getEventDetail(route.params.id)
          const event = res.data
          Object.assign(eventData, {
            title: event.title,
            type: event.type,
            gameId: event.gameId,
            timeRange: [event.startTime, event.endTime],
            isOnline: event.isOnline,
            location: event.location,
            maxParticipants: event.maxParticipants,
            coverImage: event.coverImage,
            description: event.description
          })
          fileList.value = (event.images || []).map((url, index) => ({
            name: `image-${index}`,
            url
          }))
        } catch (error) {
          ElMessage.error('获取活动信息失败')
        } finally {
          loading.value = false
        }
      }
    }

    // 获取游戏列表
    const fetchGames = async () => {
      try {
        const res = await getGameList()
        games.value = res.data
      } catch (error) {
        ElMessage.error('获取游戏列表失败')
      }
    }

    // 提交表单
    const submitForm = async () => {
      await eventForm.value.validate(async (valid) => {
        if (!valid) return

        const [startTime, endTime] = eventData.timeRange
        const formData = {
          ...eventData,
          startTime,
          endTime,
          images: fileList.value.map(file => file.url)
        }
        delete formData.timeRange

        submitting.value = true
        try {
          if (isEdit.value) {
            await updateEvent(route.params.id, formData)
            ElMessage.success('更新成功')
          } else {
            await createEvent(formData)
            ElMessage.success('创建成功')
          }
          router.push('/events')
        } catch (error) {
          ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
        } finally {
          submitting.value = false
        }
      })
    }

    // 返回上一页
    const goBack = () => {
      router.back()
    }

    // 图片上传相关方法
    const beforeUpload = (file) => {
      const isImage = file.type.startsWith('image/')
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isImage) {
        ElMessage.error('只能上传图片文件！')
        return false
      }
      if (!isLt2M) {
        ElMessage.error('图片大小不能超过 2MB！')
        return false
      }
      return true
    }

    const handleCoverSuccess = (res) => {
      eventData.coverImage = res.url
    }

    const handleImageSuccess = (res, file) => {
      file.url = res.url
    }

    const handleUploadError = () => {
      ElMessage.error('上传失败')
    }

    const handlePreview = (file) => {
      previewUrl.value = file.url
      previewVisible.value = true
    }

    const handleRemove = (file) => {
      const index = fileList.value.indexOf(file)
      if (index !== -1) {
        fileList.value.splice(index, 1)
      }
    }

    onMounted(() => {
      initData()
      fetchGames()
    })

    return {
      eventForm,
      eventData,
      loading,
      submitting,
      games,
      previewVisible,
      previewUrl,
      fileList,
      isEdit,
      rules,
      eventTypes,
      defaultTime,
      submitForm,
      goBack,
      beforeUpload,
      handleCoverSuccess,
      handleImageSuccess,
      handleUploadError,
      handlePreview,
      handleRemove
    }
  }
}
</script>

<style scoped>
.event-form-container {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.cover-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 300px;
  height: 180px;
}

.cover-uploader:hover {
  border-color: #409eff;
}

.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 300px;
  height: 180px;
  line-height: 180px;
  text-align: center;
}

.cover-image {
  width: 300px;
  height: 180px;
  display: block;
  object-fit: cover;
}

:deep(.el-upload--picture-card) {
  width: 120px;
  height: 120px;
  line-height: 120px;
}
</style>