<template>
  <div class="page-container">
    <div v-if="title || $slots.header" class="page-header">
      <slot name="header">
        <div class="page-title">
          <h2>{{ title }}</h2>
          <div v-if="subtitle" class="page-subtitle">{{ subtitle }}</div>
        </div>
      </slot>
    </div>

    <div class="page-content">
      <slot></slot>
    </div>

    <div v-if="$slots.footer" class="page-footer">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: ''
  },
  subtitle: {
    type: String,
    default: ''
  }
})
</script>

<style lang="scss" scoped>
.page-container {
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  margin-bottom: 20px;
}

.page-header {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;

  .page-title {
    h2 {
      margin: 0;
      font-size: 20px;
      font-weight: 500;
      color: #1f2d3d;
    }

    .page-subtitle {
      margin-top: 8px;
      color: #909399;
      font-size: 14px;
    }
  }
}

.page-content {
  padding: 20px;
}

.page-footer {
  padding: 20px;
  border-top: 1px solid #f0f0f0;
  background-color: #fafafa;
}

// 暗色主题支持
:root[data-theme='dark'] {
  .page-container {
    background-color: #1f1f1f;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
  }

  .page-header {
    border-bottom-color: #2f2f2f;

    .page-title {
      h2 {
        color: #fff;
      }

      .page-subtitle {
        color: #a6a6a6;
      }
    }
  }

  .page-footer {
    border-top-color: #2f2f2f;
    background-color: #1a1a1a;
  }
}
</style>