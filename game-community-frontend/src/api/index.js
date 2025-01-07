import * as authApi from './auth'
import * as userApi from './user'
import * as gameApi from './game'
import * as postApi from './post'
import * as commentApi from './comment'
import * as eventApi from './event'
import * as notificationApi from './notification'

export {
    authApi,
    userApi,
    gameApi,
    postApi,
    commentApi,
    eventApi,
    notificationApi
}

// 统一导出所有API
export default {
    auth: authApi,
    user: userApi,
    game: gameApi,
    post: postApi,
    comment: commentApi,
    event: eventApi,
    notification: notificationApi
}