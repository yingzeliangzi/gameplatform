import router from '@/router'
import store from '@/store'
import { getToken } from '@/utils/auth'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import {ElMessage} from "element-plus";

NProgress.configure({ showSpinner: false })

// 白名单路由
const whiteList = ['/login', '/register', '/404', '/403']

router.beforeEach(async (to, from, next) => {
    NProgress.start()

    const hasToken = getToken()

    if (hasToken) {
        if (to.path === '/login') {
            next({ path: '/' })
            NProgress.done()
        } else {
            const hasGetUserInfo = store.getters['auth/userInfo']
            if (hasGetUserInfo) {
                next()
            } else {
                try {
                    await store.dispatch('auth/getUserInfo')
                    next({ ...to, replace: true })
                } catch (error) {
                    await store.dispatch('auth/resetToken')
                    ElMessage.error(error || '获取用户信息失败，请重新登录')
                    next(`/login?redirect=${to.path}`)
                    NProgress.done()
                }
            }
        }
    } else {
        if (whiteList.indexOf(to.path) !== -1) {
            next()
        } else {
            next(`/login?redirect=${to.path}`)
            NProgress.done()
        }
    }
})

router.afterEach(() => {
    NProgress.done()
})