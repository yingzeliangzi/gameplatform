import { createStore } from 'vuex'
import createPersistedState from 'vuex-persistedstate'
import auth from './modules/auth'
import notification from './modules/notification'
import comment from './modules/comment'
import game from './modules/game'
import event from './modules/event'

export default createStore({
    modules: {
        auth,
        notification,
        comment,
        game,
        event
    },
    plugins: [
        createPersistedState({
            key: 'game-community',
            paths: ['auth.token', 'auth.userInfo']
        })
    ]
})