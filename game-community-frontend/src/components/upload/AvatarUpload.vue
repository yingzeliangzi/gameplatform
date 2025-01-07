<template>
  <div class="avatar-upload">
    <div class="avatar-preview" @click="triggerFileInput">
      <img v-if="imageUrl" :src="imageUrl" class="avatar" />
      <el-icon v-else class="avatar-uploader-icon"><plus /></el-icon>
    </div>

    <input
        type="file"
        ref="fileInput"
        accept="image/*"
        style="display: none"
        @change="handleFileChange"
    />

    <!-- 图片裁剪对话框 -->
    <el-dialog
        v-model="showCropper"
        title="裁剪头像"
        width="600px"
        :close-on-click-modal="false"
    >
      <div class="cropper-container">
        <vue-cropper
            ref="cropper"
            :src="cropperSrc"
            :aspect-ratio="1"
            preview=".preview"
            :view-mode="1"
            :auto-crop-area="0.8"
            :background="true"
            :rotatable="true"
            :zoomable="true"
        />
        <div class="preview-container">
          <div class="preview"></div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showCropper = false">取消</el-button>
          <el-button type="primary" @click="cropImage">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import VueCropper from 'vue-cropper'
import 'vue-cropper/dist/index.css'
import { uploadAvatar } from '@/api/user'

export default {
  name: 'AvatarUpload',
  components: {
    VueCropper,
    Plus
  },

  props: {
    value: {
      type: String,
      default: ''
    }
  },

  emits: ['update:value', 'upload-success'],

  setup(props, { emit }) {
    const fileInput = ref(null)
    const cropper = ref(null)
    const imageUrl = ref(props.value)
    const showCropper = ref(false)
    const cropperSrc = ref('')

    const triggerFileInput = () => {
      fileInput.value.click()
    }

    const handleFileChange = (e) => {
      const file = e.target.files[0]
      if (!file) return

      // 验证文件类型和大小
      const isImage = file.type.startsWith('image/')
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isImage) {
        ElMessage.error('请上传图片文件')
        return
      }
      if (!isLt2M) {
        ElMessage.error('图片大小不能超过2MB')
        return
      }

      // 读取文件并显示裁剪器
      const reader = new FileReader()
      reader.onload = (e) => {
        cropperSrc.value = e.target.result
        showCropper.value = true
      }
      reader.readAsDataURL(file)

      // 重置文件输入以允许选择相同文件
      e.target.value = ''
    }

    const cropImage = async () => {
      if (!cropper.value) return

      // 获取裁剪后的图片blob
      cropper.value.getCropBlob(async (blob) => {
        try {
          // 创建文件对象
          const file = new File([blob], 'avatar.png', { type: 'image/png' })

          // 上传文件
          const formData = new FormData()
          formData.append('file', file)

          const response = await uploadAvatar(formData)

          imageUrl.value = response.data
          emit('update:value', response.data)
          emit('upload-success', response.data)

          showCropper.value = false
          ElMessage.success('头像上传成功')
        } catch (error) {
          ElMessage.error('头像上传失败：' + error.message)
        }
      })
    }

    return {
      fileInput,
      cropper,
      imageUrl,
      showCropper,
      cropperSrc,
      triggerFileInput,
      handleFileChange,
      cropImage
    }
  }
}
</script>

<style scoped>
.avatar-upload {
  text-align: center;
}

.avatar-preview {
  width: 178px;
  height: 178px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.avatar-preview:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cropper-container {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.preview-container {
  width: 100px;
  margin-left: 20px;
}

.preview {
  width: 100px;
  height: 100px;
  overflow: hidden;
  border: 1px solid #ccc;
}

.dialog-footer {
  margin-top: 20px;
  text-align: right;
}
</style>