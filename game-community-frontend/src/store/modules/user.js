import { login, logout, getUserInfo } from '@/api/auth';
import { getToken, setToken, removeToken } from '@/utils/auth';
import router from '@/router';

const state = {
    token: getToken(),
    userInfo: null,
    roles: [],
    permissions: []
};

const mutations = {
    SET_TOKEN: (state, token) => {
        state.token = token;
    },
    SET_USER_INFO: (state, userInfo) => {
        state.userInfo = userInfo;
    },
    SET_ROLES: (state, roles) => {
        state.roles = roles;
    },
    SET_PERMISSIONS: (state, permissions) => {
        state.permissions = permissions;
    }
};

const actions = {
    // 登录
    async login({ commit }, userInfo) {
        const { username, password } = userInfo;
        try {
            const response = await login({ username: username.trim(), password });
            const { token, user } = response.data;
            commit('SET_TOKEN', token);
            setToken(token);
            commit('SET_USER_INFO', user);
            commit('SET_ROLES', user.roles);
            commit('SET_PERMISSIONS', user.permissions || []);
            return Promise.resolve(response);
        } catch (error) {
            return Promise.reject(error);
        }
    },

    // 获取用户信息
    async getUserInfo({ commit }) {
        try {
            const response = await getUserInfo();
            const { data } = response;
            if (!data) {
                throw new Error('获取用户信息失败，请重新登录');
            }
            commit('SET_USER_INFO', data);
            commit('SET_ROLES', data.roles);
            commit('SET_PERMISSIONS', data.permissions || []);
            return data;
        } catch (error) {
            return Promise.reject(error);
        }
    },

    // 登出
    async logout({ commit, state }) {
        try {
            if (state.token) {
                await logout();
            }
        } catch (error) {
            console.error('登出时发生错误:', error);
        } finally {
            commit('SET_TOKEN', '');
            commit('SET_USER_INFO', null);
            commit('SET_ROLES', []);
            commit('SET_PERMISSIONS', []);
            removeToken();
            router.push('/login');
        }
    },

    // 重置Token
    resetToken({ commit }) {
        commit('SET_TOKEN', '');
        commit('SET_USER_INFO', null);
        commit('SET_ROLES', []);
        commit('SET_PERMISSIONS', []);
        removeToken();
    },

    // 更新用户信息
    updateUserInfo({ commit }, userInfo) {
        commit('SET_USER_INFO', { ...state.userInfo, ...userInfo });
    }
};

const getters = {
    token: state => state.token,
    userInfo: state => state.userInfo,
    roles: state => state.roles,
    permissions: state => state.permissions,
    userId: state => state.userInfo?.id,
    hasUserInfo: state => !!state.userInfo,
    isAdmin: state => state.roles.includes('admin'),
    hasPermission: state => permission => {
        return state.permissions.includes(permission) || state.roles.includes('admin');
    }
};

export default {
    namespaced: true,
    state,
    mutations,
    actions,
    getters
};