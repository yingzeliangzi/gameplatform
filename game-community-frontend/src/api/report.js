import request from '@/utils/request'

// 获取举报列表
export function getReports(params) {
    return request({
        url: '/api/reports',
        method: 'get',
        params
    })
}

// 提交举报
export function submitReport(data) {
    return request({
        url: '/api/reports',
        method: 'post',
        data
    })
}

// 处理举报
export function handleReport(reportId, action) {
    return request({
        url: `/api/reports/${reportId}/${action}`,
        method: 'put'
    })
}

// 获取举报详情
export function getReportDetail(reportId) {
    return request({
        url: `/api/reports/${reportId}`,
        method: 'get'
    })
}

// 删除举报
export function deleteReport(reportId) {
    return request({
        url: `/api/reports/${reportId}`,
        method: 'delete'
    })
}

// 获取举报类型列表
export function getReportTypes() {
    return request({
        url: '/api/reports/types',
        method: 'get'
    })
}

// 获取举报统计数据
export function getReportStats() {
    return request({
        url: '/api/reports/stats',
        method: 'get'
    })
}

// 批量处理举报
export function batchHandleReports(data) {
    return request({
        url: '/api/reports/batch',
        method: 'put',
        data
    })
}