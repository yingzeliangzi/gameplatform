import request from '@/utils/request'

const commentApi = {
    // 获取帖子评论
    getPostComments(postId, params) {
        return request({
            url: `/comments/post/${postId}`,
            method: 'get',
            params
        });
    },

    // 获取评论回复
    getCommentReplies(commentId) {
        return request({
            url: `/comments/${commentId}/replies`,
            method: 'get'
        });
    },

    // 创建评论
    createComment(data) {
        return request({
            url: '/comments',
            method: 'post',
            data
        });
    },

    // 更新评论
    updateComment(commentId, content) {
        return request({
            url: `/comments/${commentId}`,
            method: 'put',
            data: { content }
        });
    },

    // 删除评论
    deleteComment(commentId) {
        return request({
            url: `/comments/${commentId}`,
            method: 'delete'
        });
    },

    // 点赞评论
    likeComment(commentId) {
        return request({
            url: `/comments/${commentId}/like`,
            method: 'post'
        });
    },

    // 取消点赞
    unlikeComment(commentId) {
        return request({
            url: `/comments/${commentId}/like`,
            method: 'delete'
        });
    },

    // 举报评论
    reportComment(commentId, reason) {
        return request({
            url: `/comments/${commentId}/report`,
            method: 'post',
            data: { reason }
        });
    },

    // 获取用户评论
    getUserComments(userId, params) {
        return request({
            url: `/comments/user/${userId}`,
            method: 'get',
            params
        });
    }
};

export default commentApi;