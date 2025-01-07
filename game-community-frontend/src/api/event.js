import request from '@/utils/request'

/**
 * 获取活动列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.type - 活动类型
 * @param {string} params.status - 活动状态(upcoming/ongoing/ended)
 * @param {string} params.gameId - 游戏ID(可选)
 */
export function getEvents(params) {
    return request({
        url: '/events',
        method: 'get',
        params
    })
}

/**
 * 获取活动详情
 * @param {string} eventId - 活动ID
 */
export function getEventDetail(eventId) {
    return request({
        url: `/events/${eventId}`,
        method: 'get'
    })
}

/**
 * 创建活动
 * @param {Object} data - 活动数据
 */
export function createEvent(data) {
    return request({
        url: '/events',
        method: 'post',
        data
    })
}

/**
 * 更新活动
 * @param {string} eventId - 活动ID
 * @param {Object} data - 更新数据
 */
export function updateEvent(eventId, data) {
    return request({
        url: `/events/${eventId}`,
        method: 'put',
        data
    })
}

/**
 * 取消活动
 * @param {string} eventId - 活动ID
 */
export function cancelEvent(eventId) {
    return request({
        url: `/events/${eventId}/cancel`,
        method: 'post'
    })
}

/**
 * 报名活动
 * @param {string} eventId - 活动ID
 * @param {Object} data - 报名信息
 */
export function joinEvent(eventId, data) {
    return request({
        url: `/events/${eventId}/join`,
        method: 'post',
        data
    })
}

/**
 * 取消报名
 * @param {string} eventId - 活动ID
 */
export function leaveEvent(eventId) {
    return request({
        url: `/events/${eventId}/leave`,
        method: 'post'
    })
}

/**
 * 获取活动参与者列表
 * @param {string} eventId - 活动ID
 * @param {Object} params - 查询参数
 */
export function getEventParticipants(eventId, params) {
    return request({
        url: `/events/${eventId}/participants`,
        method: 'get',
        params
    })
}

/**
 * 获取用户参与的活动
 * @param {Object} params - 查询参数
 */
export function getUserEvents(params) {
    return request({
        url: '/events/user',
        method: 'get',
        params
    })
}

/**
 * 提交活动反馈
 * @param {string} eventId - 活动ID
 * @param {Object} data - 反馈数据
 */
export function submitEventFeedback(eventId, data) {
    return request({
        url: `/events/${eventId}/feedback`,
        method: 'post',
        data
    })
}