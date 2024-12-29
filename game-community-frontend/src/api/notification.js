import request from '@/utils/request'

export function getNotifications(params) {
    return request({
        url: '/api/notifications',
        method: 'get',
        params
    })
}

export function getUnreadCount() {
    return request({
        url: '/api/notifications/unread',
        method: 'get'
    })
}

export function markAsRead(id) {
    return request({
        url: `/api/notifications/${id}/read`,
        method: 'patch'
    })
}

export function markAllAsRead() {
    return request({
        url: '/api/notifications/read-all',
        method: 'patch'
    })
}

export function deleteNotification(id) {
    return request({
        url: `/api/notifications/${id}`,
        method: 'delete'
    })
}