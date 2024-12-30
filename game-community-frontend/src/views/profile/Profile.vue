<template>
  <div class="profile-container">
    <!-- 用户信息卡片 -->
    <el-card class="user-card">
      <div class="user-header">
        <div class="user-avatar">
          <el-avatar :size="120" :src="userInfo.avatar" />
          <div v-if="isOwnProfile" class="avatar-upload" @click="triggerUpload">
            <el-upload
                ref="uploadRef"
                class="upload-trigger"
                :action="uploadUrl"
                :headers="uploadHeaders"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :on-success="handleAvatarSuccess"
                :on-error="handleAvatarError"
            >
              <el-icon><Camera /></el-icon>
              <span>更换头像</span>
            </el-upload>
          </div>
        </div>
        <div class="user-info">
          <div class="user-name">
            <h2>{{ userInfo.nickname }}</h2>
            <div class="user-badges">
              <el-tag
                  v-if="userInfo.isVerified"
                  type="success"
                  effect="dark"
              >
                已认证
              </el-tag>
              <el-tag
                  v-for="role in userInfo.roles"
                  :key="role"
                  :type="getRoleType(role)"
                  effect="dark"
              >
                {{ getRoleLabel(role) }}
              </el-tag>
            </div>
          </div>
          <p class="user-bio">{{ userInfo.bio || '这个人很懒，什么都没有写~' }}</p>
          <div class="user-meta">
            <span>
              <el-icon><Calendar /></el-icon>
              注册于 {{ formatDate(userInfo.createdAt) }}
            </span>
            <span>
              <el-icon><Location /></el-icon>
              {{ userInfo.location || '未设置' }}
            </span>
          </div>
          <div class="user-stats">
            <div class="stat-item">
              <div class="stat-value">{{ stats.gameCount }}</div>
              <div class="stat-label">游戏</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ stats.postCount }}</div>
              <div class="stat-label">帖子</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ stats.followingCount }}</div>
              <div class="stat-label">关注</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ stats.followerCount }}</div>
              <div class="stat-label">粉丝</div>
            </div>
          </div>
          <!-- 操作按钮 -->
          <div class="user-actions" v-if="!isOwnProfile">
            <el-button
                :type="isFollowing ? 'default' : 'primary'"
                @click="handleFollow"
            >
              {{ isFollowing ? '已关注' : '关注' }}
            </el-button>
            <el-button @click="handleMessage">发送消息</el-button>
            <el-dropdown trigger="click" @command="handleMoreAction">
              <el-button>
                <el-icon><More /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="report">举报用户</el-dropdown-item>
                  <el-dropdown-item command="block">加入黑名单</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 内容标签页 -->
    <el-card class="content-card">
      <el-tabs v-model="activeTab">
        <!-- 游戏收藏 -->
        <el-tab-pane label="游戏收藏" name="games">
          <div class="game-grid">
            <el-card
                v-for="game in games"
                :key="game.id"
                class="game-card"
                @click="viewGame(game)"
            >
              <el-image :src="game.coverImage" fit="cover" class="game-cover" />
              <div class="game-info">
                <h3>{{ game.title }}</h3>
                <div class="game-meta">
                  <el-rate v-model="game.rating" disabled text-color="#ff9900" />
                  <span>{{ formatPlayTime(game.playTime) }}</span>
                </div>
              </div>
            </el-card>
          </div>
          <div v-if="hasMoreGames" class="load-more">
            <el-button link @click="loadMoreGames">加载更多</el-button>
          </div>
        </el-tab-pane>

        <!-- 发帖记录 -->
        <el-tab-pane label="发帖记录" name="posts">
          <div class="post-list">
            <div
                v-for="post in posts"
                :key="post.id"
                class="post-item"
                @click="viewPost(post)"
            >
              <div class="post-main">
                <h3>{{ post.title }}</h3>
                <p>{{ post.content }}</p>
              </div>
              <div class="post-meta">
                <span>{{ formatTime(post.createdAt) }}</span>
                <div class="post-stats">
                  <span>
                    <el-icon><View /></el-icon>
                    {{ post.viewCount }}
                  </span>
                  <span>
                    <el-icon><ChatDotRound /></el-icon>
                    {{ post.commentCount }}
                  </span>
                  <span>
                    <el-icon><Star /></el-icon>
                    {{ post.likeCount }}
                  </span>
                </div>
              </div>
            </div>
          </div>
          <div v-if="hasMorePosts" class="load-more">
            <el-button link @click="loadMorePosts">加载更多</el-button>
          </div>
        </el-tab-pane>

        <!-- 活动记录 -->
        <el-tab-pane label="活动记录" name="events">
          <el-timeline>
            <el-timeline-item
                v-for="event in events"
                :key="event.id"
                :timestamp="formatTime(event.time)"
                :type="getEventType(event.type)"
            >
              <h4>{{ event.title }}</h4>
              <p>{{ event.description }}</p>
            </el-timeline-item>
          </el-timeline>
          <div v-if="hasMoreEvents" class="load-more">
            <el-button link @click="loadMoreEvents">加载更多</el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 举报对话框 -->
    <el-dialog
        v-model="reportDialogVisible"
        title="举报用户"
        width="500px"
    >
      <el-form
          ref="reportForm"
          :model="reportData"
          :rules="reportRules"
          label-width="100px"
      >
        <el-form-item label="举报原因" prop="reason">
          <el-select v-model="reportData.reason" placeholder="请选择举报原因">
            <el-option
                v-for="item in reportReasons"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="详细说明" prop="description">
          <el-input
              v-model="reportData.description"
              type="textarea"
              :rows="4"
              placeholder="请详细描述违规内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="reportDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReport">提交</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Camera,
  Calendar,
  Location,
  More,
  View,
  ChatDotRound,
  Star
} from '@element-plus/icons-vue'
import { formatDistanceToNow, format } from 'date-fns'
import { zhCN } from 'date-fns/locale'
import {
  getUserInfo,
  getUserGameStats,
  getUserPosts,
  followUser,
  unfollowUser,
  reportUser
} from '@/api/user'

const route = useRoute()
const router = useRouter()
const store = useStore()

// 基础数据
const userInfo = ref({})
const stats = ref({
  gameCount: 0,
  postCount: 0,
  followingCount: 0,
  followerCount: 0
})

// 选项卡相关
const activeTab = ref('games')
const games = ref([])
const posts = ref([])
const events = ref([])

// 加载更多标记
const hasMoreGames = ref(false)
const hasMorePosts = ref(false)
const hasMoreEvents = ref(false)

// 页码记录
const pageInfo = ref({
  games: { page: 1, size: 12 },
  posts: { page: 1, size: 10 },
  events: { page: 1, size: 20 }
})

// 计算属性
const isOwnProfile = computed(() => {
  return store.state.user.userInfo.id === userInfo.value.id
})

const isFollowing = computed(() => {
  return userInfo.value.isFollowing
})

// 头像上传相关
const uploadUrl = `${process.env.VUE_APP_BASE_API}/api/users/avatar`
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${store.state.user.token}`
}))

// 举报相关
const reportDialogVisible = ref(false)
const reportData = ref({
  reason: '',
  description: ''
})
const reportReasons = [
  { label: '垃圾广告', value: 'spam' },
  { label: '不当内容', value: 'inappropriate' },
  { label: '冒充他人', value: 'impersonation' },
  { label: '骚扰行为', value: 'harassment' },
  { label: '其他原因', value: 'other' }
]
const reportRules = {
  reason: [
    { required: true, message: '请选择举报原因', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请填写详细说明', trigger: 'blur' },
    { min: 10, message: '描述不能少于10个字符', trigger: 'blur' }
  ]
}

// 初始化数据
const initData = async () => {
  try {
    const userId = route.params.id || store.state.user.userInfo.id
    const [userRes, statsRes] = await Promise.all([
      getUserInfo(userId),
      getUserGameStats(userId)
    ])
    userInfo.value = userRes.data
    stats.value = statsRes.data
    loadTabData()
  } catch (error) {
    ElMessage.error('获取用户信息失败')
  }
}

// 加载标签页数据
const loadTabData = () => {
  switch (activeTab.value) {
    case 'games':
      loadGames()
      break
    case 'posts':
      loadPosts()
      break
    case 'events':
      loadEvents()
      break
  }
}

// 加载游戏列表
const loadGames = async () => {
  // TODO: 实现游戏列表加载
}

// 加载帖子列表
const loadPosts = async () => {
  try {
    const res = await getUserPosts(userInfo.value.id, {
      page: pageInfo.value.posts.page,
      size: pageInfo.value.posts.size
    })
    if (pageInfo.value.posts.page === 1) {
      posts.value = res.data.content
    } else {
      posts.value.push(...res.data.content)
    }
    hasMorePosts.value = posts.value.length < res.data.totalElements
  } catch (error) {
    ElMessage.error('获取帖子列表失败')
  }
}

// 加载事件列表
const loadEvents = async () => {
  // TODO: 实现事件列表加载
}

// 关注/取关处理
const handleFollow = async () => {
  try {
    if (isFollowing.value) {
      await unfollowUser(userInfo.value.id)
      userInfo.value.isFollowing = false
      stats.value.followerCount--
      ElMessage.success('已取消关注')
    } else {
      await followUser(userInfo.value.id)
      userInfo.value.isFollowing = true
      stats.value.followerCount++
      ElMessage.success('关注成功')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 发送消息
const handleMessage = () => {
  router.push(`/messages?target=${userInfo.value.id}`)
}

// 更多操作处理
const handleMoreAction = async (command) => {
  switch (command) {
    case 'report':
      reportDialogVisible.value = true
      break
    case 'block':
      try {
        await ElMessageBox.confirm(
            '确定要将该用户加入黑名单吗？',
            '提示',
            {
              type: 'warning'
            }
        )
        // TODO: 实现加入黑名单功能
        ElMessage.success('已加入黑名单')
      } catch {}
      break
  }
}

// 提交举报
const submitReport = async () => {
  try {
    await reportUser({
      userId: userInfo.value.id,
      ...reportData.value
    })
    reportDialogVisible.value = false
    ElMessage.success('举报已提交')
  } catch {}
}

// 头像上传相关处理
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
  userInfo.value.avatar = response.data.url
  ElMessage.success('头像更新成功')
}

const handleAvatarError = () => {
  ElMessage.error('头像上传失败')
}

// 加载更多处理
const loadMoreGames = () => {
  pageInfo.value.games.page++
  loadGames()
}

const loadMorePosts = () => {
  pageInfo.value.posts.page++
  loadPosts()
}

const loadMoreEvents = () => {
  pageInfo.value.events.page++
  loadEvents()
}

// 查看详情
const viewGame = (game) => {
  router.push(`/games/${game.id}`)
}

const viewPost = (post) => {
  router.push(`/posts/${post.id}`)
}

// 工具函数
const formatTime = (time) => {
  return formatDistanceToNow(new Date(time), { addSuffix: true, locale: zhCN })
}

const formatDate = (date) => {
  return format(new Date(date), 'yyyy-MM-dd')
}

const formatPlayTime = (minutes) => {
  if (!minutes) return '0小时'
  if (minutes < 60) return `${minutes}分钟`
  const hours = Math.floor(minutes / 60)
  return `${hours}小时`
}

const getRoleType = (role) => {
  const typeMap = {
    'admin': 'danger',
    'moderator': 'warning',
    'vip': 'success'
  }
  return typeMap[role] || 'info'
}

const getRoleLabel = (role) => {
  const labelMap = {
    'admin': '管理员',
    'moderator': '版主',
    'vip': 'VIP会员',
    'user': '用户'
  }
  return labelMap[role] || role
}

const getEventType = (type) => {
  const typeMap = {
    'post': 'primary',
    'comment': 'success',
    'game': 'warning',
    'follow': 'info'
  }
  return typeMap[type] || 'info'
}

// 监听标签页变化
watch(activeTab, () => {
  loadTabData()
})

// 初始化
onMounted(() => {
  initData()
})
</script>

<style lang="scss" scoped>
.profile-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;

  .user-card {
    margin-bottom: 20px;

    .user-header {
      display: flex;
      gap: 30px;
    }

    .user-avatar {
      position: relative;

      &:hover .avatar-upload {
        opacity: 1;
      }
    }

    .avatar-upload {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      background: rgba(0, 0, 0, 0.6);
      color: #fff;
      text-align: center;
      padding: 8px 0;
      opacity: 0;
      transition: opacity 0.3s;
      cursor: pointer;

      .upload-trigger {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 4px;
      }
    }

    .user-info {
      flex: 1;

      .user-name {
        display: flex;
        align-items: center;
        gap: 15px;
        margin-bottom: 10px;

        h2 {
          margin: 0;
        }
      }

      .user-badges {
        display: flex;
        gap: 8px;
      }

      .user-bio {
        margin: 10px 0;
        color: #666;
      }

      .user-meta {
        display: flex;
        gap: 20px;
        color: #909399;
        font-size: 14px;

        span {
          display: flex;
          align-items: center;
          gap: 5px;
        }
      }

      .user-stats {
        display: flex;
        gap: 40px;
        margin: 20px 0;

        .stat-item {
          text-align: center;

          .stat-value {
            font-size: 24px;
            font-weight: bold;
            color: #303133;
          }

          .stat-label {
            margin-top: 5px;
            color: #909399;
          }
        }
      }

      .user-actions {
        display: flex;
        gap: 10px;
      }
    }
  }

  .content-card {
    .game-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 20px;
    }

    .game-card {
      cursor: pointer;
      transition: transform 0.3s;

      &:hover {
        transform: translateY(-5px);
      }

      .game-cover {
        width: 100%;
        height: 120px;
      }

      .game-info {
        padding: 10px;

        h3 {
          margin: 0 0 10px;
          font-size: 16px;
        }

        .game-meta {
          display: flex;
          justify-content: space-between;
          align-items: center;
          color: #909399;
          font-size: 12px;
        }
      }
    }

    .post-list {
      .post-item {
        margin-bottom: 20px;
        padding: 15px;
        background: #f5f7fa;
        border-radius: 4px;
        cursor: pointer;
        transition: background-color 0.3s;

        &:hover {
          background: #ecf5ff;
        }

        .post-main {
          h3 {
            margin: 0 0 10px;
            font-size: 16px;
          }

          p {
            margin: 0;
            color: #666;
          }
        }

        .post-meta {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-top: 10px;
          color: #909399;
          font-size: 12px;

          .post-stats {
            display: flex;
            gap: 15px;

            span {
              display: flex;
              align-items: center;
              gap: 4px;
            }
          }
        }
      }
    }

    .load-more {
      text-align: center;
      margin-top: 20px;
    }
  }
}
</style>