<template>
  <div class="post-detail">
    <!-- 帖子内容 -->
    <div class="post-content">
      <div class="post-header">
        <h1 class="title">{{ post.title }}</h1>
        <div class="meta">
          <el-avatar :src="post.author.avatar" :size="40"></el-avatar>
          <div class="info">
            <span class="author">{{ post.author.nickname }}</span>
            <span class="time">发布于 {{ formatTime(post.createdAt) }}</span>
          </div>
          <div class="actions" v-if="canManagePost">
            <el-dropdown trigger="click" @command="handleCommand">
              <el-button type="text">
                <i class="el-icon-more"></i>
              </el-button>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="edit">编辑</el-dropdown-item>
                <el-dropdown-item command="delete">删除</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </div>
      </div>

      <div class="content" v-html="post.content"></div>

      <div class="post-footer">
        <div class="statistics">
          <span class="views">
            <i class="el-icon-view"></i>
            {{ post.viewCount }} 次浏览
          </span>
          <span class="comments">
            <i class="el-icon-chat-dot-round"></i>
            {{ post.commentCount }} 条评论
          </span>
        </div>
        <div class="actions">
          <el-button
              type="text"
              :class="{ 'is-liked': post.isLiked }"
              @click="handleLike"
          >
            <i class="el-icon-star-off"></i>
            {{ post.likeCount }}
          </el-button>
          <el-button type="text" @click="handleShare">
            <i class="el-icon-share"></i>
            分享
          </el-button>
          <el-button type="text" @click="handleReport" v-if="!isAuthor">
            <i class="el-icon-warning-outline"></i>
            举报
          </el-button>
        </div>
      </div>
    </div>

    <!-- 评论区 -->
    <div class="comment-section">
      <div class="section-header">
        <h2>全部评论 ({{ commentTotal }})</h2>
        <CommentFilters @filter-change="handleFilterChange" />
      </div>

      <CommentEditor
          v-if="isLoggedIn"
          :submitting="commentSubmitting"
          @submit="handleCommentSubmit"
      />
      <el-alert
          v-else
          title="登录后才能发表评论"
          type="info"
          show-icon
      >
        <template slot="title">
          登录后才能发表评论
          <el-button
              type="text"
              @click="$router.push({name: 'Login', query: {redirect: $route.fullPath}})"
          >
            立即登录
          </el-button>
        </template>
      </el-alert>

      <CommentList
          :comments="comments"
          :loading="commentLoading"
          :has-more="hasMoreComments"
          @load-more="loadMoreComments"
      />
    </div>

    <!-- 分享对话框 -->
    <el-dialog
        title="分享"
        :visible.sync="shareDialogVisible"
        width="400px"
        center
    >
      <div class="share-content">
        <div class="share-link">
          <el-input
              v-model="shareUrl"
              readonly
              ref="shareInput"
          >
            <el-button
                slot="append"
                @click="copyShareUrl"
            >复制</el-button>
          </el-input>
        </div>
        <div class="share-platforms">
          <div
              v-for="platform in sharePlatforms"
              :key="platform.name"
              class="platform-item"
              @click="shareToPlateform(platform)"
          >
            <i :class="platform.icon"></i>
            <span>{{ platform.name }}</span>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 举报对话框 -->
    <el-dialog
        title="举报内容"
        :visible.sync="reportDialogVisible"
        width="500px"
    >
      <el-form
          :model="reportForm"
          :rules="reportRules"
          ref="reportForm"
          label-width="80px"
      >
        <el-form-item label="举报理由" prop="reason">
          <el-select v-model="reportForm.reason" placeholder="请选择举报理由">
            <el-option
                v-for="reason in reportReasons"
                :key="reason.value"
                :label="reason.label"
                :value="reason.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="补充说明" prop="description">
          <el-input
              type="textarea"
              v-model="reportForm.description"
              :rows="4"
              placeholder="请详细说明举报原因（选填）"
          />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="reportDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReport" :loading="reportSubmitting">
          提交举报
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex';
import { formatTime } from '@/utils/time';
import CommentList from '@/components/comments/CommentList.vue';
import CommentEditor from '@/components/comments/CommentEditor.vue';
import CommentFilters from '@/components/comments/CommentFilters.vue';

export default {
  name: 'PostDetail',

  components: {
    CommentList,
    CommentEditor,
    CommentFilters
  },

  props: {
    id: {
      type: [String, Number],
      required: true
    }
  },

  data() {
    return {
      shareDialogVisible: false,
      reportDialogVisible: false,
      reportSubmitting: false,
      reportForm: {
        reason: '',
        description: ''
      },
      reportRules: {
        reason: [
          { required: true, message: '请选择举报理由', trigger: 'change' }
        ]
      },
      reportReasons: [
        { label: '垃圾广告', value: 'spam' },
        { label: '不当内容', value: 'inappropriate' },
        { label: '侵犯权益', value: 'infringement' },
        { label: '其他原因', value: 'other' }
      ],
      sharePlatforms: [
        { name: '微信', icon: 'el-icon-wechat' },
        { name: 'QQ', icon: 'el-icon-qq' },
        { name: '微博', icon: 'el-icon-weibo' }
      ]
    };
  },

  computed: {
    ...mapGetters({
      post: 'post/currentPost',
      isLoggedIn: 'user/isLoggedIn',
      currentUser: 'user/currentUser',
      comments: 'comment/comments',
      commentTotal: 'comment/total',
      commentLoading: 'comment/loading',
      commentSubmitting: 'comment/submitting',
      hasMoreComments: 'comment/hasMore'
    }),

    isAuthor() {
      return this.post?.author?.id === this.currentUser?.id;
    },

    canManagePost() {
      return this.isAuthor || (this.currentUser?.roles || []).includes('ADMIN');
    },

    shareUrl() {
      return window.location.href;
    }
  },

  created() {
    this.fetchPostDetail(this.id);
    this.fetchComments({
      postId: this.id,
      reset: true
    });
  },

  methods: {
    formatTime,

    ...mapActions({
      fetchPostDetail: 'post/fetchDetail',
      fetchComments: 'comment/fetchComments',
      createComment: 'comment/createComment',
      likePost: 'post/likePost',
      unlikePost: 'post/unlikePost',
      reportPost: 'post/reportPost',
      deletePost: 'post/deletePost'
    }),

    handleCommand(command) {
      switch (command) {
        case 'edit':
          this.$router.push(`/posts/${this.id}/edit`);
          break;
        case 'delete':
          this.handleDelete();
          break;
      }
    },

    async handleDelete() {
      try {
        await this.$confirm('确定要删除这篇帖子吗？', '提示', {
          type: 'warning'
        });
        await this.deletePost(this.id);
        this.$message.success('删除成功');
        this.$router.push('/posts');
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败');
        }
      }
    },

    async handleLike() {
      if (!this.isLoggedIn) {
        this.$message.warning('请先登录');
        return;
      }

      try {
        if (this.post.isLiked) {
          await this.unlikePost(this.id);
        } else {
          await this.likePost(this.id);
        }
      } catch (error) {
        this.$message.error('操作失败');
      }
    },

    handleShare() {
      this.shareDialogVisible = true;
    },

    copyShareUrl() {
      this.$refs.shareInput.$el.querySelector('input').select();
      document.execCommand('copy');
      this.$message.success('链接已复制');
    },

    shareToPlateform(platform) {
      // 实现各平台的分享逻辑
      console.log('Share to', platform.name);
      this.shareDialogVisible = false;
    },

    handleReport() {
      if (!this.isLoggedIn) {
        this.$message.warning('请先登录');
        return;
      }
      this.reportDialogVisible = true;
    },

    async submitReport() {
      try {
        await this.$refs.reportForm.validate();
        this.reportSubmitting = true;
        await this.reportPost({
          postId: this.id,
          reason: this.reportForm.reason,
          description: this.reportForm.description
        });

        this.$message.success('举报已提交');
        this.reportDialogVisible = false;
        this.reportForm.reason = '';
        this.reportForm.description = '';
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('举报提交失败');
        }
      } finally {
        this.reportSubmitting = false;
      }
    },

    async handleCommentSubmit(content) {
      try {
        await this.createComment({
          postId: this.id,
          content
        });
        this.$message.success('评论发表成功');
      } catch (error) {
        this.$message.error('评论发表失败');
      }
    },

    handleFilterChange(filters) {
      this.fetchComments({
        postId: this.id,
        reset: true,
        filters
      });
    },

    loadMoreComments() {
      if (!this.commentLoading && this.hasMoreComments) {
        this.fetchComments({
          postId: this.id
        });
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.post-detail {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

  .post-content {
    .post-header {
      margin-bottom: 20px;

      .title {
        font-size: 24px;
        font-weight: bold;
        margin-bottom: 15px;
      }

      .meta {
        display: flex;
        align-items: center;
        gap: 10px;

        .info {
          display: flex;
          flex-direction: column;

          .author {
            font-size: 16px;
            font-weight: 500;
          }

          .time {
            font-size: 14px;
            color: #999;
          }
        }

        .actions {
          margin-left: auto;
        }
      }
    }

    .content {
      font-size: 16px;
      line-height: 1.8;
      margin-bottom: 30px;
    }

    .post-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 20px;
      border-top: 1px solid #eee;

      .statistics {
        display: flex;
        gap: 20px;
        color: #666;

        span {
          display: flex;
          align-items: center;
          gap: 5px;
        }
      }

      .actions {
        display: flex;
        gap: 15px;

        .el-button {
          font-size: 16px;

          &.is-liked {
            color: #409EFF;
          }

          i {
            margin-right: 5px;
          }
        }
      }
    }
  }

  .comment-section {
    margin-top: 30px;

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;

      h2 {
        font-size: 18px;
        font-weight: bold;
      }
    }
  }
}

.share-content {
  .share-link {
    margin-bottom: 20px;
  }

  .share-platforms {
    display: flex;
    justify-content: space-around;

    .platform-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 5px;
      cursor: pointer;
      padding: 10px;
      border-radius: 4px;
      transition: background-color 0.3s;

      &:hover {
        background-color: #f5f7fa;
      }

      i {
        font-size: 24px;
      }

      span {
        font-size: 14px;
      }
    }
  }
}
</style>