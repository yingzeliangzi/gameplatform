import request from '@/utils/request'

/**
 * 获取评论列表
 * @param {string} targetId - 目标ID(帖子/游戏)
 * @param {string} targetType - 目标类型(post/game)
 * @param {Object} params - 查询参数
 */
export function getComments(targetType, targetId, params) {
    return request({
        url: `/comments/${targetType}/${targetId}`,
        method: 'get',
        params
    })
}

/**
 * 添加评论
 * @param {Object} data - 评论数据
 * @param {string} data.targetId - 目标ID
 * @param {string} data.targetType - 目标类型
 * @param {string} data.content - 评论内容
 * @param {string} data.parentId - 父评论ID(可选，用于回复)
 */
export function addComment(data) {
    return request({
        url: '/comments',
        method: 'post',
        data
    })
}

/**
 * 更新评论
 * @param {string} commentId - 评论ID
 * @param {Object} data - 更新数据
 */
export function updateComment(commentId, data) {
    return request({
        url: `/comments/${commentId}`,
        method: 'put',
        data
    })
}

/**
 * 删除评论
 * @param {string} commentId - 评论ID
 */
export function deleteComment(commentId) {
    return request({
        url: `/comments/${commentId}`,
        method: 'delete'
    })
}

/**
 * 点赞评论
 * @param {string} commentId - 评论ID
 */
export function likeComment(commentId) {
    return request({
        url: `/comments/${commentId}/like`,
        method: 'post'
    })
}

/**
 * 取消点赞
 * @param {string} commentId - 评论ID
 */
export function unlikeComment(commentId) {
    return request({
        url: `/comments/${commentId}/like`,
        method: 'delete'
    })
}

/**
 * 举报评论
 * @param {string} commentId - 评论ID
 * @param {Object} data - 举报信息
 */
export function reportComment(commentId, data) {
    return request({
        url: `/comments/${commentId}/report`,
        method: 'post',
        data
    })
}

/**
 * 获取评论回复列表
 * @param {string} commentId - 评论ID
 * @param {Object} params - 查询参数
 */
export function getReplies(commentId, params) {
    return request({
        url: `/comments/${commentId}/replies`,
        method: 'get',
        params
    })
}

/**
 * 获取用户的评论列表
 * @param {string} userId - 用户ID
 * @param {Object} params - 查询参数
 */
export function getUserComments(userId, params) {
    return request({
        url: `/users/${userId}/comments`,
        method: 'get',
        params
    })
}