<template>
  <div class="home-container">
    <!-- Banner轮播 -->
    <el-carousel height="400px" class="banner-carousel">
      <el-carousel-item v-for="banner in banners" :key="banner.id">
        <div class="banner-content" @click="handleBannerClick(banner)">
          <el-image :src="banner.imageUrl" fit="cover" />
          <div class="banner-info">
            <h2>{{ banner.title }}</h2>
            <p>{{ banner.description }}</p>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>

    <!-- 快捷功能区 -->
    <div class="quick-access">
      <el-row :gutter="20">
        <el-col :span="6" v-for="item in quickAccess" :key="item.route">
          <el-card shadow="hover" @click="$router.push(item.route)">
            <div class="quick-access-item">
              <el-icon :size="24">
                <component :is="item.icon" />
              </el-icon>
              <span>{{ item.title }}</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 热门游戏 -->
    <div class="section">
      <div class="section-header">
        <h2>热门游戏</h2>
        <el-button type="primary" link @click="$router.push('/games')">
          查看更多
        </el-button>
      </div>
      <el-row :gutter="20">
        <el-col
            :xs="12"
            :sm="8"
            :md="6"
            v-for="game in popularGames"
            :key="game.id"
        >
          <el-card
              :body-style="{ padding: '0px' }"
              shadow="hover"
              @click="$router.push(`/games/${game.id}`)"
          >
            <el-image
                :src="game.coverImage"
                fit="cover"
                class="game-cover"
            />
            <div class="game-info">
              <h3>{{ game.title }}</h3>
              <el-rate
                  v-model="game.rating"
                  disabled
                  show-score
                  text-color="#ff9900"
              />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 最新活动 -->
    <div class="section">
      <div class="section-header">
        <h2>最新活动</h2>
        <el-button type="primary" link @click="$router.push('/events')">
          查看更多
        </el-button>
      </div>
      <el-row :gutter="20">
        <el-col
            :span="8"
            v-for="event in latestEvents"
            :key="event.id"
        >
          <el-card shadow="hover" @click="$router.push(`/events/${event.id}`)">
            <el-image
                :src="event.coverImage"
                fit="cover"
                class="event-cover"
            />
            <div class="event-info">
              <h3>{{ event.title }}</h3>
              <div class="event-meta">
                <span>
                  <el-icon><Calendar /></el-icon>
                  {{ formatTime(event.startTime) }}
                </span>
                <el-tag :type="getEventStatusType(event.status)">
                  {{ getEventStatusText(event.status) }}
                </el-tag>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 热门帖子 -->
    <div class="section">
      <div class="section-header">
        <h2>热门帖子</h2>
        <el-button type="primary" link @click="$router.push('/community')">
          查看更多
        </el-button>
      </div>
      <div class="post-list">
        <el-card
            v-for="post in hotPosts"
            :key="post.id"
            shadow="hover"
            @click="$router.push(`/posts/${post.id}`)"
        >
          <div class="post-item">
            <div class="post-main">
              <h3>{{ post.title }}</h3>
              <p>{{ post.content }}</p>
            </div>
            <div class="post-meta">
              <div class="author-info">
                <el-avatar :size="32" :src="post.author.avatar" />
                <span>{{ post.author.nickname }}</span>
              </div>
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
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  Calendar,
  View,
  ChatDotRound,
  Star,
  GameController,
  Trophy,
  User,
  Setting
} from '@element-plus/icons-vue'
import { formatDistanceToNow } from 'date-fns'
import { zhCN } from 'date-fns/locale'
import { getHomeData } from '@/api/home'

// 快捷功能入口
const quickAccess = [
  { title: '我的游戏', icon: 'GameController', route: '/games' },
  { title: '我的活动', icon: 'Trophy', route: '/events' },
  { title: '个人中心', icon: 'User', route: '/profile' },
  { title: '系统设置', icon: 'Setting', route: '/settings' }
]

// 页面数据
const banners = ref([])
const popularGames = ref([])
const latestEvents = ref([])
const hotPosts = ref([])

// 获取首页数据
const fetchHomeData = async () => {
  try {
    const res = await getHomeData()
    banners.value = res.data.banners
    popularGames.value = res.data.popularGames
    latestEvents.value = res.data.latestEvents
    hotPosts.value = res.data.hotPosts
  } catch (error) {
    console.error('获取首页数据失败:', error)
  }
}

// Banner点击处理
const handleBannerClick = (banner) => {
  if (banner.link) {
    window.open(banner.link)
  }
}

// 格式化时间
const formatTime = (time) => {
  return formatDistanceToNow(new Date(time), {
    addSuffix: true,
    locale: zhCN
  })
}

// 获取活动状态
const getEventStatusType = (status) => {
  const statusMap = {
    'UPCOMING': 'info',
    'ONGOING': 'success',
    'ENDED': ''
  }
  return statusMap[status] || ''
}

const getEventStatusText = (status) => {
  const statusMap = {
    'UPCOMING': '即将开始',
    'ONGOING': '进行中',
    'ENDED': '已结束'
  }
  return statusMap[status] || status
}

onMounted(() => {
  fetchHomeData()
})
</script>

<style lang="scss" scoped>
.home-container {
  padding: 20px;
}

.banner-carousel {
  margin-bottom: 30px;
  border-radius: 8px;
  overflow: hidden;
}

.banner-content {
  position: relative;
  height: 100%;
  cursor: pointer;

  .el-image {
    width: 100%;
    height: 100%;
  }

  .banner-info {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 20px;
    background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
    color: #fff;

    h2 {
      margin: 0 0 10px;
      font-size: 24px;
    }

    p {
      margin: 0;
      font-size: 16px;
      opacity: 0.8;
    }
  }
}

.quick-access {
  margin-bottom: 30px;

  .quick-access-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
    padding: 20px;
    cursor: pointer;
    transition: transform 0.3s;

    &:hover {
      transform: translateY(-5px);
    }
  }
}

.section {
  margin-bottom: 30px;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      margin: 0;
      font-size: 20px;
      font-weight: 500;
    }
  }
}

.game-cover {
  height: 200px;
  width: 100%;
}

.game-info {
  padding: 14px;

  h3 {
    margin: 0 0 10px;
    font-size: 16px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.event-cover {
  height: 160px;
  width: 100%;
}

.event-info {
  padding: 14px;

  h3 {
    margin: 0 0 10px;
    font-size: 16px;
  }

  .event-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    color: #666;
    font-size: 14px;

    span {
      display: flex;
      align-items: center;
      gap: 5px;
    }
  }
}

.post-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.post-item {
  .post-main {
    h3 {
      margin: 0 0 10px;
      font-size: 16px;
    }

    p {
      margin: 0;
      color: #666;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }
  }

  .post-meta {
    margin-top: 15px;
    padding-top: 15px;
    border-top: 1px solid #eee;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .author-info {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .post-stats {
      display: flex;
      gap: 15px;
      color: #666;
      font-size: 14px;

      span {
        display: flex;
        align-items: center;
        gap: 5px;
      }
    }
  }
}
</style>