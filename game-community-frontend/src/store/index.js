import { createStore } from 'vuex';
import createPersistedState from 'vuex-persistedstate';
import auth from './modules/auth';
import app from './modules/app';
import user from './modules/user';
import game from './modules/game';
import post from './modules/post';
import notification from './modules/notification';

export default createStore({
    modules: {
        auth,
        app,
        user,
        game,
        post,
        notification
    },
    plugins: [
        createPersistedState({
            key: 'game-community',
            paths: [
                'auth.token',
                'user.userInfo',
                'app.settings'
            ],
            storage: window.localStorage
        })
    ],
    strict: process.env.NODE_ENV !== 'production'
});