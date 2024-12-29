import { login, logout, getUserInfo } from '@/api/auth'
import {ElMessage} from "element-plus";

const state = {
    token: localStorage.getItem('token') || '',
    userInfo: {},
    roles: []
}

const mutations = {
    SET_TOKEN: (state, token) => {
        state.token = token
    },
    SET_USER_INFO: (state, userInfo) => {
        state.userInfo = userInfo
    },
    SET_ROLES: (state, roles) => {
        state.roles = roles
    }
}

const actions = {
    // 登录
    login({ commit }, userInfo) {
        return new Promise((resolve, reject) => {
            login(userInfo).then(response => {
                const { token } = response.data
                commit('SET_TOKEN', token)
                localStorage.setItem('token', token)
                resolve()
            }).catch(error => {
                reject(error)
            })
        })
    },

    // 获取用户信息
    getUserInfo({ commit }) {
        return new Promise((resolve, reject) => {
            getUserInfo().then(response => {
                const { data } = response
                commit('SET_USER_INFO', data)
                commit('SET_ROLES', data.roles)
                resolve(data)
            }).catch(error => {
                reject(error)
            })
        })
    },

    // 登出
    logout({ commit }) {
        return new Promise((resolve, reject) => {
            logout().then(() => {
                commit('SET_TOKEN', '')
                commit('SET_USER_INFO', {})
                commit('SET_ROLES', [])
                localStorage.removeItem('token')
                resolve()
            }).catch(error => {
                reject(error)
            })
        })
    },

    register({ commit }, userInfo) {
        return new Promise((resolve, reject) => {
            register(userInfo).then(response => {
                ElMessage.success('注册成功')
                resolve(response)
            }).catch(error => {
                reject(error)
            })
        })
    }
}

export default {
    namespaced: true,
    state,
    mutations,
    actions
}

