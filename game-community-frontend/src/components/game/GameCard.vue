<template>
  <div class="game-card" @click="handleClick">
    <!-- 游戏封面 -->
    <div class="game-cover">
      <el-image
          :src="game.coverImage"
          :alt="game.title"
          fit="cover"
          loading="lazy"
      >
        <template #placeholder>
          <div class="image-placeholder">
            <el-icon><Picture /></el-icon>
          </div>
        </template>
        <template #error>
          <div class="image-error">
            <el-icon><Warning /></el-icon>
          </div>
        </template>
      </el-image>
    </div>

    <!-- 游戏信息 -->
    <div class="game-info">
      <h3 class="game-title" :title="game.title">{{ game.title }}</h3>

      <!-- 分类标签 -->
      <div class="game-categories">
        <el-tag
            v-for="category in game.categories"
            :key="category"
            size="small"
            type="info"
            effect="plain"
        >
          {{ category }}
        </el-tag>
      </div>

      <!-- 评分 -->
      <div class="game-rating">
        <el-rate
            v-model="game.rating"
            disabled
            :max="5"
            :allow-half="true"
            :colors="['#ffd21e', '#ffd21e', '#ffd21e']"
        />
        <span class="rating-count">{{ game.ratingCount }}人评分</span>
      </div>

      <!-- 操作按钮 -->
      <div class="game-actions">
        <el-button-group>
          <el-button
              v-if="!isCollected"
              type="primary"
              :icon="Star"
              @click.stop="handleCollect"
          >
            收藏
          </el-button>
          <el-button
              v-else
              type="danger"
              :icon="StarFilled"
              @click.stop="handleUncollect"
          >
            已收藏
          </el-button>
          <el-button
              type="primary"
              :icon="Share"
              @click.stop="handleShare"
          >
            分享
          </el-button>
        </el-button-group>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture, Warning, Star, StarFilled, Share } from '@element-plus/icons-vue'
import { gameApi } from '@/api'

const props = defineProps({
  game: {
    type: Object,
    required: true
  }
})

const router = useRouter()
const isCollected = ref(props.game.isCollected || false)

// 跳转到详情页
const handleClick = () => {
  router.push(`/games/${props.game.id}`)
}

// 收藏/取消收藏
const handleCollect = async () => {
  try {
    if (isCollected.value) {
      await gameApi.uncollectGame(props.game.id)
      isCollected.value = false
      ElMessage.success('已取消收藏')
    } else {
      await gameApi.collectGame(props.game.id)
      isCollected.value = true
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    ElMessage.error('操作失败，请重试')
  }
}

// 分享游戏
const handleShare = () => {
  const shareUrl = `${window.location.origin}/games/${props.game.id}`
  // 复制链接到剪贴板
  navigator.clipboard.writeText(shareUrl).then(() => {
    ElMessage.success('链接已复制到剪贴板')
  })
}
</script>

<style lang="scss" scoped>
.game-card {
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  background: var(--el-bg-color);
  box-shadow: var(--el-box-shadow-light);
  transition: transform 0.3s, box-shadow 0.3s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--el-box-shadow);
  }

  .game-cover {
    position: relative;
    padding-top: 56.25%; // 16:9 比例

    .el-image {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
    }

    .image-placeholder,
    .image-error {
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: var(--el-fill-color-light);
      color: var(--el-text-color-secondary);
      font-size: 24px;
    }
  }

  .game-info {
    padding: 16px;

    .game-title {
      margin: 0 0 8px;
      font-size: 16px;
      font-weight: 500;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .game-categories {
      margin-bottom: 8px;
      .el-tag {
        margin-right: 4px;
        margin-bottom: 4px;
      }
    }

    .game-rating {
      margin-bottom: 12px;
      display: flex;
      align-items: center;
      gap: 8px;

      .rating-count {
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }
    }

    .game-actions {
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>