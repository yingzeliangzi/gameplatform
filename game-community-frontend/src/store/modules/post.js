import request from '@/utils/request'

// 获取帖子列表
export function getPosts(params) {
    return request({
        url: '/api/posts',
        method: 'get',
        params
    })
}

// 获取帖子详情
export function getPostDetail(id) {
    return request({
        url: `/api/posts/${id}`,
        method: 'get'
    })
}

// 创建帖子
export function createPost(data) {
    return request({
        url: '/api/posts',
        method: 'post',
        data
    })
}

// 更新帖子
export function updatePost(id, data) {
    return request({
        url: `/api/posts/${id}`,
        method: 'put',
        data
    })
}

// 删除帖子
export function deletePost(id) {
    return request({
        url: `/api/posts/${id}`,
        method: 'delete'
    })
}

// 点赞帖子
export function likePost(id) {
    return request({
        url: `/api/posts/${id}/like`,
        method: 'post'
    })
}

// 取消点赞
export function unlikePost(id) {
    return request({
        url: `/api/posts/${id}/like`,
        method: 'delete'
    })
}

// 添加评论
export function addComment(postId, data) {
    return request({
        url: `/api/posts/${postId}/comments`,
        method: 'post',
        data
    })
}

// 回复评论
export function replyComment(commentId, data) {
    return request({
        url: `/api/posts/comments/${commentId}/replies`,
        method: 'post',
        data
    })
}

// 获取评论列表
export function getComments(postId, params) {
    return request({
        url: `/api/posts/${postId}/comments`,
        method: 'get',
        params
    })
}

// 删除评论
export function deleteComment(commentId) {
    return request({
        url: `/api/posts/comments/${commentId}`,
        method: 'delete'
    })
}

// 举报帖子
export function reportPost(id, data) {
    return request({
        url: `/api/posts/${id}/report`,
        method: 'post',
        data
    })
}

// 获取用户的帖子
export function getUserPosts(userId, params) {
    return request({
        url: `/api/users/${userId}/posts`,
        method: 'get',
        params
    })
}

// 收藏帖子
export function collectPost(id) {
    return request({
        url: `/api/posts/${id}/collect`,
        method: 'post'
    })
}

// 取消收藏
export function uncollectPost(id) {
    return request({
        url: `/api/posts/${id}/collect`,
        method: 'delete'
    })
}

// 获取收藏的帖子列表
export function getCollectedPosts(params) {
    return request({
        url: '/api/posts/collected',
        method: 'get',
        params
    })
}