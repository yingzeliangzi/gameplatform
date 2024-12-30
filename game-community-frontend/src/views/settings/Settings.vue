<template>
  <div class="settings-container">
    <el-row :gutter="20">
      <!-- 左侧导航菜单 -->
      <el-col :span="6">
        <el-card class="menu-card">
          <el-menu
              :default-active="activeMenu"
              @select="handleMenuSelect"
          >
            <el-menu-item index="profile">
              <el-icon>
                <User/>
              </el-icon>
              <span>个人资料</span>
            </el-menu-item>
            <el-menu-item index="security">
              <el-icon>
                <Lock/>
              </el-icon>
              <span>安全设置</span>
            </el-menu-item>
            <el-menu-item index="privacy">
              <el-icon>
                <Hide/>
              </el-icon>
              <span>隐私设置</span>
            </el-menu-item>
            <el-menu-item index="notification">
              <el-icon>
                <Bell/>
              </el-icon>
              <span>通知设置</span>
            </el-menu-item>
            <el-menu-item index="binding">
              <el-icon>
                <Connection/>
              </el-icon>
              <span>账号绑定</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>

      <!-- 右侧设置内容 -->
      <el-col :span="18">
        <el-card v-loading="loading">
          <!-- 个人资料设置 -->
          <div v-show="activeMenu === 'profile'" class="settings-section">
            <h2>个人资料</h2>
            <el-form
                ref="profileForm"
                :model="profileData"
                :rules="profileRules"
                label-width="100px"
            >
              <el-form-item label="用户名">
                <el-input v-model="profileData.username" disabled/>
              </el-form-item>
              <el-form-item label="昵称" prop="nickname">
                <el-input v-model="profileData.nickname"/>
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="profileData.email"/>
              </el-form-item>
              <el-form-item label="个人简介" prop="bio">
                <el-input
                    v-model="profileData.bio"
                    type="textarea"
                    :rows="4"
                    placeholder="介绍一下自己吧"
                />
              </el-form-item>
              <el-form-item label="所在地" prop="location">
                <el-cascader
                    v-model="profileData.location"
                    :options="locationOptions"
                    placeholder="请选择所在地"
                />
              </el-form-item>
              <el-form-item>
                <el-button
                    type="primary"
                    :loading="submitting"
                    @click="updateProfile"
                >
                  保存修改
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 安全设置 -->
          <div v-show="activeMenu === 'security'" class="settings-section">
            <h2>安全设置</h2>
            <div class="security-items">
              <div class="security-item">
                <div class="item-info">
                  <div class="item-title">登录密码</div>
                  <div class="item-desc">定期更改密码可以提升账号安全性</div>
                </div>
                <el-button @click="showChangePassword">修改</el-button>
              </div>

              <div class="security-item">
                <div class="item-info">
                  <div class="item-title">手机验证</div>
                  <div class="item-desc">
                    {{ securityData.phone ? `已绑定手机: ${securityData.phone}` : '未绑定手机号' }}
                  </div>
                </div>
                <el-button @click="showBindPhone">
                  {{ securityData.phone ? '更换' : '绑定' }}
                </el-button>
              </div>

              <div class="security-item">
                <div class="item-info">
                  <div class="item-title">两步验证</div>
                  <div class="item-desc">开启后登录需要进行二次验证</div>
                </div>
                <el-switch
                    v-model="securityData.twoFactorEnabled"
                    @change="handleTwoFactorChange"
                />
              </div>

              <div class="security-item">
                <div class="item-info">
                  <div class="item-title">登录记录</div>
                  <div class="item-desc">查看登录设备和历史记录</div>
                </div>
                <el-button @click="showLoginHistory">查看</el-button>
              </div>
            </div>
          </div>

          <!-- 隐私设置 -->
          <div v-show="activeMenu === 'privacy'" class="settings-section">
            <h2>隐私设置</h2>
            <el-form label-width="160px">
              <el-form-item label="个人资料可见性">
                <el-radio-group v-model="privacyData.profileVisibility">
                  <el-radio label="public">所有人可见</el-radio>
                  <el-radio label="friends">仅好友可见</el-radio>
                  <el-radio label="private">仅自己可见</el-radio>
                </el-radio-group>
              </el-form-item>

              <el-form-item label="游戏库可见性">
                <el-radio-group v-model="privacyData.libraryVisibility">
                  <el-radio label="public">所有人可见</el-radio>
                  <el-radio label="friends">仅好友可见</el-radio>
                  <el-radio label="private">仅自己可见</el-radio>
                </el-radio-group>
              </el-form-item>

              <el-form-item label="好友请求">
                <el-radio-group v-model="privacyData.friendRequest">
                  <el-radio label="all">允许所有人</el-radio>
                  <el-radio label="mutual">仅互关用户</el-radio>
                  <el-radio label="none">不允许</el-radio>
                </el-radio-group>
              </el-form-item>

              <el-form-item label="搜索引擎收录">
                <el-switch v-model="privacyData.searchable"/>
              </el-form-item>

              <el-form-item label="在线状态">
                <el-switch v-model="privacyData.showOnlineStatus"/>
              </el-form-item>

              <el-form-item>
                <el-button
                    type="primary"
                    :loading="submitting"
                    @click="updatePrivacy"
                >
                  保存设置
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 通知设置 -->
          <div v-show="activeMenu === 'notification'" class="settings-section">
            <h2>通知设置</h2>
            <el-form label-width="160px">
              <el-divider content-position="left">站内通知</el-divider>
              <el-form-item label="系统通知">
                <el-switch v-model="notificationData.system"/>
              </el-form-item>
              <el-form-item label="活动提醒">
                <el-switch v-model="notificationData.event"/>
              </el-form-item>
              <el-form-item label="游戏更新">
                <el-switch v-model="notificationData.game"/>
              </el-form-item>
              <el-form-item label="社区互动">
                <el-switch v-model="notificationData.social"/>
              </el-form-item>

              <el-divider content-position="left">邮件通知</el-divider>
              <el-form-item label="重要消息">
                <el-switch v-model="notificationData.emailImportant"/>
              </el-form-item>
              <el-form-item label="活动报名">
                <el-switch v-model="notificationData.emailEvent"/>
              </el-form-item>
              <el-form-item label="促销信息">
                <el-switch v-model="notificationData.emailPromotion"/>
              </el-form-item>

              <el-divider content-position="left">消息提醒方式</el-divider>
              <el-form-item label="提醒类型">
                <el-checkbox-group v-model="notificationData.alertTypes">
                  <el-checkbox label="popup">弹窗提醒</el-checkbox>
                  <el-checkbox label="sound">声音提醒</el-checkbox>
                  <el-checkbox label="desktop">桌面通知</el-checkbox>
                </el-checkbox-group>
              </el-form-item>

              <el-form-item>
                <el-button
                    type="primary"
                    :loading="submitting"
                    @click="updateNotification"
                >
                  保存设置
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 账号绑定 -->
          <div v-show="activeMenu === 'binding'" class="settings-section">
            <h2>账号绑定</h2>
            <div class="binding-list">
              <div class="binding-item">
                <div class="binding-info">
                  <el-icon>
                    <Iphone/>
                  </el-icon>
                  <div>
                    <div class="binding-name">手机绑定</div>
                    <div class="binding-desc">
                      {{ bindingData.phone || '未绑定手机号' }}
                    </div>
                  </div>
                </div>
                <el-button
                    :type="bindingData.phone ? 'default' : 'primary'"
                    @click="handleBinding('phone')"
                >
                  {{ bindingData.phone ? '更换' : '绑定' }}
                </el-button>
              </div>

              <div class="binding-item">
                <div class="binding-info">
                  <el-icon>
                    <Message/>
                  </el-icon>
                  <div>
                    <div class="binding-name">邮箱绑定</div>
                    <div class="binding-desc">
                      {{ bindingData.email || '未绑定邮箱' }}
                    </div>
                  </div>
                </div>
                <el-button
                    :type="bindingData.email ? 'default' : 'primary'"
                    @click="handleBinding('email')"
                >
                  {{ bindingData.email ? '更换' : '绑定' }}
                </el-button>
              </div>

              <div
                  v-for="platform in socialPlatforms"
                  :key="platform.id"
                  class="binding-item"
              >
                <div class="binding-info">
                  <el-icon>
                    <component :is="platform.icon"/>
                  </el-icon>
                  <div>
                    <div class="binding-name">{{ platform.name }}账号</div>
                    <div class="binding-desc">
                      {{ bindingData[platform.id] ? '已绑定' : '未绑定' }}
                    </div>
                  </div>
                </div>
                <el-button
                    :type="bindingData[platform.id] ? 'default' : 'primary'"
                    @click="handleBinding(platform.id)"
                >
                  {{ bindingData[platform.id] ? '解绑' : '绑定' }}
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 修改密码对话框 -->
    <el-dialog
        v-model="passwordDialogVisible"
        title="修改密码"
        width="500px"
    >
      <el-form
          ref="passwordForm"
          :model="passwordData"
          :rules="passwordRules"
          label-width="100px"
      >
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input
              v-model="passwordData.oldPassword"
              type="password"
              show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
              v-model="passwordData.newPassword"
              type="password"
              show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
              v-model="passwordData.confirmPassword"
              type="password"
              show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="updatePassword">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 绑定手机对话框 -->
    <el-dialog
        v-model="phoneDialogVisible"
        :title="bindingData.phone ? '更换手机' : '绑定手机'"
        width="500px"
    >
      <el-form
          ref="phoneForm"
          :model="phoneData"
          :rules="phoneRules"
          label-width="100px"
      >
        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="phoneData.phone">
            <template #append>
              <el-button
                  :disabled="!!countdown"
                  @click="handleSendVerificationCode"
              >
                {{ countdown ? `${countdown}s后重试` : '获取验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <el-input v-model="phoneData.code" maxlength="6"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="phoneDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitBindPhone">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 登录记录对话框 -->
    <el-dialog
        v-model="loginHistoryVisible"
        title="登录记录"
        width="680px"
    >
      <el-tabs v-model="loginHistoryTab">
        <el-tab-pane label="登录设备" name="devices">
          <el-table :data="loginDevices" stripe style="width: 100%">
            <el-table-column prop="deviceName" label="设备名称"/>
            <el-table-column prop="loginTime" label="登录时间">
              <template #default="{ row }">
                {{ formatDateTime(row.loginTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="location" label="登录地点"/>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button
                    type="danger"
                    link
                    @click="removeDevice(row)"
                >
                  移除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="登录历史" name="history">
          <el-table :data="loginHistory" stripe style="width: 100%">
            <el-table-column prop="loginTime" label="登录时间">
              <template #default="{ row }">
                {{ formatDateTime(row.loginTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="deviceName" label="设备"/>
            <el-table-column prop="location" label="登录地点"/>
            <el-table-column prop="ip" label="IP地址"/>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, reactive, onMounted} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  User,
  Lock,
  Hide,
  Bell,
  Connection,
  Iphone,
  Message
} from '@element-plus/icons-vue'
import {format} from 'date-fns'
import {
  getPrivacySettings,
  updatePrivacySettings,
  getSecuritySettings,
  updateSecuritySettings,
  updatePassword as updateUserPassword,
  getNotificationSettings,
  updateNotificationSettings,
  getBindingInfo,
  bindPhone,
  unbindAccount,
  getLoginHistory,
  getDevices,
  removeDevice as removeDeviceAPI
} from '@/api/settings'
import { sendVerificationCode as sendSmsCode } from '@/api/user'
import {validatePassword} from '@/utils/validate'

const loading = ref(false)
const submitting = ref(false)
const activeMenu = ref('profile')

// 各种对话框的显示控制
const passwordDialogVisible = ref(false)
const phoneDialogVisible = ref(false)
const loginHistoryVisible = ref(false)
const loginHistoryTab = ref('devices')
const countdown = ref(0)

// 个人资料表单
const profileData = reactive({
  nickname: '',
  email: '',
  bio: '',
  location: []
})

const profileRules = {
  nickname: [
    {required: true, message: '请输入昵称', trigger: 'blur'},
    {min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur'}
  ],
  email: [
    {required: true, message: '请输入邮箱地址', trigger: 'blur'},
    {type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur'}
  ]
}

// 安全设置数据
const securityData = reactive({
  phone: '',
  twoFactorEnabled: false
})

// 隐私设置数据
const privacyData = reactive({
  profileVisibility: 'public',
  libraryVisibility: 'public',
  friendRequest: 'all',
  searchable: true,
  showOnlineStatus: true
})

// 通知设置数据
const notificationData = reactive({
  system: true,
  event: true,
  game: true,
  social: true,
  emailImportant: true,
  emailEvent: true,
  emailPromotion: false,
  alertTypes: ['popup', 'sound']
})

// 账号绑定数据
const bindingData = reactive({
  phone: '',
  email: '',
  wechat: false,
  qq: false,
  steam: false
})

// 社交平台配置
const socialPlatforms = [
  {id: 'wechat', name: '微信', icon: 'ChatDotRound'},
  {id: 'qq', name: 'QQ', icon: 'ChatDotSquare'},
  {id: 'steam', name: 'Steam', icon: 'Monitor'}
]

// 登录记录数据
const loginDevices = ref([])
const loginHistory = ref([])

// 密码修改表单
const passwordData = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordData.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    {required: true, message: '请输入当前密码', trigger: 'blur'}
  ],
  newPassword: [
    {required: true, message: '请输入新密码', trigger: 'blur'},
    {
      validator: (rule, value, callback) => {
        const result = validatePassword(value)
        if (!result.valid) {
          callback(new Error(result.message.join(',')))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 手机绑定表单
const phoneData = reactive({
  phone: '',
  code: ''
})

const phoneRules = {
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度应为6位', trigger: 'blur' }
  ]
}

// 初始化数据
const initData = async () => {
  loading.value = true
  try {
    const [privacyRes, securityRes, notificationRes, bindingRes] = await Promise.all([
      getPrivacySettings(),
      getSecuritySettings(),
      getNotificationSettings(),
      getBindingInfo()
    ])

    Object.assign(privacyData, privacyRes.data)
    Object.assign(securityData, securityRes.data)
    Object.assign(notificationData, notificationRes.data)
    Object.assign(bindingData, bindingRes.data)
  } catch (error) {
    ElMessage.error('获取设置信息失败')
  } finally {
    loading.value = false
  }
}

// 更新个人资料
const updateProfile = async () => {
  submitting.value = true
  try {
    await updateUserInfo(profileData)
    ElMessage.success('个人资料更新成功')
  } catch (error) {
    ElMessage.error('更新失败')
  } finally {
    submitting.value = false
  }
}

// 更新隐私设置
const updatePrivacy = async () => {
  submitting.value = true
  try {
    await updatePrivacySettings(privacyData)
    ElMessage.success('隐私设置更新成功')
  } catch (error) {
    ElMessage.error('更新失败')
  } finally {
    submitting.value = false
  }
}

// 更新通知设置
const updateNotification = async () => {
  submitting.value = true
  try {
    await updateNotificationSettings(notificationData)
    ElMessage.success('通知设置更新成功')
  } catch (error) {
    ElMessage.error('更新失败')
  } finally {
    submitting.value = false
  }
}

// 修改密码
const showChangePassword = () => {
  Object.assign(passwordData, {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  })
  passwordDialogVisible.value = true
}

const updatePassword = async () => {
  submitting.value = true
  try {
    await updateUserPassword(passwordData)
    passwordDialogVisible.value = false
    ElMessage.success('密码修改成功')
  } catch (error) {
    ElMessage.error('密码修改失败')
  } finally {
    submitting.value = false
  }
}

// 发送验证码
const handleSendVerificationCode = async () => {
  if (!phoneData.phone) {
    ElMessage.warning('请先输入手机号码')
    return
  }

  try {
    await sendSmsCode(phoneData.phone)
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

// 账号绑定处理
const handleBinding = async (type) => {
  if (bindingData[type]) {
    // 解绑
    try {
      await ElMessageBox.confirm(
          `确定要解除${getBindingName(type)}绑定吗？`,
          '提示',
          { type: 'warning' }
      )
      await unbindAccount(type)
      bindingData[type] = false
      ElMessage.success('解绑成功')
    } catch {}
  } else {
    // 绑定
    if (type === 'phone') {
      phoneDialogVisible.value = true
    } else {
      // 其他平台的绑定处理
      window.location.href = `${process.env.VUE_APP_BASE_API}/api/auth/${type}`
    }
  }
}

// 提交手机绑定
const submitBindPhone = async () => {
  submitting.value = true
  try {
    await bindPhone(phoneData)
    phoneDialogVisible.value = false
    bindingData.phone = phoneData.phone
    ElMessage.success('手机绑定成功')
  } catch (error) {
    ElMessage.error('手机绑定失败')
  } finally {
    submitting.value = false
  }
}

// 查看登录记录
const showLoginHistory = async () => {
  loginHistoryVisible.value = true
  try {
    const [devicesRes, historyRes] = await Promise.all([
      getDevices(),
      getLoginHistory()
    ])
    loginDevices.value = devicesRes.data
    loginHistory.value = historyRes.data
  } catch (error) {
    ElMessage.error('获取登录记录失败')
  }
}

// 移除设备
const removeDevice = async (device) => {
  try {
    await ElMessageBox.confirm(
        '确定要移除该设备吗？该设备将需要重新登录',
        '提示',
        { type: 'warning' }
    )
    await removeDeviceAPI(device.id)
    const index = loginDevices.value.findIndex(d => d.id === device.id)
    if (index > -1) {
      loginDevices.value.splice(index, 1)
    }
    ElMessage.success('设备已移除')
  } catch {}
}

// 格式化日期时间
const formatDateTime = (time) => {
  return format(new Date(time), 'yyyy-MM-dd HH:mm:ss')
}

// 获取绑定平台名称
const getBindingName = (type) => {
  const platform = socialPlatforms.find(p => p.id === type)
  if (platform) return platform.name
  const bindingMap = {
    phone: '手机',
    email: '邮箱'
  }
  return bindingMap[type] || type
}

// 菜单切换处理
const handleMenuSelect = (index) => {
  activeMenu.value = index
}

onMounted(() => {
  initData()
})
</script>

<style lang="scss" scoped>
.settings-container {
  padding: 20px;

  .menu-card {
    margin-bottom: 20px;

    :deep(.el-menu) {
      border-right: none;
    }

    :deep(.el-menu-item) {
      display: flex;
      align-items: center;
      gap: 10px;
    }
  }

  .settings-section {
    padding: 20px;

    h2 {
      margin: 0 0 20px 0;
      font-size: 18px;
      font-weight: 500;
    }
  }

  .security-items {
    .security-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px;
      border-bottom: 1px solid #ebeef5;

      &:last-child {
        border-bottom: none;
      }

      .item-info {
        .item-title {
          font-weight: 500;
          margin-bottom: 5px;
        }

        .item-desc {
          color: #909399;
          font-size: 14px;
        }
      }
    }
  }

  .binding-list {
    .binding-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px;
      background-color: #f5f7fa;
      border-radius: 4px;
      margin-bottom: 15px;

      .binding-info {
        display: flex;
        align-items: center;
        gap: 15px;

        .binding-name {
          font-weight: 500;
          margin-bottom: 5px;
        }

        .binding-desc {
          color: #909399;
          font-size: 14px;
        }
      }
    }
  }

  .el-divider {
    margin: 24px 0;
  }

  :deep(.el-form-item__label) {
    font-weight: 500;
  }
}
</style>