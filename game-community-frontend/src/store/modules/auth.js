import { login, logout, getUserInfo } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'
import router from '@/router'

const state = () => ({
    token: getToken() || '',
    userInfo: null,
    roles: [],
    permissions: []
})

const mutations = {
    SET_TOKEN: (state, token) => {
        state.token = token
    },
    SET_USER_INFO: (state, userInfo) => {
        state.userInfo = userInfo
    },
    SET_ROLES: (state, roles) => {
        state.roles = roles
    },
    SET_PERMISSIONS: (state, permissions) => {
        state.permissions = permissions
    }
}

const actions = {
    // 登录
    async login({ commit }, userInfo) {
        try {
            const { username, password } = userInfo
            const { data } = await login({ username: username.trim(), password })
            const { token, user } = data

            commit('SET_TOKEN', token)
            setToken(token)

            // 设置用户信息
            commit('SET_USER_INFO', user)
            commit('SET_ROLES', user.roles)
            commit('SET_PERMISSIONS', user.permissions || [])

            return data
        } catch (error) {
            ElMessage.error(error?.message || '登录失败')
            throw error
        }
    },

    // 获取用户信息
    async getUserInfo({ commit }) {
        try {
            const { data } = await getUserInfo()
            commit('SET_USER_INFO', data)
            commit('SET_ROLES', data.roles)
            commit('SET_PERMISSIONS', data.permissions || [])
            return data
        } catch (error) {
            ElMessage.error(error?.message || '获取用户信息失败')
            throw error
        }
    },

    // 登出
    async logout({ commit, state }) {
        try {
            if (state.token) {
                await logout()
            }
        } catch (error) {
            console.error('登出失败:', error)
        } finally {
            commit('SET_TOKEN', '')
            commit('SET_USER_INFO', null)
            commit('SET_ROLES', [])
            commit('SET_PERMISSIONS', [])
            removeToken()
            router.push('/login')
        }
    },

    // 重置Token
    resetToken({ commit }) {
        commit('SET_TOKEN', '')
        commit('SET_USER_INFO', null)
        commit('SET_ROLES', [])
        commit('SET_PERMISSIONS', [])
        removeToken()
    }
}

const getters = {
    token: state => state.token,
    userInfo: state => state.userInfo,
    roles: state => state.roles,
    permissions: state => state.permissions,
    isAuthenticated: state => !!state.token,
    isAdmin: state => state.roles.includes('ADMIN')
}

export default {
    namespaced: true,
    state,
    mutations,
    actions,
    getters
}