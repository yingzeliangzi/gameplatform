<template>
  <div class="game-list-container">
    <!-- 搜索和筛选区域 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="游戏名称">
          <el-input
              v-model="searchForm.title"
              placeholder="请输入游戏名称"
              clearable
              @clear="handleSearch"
          />
        </el-form-item>

        <el-form-item label="分类">
          <el-select
              v-model="searchForm.categories"
              multiple
              collapse-tags
              placeholder="请选择分类"
              clearable
              @change="handleSearch"
          >
            <el-option
                v-for="category in categories"
                :key="category"
                :label="category"
                :value="category"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="最低评分">
          <el-rate
              v-model="searchForm.minRating"
              allow-half
              @change="handleSearch"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleExport">导出我的游戏</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 游戏列表 -->
    <el-row :gutter="20" class="game-grid">
      <el-col
          v-for="game in gameList"
          :key="game.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
          :xl="4"
      >
        <el-card class="game-card" :body-style="{ padding: '0px' }">
          <el-image
              :src="game.coverImage"
              fit="cover"
              class="game-image"
              @click="handleGameClick(game)"
          >
            <template #error>
              <div class="image-placeholder">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>

          <div class="game-info">
            <h3 class="game-title">{{ game.title }}</h3>
            <div class="game-rating">
              <el-rate
                  v-model="game.rating"
                  disabled
                  show-score
                  text-color="#ff9900"
              />
              <span class="rating-count">({{ game.ratingCount }})</span>
            </div>
            <div class="game-price">￥{{ game.price }}</div>
          </div>

          <div class="game-actions">
            <el-button type="primary" @click="handleGameClick(game)">
              查看详情
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[12, 24, 36, 48]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { getGameList, getCategories, exportGames } from '@/api/game'

export default {
  name: 'GameList',
  components: { Picture },

  setup() {
    const router = useRouter()
    const gameList = ref([])
    const categories = ref([])
    const total = ref(0)
    const currentPage = ref(1)
    const pageSize = ref(12)

    const searchForm = reactive({
      title: '',
      categories: [],
      minRating: 0
    })

    // 获取游戏列表
    const fetchGames = async () => {
      try {
        const params = {
          page: currentPage.value - 1,
          size: pageSize.value,
          ...searchForm
        }
        const res = await getGameList(params)
        gameList.value = res.data.content
        total.value = res.data.totalElements
      } catch (error) {
        ElMessage.error('获取游戏列表失败')
      }
    }

    // 获取分类列表
    const fetchCategories = async () => {
      try {
        const res = await getCategories()
        categories.value = res.data
      } catch (error) {
        ElMessage.error('获取分类列表失败')
      }
    }

    // 搜索处理
    const handleSearch = () => {
      currentPage.value = 1
      fetchGames()
    }

    // 重置搜索
    const handleReset = () => {
      Object.keys(searchForm).forEach(key => {
        searchForm[key] = key === 'categories' ? [] : ''
      })
      handleSearch()
    }

    // 导出游戏
    const handleExport = async () => {
      try {
        const res = await exportGames()
        const blob = new Blob([res.data], { type: 'text/csv' })
        const link = document.createElement('a')
        link.href = window.URL.createObjectURL(blob)
        link.download = 'my-games.csv'
        link.click()
        window.URL.revokeObjectURL(link.href)
      } catch (error) {
        ElMessage.error('导出失败')
      }
    }

    // 分页处理
    const handleSizeChange = (val) => {
      pageSize.value = val
      fetchGames()
    }

    const handleCurrentChange = (val) => {
      currentPage.value = val
      fetchGames()
    }

    // 游戏点击处理
    const handleGameClick = (game) => {
      router.push(`/games/${game.id}`)
    }

    onMounted(() => {
      fetchCategories()
      fetchGames()
    })

    return {
      gameList,
      categories,
      total,
      currentPage,
      pageSize,
      searchForm,
      handleSearch,
      handleReset,
      handleExport,
      handleSizeChange,
      handleCurrentChange,
      handleGameClick
    }
  }
}
</script>

<style scoped>
.game-list-container {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.game-grid {
  margin-bottom: 20px;
}

.game-card {
  margin-bottom: 20px;
  transition: transform 0.3s;
}

.game-card:hover {
  transform: translateY(-5px);
}

.game-image {
  height: 200px;
  width: 100%;
  cursor: pointer;
}

.image-placeholder {
  height: 200px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}

.game-info {
  padding: 14px;
}

.game-title {
  margin: 0;
  font-size: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.game-rating {
  margin: 10px 0;
  display: flex;
  align-items: center;
}

.rating-count {
  margin-left: 5px;
  color: #909399;
  font-size: 12px;
}

.game-price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

.game-actions {
  padding: 10px 14px;
  border-top: 1px solid #ebeef5;
  text-align: center;
}

.pagination-container {
  text-align: center;
  margin-top: 20px;
}
</style>