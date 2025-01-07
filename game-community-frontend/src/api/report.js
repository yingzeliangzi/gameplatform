import request from '@/utils/request'

/**
 * 提交举报
 * @param {Object} data - 举报数据
 * @param {string} data.targetType - 举报目标类型(user/post/comment/game)
 * @param {string} data.targetId - 举报目标ID
 * @param {string} data.reason - 举报原因
 * @param {string} data.description - 详细描述
 * @param {Array} data.evidence - 证据（图片URL等）
 */
export function submitReport(data) {
    return request({
        url: '/reports',
        method: 'post',
        data
    })
}

/**
 * 获取举报列表（管理员用）
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.status - 举报状态(pending/processed/rejected)
 * @param {string} params.targetType - 目标类型
 */
export function getReports(params) {
    return request({
        url: '/reports',
        method: 'get',
        params
    })
}

/**
 * 获取举报详情
 * @param {string} reportId - 举报ID
 */
export function getReportDetail(reportId) {
    return request({
        url: `/reports/${reportId}`,
        method: 'get'
    })
}

/**
 * 处理举报
 * @param {string} reportId - 举报ID
 * @param {Object} data - 处理数据
 * @param {string} data.action - 处理操作(accept/reject)
 * @param {string} data.reason - 处理原因
 */
export function handleReport(reportId, data) {
    return request({
        url: `/reports/${reportId}/handle`,
        method: 'post',
        data
    })
}

/**
 * 批量处理举报
 * @param {Object} data - 批量处理数据
 * @param {Array} data.reportIds - 举报ID列表
 * @param {string} data.action - 处理操作
 * @param {string} data.reason - 处理原因
 */
export function batchHandleReports(data) {
    return request({
        url: '/reports/batch',
        method: 'post',
        data
    })
}

/**
 * 获取举报类型列表
 */
export function getReportTypes() {
    return request({
        url: '/reports/types',
        method: 'get'
    })
}

/**
 * 获取举报统计信息
 * @param {Object} params - 查询参数
 */
export function getReportStats(params) {
    return request({
        url: '/reports/stats',
        method: 'get',
        params
    })
}

/**
 * 添加举报备注
 * @param {string} reportId - 举报ID
 * @param {Object} data - 备注数据
 */
export function addReportNote(reportId, data) {
    return request({
        url: `/reports/${reportId}/notes`,
        method: 'post',
        data
    })
}

/**
 * 获取举报处理历史
 * @param {string} reportId - 举报ID
 */
export function getReportHistory(reportId) {
    return request({
        url: `/reports/${reportId}/history`,
        method: 'get'
    })
}