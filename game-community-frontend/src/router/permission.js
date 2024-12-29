import router from './index'
import store from '../store'
import { getToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'

// 白名单路由（不需要登录就可以访问的路由）
const whiteList = ['/login', '/register', '/404']

router.beforeEach(async (to, from, next) => {
    // 获取token
    const hasToken = getToken()

    if (hasToken) {
        if (to.path === '/login') {
            // 如果已登录，重定向到首页
            next({ path: '/' })
        } else {
            // 判断是否获取了用户信息
            const hasRoles = store.state.auth.roles && store.state.auth.roles.length > 0
            if (hasRoles) {
                // 如果有用户角色信息，直接访问
                if (to.meta.roles && !hasPermission(to, store.state.auth.roles)) {
                    next({ path: '/403' })
                } else {
                    next()
                }
            } else {
                try {
                    // 获取用户信息
                    const { roles } = await store.dispatch('auth/getUserInfo')

                    // 根据角色生成可访问路由表
                    await store.dispatch('permission/generateRoutes', roles)

                    // 动态添加可访问路由
                    next({ ...to, replace: true })
                } catch (error) {
                    // 获取用户信息失败，清除token并跳转登录页
                    await store.dispatch('auth/resetToken')
                    ElMessage.error(error.message || '验证失败，请重新登录')
                    next(`/login?redirect=${to.path}`)
                }
            }
        }
    } else {
        if (whiteList.includes(to.path)) {
            // 白名单路由直接访问
            next()
        } else {
            // 没有token重定向到登录页
            next(`/login?redirect=${to.path}`)
        }
    }
})

router.afterEach(() => {
    // 路由加载完成后的处理
    // 比如关闭loading等
})