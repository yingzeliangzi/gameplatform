import {
    getNotifications,
    getUnreadCount,
    markAsRead,
    markAllAsRead
} from '@/api/notification';

const state = {
    notifications: [],
    unreadCount: {
        total: 0,
        system: 0,
        gameDiscount: 0,
        eventReminder: 0,
        postReply: 0
    },
    loading: false
};

const mutations = {
    SET_NOTIFICATIONS: (state, notifications) => {
        state.notifications = notifications;
    },
    SET_UNREAD_COUNT: (state, unreadCount) => {
        state.unreadCount = unreadCount;
    },
    SET_LOADING: (state, loading) => {
        state.loading = loading;
    },
    ADD_NOTIFICATION: (state, notification) => {
        state.notifications.unshift(notification);
        state.unreadCount.total++;
        switch (notification.type) {
            case 'SYSTEM':
                state.unreadCount.system++;
                break;
            case 'GAME_DISCOUNT':
                state.unreadCount.gameDiscount++;
                break;
            case 'EVENT_REMINDER':
                state.unreadCount.eventReminder++;
                break;
            case 'POST_REPLY':
                state.unreadCount.postReply++;
                break;
        }
    },
    MARK_AS_READ: (state, notificationId) => {
        const notification = state.notifications.find(n => n.id === notificationId);
        if (notification && !notification.isRead) {
            notification.isRead = true;
            state.unreadCount.total--;
            switch (notification.type) {
                case 'SYSTEM':
                    state.unreadCount.system--;
                    break;
                case 'GAME_DISCOUNT':
                    state.unreadCount.gameDiscount--;
                    break;
                case 'EVENT_REMINDER':
                    state.unreadCount.eventReminder--;
                    break;
                case 'POST_REPLY':
                    state.unreadCount.postReply--;
                    break;
            }
        }
    },
    MARK_ALL_AS_READ: (state) => {
        state.notifications.forEach(notification => {
            notification.isRead = true;
        });
        state.unreadCount = {
            total: 0,
            system: 0,
            gameDiscount: 0,
            eventReminder: 0,
            postReply: 0
        };
    }
};

const actions = {
    async getNotifications({ commit }, params) {
        commit('SET_LOADING', true);
        try {
            const { data } = await getNotifications(params);
            commit('SET_NOTIFICATIONS', data.content);
            return data;
        } finally {
            commit('SET_LOADING', false);
        }
    },
    async getUnreadCount({ commit }) {
        try {
            const { data } = await getUnreadCount();
            commit('SET_UNREAD_COUNT', data);
            return data;
        } catch (error) {
            console.error('获取未读消息数失败:', error);
        }
    },
    async markAsRead({ commit }, notificationId) {
        try {
            await markAsRead(notificationId);
            commit('MARK_AS_READ', notificationId);
        } catch (error) {
            console.error('标记已读失败:', error);
            return Promise.reject(error);
        }
    },
    async markAllAsRead({ commit }) {
        try {
            await markAllAsRead();
            commit('MARK_ALL_AS_READ');
        } catch (error) {
            console.error('标记全部已读失败:', error);
            return Promise.reject(error);
        }
    },
    addNotification({ commit }, notification) {
        commit('ADD_NOTIFICATION', notification);
    }
};

const getters = {
    notifications: state => state.notifications,
    unreadCount: state => state.unreadCount,
    loading: state => state.loading,
    hasUnread: state => state.unreadCount.total > 0
};

export default {
    namespaced: true,
    state,
    mutations,
    actions,
    getters
};