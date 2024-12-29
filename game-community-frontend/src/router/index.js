import { createRouter, createWebHistory } from 'vue-router'
import store from '@/store'
import Layout from '@/layouts/Layout.vue'

// 路由懒加载
const Home = () => import('@/views/Home.vue')
const Login = () => import('@/views/auth/Login.vue')
const Register = () => import('@/views/auth/Register.vue')
const GameList = () => import('@/views/game/GameList.vue')
const GameDetail = () => import('@/views/game/GameDetail.vue')
const Community = () => import('@/views/community/Community.vue')
const PostDetail = () => import('@/views/community/PostDetail.vue')
const EventList = () => import('@/views/event/EventList.vue')
const EventDetail = () => import('@/views/event/EventDetail.vue')
const Profile = () => import('@/views/profile/Profile.vue')
const Settings = () => import('@/views/settings/Settings.vue')
const Error404 = () => import('@/views/error/404.vue')

// Admin 页面
const AdminLayout = () => import('@/layouts/AdminLayout.vue')
const Dashboard = () => import('@/views/admin/Dashboard.vue')
const UserManagement = () => import('@/views/admin/UserManagement.vue')
const ContentManagement = () => import('@/views/admin/ContentManagement.vue')

const routes = [
    {
        path: '/',
        component: Layout,
        children: [
            {
                path: '',
                name: 'Home',
                component: Home,
                meta: { title: '首页' }
            },
            {
                path: 'games',
                name: 'GameList',
                component: GameList,
                meta: { title: '游戏库' }
            },
            {
                path: 'games/:id',
                name: 'GameDetail',
                component: GameDetail,
                meta: { title: '游戏详情' }
            },
            {
                path: 'community',
                name: 'Community',
                component: Community,
                meta: { title: '社区' }
            },
            {
                path: 'posts/:id',
                name: 'PostDetail',
                component: PostDetail,
                meta: { title: '帖子详情' }
            },
            {
                path: 'events',
                name: 'EventList',
                component: EventList,
                meta: { title: '活动' }
            },
            {
                path: 'events/:id',
                name: 'EventDetail',
                component: EventDetail,
                meta: { title: '活动详情' }
            },
            {
                path: 'profile',
                name: 'Profile',
                component: Profile,
                meta: {
                    title: '个人中心',
                    requiresAuth: true
                }
            },
            {
                path: 'settings',
                name: 'Settings',
                component: Settings,
                meta: {
                    title: '设置',
                    requiresAuth: true
                }
            }
        ]
    },
    {
        path: '/auth',
        children: [
            {
                path: 'login',
                name: 'Login',
                component: Login,
                meta: { title: '登录' }
            },
            {
                path: 'register',
                name: 'Register',
                component: Register,
                meta: { title: '注册' }
            }
        ]
    },
    {
        path: '/admin',
        component: AdminLayout,
        meta: {
            requiresAuth: true,
            requiresAdmin: true
        },
        children: [
            {
                path: '',
                name: 'Dashboard',
                component: Dashboard,
                meta: { title: '仪表盘' }
            },
            {
                path: 'users',
                name: 'UserManagement',
                component: UserManagement,
                meta: { title: '用户管理' }
            },
            {
                path: 'content',
                name: 'ContentManagement',
                component: ContentManagement,
                meta: { title: '内容管理' }
            }
        ]
    },
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: Error404,
        meta: { title: '404' }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes,
    scrollBehavior(to, from, savedPosition) {
        if (savedPosition) {
            return savedPosition
        } else {
            return { top: 0 }
        }
    }
})

// 全局路由守卫
router.beforeEach((to, from, next) => {
    // 设置页面标题
    document.title = to.meta.title
        ? `${to.meta.title} - 游戏社区`
        : '游戏社区'

    const isAuthenticated = store.getters['auth/isAuthenticated']
    const isAdmin = store.getters['auth/isAdmin']

    // 需要登录的页面
    if (to.matched.some(record => record.meta.requiresAuth)) {
        if (!isAuthenticated) {
            next({
                path: '/auth/login',
                query: { redirect: to.fullPath }
            })
            return
        }
    }

    // 需要管理员权限的页面
    if (to.matched.some(record => record.meta.requiresAdmin)) {
        if (!isAdmin) {
            next({ name: 'Home' })
            return
        }
    }

    // 已登录用户不能访问登录/注册页
    if (isAuthenticated && (to.name === 'Login' || to.name === 'Register')) {
        next({ name: 'Home' })
        return
    }

    next()
})

export default router