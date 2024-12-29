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
            <el-menu-item index="personalization">
              <el-icon><Brush /></el-icon>
              <span>个性化设置</span>
            </el-menu-item>
            <el-menu-item index="notification">
              <el-icon><Bell /></el-icon>
              <span>通知设置</span>
            </el-menu-item>
            <el-menu-item index="security">
              <el-icon><Lock /></el-icon>
              <span>安全设置</span>
            </el-menu-item>
            <el-menu-item index="binding">
              <el-icon><Link /></el-icon>
              <span>账号绑定</span>
            </el-menu-item>
            <el-menu-item index="language">
              <el-icon><Language /></el-icon>
              <span>语言与地区</span>
            </el-menu-item>
            <el-menu-item index="blacklist">
              <el-icon><CircleClose /></el-icon>
              <span>黑名单管理</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>

      <!-- 右侧设置内容 -->
      <el-col :span="18">
        <el-card v-loading="loading">
          <!-- 个性化设置 -->
          <div v-show="activeMenu === 'personalization'" class="settings-section">
            <h2>个性化设置</h2>
            <el-form label-width="120px">
              <el-form-item label="主题模式">
                <el-radio-group v-model="settings.theme">
                  <el-radio-button label="light">浅色</el-radio-button>
                  <el-radio-button label="dark">深色</el-radio-button>
                  <el-radio-button label="auto">跟随系统</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="主题色">
                <el-color-picker v-model="settings.primaryColor" />
              </el-form-item>
              <el-form-item label="字体大小">
                <el-slider
                    v-model="settings.fontSize"
                    :min="12"
                    :max="20"
                    :step="1"
                    show-input
                />
              </el-form-item>
              <el-form-item label="界面动画">
                <el-switch v-model="settings.enableAnimation" />
              </el-form-item>
            </el-form>
          </div>

          <!-- 通知设置 -->
          <div v-show="activeMenu === 'notification'" class="settings-section">
            <h2>通知设置</h2>
            <el-form label-width="120px">
              <el-divider content-position="left">站内通知</el-divider>
              <el-form-item label="系统通知">
                <el-switch v-model="settings.notifications.system" />
              </el-form-item>
              <el-form-item label="活动提醒">
                <el-switch v-model="settings.notifications.event" />
              </el-form-item>
              <el-form-item label="游戏更新">
                <el-switch v-model="settings.notifications.game" />
              </el-form-item>
              <el-form-item label="社区互动">
                <el-switch v-model="settings.notifications.social" />
              </el-form-item>

              <el-divider content-position="left">邮件通知</el-divider>
              <el-form-item label="重要消息">
                <el-switch v-model="settings.emailNotifications.important" />
              </el-form-item>
              <el-form-item label="活动报名">
                <el-switch v-model="settings.emailNotifications.event" />
              </el-form-item>
              <el-form-item label="促销信息">
                <el-switch v-model="settings.emailNotifications.promotion" />
              </el-form-item>
            </el-form>
          </div>

          <!-- 安全设置 -->
          <div v-show="activeMenu === 'security'" class="settings-section">
            <h2>安全设置</h2>
            <el-form label-width="120px">
              <el-form-item label="登录验证">
                <el-switch
                    v-model="settings.security.twoFactor"
                    @change="handleTwoFactorChange"
                />
                <span class="setting-tip">开启后需要短信验证码才能登录</span>
              </el-form-item>
              <el-form-item label="登录记录">
                <el-button @click="showLoginHistory">查看登录记录</el-button>
              </el-form-item>
              <el-form-item label="登录设备">
                <el-button @click="showDevices">管理登录设备</el-button>
              </el-form-item>
              <el-form-item label="密码更新">
                <el-button type="primary" @click="showChangePassword">
                  修改密码
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
                  <el-icon><Mobile /></el-icon>
                  <div class="binding-detail">
                    <div class="binding-name">手机绑定</div>
                    <div class="binding-status">
                      {{ settings.binding.phone || '未绑定' }}
                    </div>
                  </div>
                </div>
                <el-button
                    :type="settings.binding.phone ? 'info' : 'primary'"
                    @click="handleBinding('phone')"
                >
                  {{ settings.binding.phone ? '更换' : '绑定' }}
                </el-button>
              </div>

              <div class="binding-item">
                <div class="binding-info">
                  <el-icon><Message /></el-icon>
                  <div class="binding-detail">
                    <div class="binding-name">邮箱绑定</div>
                    <div class="binding-status">
                      {{ settings.binding.email || '未绑定' }}
                    </div>
                  </div>
                </div>
                <el-button
                    :type="settings.binding.email ? 'info' : 'primary'"
                    @click="handleBinding('email')"
                >
                  {{ settings.binding.email ? '更换' : '绑定' }}
                </el-button>
              </div>

              <div
                  v-for="platform in socialPlatforms"
                  :key="platform.id"
                  class="binding-item"
              >
                <div class="binding-info">
                  <el-icon><component :is="platform.icon" /></el-icon>
                  <div class="binding-detail">
                    <div class="binding-name">{{ platform.name }}</div>
                    <div class="binding-status">
                      {{ settings.binding[platform.id] ? '已绑定' : '未绑定' }}
                    </div>
                  </div>
                </div>
                <el-button
                    :type="settings.binding[platform.id] ? 'info' : 'primary'"
                    @click="handleBinding(platform.id)"
                >
                  {{ settings.binding[platform.id] ? '解绑' : '绑定' }}
                </el-button>
              </div>
            </div>
          </div>

          <!-- 语言与地区 -->
          <div v-show="activeMenu === 'language'" class="settings-section">
            <h2>语言与地区</h2>
            <el-form label-width="120px">
              <el-form-item label="界面语言">
                <el-select v-model="settings.language">
                  <el-option label="简体中文" value="zh-CN" />
                  <el-option label="English" value="en-US" />
                  <el-option label="日本語" value="ja-JP" />
                </el-select>
              </el-form-item>
              <el-form-item label="时区">
                <el-select v-model="settings.timezone">
                  <el-option label="(GMT+08:00) 北京" value="Asia/Shanghai" />
                  <el-option label="(GMT+00:00) 伦敦" value="Europe/London" />
                  <el-option label="(GMT-08:00) 洛杉矶" value="America/Los_Angeles" />
                </el-select>
              </el-form-item>
              <el-form-item label="日期格式">
                <el-select v-model="settings.dateFormat">
                  <el-option label="YYYY-MM-DD" value="YYYY-MM-DD" />
                  <el-option label="DD/MM/YYYY" value="DD/MM/YYYY" />
                  <el-option label="MM/DD/YYYY" value="MM/DD/YYYY" />
                </el-select>
              </el-form-item>
            </el-form>
          </div>

          <!-- 黑名单管理 -->
          <div v-show="activeMenu === 'blacklist'" class="settings-section">
            <h2>黑名单管理</h2>
            <div class="blacklist-header">
              <el-input
                  v-model="blacklistKeyword"
                  placeholder="搜索用户"
                  prefix-icon="Search"
                  clearable
              />
            </div>
            <el-table
                :data="filteredBlacklist"
                style="width: 100%"
            >
              <el-table-column label="用户">
                <template #default="{ row }">
                  <div class="user-info">
                    <el-avatar :size="32" :src="row.avatar" />
                    <span class="username">{{ row.username }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column
                  prop="addTime"
                  label="加入时间"
                  width="180"
              />
              <el-table-column
                  prop="reason"
                  label="拉黑原因"
                  show-overflow-tooltip
              />
              <el-table-column
                  fixed="right"
                  label="操作"
                  width="120"
              >
                <template #default="{ row }">
                  <el-button
                      type="primary"
                      link
                      @click="removeFromBlacklist(row)"
                  >
                    移出黑名单
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 底部保存按钮 -->
          <div class="settings-footer">
            <el-button
                type="primary"
                :loading="saving"
                @click="saveSettings"
            >
              保存设置
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 对话框组件... -->
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Brush,
  Bell,
  Lock,
  Link,
  Language,
  CircleClose,
  Mobile,
  Message
} from '@element-plus/icons-vue'
import { getSettings, updateSettings, bindAccount, unbindAccount } from '@/api/settings'
import { formatDateTime } from '@/utils/format'

export default {
  name: 'Settings',
  components: {
    Brush,
    Bell,
    Lock,
    Link,
    Language,
    CircleClose,
    Mobile,
    Message
  },

  setup() {
    const loading = ref(false)
    const saving = ref(false)
    const activeMenu = ref('personalization')
    const blacklistKeyword = ref('')

    // 设置数据
    const settings = reactive({
      theme: 'light',
      primaryColor: '#409EFF',
      fontSize: 14,
      enableAnimation: true,
      notifications: {
        system: true,
        event: true,
        game: true,
        social: true
      },
      emailNotifications: {
        important: true,
        event: true,
        promotion: false
      },
      security: {
        twoFactor: false
      },
      binding: {
        phone: '',
        email: '',
        wechat: false,
        qq: false,
        steam: false
      },
      language: 'zh-CN',
      timezone: 'Asia/Shanghai',
      dateFormat: 'YYYY-MM-DD'
    })

    // 社交平台配置
    const socialPlatforms = [
      { id: 'wechat', name: '微信', icon: 'Chat' },
      { id: 'qq', name: 'QQ', icon: 'ChatDotSquare' },
      { id: 'steam', name: 'Steam', icon: 'Monitor' }
    ]

    // 黑名单列表
    const blacklist = ref([])
    const filteredBlacklist = computed(() => {
      const keyword = blacklistKeyword.value.toLowerCase()
      if (!keyword) return blacklist.value
      return blacklist.value.filter(item =>
          item.username.toLowerCase().includes(keyword)
      )
    })

    // 初始化数据
    const initData = async () => {
      loading.value = true
      try {
        const res = await getSettings()
        Object.assign(settings, res.data.settings)
        blacklist.value = res.data.blacklist
      } catch (error) {
        ElMessage.error('获取设置失败')
      } finally {
        loading.value = false
      }
    }

    // 保存设置
    const saveSettings = async () => {
      saving.value = true
      try {
        await updateSettings(settings)
        ElMessage.success('设置保存成功')
      } catch (error) {
        ElMessage.error('设置保存失败')
      } finally {
        saving.value = false
      }
    }

    // 处理二次验证更改
    const handleTwoFactorChange = async (value) => {
      if (value) {
        try {
          await ElMessageBox.confirm(
              '开启二次验证后，登录时需要输入手机验证码，是否继续？',
              '提示'
          )
          // TODO: 实现二次验证开启流程
        } catch {
          settings.security.twoFactor = false
        }
      } else {
        try {
          await ElMessageBox.confirm(
              '关闭二次验证可能会降低账号安全性，是否继续？',
              '警告',
              {
                type: 'warning'
              }
          )
          // TODO: 实现二次验证关闭流程
        } catch {
          settings.security.twoFactor = true
        }
      }
    }

    // 处理账号绑定
    const handleBinding = async (type) => {
      if (settings.binding[type]) {
        // 解绑
        try {
          await ElMessageBox.confirm(
              `确定要解除${getBindingName(type)}绑定吗？`,
              '提示'
          )
          await unbindAccount(type)
          settings.binding[type] = false
          ElMessage.success('解绑成功')
        } catch {
          // 用户取消操作
        }
      } else {
        // 绑定
        try {
          await bindAccount(type)
          settings.binding[type] = true
          ElMessage.success('绑定成功')
        } catch (error) {
          ElMessage.error('绑定失败')
        }
      }
    }

    // 移出黑名单
    const removeFromBlacklist = async (user) => {
      try {
        await ElMessageBox.confirm(
            `确定要将用户 ${user.username} 从黑名单中移除吗？`,
            '提示'
        )
        const index = blacklist.value.findIndex(item => item.id === user.id)
        if (index !== -1) {
          blacklist.value.splice(index, 1)
          ElMessage.success('已移出黑名单')
        }
      } catch {
        // 用户取消操作
      }
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

    // 菜单切换
    const handleMenuSelect = (index) => {
      activeMenu.value = index
    }

    onMounted(() => {
      initData()
    })

    return {
      loading,
      saving,
      activeMenu,
      settings,
      blacklistKeyword,
      blacklist,
      filteredBlacklist,
      socialPlatforms,
      handleMenuSelect,
      handleTwoFactorChange,
      handleBinding,
      removeFromBlacklist,
      saveSettings
    }
  }
}
</script>

<style scoped>
.settings-container {
  padding: 20px;
}

.menu-card {
  margin-bottom: 20px;
}

.menu-card :deep(.el-menu) {
  border-right: none;
}

.menu-card :deep(.el-menu-item) {
  display: flex;
  align-items: center;
  gap: 10px;
}

.settings-section {
  padding: 20px;
}

.settings-section h2 {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 500;
}

.setting-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}

.binding-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.binding-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.binding-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.binding-detail {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.binding-name {
  font-weight: 500;
}

.binding-status {
  font-size: 12px;
  color: #909399;
}

.blacklist-header {
  margin-bottom: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.settings-footer {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  text-align: center;
}

.el-divider {
  margin: 24px 0;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}
</style>