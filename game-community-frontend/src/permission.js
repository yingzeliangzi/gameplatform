import router from '@/router'
import store from '@/store'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'

NProgress.configure({ showSpinner: false })

// 白名单路由
const whiteList = ['/login', '/register', '/404', '/403', '/reset-password']

router.beforeEach(async (to, from, next) => {
    NProgress.start()

    // 设置页面标题
    document.title = to.meta.title ? `${to.meta.title} - 游戏社区` : '游戏社区'

    const hasToken = getToken()

    if (hasToken) {
        if (to.path === '/login') {
            next({ path: '/' })
            NProgress.done()
        } else {
            const hasUserInfo = store.getters['auth/userInfo']
            if (hasUserInfo) {
                // 检查权限
                if (to.meta.roles && !hasPermission(store.getters['auth/roles'], to.meta.roles)) {
                    next('/403')
                } else {
                    next()
                }
            } else {
                try {
                    // 获取用户信息
                    await store.dispatch('auth/getUserInfo')
                    // 重新检查权限
                    if (to.meta.roles && !hasPermission(store.getters['auth/roles'], to.meta.roles)) {
                        next('/403')
                    } else {
                        next({ ...to, replace: true })
                    }
                } catch (error) {
                    // 清除token并跳转登录页
                    await store.dispatch('auth/resetToken')
                    ElMessage.error(error?.message || '获取用户信息失败')
                    next(`/login?redirect=${to.path}`)
                    NProgress.done()
                }
            }
        }
    } else {
        if (whiteList.includes(to.path)) {
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

// 检查权限
function hasPermission(userRoles, requireRoles) {
    if (!requireRoles) return true
    return userRoles.some(role => requireRoles.includes(role))
}