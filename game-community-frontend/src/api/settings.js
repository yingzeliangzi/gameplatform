import request from '@/utils/request'

export function getSettings() {
    return request({
        url: '/api/settings',
        method: 'get'
    })
}

export function updateSettings(data) {
    return request({
        url: '/api/settings',
        method: 'put',
        data
    })
}

export function bindAccount(type) {
    return request({
        url: `/api/settings/binding/${type}`,
        method: 'post'
    })
}

export function unbindAccount(type) {
    return request({
        url: `/api/settings/binding/${type}`,
        method: 'delete'
    })
}

export function getTwoFactorCode() {
    return request({
        url: '/api/settings/2fa/code',
        method: 'get'
    })
}

export function enableTwoFactor(data) {
    return request({
        url: '/api/settings/2fa/enable',
        method: 'post',
        data
    })
}

export function disableTwoFactor(data) {
    return request({
        url: '/api/settings/2fa/disable',
        method: 'post',
        data
    })
}

export function getLoginHistory() {
    return request({
        url: '/api/settings/security/login-history',
        method: 'get'
    })
}

export function getDevices() {
    return request({
        url: '/api/settings/security/devices',
        method: 'get'
    })
}

export function removeDevice(id) {
    return request({
        url: `/api/settings/security/devices/${id}`,
        method: 'delete'
    })
}