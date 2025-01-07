import request from '@/utils/request'

/**
 * 更新用户资料
 * @param {Object} data - 用户资料
 */
export function updateProfile(data) {
    return request({
        url: '/users/profile',
        method: 'put',
        data
    })
}

/**
 * 上传头像
 * @param {FormData} data - 包含头像文件的FormData
 */
export function uploadAvatar(data) {
    return request({
        url: '/users/avatar',
        method: 'post',
        headers: {
            'Content-Type': 'multipart/form-data'
        },
        data
    })
}

/**
 * 获取用户资料
 * @param {string} userId - 用户ID
 */
export function getUserProfile(userId) {
    return request({
        url: `/users/${userId}/profile`,
        method: 'get'
    })
}

/**
 * 关注用户
 * @param {string} userId - 要关注的用户ID
 */
export function followUser(userId) {
    return request({
        url: `/users/${userId}/follow`,
        method: 'post'
    })
}

/**
 * 取消关注
 * @param {string} userId - 要取消关注的用户ID
 */
export function unfollowUser(userId) {
    return request({
        url: `/users/${userId}/follow`,
        method: 'delete'
    })
}

/**
 * 获取关注者列表
 * @param {string} userId - 用户ID
 * @param {Object} params - 查询参数
 */
export function getFollowers(userId, params) {
    return request({
        url: `/users/${userId}/followers`,
        method: 'get',
        params
    })
}

/**
 * 获取关注列表
 * @param {string} userId - 用户ID
 * @param {Object} params - 查询参数
 */
export function getFollowing(userId, params) {
    return request({
        url: `/users/${userId}/following`,
        method: 'get',
        params
    })
}

/**
 * 获取用户统计信息
 * @param {string} userId - 用户ID
 */
export function getUserStats(userId) {
    return request({
        url: `/users/${userId}/stats`,
        method: 'get'
    })
}

/**
 * 更新用户设置
 * @param {Object} data - 设置信息
 */
export function updateSettings(data) {
    return request({
        url: '/users/settings',
        method: 'put',
        data
    })
}

/**
 * 获取用户设置
 */
export function getSettings() {
    return request({
        url: '/users/settings',
        method: 'get'
    })
}