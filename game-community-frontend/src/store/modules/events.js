import {
    getEvents,
    getUpcomingEvents,
    getOngoingEvents,
    getEventDetail
} from '@/api/event'

const state = {
    eventList: [],
    upcomingEvents: [],
    ongoingEvents: [],
    currentEvent: null,
    total: 0,
    loading: false
}

const mutations = {
    SET_EVENT_LIST: (state, { events, total }) => {
        state.eventList = events
        state.total = total
    },
    SET_UPCOMING_EVENTS: (state, events) => {
        state.upcomingEvents = events
    },
    SET_ONGOING_EVENTS: (state, events) => {
        state.ongoingEvents = events
    },
    SET_CURRENT_EVENT: (state, event) => {
        state.currentEvent = event
    },
    SET_LOADING: (state, loading) => {
        state.loading = loading
    }
}

const actions = {
    async fetchEvents({ commit }, params) {
        commit('SET_LOADING', true)
        try {
            const response = await getEvents(params)
            commit('SET_EVENT_LIST', {
                events: response.data.content,
                total: response.data.totalElements
            })
            return response
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async fetchUpcomingEvents({ commit }, params) {
        commit('SET_LOADING', true)
        try {
            const response = await getUpcomingEvents(params)
            commit('SET_UPCOMING_EVENTS', response.data.content)
            return response
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async fetchOngoingEvents({ commit }, params) {
        commit('SET_LOADING', true)
        try {
            const response = await getOngoingEvents(params)
            commit('SET_ONGOING_EVENTS', response.data.content)
            return response
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async fetchEventDetail({ commit }, id) {
        commit('SET_LOADING', true)
        try {
            const response = await getEventDetail(id)
            commit('SET_CURRENT_EVENT', response.data)
            return response
        } finally {
            commit('SET_LOADING', false)
        }
    }
}

export default {
    namespaced: true,
    state,
    mutations,
    actions
}