import request from '@/utils/request'

export function getPosts(params) {
    return request({
        url: '/api/posts',
        method: 'get',
        params
    })
}

export function getPostDetail(id) {
    return request({
        url: `/api/posts/${id}`,
        method: 'get'
    })
}

export function createPost(data) {
    return request({
        url: '/api/posts',
        method: 'post',
        data
    })
}

export function addComment(postId, data) {
    return request({
        url: `/api/posts/${postId}/comments`,
        method: 'post',
        data
    })
}

export function replyComment(commentId, data) {
    return request({
        url: `/api/posts/comments/${commentId}/replies`,
        method: 'post',
        data
    })
}

export function reportContent(data) {
    return request({
        url: '/api/posts/report',
        method: 'post',
        data
    })
}

export function likePost(postId) {
    return request({
        url: `/api/posts/${postId}/like`,
        method: 'post'
    })
}