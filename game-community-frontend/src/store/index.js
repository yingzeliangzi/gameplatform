import { createStore } from 'vuex'
import auth from './modules/auth'
import game from './modules/game'
import post from './modules/post'
import event from './modules/event'
import notification from './modules/notification'

export default createStore({
    state: {
        error: null,
        loading: false,
        theme: localStorage.getItem('theme') || 'light'
    },

    mutations: {
        SET_ERROR(state, error) {
            state.error = error
        },
        SET_LOADING(state, loading) {
            state.loading = loading
        },
        SET_THEME(state, theme) {
            state.theme = theme
            localStorage.setItem('theme', theme)
            document.documentElement.setAttribute('data-theme', theme)
        }
    },

    actions: {
        setError({ commit }, error) {
            commit('SET_ERROR', error)
        },
        clearError({ commit }) {
            commit('SET_ERROR', null)
        },
        setLoading({ commit }, loading) {
            commit('SET_LOADING', loading)
        },
        toggleTheme({ commit, state }) {
            const newTheme = state.theme === 'light' ? 'dark' : 'light'
            commit('SET_THEME', newTheme)
        }
    },

    modules: {
        auth,
        game,
        post,
        event,
        notification
    }
})