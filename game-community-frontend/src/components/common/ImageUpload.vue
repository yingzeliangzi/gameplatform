<template>
  <div class="image-upload">
    <el-upload
        ref="uploadRef"
        :action="uploadUrl"
        :headers="headers"
        :show-file-list="showFileList"
        :on-preview="handlePreview"
        :on-remove="handleRemove"
        :before-upload="beforeUpload"
        :on-success="handleSuccess"
        :on-error="handleError"
        :limit="limit"
        :multiple="multiple"
        :accept="accept"
        :list-type="listType"
        :class="{ 'hide-upload': hideUpload }"
    >
      <template #trigger>
        <slot name="trigger">
          <el-button type="primary" :icon="Plus">{{ buttonText }}</el-button>
        </slot>
      </template>

      <template #tip>
        <slot name="tip">
          <div class="el-upload__tip" v-if="tip">
            {{ tip }}
          </div>
        </slot>
      </template>
    </el-upload>

    <!-- 图片预览对话框 -->
    <el-dialog
        v-model="previewVisible"
        :title="previewTitle"
        append-to-body
    >
      <img :src="previewUrl" style="width: 100%" :alt="previewTitle">
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  // 文件列表类型
  listType: {
    type: String,
    default: 'text'
  },
  // 显示文件列表
  showFileList: {
    type: Boolean,
    default: true
  },
  // 上传数量限制
  limit: {
    type: Number,
    default: 5
  },
  // 是否支持多选
  multiple: {
    type: Boolean,
    default: true
  },
  // 接受上传的文件类型
  accept: {
    type: String,
    default: 'image/*'
  },
  // 按钮文字
  buttonText: {
    type: String,
    default: '上传图片'
  },
  // 提示文字
  tip: {
    type: String,
    default: '只能上传jpg/png文件，且不超过2MB'
  },
  // 是否隐藏上传按钮
  hideUpload: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'update:fileList',
  'success',
  'error',
  'exceed',
  'remove'
])

const store = useStore()
const uploadRef = ref(null)
const previewVisible = ref(false)
const previewUrl = ref('')
const previewTitle = ref('')

// 上传地址
const uploadUrl = process.env.VUE_APP_BASE_API + '/api/file/upload'

// 请求头
const headers = computed(() => ({
  Authorization: 'Bearer ' + store.state.user.token
}))

// 上传前校验
const beforeUpload = (file) => {
  // 类型限制
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }

  // 大小限制
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('上传图片大小不能超过 2MB!')
    return false
  }

  return true
}

// 上传成功
const handleSuccess = (response, file, fileList) => {
  emit('update:fileList', fileList)
  emit('success', { response, file, fileList })
}

// 上传失败
const handleError = (error, file, fileList) => {
  ElMessage.error('上传失败')
  emit('error', { error, file, fileList })
}

// 预览图片
const handlePreview = (file) => {
  previewUrl.value = file.url
  previewTitle.value = file.name
  previewVisible.value = true
}

// 移除图片
const handleRemove = (file, fileList) => {
  emit('update:fileList', fileList)
  emit('remove', { file, fileList })
}

// 手动上传
const submit = () => {
  uploadRef.value?.submit()
}

// 清空上传列表
const clearFiles = () => {
  uploadRef.value?.clearFiles()
}

// 中止上传
const abort = (file) => {
  uploadRef.value?.abort(file)
}

// 暴露方法
defineExpose({
  submit,
  clearFiles,
  abort
})
</script>

<style lang="scss" scoped>
.image-upload {
  :deep(.hide-upload) {
    .el-upload--picture-card {
      display: none;
    }
  }

  :deep(.el-upload-list--picture-card) {
    .el-upload-list__item {
      width: 100px;
      height: 100px;
    }
  }
}
</style>