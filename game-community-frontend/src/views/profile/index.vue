<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <!-- 顶部用户信息 -->
      <div class="profile-header">
        <div class="avatar-container">
          <el-avatar
              :size="120"
              :src="userInfo.avatar"
              @click="triggerAvatarUpload"
          />
          <div class="avatar-upload">
            <el-upload
                ref="uploadRef"
                class="avatar-uploader"
                :action="uploadUrl"
                :headers="uploadHeaders"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :on-success="handleAvatarSuccess"
                :on-error="handleAvatarError"
            >
              <div class="upload-trigger">
                <el-icon><Camera /></el-icon>
                <span>更换头像</span>
              </div>
            </el-upload>
          </div>
        </div>
        <div class="user-info">
          <h2>{{ userInfo.nickname }}</h2>
          <p>{{ userInfo.email }}</p>
          <p>注册时间：{{ formatDateTime(userInfo.createdAt) }}</p>
        </div>
      </div>

      <!-- 标签页切换 -->
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本资料" name="basic">
          <el-form
              ref="basicForm"
              :model="basicInfo"
              :rules="basicRules"
              label-width="100px"
          >
            <el-form-item label="用户名" prop="username">
              <el-input v-model="basicInfo.username" disabled />
            </el-form-item>
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="basicInfo.nickname" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="basicInfo.email" />
            </el-form-item>
            <el-form-item label="个人简介" prop="bio">
              <el-input
                  v-model="basicInfo.bio"
                  type="textarea"
                  :rows="4"
                  placeholder="介绍一下自己吧"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                  type="primary"
                  :loading="submitting"
                  @click="updateBasicInfo"
              >
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="修改密码" name="password">
          <el-form
              ref="passwordForm"
              :model="passwordInfo"
              :rules="passwordRules"
              label-width="100px"
          >
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input
                  v-model="passwordInfo.oldPassword"
                  type="password"
                  show-password
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                  v-model="passwordInfo.newPassword"
                  type="password"
                  show-password
              />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                  v-model="passwordInfo.confirmPassword"
                  type="password"
                  show-password
              />
            </el-form-item>
            <el-form-item>
              <el-button
                  type="primary"
                  :loading="submitting"
                  @click="updatePassword"
              >
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="实名认证" name="verification">
          <div class="verification-status">
            <template v-if="userInfo.isVerified">
              <el-result
                  icon="success"
                  title="已完成实名认证"
                  sub-title="您已完成实名认证，享有更多平台权益"
              />
            </template>
            <template v-else>
              <el-result
                  icon="warning"
                  title="未实名认证"
                  sub-title="完成实名认证可参与更多平台活动"
              >
                <template #extra>
                  <el-button type="primary" @click="startVerification">
                    立即认证
                  </el-button>
                </template>
              </el-result>
            </template>
          </div>
        </el-tab-pane>

        <el-tab-pane label="隐私设置" name="privacy">
          <div class="privacy-settings">
            <el-form label-width="200px">
              <el-form-item label="个人资料可见性">
                <el-radio-group v-model="privacySettings.profileVisibility">
                  <el-radio label="public">所有人可见</el-radio>
                  <el-radio label="friends">仅好友可见</el-radio>
                  <el-radio label="private">仅自己可见</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="游戏库可见性">
                <el-radio-group v-model="privacySettings.libraryVisibility">
                  <el-radio label="public">所有人可见</el-radio>
                  <el-radio label="friends">仅好友可见</el-radio>
                  <el-radio label="private">仅自己可见</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="电子邮件通知">
                <el-switch v-model="privacySettings.emailNotification" />
              </el-form-item>
              <el-form-item>
                <el-button
                    type="primary"
                    :loading="submitting"
                    @click="updatePrivacySettings"
                >
                  保存设置
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 实名认证对话框 -->
    <el-dialog
        v-model="verificationDialogVisible"
        title="实名认证"
        width="500px"
    >
      <el-form
          ref="verificationForm"
          :model="verificationInfo"
          :rules="verificationRules"
          label-width="100px"
      >
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="verificationInfo.realName" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idNumber">
          <el-input v-model="verificationInfo.idNumber" />
        </el-form-item>
        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="verificationInfo.phone">
            <template #append>
              <el-button
                  :disabled="!!countdown"
                  @click="sendVerificationCode"
              >
                {{ countdown ? `${countdown}s后重试` : '获取验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="验证码" prop="verificationCode">
          <el-input v-model="verificationInfo.verificationCode" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="verificationDialogVisible = false">取消</el-button>
          <el-button
              type="primary"
              :loading="submitting"
              @click="submitVerification"
          >
            提交认证
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'
import { format } from 'date-fns'
import {
  updateUserInfo,
  updatePassword,
  updatePrivacy,
  submitVerification,
  sendVerificationCode
} from '@/api/user'

export default {
  name: 'UserProfile',
  components: { Camera },

  setup() {
    const store = useStore()
    const activeTab = ref('basic')
    const submitting = ref(false)
    const verificationDialogVisible = ref(false)
    const countdown = ref(0)

    const userInfo = computed(() => store.state.user.userInfo)

    // 基本信息表单
    const basicForm = ref(null)
    const basicInfo = reactive({
      username: userInfo.value.username,
      nickname: userInfo.value.nickname,
      email: userInfo.value.email,
      bio: userInfo.value.bio
    })

// ... 接上文
    const basicRules = {
      nickname: [
        { required: true, message: '请输入昵称', trigger: 'blur' },
        { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
      ],
      bio: [
        { max: 200, message: '简介不能超过200个字符', trigger: 'blur' }
      ]
    }

    // 密码修改表单
    const passwordForm = ref(null)
    const passwordInfo = reactive({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })

    const validatePass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'))
      } else if (value !== passwordInfo.newPassword) {
        callback(new Error('两次输入密码不一致!'))
      } else {
        callback()
      }
    }

    const passwordRules = {
      oldPassword: [
        { required: true, message: '请输入当前密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
      ],
      newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, validator: validatePass, trigger: 'blur' }
      ]
    }

    // 实名认证表单
    const verificationForm = ref(null)
    const verificationInfo = reactive({
      realName: '',
      idNumber: '',
      phone: '',
      verificationCode: ''
    })

    const validateIdNumber = (rule, value, callback) => {
      const reg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/
      if (!reg.test(value)) {
        callback(new Error('请输入正确的身份证号码'))
      } else {
        callback()
      }
    }

    const verificationRules = {
      realName: [
        { required: true, message: '请输入真实姓名', trigger: 'blur' }
      ],
      idNumber: [
        { required: true, validator: validateIdNumber, trigger: 'blur' }
      ],
      phone: [
        { required: true, message: '请输入手机号码', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码' }
      ],
      verificationCode: [
        { required: true, message: '请输入验证码', trigger: 'blur' },
        { len: 6, message: '验证码长度应为6位', trigger: 'blur' }
      ]
    }

    // 隐私设置
    const privacySettings = reactive({
      profileVisibility: 'public',
      libraryVisibility: 'public',
      emailNotification: true
    })

    // 更新基本信息
    const updateBasicInfo = async () => {
      if (!basicForm.value) return

      await basicForm.value.validate(async (valid) => {
        if (valid) {
          submitting.value = true
          try {
            await updateUserInfo(basicInfo)
            await store.dispatch('user/updateUserInfo')
            ElMessage.success('个人信息更新成功')
          } catch (error) {
            ElMessage.error('更新失败')
          } finally {
            submitting.value = false
          }
        }
      })
    }

    // 更新密码
    const updatePassword = async () => {
      if (!passwordForm.value) return

      await passwordForm.value.validate(async (valid) => {
        if (valid) {
          submitting.value = true
          try {
            await updatePassword(passwordInfo)
            ElMessage.success('密码修改成功')
            passwordForm.value.resetFields()
          } catch (error) {
            ElMessage.error('密码修改失败')
          } finally {
            submitting.value = false
          }
        }
      })
    }

    // 头像上传
    const uploadRef = ref(null)
    const uploadUrl = process.env.VUE_APP_BASE_API + '/api/user/avatar'
    const uploadHeaders = computed(() => ({
      Authorization: 'Bearer ' + store.state.user.token
    }))

    const beforeAvatarUpload = (file) => {
      const isJPG = file.type === 'image/jpeg'
      const isPNG = file.type === 'image/png'
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isJPG && !isPNG) {
        ElMessage.error('头像只能是 JPG 或 PNG 格式!')
        return false
      }
      if (!isLt2M) {
        ElMessage.error('头像大小不能超过 2MB!')
        return false
      }
      return true
    }

    const handleAvatarSuccess = async (response) => {
      await store.dispatch('user/updateUserInfo')
      ElMessage.success('头像更新成功')
    }

    const handleAvatarError = () => {
      ElMessage.error('头像上传失败')
    }

    // 实名认证相关
    const startVerification = () => {
      verificationDialogVisible.value = true
    }

    const sendVerificationCode = async () => {
      if (!verificationInfo.phone) {
        ElMessage.warning('请先输入手机号码')
        return
      }

      try {
        await sendVerificationCode(verificationInfo.phone)
        ElMessage.success('验证码已发送')
        countdown.value = 60
        const timer = setInterval(() => {
          countdown.value--
          if (countdown.value <= 0) {
            clearInterval(timer)
          }
        }, 1000)
      } catch (error) {
        ElMessage.error('验证码发送失败')
      }
    }

    const submitVerification = async () => {
      if (!verificationForm.value) return

      await verificationForm.value.validate(async (valid) => {
        if (valid) {
          submitting.value = true
          try {
            await submitVerification(verificationInfo)
            ElMessage.success('实名认证提交成功，请等待审核')
            verificationDialogVisible.value = false
            await store.dispatch('user/updateUserInfo')
          } catch (error) {
            ElMessage.error('实名认证提交失败')
          } finally {
            submitting.value = false
          }
        }
      })
    }

    // 更新隐私设置
    const updatePrivacySettings = async () => {
      submitting.value = true
      try {
        await updatePrivacy(privacySettings)
        ElMessage.success('隐私设置更新成功')
      } catch (error) {
        ElMessage.error('隐私设置更新失败')
      } finally {
        submitting.value = false
      }
    }

    const formatDateTime = (time) => {
      return time ? format(new Date(time), 'yyyy-MM-dd HH:mm:ss') : '-'
    }

    // 初始化数据
    onMounted(async () => {
      // 获取隐私设置
      try {
        const response = await getPrivacySettings()
        Object.assign(privacySettings, response.data)
      } catch (error) {
        console.error('获取隐私设置失败:', error)
      }
    })

    return {
      activeTab,
      userInfo,
      basicForm,
      basicInfo,
      basicRules,
      passwordForm,
      passwordInfo,
      passwordRules,
      verificationForm,
      verificationInfo,
      verificationRules,
      privacySettings,
      verificationDialogVisible,
      submitting,
      countdown,
      uploadRef,
      uploadUrl,
      uploadHeaders,
      updateBasicInfo,
      updatePassword,
      beforeAvatarUpload,
      handleAvatarSuccess,
      handleAvatarError,
      startVerification,
      sendVerificationCode,
      submitVerification,
      updatePrivacySettings,
      formatDateTime
    }
  }
}
</script>

<style scoped>
.profile-container {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.profile-card {
  margin-bottom: 20px;
}

.profile-header {
  display: flex;
  align-items: center;
  margin-bottom: 40px;
}

.avatar-container {
  position: relative;
  margin-right: 40px;
}

.avatar-upload {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-container:hover .avatar-upload {
  opacity: 1;
}

.upload-trigger {
  display: flex;
  align-items: center;
  gap: 5px;
  background-color: rgba(0, 0, 0, 0.6);
  color: #fff;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.user-info h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
}

.user-info p {
  margin: 5px 0;
  color: #666;
}

.verification-status {
  padding: 40px 0;
}

:deep(.el-upload) {
  width: 100%;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}
</style>