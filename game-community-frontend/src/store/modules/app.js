import { getSettings, updateSettings } from '@/api/settings';
import Cookies from 'js-cookie';

const state = {
    sidebar: {
        opened: Cookies.get('sidebarStatus') ? !!+Cookies.get('sidebarStatus') : true,
        withoutAnimation: false
    },
    device: 'desktop',
    size: Cookies.get('size') || 'medium',
    theme: localStorage.getItem('theme') || 'light',
    language: localStorage.getItem('language') || 'zh-CN',
    settings: {
        showTags: true,
        fixedHeader: true,
        showSidebarLogo: true,
        showNotification: true,
        showBreadcrumb: true
    }
};

const mutations = {
    TOGGLE_SIDEBAR: state => {
        state.sidebar.opened = !state.sidebar.opened;
        state.sidebar.withoutAnimation = false;
        if (state.sidebar.opened) {
            Cookies.set('sidebarStatus', 1);
        } else {
            Cookies.set('sidebarStatus', 0);
        }
    },
    CLOSE_SIDEBAR: (state, withoutAnimation) => {
        Cookies.set('sidebarStatus', 0);
        state.sidebar.opened = false;
        state.sidebar.withoutAnimation = withoutAnimation;
    },
    TOGGLE_DEVICE: (state, device) => {
        state.device = device;
    },
    SET_SIZE: (state, size) => {
        state.size = size;
        Cookies.set('size', size);
    },
    SET_THEME: (state, theme) => {
        state.theme = theme;
        localStorage.setItem('theme', theme);
        document.documentElement.setAttribute('data-theme', theme);
    },
    SET_LANGUAGE: (state, language) => {
        state.language = language;
        localStorage.setItem('language', language);
    },
    UPDATE_SETTINGS: (state, settings) => {
        state.settings = { ...state.settings, ...settings };
    }
};

const actions = {
    toggleSideBar({ commit }) {
        commit('TOGGLE_SIDEBAR');
    },
    closeSideBar({ commit }, { withoutAnimation }) {
        commit('CLOSE_SIDEBAR', withoutAnimation);
    },
    toggleDevice({ commit }, device) {
        commit('TOGGLE_DEVICE', device);
    },
    setSize({ commit }, size) {
        commit('SET_SIZE', size);
    },
    setTheme({ commit }, theme) {
        commit('SET_THEME', theme);
    },
    setLanguage({ commit }, language) {
        commit('SET_LANGUAGE', language);
    },
    async getSettings({ commit }) {
        try {
            const { data } = await getSettings();
            commit('UPDATE_SETTINGS', data);
            return data;
        } catch (error) {
            console.error('获取设置失败:', error);
            return Promise.reject(error);
        }
    },
    async updateSettings({ commit }, settings) {
        try {
            await updateSettings(settings);
            commit('UPDATE_SETTINGS', settings);
        } catch (error) {
            console.error('更新设置失败:', error);
            return Promise.reject(error);
        }
    }
};

const getters = {
    sidebar: state => state.sidebar,
    device: state => state.device,
    size: state => state.size,
    theme: state => state.theme,
    language: state => state.language,
    settings: state => state.settings
};

export default {
    namespaced: true,
    state,
    mutations,
    actions,
    getters
};