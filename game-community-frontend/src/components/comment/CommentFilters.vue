<template>
  <div class="comment-filters">
    <el-radio-group
        v-model="currentSort"
        @change="handleSortChange"
    >
      <el-radio-button label="newest">最新</el-radio-button>
      <el-radio-button label="hottest">最热</el-radio-button>
    </el-radio-group>

    <div class="filter-actions">
      <el-checkbox
          v-model="onlyShowAuthor"
          @change="handleFilterChange"
      >只看楼主</el-checkbox>

      <el-select
          v-if="isAdmin"
          v-model="reportStatus"
          placeholder="举报状态"
          clearable
          @change="handleFilterChange"
      >
        <el-option label="全部" value=""></el-option>
        <el-option label="已举报" value="reported"></el-option>
        <el-option label="未举报" value="normal"></el-option>
      </el-select>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';

export default {
  name: 'CommentFilters',

  data() {
    return {
      currentSort: 'newest',
      onlyShowAuthor: false,
      reportStatus: ''
    };
  },

  computed: {
    ...mapGetters(['isAdmin']),

    filters() {
      return {
        sort: this.currentSort,
        onlyShowAuthor: this.onlyShowAuthor,
        reportStatus: this.reportStatus
      };
    }
  },

  methods: {
    handleSortChange() {
      this.$emit('filter-change', this.filters);
    },

    handleFilterChange() {
      this.$emit('filter-change', this.filters);
    },

    reset() {
      this.currentSort = 'newest';
      this.onlyShowAuthor = false;
      this.reportStatus = '';
      this.$emit('filter-change', this.filters);
    }
  }
};
</script>

<style lang="scss" scoped>
.comment-filters {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 4px;

  .filter-actions {
    display: flex;
    align-items: center;
    gap: 15px;
  }
}
</style>