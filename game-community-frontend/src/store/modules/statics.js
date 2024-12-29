import { getStatistics } from '@/api/dashboard'

const state = {
    todayStats: {
        newUsers: 0,
        activePosts: 0,
        newComments: 0,
        onlineUsers: 0
    },
    weeklyStats: {
        userGrowth: [],
        postActivity: [],
        gamePopularity: []
    },
    monthlyStats: {
        userDistribution: {},
        categoryStats: {},
        topGames: []
    }
}

const mutations = {
    SET_TODAY_STATS: (state, stats) => {
        state.todayStats = stats
    },
    SET_WEEKLY_STATS: (state, stats) => {
        state.weeklyStats = stats
    },
    SET_MONTHLY_STATS: (state, stats) => {
        state.monthlyStats = stats
    }
}

const actions = {
    // 获取统计数据
    async fetchStatistics({ commit }) {
        try {
            const { data } = await getStatistics()
            commit('SET_TODAY_STATS', data.today)
            commit('SET_WEEKLY_STATS', data.weekly)
            commit('SET_MONTHLY_STATS', data.monthly)
            return data
        } catch (error) {
            console.error('获取统计数据失败:', error)
            throw error
        }
    },

    // 更新今日统计
    updateTodayStats({ commit }, stats) {
        commit('SET_TODAY_STATS', stats)
    }
}

const getters = {
    todayStats: state => state.todayStats,
    weeklyStats: state => state.weeklyStats,
    monthlyStats: state => state.monthlyStats,

    // 转换为图表数据格式
    weeklyUserGrowthChart: state => {
        return {
            xAxis: state.weeklyStats.userGrowth.map(item => item.date),
            data: state.weeklyStats.userGrowth.map(item => item.count)
        }
    },

    // 获取排行榜数据
    topGamesList: state => {
        return state.monthlyStats.topGames.slice(0, 10)
    }
}

export default {
    namespaced: true,
    state,
    mutations,
    actions,
    getters
}