<template>
  <div class="base-list">
    <!-- 列表头部工具栏 -->
    <div v-if="$slots.toolbar" class="list-toolbar">
      <slot name="toolbar"></slot>
    </div>

    <!-- 列表内容 -->
    <el-scrollbar>
      <div v-if="loading" class="list-loading">
        <el-skeleton :rows="5" animated />
      </div>

      <div v-else-if="!items.length" class="list-empty">
        <el-empty :description="emptyText" />
      </div>

      <div v-else class="list-content">
        <template v-for="item in items" :key="item.id">
          <slot name="item" :item="item">
            {{ item }}
          </slot>
        </template>
      </div>
    </el-scrollbar>

    <!-- 分页器 -->
    <div v-if="showPagination" class="list-pagination">
      <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </div>

    <!-- 加载更多按钮 -->
    <div v-if="loadMore && hasMore" class="list-load-more">
      <el-button
          :loading="loading"
          type="primary"
          text
          @click="handleLoadMore"
      >
        加载更多
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  // 列表数据
  items: {
    type: Array,
    default: () => []
  },
  // 总数
  total: {
    type: Number,
    default: 0
  },
  // 是否显示分页器
  showPagination: {
    type: Boolean,
    default: true
  },
  // 是否使用加载更多模式
  loadMore: {
    type: Boolean,
    default: false
  },
  // 加载状态
  loading: {
    type: Boolean,
    default: false
  },
  // 空状态文本
  emptyText: {
    type: String,
    default: '暂无数据'
  }
})

const emit = defineEmits(['update:page', 'update:pageSize', 'load-more'])

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)

// 是否还有更多数据
const hasMore = computed(() => {
  return props.items.length < props.total
})

// 监听分页变化
watch(currentPage, (newPage) => {
  emit('update:page', newPage)
})

watch(pageSize, (newSize) => {
  emit('update:pageSize', newSize)
})

// 处理分页变化
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}

// 处理加载更多
const handleLoadMore = () => {
  emit('load-more')
}
</script>

<style lang="scss" scoped>
.base-list {
  height: 100%;
  display: flex;
  flex-direction: column;

  .list-toolbar {
    padding: 16px;
    border-bottom: 1px solid var(--el-border-color-light);
  }

  .list-content {
    flex: 1;
    padding: 16px;
  }

  .list-loading,
  .list-empty {
    padding: 32px 16px;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .list-pagination {
    padding: 16px;
    display: flex;
    justify-content: flex-end;
    border-top: 1px solid var(--el-border-color-light);
  }

  .list-load-more {
    padding: 16px;
    text-align: center;
  }
}
</style>