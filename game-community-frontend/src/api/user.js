import request from '@/utils/request'

// 获取用户信息
export function getUserInfo(userId) {
    return request({
        url: `/api/users/${userId}`,
        method: 'get'
    })
}

// 更新用户信息
export function updateUserInfo(data) {
    return request({
        url: '/api/users/profile',
        method: 'put',
        data
    })
}

// 更新密码
export function updatePassword(data) {
    return request({
        url: '/api/users/password',
        method: 'put',
        data
    })
}

// 上传头像
export function uploadAvatar(data) {
    return request({
        url: '/api/users/avatar',
        method: 'post',
        headers: {
            'Content-Type': 'multipart/form-data'
        },
        data
    })
}

// 获取用户游戏库统计
export function getUserGameStats() {
    return request({
        url: '/api/users/game-stats',
        method: 'get'
    })
}

// 获取用户帖子统计
export function getUserPostStats() {
    return request({
        url: '/api/users/post-stats',
        method: 'get'
    })
}

// 实名认证
export function submitVerification(data) {
    return request({
        url: '/api/users/verify',
        method: 'post',
        data
    })
}

// 获取实名认证状态
export function getVerificationStatus() {
    return request({
        url: '/api/users/verify/status',
        method: 'get'
    })
}

// 获取用户安全设置
export function getSecuritySettings() {
    return request({
        url: '/api/users/security',
        method: 'get'
    })
}

// 更新安全设置
export function updateSecuritySettings(data) {
    return request({
        url: '/api/users/security',
        method: 'put',
        data
    })
}

// 获取用户隐私设置
export function getPrivacySettings() {
    return request({
        url: '/api/users/privacy',
        method: 'get'
    })
}

// 更新隐私设置
export function updatePrivacySettings(data) {
    return request({
        url: '/api/users/privacy',
        method: 'put',
        data
    })
}

// 绑定手机号
export function bindPhone(data) {
    return request({
        url: '/api/users/bind/phone',
        method: 'post',
        data
    })
}

// 绑定邮箱
export function bindEmail(data) {
    return request({
        url: '/api/users/bind/email',
        method: 'post',
        data
    })
}

// 获取用户粉丝列表
export function getUserFollowers(userId, params) {
    return request({
        url: `/api/users/${userId}/followers`,
        method: 'get',
        params
    })
}

// 获取用户关注列表
export function getUserFollowing(userId, params) {
    return request({
        url: `/api/users/${userId}/following`,
        method: 'get',
        params
    })
}

// 关注用户
export function followUser(userId) {
    return request({
        url: `/api/users/${userId}/follow`,
        method: 'post'
    })
}

// 取消关注
export function unfollowUser(userId) {
    return request({
        url: `/api/users/${userId}/follow`,
        method: 'delete'
    })
}

// 获取用户登录历史
export function getLoginHistory(params) {
    return request({
        url: '/api/users/login-history',
        method: 'get',
        params
    })
}

// 获取用户设备列表
export function getDevices() {
    return request({
        url: '/api/users/devices',
        method: 'get'
    })
}

// 删除设备
export function removeDevice(deviceId) {
    return request({
        url: `/api/users/devices/${deviceId}`,
        method: 'delete'
    })
}

// 发送验证码
export function sendVerificationCode(phone) {
    return request({
        url: '/api/users/verify-code',
        method: 'post',
        data: { phone }
    })
}