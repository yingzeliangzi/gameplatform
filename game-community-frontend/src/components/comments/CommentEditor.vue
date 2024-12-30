<template>
  <div class="comment-editor">
    <div class="editor-header">
      <span class="title">{{ isReply ? '回复评论' : '发表评论' }}</span>
      <el-button
          v-if="isReply"
          type="text"
          @click="$emit('cancel')"
      >取消</el-button>
    </div>

    <el-input
        v-model="content"
        type="textarea"
        :rows="rows"
        :placeholder="placeholder"
        :maxlength="maxLength"
        show-word-limit
    />

    <div class="editor-footer">
      <slot name="actions"></slot>
      <el-button
          type="primary"
          :loading="submitting"
          @click="handleSubmit"
      >{{ submitText }}</el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CommentEditor',

  props: {
    isReply: {
      type: Boolean,
      default: false
    },
    initialContent: {
      type: String,
      default: ''
    },
    submitting: {
      type: Boolean,
      default: false
    },
    maxLength: {
      type: Number,
      default: 1000
    }
  },

  data() {
    return {
      content: this.initialContent
    };
  },

  computed: {
    rows() {
      return this.isReply ? 3 : 4;
    },
    placeholder() {
      return this.isReply ? '写下你的回复...' : '写下你的评论...';
    },
    submitText() {
      return this.isReply ? '回复' : '发表';
    }
  },

  watch: {
    initialContent(val) {
      this.content = val;
    }
  },

  methods: {
    handleSubmit() {
      if (!this.content.trim()) {
        this.$message.warning('请输入内容');
        return;
      }
      this.$emit('submit', this.content);
    },

    clear() {
      this.content = '';
    }
  }
};
</script>

<style lang="scss" scoped>
.comment-editor {
  .editor-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;

    .title {
      font-weight: 500;
      font-size: 16px;
    }
  }

  .editor-footer {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    margin-top: 10px;
    gap: 10px;
  }
}
</style>