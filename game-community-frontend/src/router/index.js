import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

// 路由懒加载
const Layout = () => import('@/layouts/BasicLayout.vue')
const Login = () => import('@/views/auth/Login.vue')
const Register = () => import('@/views/auth/Register.vue')
const Home = () => import('@/views/Home.vue')
const Profile = () => import('@/views/user/Profile.vue')
const NotFound = () => import('@/views/error/404.vue')

// 公共路由
export const constantRoutes = [
    {
        path: '/login',
        component: Login,
        meta: { title: '登录' },
        hidden: true
    },
    {
        path: '/register',
        component: Register,
        meta: { title: '注册' },
        hidden: true
    },
    {
        path: '/404',
        component: NotFound,
        meta: { title: '404' },
        hidden: true
    },
    {
        path: '/',
        component: Layout,
        redirect: '/home',
        children: [
            {
                path: 'home',
                component: Home,
                name: 'Home',
                meta: { title: '首页', icon: 'home' }
            }
        ]
    },
    {
        path: '/user',
        component: Layout,
        redirect: '/user/profile',
        hidden: true,
        children: [
            {
                path: 'profile',
                component: Profile,
                name: 'Profile',
                meta: { title: '个人中心', requireAuth: true }
            }
        ]
    }
]

// 动态路由，用于加载有权限的路由
export const asyncRoutes = []

const router = createRouter({
    history: createWebHistory(),
    routes: constantRoutes,
    scrollBehavior(to, from, savedPosition) {
        if (savedPosition) {
            return savedPosition
        } else {
            return { top: 0 }
        }
    }
})

// 白名单路由
const whiteList = ['/login', '/register', '/404', '/403']

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
            try {
                next()
            } catch (error) {
                console.error('路由错误:', error)
                next(`/login?redirect=${to.path}`)
                NProgress.done()
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

export default router