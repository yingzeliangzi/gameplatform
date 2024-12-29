import request from '@/utils/request'

export function getGameList(params) {
    return request({
        url: '/api/games',
        method: 'get',
        params
    })
}

export function getGameDetail(id) {
    return request({
        url: `/api/games/${id}`,
        method: 'get'
    })
}

export function getCategories() {
    return request({
        url: '/api/games/categories',
        method: 'get'
    })
}

export function rateGame(id, rating) {
    return request({
        url: `/api/games/${id}/rate`,
        method: 'post',
        params: { rating }
    })
}

export function exportGames() {
    return request({
        url: '/api/games/export',
        method: 'get',
        responseType: 'blob'
    })
}