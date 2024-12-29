<template>
  <div class="game-detail-container" v-loading="loading">
    <el-card class="game-detail-card">
      <div class="game-header">
        <div class="game-cover">
          <el-image
              :src="gameDetail.coverImage"
              fit="cover"
              class="cover-image"
          >
            <template #error>
              <div class="image-placeholder">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
        </div>

        <div class="game-info">
          <h1 class="game-title">{{ gameDetail.title }}</h1>
          <div class="game-rating">
            <span class="label">游戏评分：</span>
            <el-rate
                v-model="gameDetail.rating"
                disabled
                show-score
                text-color="#ff9900"
            />
            <span class="rating-count">({{ gameDetail.ratingCount }}条评分)</span>
          </div>

          <div class="game-categories">
            <span class="label">游戏分类：</span>
            <el-tag
                v-for="category in gameDetail.categories"
                :key="category"
                class="category-tag"
            >
              {{ category }}
            </el-tag>
          </div>

          <div class="game-price">
            <span class="label">价格：</span>
            <span class="price">￥{{ gameDetail.price }}</span>
          </div>

          <div class="game-actions">
            <el-button type="primary" @click="handleAddToLibrary">
              添加到游戏库
            </el-button>
          </div>
        </div>
      </div>

      <div class="game-description">
        <h2>游戏简介</h2>
        <p>{{ gameDetail.description }}</p>
      </div>

      <div class="game-screenshots">
        <h2>游戏截图</h2>
        <el-carousel :interval="4000" type="card" height="400px">
          <el-carousel-item v-for="(screenshot, index) in gameDetail.screenshots" :key="index">
            <el-image
                :src="screenshot"
                fit="contain"
                class="screenshot-image"
            />
          </el-carousel-item>
        </el-carousel>
      </div>

      <div class="game-rating-section">
        <h2>评分</h2>
        <div class="rating-form">
          <span class="label">我的评分：</span>
          <el-rate
              v-model="userRating"
              allow-half
              @change="handleRating"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { getGameDetail, rateGame } from '@/api/game'

export default {
  name: 'GameDetail',
  components: { Picture },

  setup() {
    const route = useRoute()
    const loading = ref(false)
    const gameDetail = ref({})
    const userRating = ref(0)

    const fetchGameDetail = async () => {
      loading.value = true
      try {
        const res = await getGameDetail(route.params.id)
        gameDetail.value = res.data
        userRating.value = res.data.userRating || 0
      } catch (error) {
        ElMessage.error('获取游戏详情失败')
      } finally {
        loading.value = false
      }
    }

    const handleRating = async (value) => {
      try {
        await rateGame(route.params.id, value)
        ElMessage.success('评分成功')
        fetchGameDetail()
      } catch (error) {
        ElMessage.error('评分失败')
        userRating.value = gameDetail.value.userRating || 0
      }
    }

    const handleAddToLibrary = () => {
      // TODO: 实现添加到游戏库的功能
      ElMessage.success('添加成功')
    }

    onMounted(() => {
      fetchGameDetail()
    })

    return {
      loading,
      gameDetail,
      userRating,
      handleRating,
      handleAddToLibrary
    }
  }
}
</script>

<style scoped>
.game-detail-container {
  padding: 20px;
}

.game-detail-card {
  margin-bottom: 20px;
}

.game-header {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
}

.game-cover {
  flex-shrink: 0;
  width: 300px;
}

.cover-image {
  width: 100%;
  height: 400px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.game-info {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.game-title {
  margin: 0;
  font-size: 28px;
  color: #303133;
}

.game-rating {
  display: flex;
  align-items: center;
  gap: 10px;
}

.label {
  color: #606266;
  font-size: 14px;
  min-width: 70px;
}

.rating-count {
  color: #909399;
  font-size: 14px;
}

.game-categories {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.category-tag {
  margin-right: 5px;
}

.game-price {
  margin-top: 10px;
}

.price {
  color: #f56c6c;
  font-size: 24px;
  font-weight: bold;
}

.game-actions {
  margin-top: auto;
  padding-top: 20px;
}

.game-description {
  margin: 30px 0;
}

.game-description h2 {
  font-size: 20px;
  margin-bottom: 15px;
  color: #303133;
}

.game-description p {
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}

.game-screenshots {
  margin: 30px 0;
}

.game-screenshots h2 {
  font-size: 20px;
  margin-bottom: 15px;
  color: #303133;
}

.screenshot-image {
  width: 100%;
  height: 100%;
  border-radius: 8px;
}

.game-rating-section {
  margin: 30px 0;
}

.game-rating-section h2 {
  font-size: 20px;
  margin-bottom: 15px;
  color: #303133;
}

.rating-form {
  display: flex;
  align-items: center;
  gap: 10px;
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 30px;
}

@media screen and (max-width: 768px) {
  .game-header {
    flex-direction: column;
  }

  .game-cover {
    width: 100%;
  }

  .cover-image {
    height: 300px;
  }
}
</style>