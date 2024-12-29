import { getGameList, getGameDetail, rateGame } from '@/api/game'

const state = {
    gameList: [],
    totalGames: 0,
    gameDetail: null,
    gameCategories: [],
    loading: false
}

const mutations = {
    SET_GAME_LIST: (state, { games, total }) => {
        state.gameList = games
        state.totalGames = total
    },
    SET_GAME_DETAIL: (state, game) => {
        state.gameDetail = game
    },
    SET_CATEGORIES: (state, categories) => {
        state.gameCategories = categories
    },
    SET_LOADING: (state, loading) => {
        state.loading = loading
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
    async fetchGameList({ commit }, params) {
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
    async fetchGameDetail({ commit }, gameId) {
        commit('SET_LOADING', true)
        try {
            const response = await getGameDetail(gameId)
            commit('SET_GAME_DETAIL', response.data)
            return response
        } finally {
            commit('SET_LOADING', false)
        }
    },

    // 评分
    async rateGame({ commit }, { gameId, rating }) {
        try {
            await rateGame(gameId, rating)
            commit('UPDATE_GAME_RATING', { gameId, rating })
            return true
        } catch (error) {
            throw error
        }
    }
}

const getters = {
    gameById: (state) => (id) => {
        return state.gameList.find(game => game.id === id)
    },
    isLoading: (state) => state.loading
}

export default {
    namespaced: true,
    state,
    mutations,
    actions,
    getters
}