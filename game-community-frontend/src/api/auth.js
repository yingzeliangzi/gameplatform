import request from '@/utils/request'

/**
 * 登录
 * @param {Object} data - 登录信息
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 */
export function login(data) {
    return request({
        url: '/auth/login',
        method: 'post',
        data
    })
}

/**
 * 注册
 * @param {Object} data - 注册信息
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @param {string} data.email - 邮箱
 * @param {string} data.nickname - 昵称
 */
export function register(data) {
    return request({
        url: '/auth/register',
        method: 'post',
        data
    })
}

/**
 * 注销
 */
export function logout() {
    return request({
        url: '/auth/logout',
        method: 'post'
    })
}

/**
 * 获取当前用户信息
 */
export function getCurrentUser() {
    return request({
        url: '/auth/me',
        method: 'get'
    })
}

/**
 * 发送验证码
 * @param {string} email - 邮箱
 */
export function sendVerificationCode(email) {
    return request({
        url: '/auth/verification-code',
        method: 'post',
        params: { email }
    })
}

/**
 * 重置密码
 * @param {Object} data - 重置信息
 * @param {string} data.email - 邮箱
 * @param {string} data.code - 验证码
 * @param {string} data.newPassword - 新密码
 */
export function resetPassword(data) {
    return request({
        url: '/auth/reset-password',
        method: 'post',
        data
    })
}

/**
 * 更新密码
 * @param {Object} data - 密码信息
 * @param {string} data.oldPassword - 旧密码
 * @param {string} data.newPassword - 新密码
 */
export function updatePassword(data) {
    return request({
        url: '/auth/password',
        method: 'put',
        data
    })
}

/**
 * 刷新Token
 */
export function refreshToken() {
    return request({
        url: '/auth/refresh-token',
        method: 'post'
    })
}