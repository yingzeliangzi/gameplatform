import request from '@/utils/request'

/**
 * 获取游戏列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.keyword - 搜索关键字
 * @param {string[]} params.categories - 游戏分类
 * @param {string} params.sort - 排序方式
 */
export function getGames(params) {
    return request({
        url: '/games',
        method: 'get',
        params
    })
}

/**
 * 获取游戏详情
 * @param {string} gameId - 游戏ID
 */
export function getGameDetail(gameId) {
    return request({
        url: `/games/${gameId}`,
        method: 'get'
    })
}

/**
 * 获取游戏分类列表
 */
export function getCategories() {
    return request({
        url: '/games/categories',
        method: 'get'
    })
}

/**
 * 评分游戏
 * @param {string} gameId - 游戏ID
 * @param {number} rating - 评分(1-5)
 * @param {string} comment - 评价内容
 */
export function rateGame(gameId, rating, comment = '') {
    return request({
        url: `/games/${gameId}/rate`,
        method: 'post',
        data: { rating, comment }
    })
}

/**
 * 收藏游戏
 * @param {string} gameId - 游戏ID
 */
export function collectGame(gameId) {
    return request({
        url: `/games/${gameId}/collect`,
        method: 'post'
    })
}

/**
 * 取消收藏
 * @param {string} gameId - 游戏ID
 */
export function uncollectGame(gameId) {
    return request({
        url: `/games/${gameId}/collect`,
        method: 'delete'
    })
}

/**
 * 获取游戏统计信息
 * @param {string} gameId - 游戏ID
 */
export function getGameStats(gameId) {
    return request({
        url: `/games/${gameId}/stats`,
        method: 'get'
    })
}

/**
 * 获取推荐游戏
 * @param {Object} params - 查询参数
 */
export function getRecommendGames(params) {
    return request({
        url: '/games/recommend',
        method: 'get',
        params
    })
}

/**
 * 获取热门游戏
 * @param {Object} params - 查询参数
 */
export function getHotGames(params) {
    return request({
        url: '/games/hot',
        method: 'get',
        params
    })
}

/**
 * 获取游戏评论列表
 * @param {string} gameId - 游戏ID
 * @param {Object} params - 查询参数
 */
export function getGameComments(gameId, params) {
    return request({
        url: `/games/${gameId}/comments`,
        method: 'get',
        params
    })
}