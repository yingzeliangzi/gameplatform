import {
    getNotifications,
    getUnreadCount,
    markAsRead,
    markAllAsRead,
    deleteNotification
} from '@/api/notification'

const state = {
    notifications: [],
    unreadCount: {
        total: 0,
        system: 0,
        gameDiscount: 0,
        eventReminder: 0,
        postReply: 0
    },
    loading: false,
    total: 0
}

const mutations = {
    SET_NOTIFICATIONS: (state, notifications) => {
        state.notifications = notifications
    },
    SET_UNREAD_COUNT: (state, unreadCount) => {
        state.unreadCount = unreadCount
    },
    SET_LOADING: (state, loading) => {
        state.loading = loading
    },
    SET_TOTAL: (state, total) => {
        state.total = total
    },
    ADD_NOTIFICATION: (state, notification) => {
        state.notifications.unshift(notification)
        state.unreadCount.total++
        switch (notification.type) {
            case 'SYSTEM':
                state.unreadCount.system++
                break
            case 'GAME_DISCOUNT':
                state.unreadCount.gameDiscount++
                break
            case 'EVENT_REMINDER':
                state.unreadCount.eventReminder++
                break
            case 'POST_REPLY':
                state.unreadCount.postReply++
                break
        }
    },
    UPDATE_NOTIFICATION: (state, { id, changes }) => {
        const index = state.notifications.findIndex(n => n.id === id)
        if (index !== -1) {
            state.notifications[index] = { ...state.notifications[index], ...changes }
        }
    },
    REMOVE_NOTIFICATION: (state, id) => {
        state.notifications = state.notifications.filter(n => n.id !== id)
    }
}

const actions = {
    async fetchNotifications({ commit }, params) {
        commit('SET_LOADING', true)
        try {
            const response = await getNotifications(params)
            commit('SET_NOTIFICATIONS', response.data.content)
            commit('SET_TOTAL', response.data.totalElements)
            return response
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async fetchUnreadCount({ commit }) {
        try {
            const response = await getUnreadCount()
            commit('SET_UNREAD_COUNT', response.data)
            return response
        } catch (error) {
            console.error('获取未读消息数失败:', error)
        }
    },

    async markAsRead({ commit }, id) {
        try {
            await markAsRead(id)
            commit('UPDATE_NOTIFICATION', {
                id,
                changes: {
                    isRead: true,
                    readAt: new Date().toISOString()
                }
            })
        } catch (error) {
            console.error('标记已读失败:', error)
        }
    },

    async markAllAsRead({ commit }) {
        try {
            await markAllAsRead()
            commit('SET_UNREAD_COUNT', {
                total: 0,
                system: 0,
                gameDiscount: 0,
                eventReminder: 0,
                postReply: 0
            })
        } catch (error) {
            console.error('标记全部已读失败:', error)
        }
    },

    async deleteNotification({ commit }, id) {
        try {
            await deleteNotification(id)
            commit('REMOVE_NOTIFICATION', id)
        } catch (error) {
            console.error('删除通知失败:', error)
        }
    },

    addNotification({ commit }, notification) {
        commit('ADD_NOTIFICATION', notification)
    }
}

const getters = {
    unreadTotal: state => state.unreadCount.total,
    hasUnread: state => state.unreadCount.total > 0,
    getNotificationById: state => id => {
        return state.notifications.find(n => n.id === id)
    }
}

export default {
    namespaced: true,
    state,
    mutations,
    actions,
    getters
}