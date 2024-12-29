<template>
  <div class="user-profile">
    <!-- 用户信息头部 -->
    <el-card class="profile-header">
      <div class="profile-cover">
        <el-image
            :src="userInfo.coverImage || defaultCover"
            fit="cover"
            class="cover-image"
        >
          <template #error>
            <div class="image-placeholder">
              <el-icon><Picture /></el-icon>
            </div>
          </template>
        </el-image>
        <div class="profile-avatar">
          <el-avatar
              :size="120"
              :src="userInfo.avatar"
              :alt="userInfo.nickname"
          />
        </div>
      </div>

      <div class="profile-info">
        <div class="info-main">
          <h1 class="nickname">{{ userInfo.nickname }}</h1>
          <div class="user-tags">
            <el-tag
                v-if="userInfo.isVerified"
                type="success"
            >
              已认证
            </el-tag>
            <el-tag
                v-for="role in userInfo.roles"
                :key="role"
                :type="getRoleType(role)"
            >
              {{ getRoleLabel(role) }}
            </el-tag>
          </div>
        </div>

        <div class="user-bio">{{ userInfo.bio || '这个人很懒，什么都没有写~' }}</div>

        <div class="user-stats">
          <div class="stat-item">
            <div class="stat-value">{{ userStats.gameCount }}</div>
            <div class="stat-label">游戏</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ userStats.postCount }}</div>
            <div class="stat-label">帖子</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ userStats.followingCount }}</div>
            <div class="stat-label">关注</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ userStats.followerCount }}</div>
            <div class="stat-label">粉丝</div>
          </div>
        </div>

        <div class="user-actions">
          <template v-if="isCurrentUser">
            <el-button type="primary" @click="editProfile">
              编辑资料
            </el-button>
          </template>
          <template v-else>
            <el-button
                :type="isFollowing ? 'default' : 'primary'"
                @click="handleFollow"
            >
              {{ isFollowing ? '已关注' : '关注' }}
            </el-button>
            <el-button @click="sendMessage">
              发送消息
            </el-button>
            <el-dropdown trigger="click">
              <el-button>
                <el-icon><More /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleReport">举报用户</el-dropdown-item>
                  <el-dropdown-item @click="handleBlock">加入黑名单</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </div>
      </div>
    </el-card>

    <!-- 内容标签页 -->
    <el-card class="profile-content">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="游戏库" name="games">
          <div class="game-list" v-loading="loading">
            <div
                v-for="game in games"
                :key="game.id"
                class="game-item"
                @click="viewGame(game)"
            >
              <el-image
                  :src="game.coverImage"
                  fit="cover"
                  class="game-cover"
              />
              <div class="game-info">
                <div class="game-title">{{ game.title }}</div>
                <div class="game-meta">
                  <span>游戏时长: {{ formatPlayTime(game.playTime) }}</span>
                  <span>最后游玩: {{ formatLastPlayed(game.lastPlayedAt) }}</span>
                </div>
                <div class="game-achievements" v-if="game.achievements">
                  <el-progress
                      :percentage="game.achievements.percentage"
                      :format="percent => `${game.achievements.completed}/${game.achievements.total}`"
                  />
                </div>
              </div>
            </div>
            <el-empty v-if="!games.length" description="暂无游戏" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="发帖" name="posts">
          <div class="post-list" v-loading="loading">
            <div
                v-for="post in posts"
                :key="post.id"
                class="post-item"
                @click="viewPost(post)"
            >
              <div class="post-title">{{ post.title }}</div>
              <div class="post-content">{{ post.content }}</div>
              <div class="post-meta">
                <span>
                  <el-icon><View /></el-icon>
                  {{ post.viewCount }}
                </span>
                <span>
                  <el-icon><ChatDotRound /></el-icon>
                  {{ post.commentCount }}
                </span>
                <span>
                  <el-icon><ThumbsUp /></el-icon>
                  {{ post.likeCount }}
                </span>
                <span class="post-time">{{ formatTime(post.createdAt) }}</span>
              </div>
            </div>
            <el-empty v-if="!posts.length" description="暂无帖子" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="收藏" name="collections">
          <div class="collection-list" v-loading="loading">
            <el-tabs v-model="collectionType" class="collection-tabs">
              <el-tab-pane label="游戏" name="games">
                <div class="collection-grid">
                  <div
                      v-for="game in collections.games"
                      :key="game.id"
                      class="collection-item"
                  >
                    <el-card :body-style="{ padding: 0 }">
                      <el-image
                          :src="game.coverImage"
                          fit="cover"
                          class="collection-image"
                      />
                      <div class="collection-info">
                        <div class="collection-title">{{ game.title }}</div>
                        <div class="collection-time">
                          收藏于 {{ formatTime(game.collectedAt) }}
                        </div>
                      </div>
                    </el-card>
                  </div>
                </div>
              </el-tab-pane>

              <el-tab-pane label="帖子" name="posts">
                <div class="collection-list">
                  <div
                      v-for="post in collections.posts"
                      :key="post.id"
                      class="collection-item"
                  >
                    <div class="collection-title">{{ post.title }}</div>
                    <div class="collection-time">
                      收藏于 {{ formatTime(post.collectedAt) }}
                    </div>
                  </div>
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
        </el-tab-pane>

        <el-tab-pane label="活动" name="events">
          <div class="event-list" v-loading="loading">
            <el-timeline>
              <el-timeline-item
                  v-for="event in events"
                  :key="event.id"
                  :timestamp="formatTime(event.time)"
                  :type="getEventType(event.type)"
              >
                <div class="event-content">
                  <div class="event-title">{{ event.title }}</div>
                  <div class="event-desc">{{ event.description }}</div>
                </div>
              </el-timeline-item>
            </el-timeline>
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
          :model="reportForm"
          :rules="reportRules"
          label-width="80px"
      >
        <el-form-item label="举报理由" prop="reason">
          <el-select v-model="reportForm.reason" placeholder="请选择举报理由">
            <el-option
                v-for="reason in reportReasons"
                :key="reason.value"
                :label="reason.label"
                :value="reason.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="详细说明" prop="description">
          <el-input
              v-model="reportForm.description"
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

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDistanceToNow, format } from 'date-fns'
import { zhCN } from 'date-fns/locale'
import {
  getUserInfo,
  getUserGames,
  getUserPosts,
  getUserCollections,
  getUserEvents,
  followUser,
  unfollowUser,
  reportUser,
  blockUser
} from '@/api/user'

export default {
  name: 'UserProfile',

  setup() {
    const route = useRoute()
    const router = useRouter()
    const store = useStore()

    // 页面状态
    const loading = ref(false)
    const activeTab = ref('games')
    const collectionType = ref('games')
    const reportDialogVisible = ref(false)

    // 用户信息
    const userInfo = ref({})
    const userStats = ref({
      gameCount: 0,
      postCount: 0,
      followingCount: 0,
      followerCount: 0
    })

    // 内容列表
    const games = ref([])
    const posts = ref([])
    const collections = ref({
      games: [],
      posts: []
    })
    const events = ref([])

    // 计算属性
    const isCurrentUser = computed(() => {
      return store.state.user.id === userInfo.value.id
    })

    const isFollowing = computed(() => {
      return userInfo.value.isFollowing
    })

    // 举报表单
    const reportForm = reactive({
      reason: '',
      description: ''
    })

    const reportRules = {
      reason: [
        { required: true, message: '请选择举报理由', trigger: 'change' }
      ],
      description: [
        { required: true, message: '请填写详细说明', trigger: 'blur' },
        { min: 10, message: '详细说明不能少于10个字符', trigger: 'blur' }
      ]
    }

    const reportReasons = [
      { label: '垃圾广告', value: 'spam' },
      { label: '不当内容', value: 'inappropriate' },
      { label: '冒充他人', value: 'impersonation' },
      { label: '骚扰行为', value: 'harassment' },
      { label: '其他原因', value: 'other' }
    ]

    // 初始化数据
    const initData = async () => {
      const userId = route.params.id
      if (!userId) {
        router.push('/404')
        return
      }

      loading.value = true
      try {
        const [
          userInfoRes,
          gamesRes,
          postsRes,
          collectionsRes,
          eventsRes
        ] = await Promise.all([
          getUserInfo(userId),
          getUserGames(userId),
          getUserPosts(userId),
          getUserCollections(userId),
          getUserEvents(userId)
        ])

        userInfo.value = userInfoRes.data
        userStats.value = userInfoRes.data.stats
        games.value = gamesRes.data
        posts.value = postsRes.data
        collections.value = collectionsRes.data
        events.value = eventsRes.data
      } catch (error) {
        ElMessage.error('获取用户信息失败')
      } finally {
        loading.value = false
      }
    }

    // 编辑个人资料
    const editProfile = () => {
      router.push('/settings/profile')
    }

    // 关注/取关用户
    const handleFollow = async () => {
      try {
        if (isFollowing.value) {
          await unfollowUser(userInfo.value.id)
          userInfo.value.isFollowing = false
          userStats.value.followerCount--
          ElMessage.success('已取消关注')
        } else {
          await followUser(userInfo.value.id)
          userInfo.value.isFollowing = true
          userStats.value.followerCount++
          ElMessage.success('关注成功')
        }
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }

    // 发送消息
    const sendMessage = () => {
      router.push(`/messages?target=${userInfo.value.id}`)
    }

    // 举报用户
    const handleReport = () => {
      reportDialogVisible.value = true
    }

    const submitReport = async () => {
      try {
        await reportUser(userInfo.value.id, reportForm)
        reportDialogVisible.value = false
        ElMessage.success('举报已提交')
      } catch (error) {
        ElMessage.error('举报提交失败')
      }
    }

    // 加入黑名单
    const handleBlock = async () => {
      try {
        await ElMessageBox.confirm(
            `确定要将 ${userInfo.value.nickname} 加入黑名单吗？`,
            '警告',
            {
              type: 'warning',
              confirmButtonText: '确定',
              cancelButtonText: '取消'
            }
        )
        await blockUser(userInfo.value.id)
        ElMessage.success('已加入黑名单')
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('操作失败')
        }
      }
    }

    // 查看游戏详情
    const viewGame = (game) => {
      router.push(`/games/${game.id}`)
    }

    // 查看帖子详情
    const viewPost = (post) => {
      router.push(`/posts/${post.id}`)
    }

    // 格式化工具函数
    const formatTime = (time) => {
      return formatDistanceToNow(new Date(time), {
        addSuffix: true,
        locale: zhCN
      })
    }

    const formatPlayTime = (minutes) => {
      if (!minutes) return '0小时'
      if (minutes < 60) return `${minutes}分钟`
      const hours = Math.floor(minutes / 60)
      return `${hours}小时`
    }

    const formatLastPlayed = (time) => {
      if (!time) return '从未游玩'
      return formatTime(time)
    }

    const getRoleType = (role) => {
      const typeMap = {
        'ADMIN': 'danger',
        'MODERATOR': 'warning',
        'VIP': 'success'
      }
      return typeMap[role]
    }

    const getRoleLabel = (role) => {
      const labelMap = {
        'ADMIN': '管理员',
        'MODERATOR': '版主',
        'VIP': 'VIP会员',
        'USER': '用户'
      }
      return labelMap[role] || role
    }

    const getEventType = (type) => {
      const typeMap = {
        'POST': 'primary',
        'COMMENT': 'success',
        'EVENT': 'warning',
        'ACHIEVEMENT': 'info'
      }
      return typeMap[type] || 'info'
    }

    onMounted(() => {
      initData()
    })

    return {
      loading,
      activeTab,
      collectionType,
      userInfo,
      userStats,
      games,
      posts,
      collections,
      events,
      isCurrentUser,
      isFollowing,
      reportDialogVisible,
      reportForm,
      reportRules,
      reportReasons,
      editProfile,
      handleFollow,
      sendMessage,
      handleReport,
      submitReport,
      handleBlock,
      viewGame,
      viewPost,
      formatTime,
      formatPlayTime,
      formatLastPlayed,
      getRoleType,
      getRoleLabel,
      getEventType
    }
  }
}
</script>

<style scoped>
.user-profile {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 个人信息头部 */
.profile-header {
  margin-bottom: 20px;
}

.profile-cover {
  position: relative;
  height: 200px;
  margin: -20px -20px 0;
}

.cover-image {
  width: 100%;
  height: 100%;
}

.profile-avatar {
  position: absolute;
  left: 40px;
  bottom: -60px;
  border: 4px solid #fff;
  border-radius: 50%;
  background-color: #fff;
}

.profile-info {
  margin-top: 80px;
  padding: 0 40px;
}

.info-main {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
}

.nickname {
  margin: 0;
  font-size: 24px;
}

.user-tags {
  display: flex;
  gap: 8px;
}

.user-bio {
  color: #666;
  margin-bottom: 20px;
}

.user-stats {
  display: flex;
  gap: 40px;
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  margin-top: 5px;
  font-size: 14px;
  color: #909399;
}

.user-actions {
  display: flex;
  gap: 10px;
}

/* 内容区域 */
.profile-content {
  min-height: 500px;
}

/* 游戏列表 */
.game-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.game-item {
  cursor: pointer;
  transition: transform 0.3s;
}

.game-item:hover {
  transform: translateY(-5px);
}

.game-cover {
  width: 100%;
  height: 150px;
  border-radius: 4px;
}

.game-info {
  padding: 10px 0;
}

.game-title {
  font-weight: 500;
  margin-bottom: 5px;
}

.game-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  margin-bottom: 10px;
}

/* 帖子列表 */
.post-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.post-item {
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.post-item:hover {
  background-color: #eef1f6;
}

.post-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 10px;
}

.post-content {
  color: #666;
  margin-bottom: 15px;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 12px;
  color: #909399;
}

.post-meta span {
  display: flex;
  align-items: center;
  gap: 5px;
}

/* 收藏内容 */
.collection-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.collection-image {
  width: 100%;
  height: 120px;
}

.collection-info {
  padding: 10px;
}

.collection-title {
  font-weight: 500;
  margin-bottom: 5px;
}

.collection-time {
  font-size: 12px;
  color: #909399;
}

/* 活动时间线 */
.event-content {
  margin-bottom: 20px;
}

.event-title {
  font-weight: 500;
  margin-bottom: 5px;
}

.event-desc {
  color: #666;
  font-size: 14px;
}
</style>