import {
    getGameList,
    getGameDetail,
    getCategories,
    rateGame,
    exportGames
} from '@/api/game'

const state = {
    gameList: [],
    gameDetail: null,
    categories: [],
    total: 0,
    loading: false,
    downloadLoading: false,
    userGameStats: {
        totalCount: 0,
        totalPlayTime: 0,
        achievementRate: 0
    }
}

const mutations = {
    SET_GAME_LIST: (state, { games, total }) => {
        state.gameList = games
        state.total = total
    },
    SET_GAME_DETAIL: (state, game) => {
        state.gameDetail = game
    },
    SET_CATEGORIES: (state, categories) => {
        state.categories = categories
    },
    SET_LOADING: (state, loading) => {
        state.loading = loading
    },
    SET_DOWNLOAD_LOADING: (state, loading) => {
        state.downloadLoading = loading
    },
    SET_USER_GAME_STATS: (state, stats) => {
        state.userGameStats = stats
    },
    UPDATE_GAME_RATING: (state, { gameId, rating }) => {
        if (state.gameDetail && state.gameDetail.id === gameId) {
            state.gameDetail.rating = rating
        }
        const game = state.gameList.find(g => g.id === gameId)
        if (game) {
            game.rating = rating
        }
    }
}

const actions = {
    // 获取游戏列表
    async getGameList({ commit }, params) {
        commit('SET_LOADING', true)
        try {
            const response = await getGameList(params)
            const { content, totalElements } = response.data
            commit('SET_GAME_LIST', {
                games: content,
                total: totalElements
            })
            return response
        } finally {
            commit('SET_LOADING', false)
        }
    },

    // 获取游戏详情
    async getGameDetail({ commit }, id) {
        commit('SET_LOADING', true)
        try {
            const response = await getGameDetail(id)
            commit('SET_GAME_DETAIL', response.data)
            return response
        } finally {
            commit('SET_LOADING', false)
        }
    },

    // 获取游戏分类
    async getCategories({ commit }) {
        try {
            const response = await getCategories()
            commit('SET_CATEGORIES', response.data)
            return response
        } catch (error) {
            console.error('获取游戏分类失败:', error)
            throw error
        }
    },

    // 评分
    async rateGame({ commit }, { gameId, rating }) {
        try {
            await rateGame(gameId, rating)
            commit('UPDATE_GAME_RATING', { gameId, rating })
        } catch (error) {
            console.error('评分失败:', error)
            throw error
        }
    },

    // 导出游戏列表
    async exportGames({ commit }) {
        commit('SET_DOWNLOAD_LOADING', true)
        try {
            const response = await exportGames()
            const blob = new Blob([response.data], { type: 'text/csv' })
            const downloadUrl = window.URL.createObjectURL(blob)
            const link = document.createElement('a')
            link.href = downloadUrl
            link.setAttribute('download', 'games.csv')
            document.body.appendChild(link)
            link.click()
            link.remove()
            window.URL.revokeObjectURL(downloadUrl)
        } finally {
            commit('SET_DOWNLOAD_LOADING', false)
        }
    }
}

const getters = {
    gameList: state => state.gameList,
    gameDetail: state => state.gameDetail,
    categories: state => state.categories,
    loading: state => state.loading,
    downloadLoading: state => state.downloadLoading,
    userGameStats: state => state.userGameStats,

    // 获取游戏分类Map
    categoryMap: state => {
        const map = {}
        state.categories.forEach(category => {
            map[category.id] = category.name
        })
        return map
    },

    // 获取游戏的分类名称
    getCategoryName: (state, getters) => (categoryId) => {
        return getters.categoryMap[categoryId] || ''
    }
}

export default {
    namespaced: true,
    state,
    mutations,
    actions,
    getters
}