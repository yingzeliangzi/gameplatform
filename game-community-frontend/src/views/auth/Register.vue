<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <h2 class="register-title">游戏社区注册</h2>
      </template>
      <el-form
          ref="registerForm"
          :model="registerForm"
          :rules="registerRules"
          label-width="80px"
          class="register-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名"
              prefix-icon="User"
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input
              v-model="registerForm.email"
              placeholder="请输入邮箱"
              prefix-icon="Message"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请确认密码"
              prefix-icon="Lock"
              show-password
          />
        </el-form-item>

        <el-form-item label="昵称" prop="nickname">
          <el-input
              v-model="registerForm.nickname"
              placeholder="请输入昵称"
              prefix-icon="User"
          />
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              :loading="loading"
              class="register-button"
              @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>

        <div class="register-options">
          已有账号？
          <el-link type="primary" @click="$router.push('/login')">
            立即登录
          </el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { defineComponent, reactive, ref } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

export default defineComponent({
  name: 'Register',
  setup() {
    const store = useStore()
    const router = useRouter()
    const registerForm = reactive({
      username: '',
      email: '',
      password: '',
      confirmPassword: '',
      nickname: ''
    })

    // 验证密码是否一致的方法
    const validatePass2 = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'))
      } else if (value !== registerForm.password) {
        callback(new Error('两次输入密码不一致!'))
      } else {
        callback()
      }
    }

    const registerRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱地址', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请再次输入密码', trigger: 'blur' },
        { validator: validatePass2, trigger: 'blur' }
      ],
      nickname: [
        { required: true, message: '请输入昵称', trigger: 'blur' },
        { min: 2, max: 10, message: '长度在 2 到 10 个字符', trigger: 'blur' }
      ]
    }

    const loading = ref(false)

    const handleRegister = () => {
      registerForm.value?.validate((valid) => {
        if (valid) {
          loading.value = true
          const registrationData = {
            username: registerForm.username,
            email: registerForm.email,
            password: registerForm.password,
            nickname: registerForm.nickname
          }

          store.dispatch('auth/register', registrationData)
              .then(() => {
                ElMessage.success('注册成功，请登录')
                router.push('/login')
              })
              .catch((error) => {
                ElMessage.error(error.message || '注册失败，请重试')
              })
              .finally(() => {
                loading.value = false
              })
        }
      })
    }

    return {
      registerForm,
      registerRules,
      loading,
      handleRegister
    }
  }
})
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.register-card {
  width: 400px;
}

.register-title {
  text-align: center;
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.register-form {
  margin-top: 20px;
}

.register-button {
  width: 100%;
}

.register-options {
  text-align: center;
  margin-top: 15px;
}
</style>