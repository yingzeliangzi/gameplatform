import {
    getGames,
    getGameDetail,
    getCategories,
    rateGame,
    getHotGames,
    getRecommendedGames,
    collectGame,
    uncollectGame
} from '@/api/game';

const state = {
    gameList: [],
    gameDetail: null,
    categories: [],
    hotGames: [],
    recommendedGames: [],
    total: 0,
    loading: false,
    collecting: false
};

const mutations = {
    SET_GAME_LIST: (state, { games, total }) => {
        state.gameList = games;
        state.total = total;
    },
    SET_GAME_DETAIL: (state, game) => {
        state.gameDetail = game;
    },
    SET_CATEGORIES: (state, categories) => {
        state.categories = categories;
    },
    SET_HOT_GAMES: (state, games) => {
        state.hotGames = games;
    },
    SET_RECOMMENDED_GAMES: (state, games) => {
        state.recommendedGames = games;
    },
    SET_LOADING: (state, loading) => {
        state.loading = loading;
    },
    SET_COLLECTING: (state, collecting) => {
        state.collecting = collecting;
    },
    UPDATE_GAME_RATING: (state, { gameId, rating }) => {
        if (state.gameDetail && state.gameDetail.id === gameId) {
            state.gameDetail.rating = rating;
            state.gameDetail.userRating = rating;
        }
        const game = state.gameList.find(g => g.id === gameId);
        if (game) {
            game.rating = rating;
            game.userRating = rating;
        }
    },
    UPDATE_GAME_COLLECTION: (state, { gameId, collected }) => {
        if (state.gameDetail && state.gameDetail.id === gameId) {
            state.gameDetail.collected = collected;
        }
        const game = state.gameList.find(g => g.id === gameId);
        if (game) {
            game.collected = collected;
        }
    }
};

const actions = {
    async getGameList({ commit }, params) {
        commit('SET_LOADING', true);
        try {
            const { data } = await getGames(params);
            commit('SET_GAME_LIST', {
                games: data.content,
                total: data.totalElements
            });
            return data;
        } finally {
            commit('SET_LOADING', false);
        }
    },

    async getGameDetail({ commit }, id) {
        commit('SET_LOADING', true);
        try {
            const { data } = await getGameDetail(id);
            commit('SET_GAME_DETAIL', data);
            return data;
        } finally {
            commit('SET_LOADING', false);
        }
    },

    async getCategories({ commit }) {
        try {
            const { data } = await getCategories();
            commit('SET_CATEGORIES', data);
            return data;
        } catch (error) {
            console.error('获取游戏分类失败:', error);
            return [];
        }
    },

    async rateGame({ commit }, { gameId, rating }) {
        try {
            await rateGame(gameId, rating);
            commit('UPDATE_GAME_RATING', { gameId, rating });
        } catch (error) {
            console.error('评分失败:', error);
            return Promise.reject(error);
        }
    },

    async getHotGames({ commit }) {
        try {
            const { data } = await getHotGames();
            commit('SET_HOT_GAMES', data);
            return data;
        } catch (error) {
            console.error('获取热门游戏失败:', error);
            return [];
        }
    },

    async getRecommendedGames({ commit }) {
        try {
            const { data } = await getRecommendedGames();
            commit('SET_RECOMMENDED_GAMES', data);
            return data;
        } catch (error) {
            console.error('获取推荐游戏失败:', error);
            return [];
        }
    },

    async collectGame({ commit }, gameId) {
        commit('SET_COLLECTING', true);
        try {
            await collectGame(gameId);
            commit('UPDATE_GAME_COLLECTION', { gameId, collected: true });
        } finally {
            commit('SET_COLLECTING', false);
        }
    },

    async uncollectGame({ commit }, gameId) {
        commit('SET_COLLECTING', true);
        try {
            await uncollectGame(gameId);
            commit('UPDATE_GAME_COLLECTION', { gameId, collected: false });
        } finally {
            commit('SET_COLLECTING', false);
        }
    }
};

const getters = {
    gameList: state => state.gameList,
    gameDetail: state => state.gameDetail,
    categories: state => state.categories,
    hotGames: state => state.hotGames,
    recommendedGames: state => state.recommendedGames,
    loading: state => state.loading,
    collecting: state => state.collecting,
    total: state => state.total,
    categoryMap: state => {
        const map = {};
        state.categories.forEach(category => {
            map[category.id] = category.name;
        });
        return map;
    }
};

export default {
    namespaced: true,
    state,
    mutations,
    actions,
    getters
};