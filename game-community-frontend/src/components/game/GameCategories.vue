<template>
  <div class="game-categories">
    <!-- 分类展示方式切换 -->
    <div class="view-switch" v-if="showViewSwitch">
      <el-radio-group v-model="viewType" size="small">
        <el-radio-button label="grid">网格</el-radio-button>
        <el-radio-button label="list">列表</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 网格视图 -->
    <div v-if="viewType === 'grid'" class="category-grid">
      <el-row :gutter="20">
        <el-col
            v-for="category in categories"
            :key="category.id"
            :xs="12"
            :sm="8"
            :md="6"
            :lg="4"
        >
          <el-card
              :body-style="{ padding: '0px' }"
              :class="{ active: selectedCategories.includes(category.id) }"
              @click="handleCategoryClick(category)"
          >
            <el-image
                :src="category.icon"
                fit="cover"
                class="category-icon"
            >
              <template #error>
                <div class="image-slot">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="category-info">
              <span class="category-name">{{ category.name }}</span>
              <span class="category-count">{{ category.count }}款游戏</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 列表视图 -->
    <div v-else class="category-list">
      <el-checkbox-group
          v-model="selectedCategories"
          @change="handleSelectionChange"
      >
        <el-checkbox
            v-for="category in categories"
            :key="category.id"
            :label="category.id"
        >
          <div class="category-item">
            <el-image
                :src="category.icon"
                :size="24"
                fit="cover"
                class="category-icon"
            />
            <span class="category-name">{{ category.name }}</span>
            <span class="category-count">({{ category.count }})</span>
          </div>
        </el-checkbox>
      </el-checkbox-group>
    </div>

    <!-- 已选分类展示 -->
    <div v-if="showSelected && selectedCategories.length" class="selected-categories">
      <span class="label">已选分类：</span>
      <el-tag
          v-for="id in selectedCategories"
          :key="id"
          closable
          @close="handleRemoveCategory(id)"
      >
        {{ getCategoryName(id) }}
      </el-tag>
      <el-button
          type="primary"
          link
          @click="clearSelection"
      >
        清空
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Picture } from '@element-plus/icons-vue'

const props = defineProps({
  // 分类数据
  categories: {
    type: Array,
    required: true
  },
  // 是否显示视图切换
  showViewSwitch: {
    type: Boolean,
    default: true
  },
  // 是否显示已选分类
  showSelected: {
    type: Boolean,
    default: true
  },
  // 是否支持多选
  multiple: {
    type: Boolean,
    default: true
  },
  // 默认选中的分类
  defaultSelected: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:selected', 'change'])

// 视图类型
const viewType = ref('grid')

// 选中的分类
const selectedCategories = ref([...props.defaultSelected])

// 监听默认选中值变化
watch(() => props.defaultSelected, (newVal) => {
  selectedCategories.value = [...newVal]
})

// 处理分类点击
const handleCategoryClick = (category) => {
  if (props.multiple) {
    const index = selectedCategories.value.indexOf(category.id)
    if (index > -1) {
      selectedCategories.value.splice(index, 1)
    } else {
      selectedCategories.value.push(category.id)
    }
  } else {
    selectedCategories.value = [category.id]
  }
  emitChange()
}

// 处理分类选择变化
const handleSelectionChange = (value) => {
  if (!props.multiple && value.length > 1) {
    selectedCategories.value = [value[value.length - 1]]
  }
  emitChange()
}

// 移除选中分类
const handleRemoveCategory = (categoryId) => {
  const index = selectedCategories.value.indexOf(categoryId)
  if (index > -1) {
    selectedCategories.value.splice(index, 1)
    emitChange()
  }
}

// 清空选择
const clearSelection = () => {
  selectedCategories.value = []
  emitChange()
}

// 获取分类名称
const getCategoryName = (categoryId) => {
  const category = props.categories.find(c => c.id === categoryId)
  return category ? category.name : ''
}

// 发出变更事件
const emitChange = () => {
  emit('update:selected', selectedCategories.value)
  emit('change', selectedCategories.value)
}
</script>

<style lang="scss" scoped>
.game-categories {
  .view-switch {
    margin-bottom: 20px;
    text-align: right;
  }

  .category-grid {
    .el-card {
      margin-bottom: 20px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-5px);
      }

      &.active {
        border-color: var(--el-color-primary);
        box-shadow: 0 2px 12px 0 rgba(var(--el-color-primary-rgb), 0.1);
      }
    }

    .category-icon {
      width: 100%;
      height: 120px;
    }

    .category-info {
      padding: 14px;
      text-align: center;

      .category-name {
        display: block;
        font-size: 16px;
        margin-bottom: 5px;
      }

      .category-count {
        color: #909399;
        font-size: 12px;
      }
    }
  }

  .category-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 10px;

    .el-checkbox {
      margin-right: 0;
      margin-bottom: 10px;
    }

    .category-item {
      display: flex;
      align-items: center;
      gap: 10px;

      .category-icon {
        width: 24px;
        height: 24px;
      }

      .category-count {
        color: #909399;
        font-size: 12px;
      }
    }
  }

  .selected-categories {
    margin-top: 20px;
    padding: 10px;
    background-color: #f5f7fa;
    border-radius: 4px;
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;

    .label {
      color: #606266;
    }
  }

  .image-slot {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100%;
    background: #f5f7fa;
    color: #909399;
  }
}
</style>