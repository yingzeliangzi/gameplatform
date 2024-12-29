<template>
  <div class="home-container">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>热门游戏</span>
              <el-button class="button" text>查看更多</el-button>
            </div>
          </template>
          <div class="game-list">
            <!-- 这里后续会展示热门游戏列表 -->
            <el-empty description="暂无数据" />
          </div>
        </el-card>

        <el-card class="box-card mt-20">
          <template #header>
            <div class="card-header">
              <span>最新动态</span>
              <el-button class="button" text>查看更多</el-button>
            </div>
          </template>
          <div class="post-list">
            <!-- 这里后续会展示最新帖子列表 -->
            <el-empty description="暂无数据" />
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>近期活动</span>
            </div>
          </template>
          <div class="event-list">
            <!-- 这里后续会展示活动列表 -->
            <el-empty description="暂无数据" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'Home',
  setup() {
    // 这里后续会添加获取数据的逻辑
  }
})
</script>

<style scoped>
.home-container {
  padding: 20px;
}

.box-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mt-20 {
  margin-top: 20px;
}

.game-list,
.post-list,
.event-list {
  min-height: 200px;
}
</style>