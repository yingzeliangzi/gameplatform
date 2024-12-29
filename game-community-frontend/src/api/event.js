import request from '@/utils/request'

export function getEvents(params) {
    return request({
        url: '/api/events',
        method: 'get',
        params
    })
}

export function getUpcomingEvents(params) {
    return request({
        url: '/api/events/upcoming',
        method: 'get',
        params
    })
}

export function getOngoingEvents(params) {
    return request({
        url: '/api/events/ongoing',
        method: 'get',
        params
    })
}

export function getEventDetail(id) {
    return request({
        url: `/api/events/${id}`,
        method: 'get'
    })
}

export function createEvent(data) {
    return request({
        url: '/api/events',
        method: 'post',
        data
    })
}

export function updateEvent(id, data) {
    return request({
        url: `/api/events/${id}`,
        method: 'put',
        data
    })
}

export function cancelEvent(id) {
    return request({
        url: `/api/events/${id}`,
        method: 'delete'
    })
}

export function registerEvent(id, data) {
    return request({
        url: `/api/events/${id}/register`,
        method: 'post',
        data
    })
}

export function cancelRegistration(id) {
    return request({
        url: `/api/events/${id}/register`,
        method: 'delete'
    })
}

export function getMyRegistrations(params) {
    return request({
        url: '/api/events/registrations/me',
        method: 'get',
        params
    })
}

export function getEventRegistrations(id, params) {
    return request({
        url: `/api/events/${id}/registrations`,
        method: 'get',
        params
    })
}