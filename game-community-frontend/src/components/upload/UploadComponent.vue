<template>
  <div class="upload-component">
    <el-upload
        :action="uploadUrl"
        :headers="headers"
        :before-upload="beforeUpload"
        :on-success="handleSuccess"
        :on-error="handleError"
        :on-progress="handleProgress"
        :multiple="multiple"
        :limit="limit"
        :file-list="fileList"
        :accept="accept"
    >
      <template #trigger>
        <el-button type="primary">选择文件</el-button>
      </template>
      <template #tip>
        <div class="el-upload__tip">
          {{ tip }}
        </div>
      </template>
    </el-upload>

    <el-progress
        v-if="showProgress"
        :percentage="uploadProgress"
        :status="uploadStatus"
    />
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'

export default {
  name: 'UploadComponent',

  props: {
    type: {
      type: String,
      required: true
    },
    multiple: {
      type: Boolean,
      default: false
    },
    limit: {
      type: Number,
      default: 1
    },
    accept: {
      type: String,
      default: '*'
    },
    tip: {
      type: String,
      default: '支持jpg、png、gif格式，单个文件不超过2MB'
    }
  },

  setup(props, { emit }) {
    const store = useStore()
    const fileList = ref([])
    const uploadProgress = ref(0)
    const uploadStatus = ref('')
    const showProgress = ref(false)

    const uploadUrl = computed(() => {
      return `${process.env.VUE_APP_BASE_API}/api/files/upload?type=${props.type}`
    })

    const headers = computed(() => ({
      Authorization: `Bearer ${store.getters['auth/token']}`
    }))

    const beforeUpload = (file) => {
      const isValidSize = file.size / 1024 / 1024 < 2
      if (!isValidSize) {
        ElMessage.error('文件大小不能超过2MB')
        return false
      }
      showProgress.value = true
      uploadProgress.value = 0
      uploadStatus.value = ''
      return true
    }

    const handleSuccess = (response, file) => {
      uploadProgress.value = 100
      uploadStatus.value = 'success'
      showProgress.value = false
      emit('upload-success', response, file)
    }

    const handleError = (error) => {
      uploadStatus.value = 'exception'
      showProgress.value = false
      ElMessage.error('上传失败：' + (error.message || '未知错误'))
      emit('upload-error', error)
    }

    const handleProgress = (event) => {
      uploadProgress.value = Math.round(event.percent)
    }

    return {
      fileList,
      uploadUrl,
      headers,
      uploadProgress,
      uploadStatus,
      showProgress,
      beforeUpload,
      handleSuccess,
      handleError,
      handleProgress
    }
  }
}
</script>

<style scoped>
.upload-component {
  width: 100%;
}
.el-upload__tip {
  font-size: 12px;
  color: #606266;
  margin-top: 7px;
}
</style>