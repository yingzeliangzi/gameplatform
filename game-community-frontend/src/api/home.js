import request from '@/utils/request'

export function getHomeData() {
    return request({
        url: '/api/home',
        method: 'get'
    })
}

export function getBanners() {
    return request({
        url: '/api/banners',
        method: 'get'
    })
}

export function getHomeStats() {
    return request({
        url: '/api/home/stats',
        method: 'get'
    })
}