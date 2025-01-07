import request from '@/utils/request'

/**
 * 获取通知列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.type - 通知类型(system/post/comment/game/event)
 * @param {boolean} params.onlyUnread - 是否只获取未读通知
 */
export function getNotifications(params) {
    return request({
        url: '/notifications',
        method: 'get',
        params
    })
}

/**
 * 获取未读通知数量
 */
export function getUnreadCount() {
    return request({
        url: '/notifications/unread/count',
        method: 'get'
    })
}

/**
 * 标记通知为已读
 * @param {string} notificationId - 通知ID
 */
export function markAsRead(notificationId) {
    return request({
        url: `/notifications/${notificationId}/read`,
        method: 'put'
    })
}

/**
 * 标记所有通知为已读
 * @param {string} type - 通知类型(可选，不传则标记所有类型)
 */
export function markAllAsRead(type) {
    return request({
        url: '/notifications/read/all',
        method: 'put',
        params: { type }
    })
}

/**
 * 删除通知
 * @param {string} notificationId - 通知ID
 */
export function deleteNotification(notificationId) {
    return request({
        url: `/notifications/${notificationId}`,
        method: 'delete'
    })
}

/**
 * 获取通知设置
 */
export function getNotificationSettings() {
    return request({
        url: '/notifications/settings',
        method: 'get'
    })
}

/**
 * 更新通知设置
 * @param {Object} settings - 通知设置
 */
export function updateNotificationSettings(settings) {
    return request({
        url: '/notifications/settings',
        method: 'put',
        data: settings
    })
}