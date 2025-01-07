import request from '@/utils/request'

/**
 * 获取帖子列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.keyword - 搜索关键字
 * @param {string} params.category - 帖子分类
 * @param {string} params.sort - 排序方式
 * @param {string} params.gameId - 游戏ID(可选)
 */
export function getPosts(params) {
    return request({
        url: '/posts',
        method: 'get',
        params
    })
}

/**
 * 获取帖子详情
 * @param {string} postId - 帖子ID
 */
export function getPostDetail(postId) {
    return request({
        url: `/posts/${postId}`,
        method: 'get'
    })
}

/**
 * 创建帖子
 * @param {Object} data - 帖子数据
 * @param {string} data.title - 标题
 * @param {string} data.content - 内容
 * @param {string} data.gameId - 关联游戏ID(可选)
 * @param {string} data.category - 分类(可选)
 * @param {Array} data.tags - 标签(可选)
 */
export function createPost(data) {
    return request({
        url: '/posts',
        method: 'post',
        data
    })
}

/**
 * 更新帖子
 * @param {string} postId - 帖子ID
 * @param {Object} data - 更新数据
 */
export function updatePost(postId, data) {
    return request({
        url: `/posts/${postId}`,
        method: 'put',
        data
    })
}

/**
 * 删除帖子
 * @param {string} postId - 帖子ID
 */
export function deletePost(postId) {
    return request({
        url: `/posts/${postId}`,
        method: 'delete'
    })
}

/**
 * 点赞帖子
 * @param {string} postId - 帖子ID
 */
export function likePost(postId) {
    return request({
        url: `/posts/${postId}/like`,
        method: 'post'
    })
}

/**
 * 取消点赞
 * @param {string} postId - 帖子ID
 */
export function unlikePost(postId) {
    return request({
        url: `/posts/${postId}/like`,
        method: 'delete'
    })
}

/**
 * 收藏帖子
 * @param {string} postId - 帖子ID
 */
export function collectPost(postId) {
    return request({
        url: `/posts/${postId}/collect`,
        method: 'post'
    })
}

/**
 * 举报帖子
 * @param {string} postId - 帖子ID
 * @param {Object} data - 举报信息
 */
export function reportPost(postId, data) {
    return request({
        url: `/posts/${postId}/report`,
        method: 'post',
        data
    })
}

/**
 * 获取用户的帖子列表
 * @param {string} userId - 用户ID
 * @param {Object} params - 查询参数
 */
export function getUserPosts(userId, params) {
    return request({
        url: `/users/${userId}/posts`,
        method: 'get',
        params
    })
}

/**
 * 获取热门帖子
 * @param {Object} params - 查询参数
 */
export function getHotPosts(params) {
    return request({
        url: '/posts/hot',
        method: 'get',
        params
    })
}