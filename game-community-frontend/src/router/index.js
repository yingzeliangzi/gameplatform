import { createRouter, createWebHistory } from 'vue-router'

import MainLayout from '@/layouts/MainLayout.vue'

const routes = [
    {
        path: '/',
        component: MainLayout,
        children: [
            {
                path: '',
                name: 'Home',
                component: () => import('../views/Home.vue'),
                meta: { title: '首页' }
            },
            {
                path: 'games',
                name: 'Games',
                component: () => import('../views/game/GameList.vue'),
                meta: { title: '游戏库', requiresAuth: true }
            },
            {
                path: 'community',
                name: 'Community',
                component: () => import('../views/community/PostList.vue'),
                meta: { title: '社区', requiresAuth: true }
            },
            {
                path: 'events',
                name: 'Events',
                component: () => import('../views/event/EventList.vue'),
                meta: { title: '活动', requiresAuth: true }
            }
        ]
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/auth/Login.vue'),
        meta: { title: '登录' }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('../views/auth/Register.vue'),
        meta: { title: '注册' }
    },
    {
        path: '/community',
        component: () => import('../layouts/MainLayout.vue'),
        children: [
            {
                path: '',
                name: 'PostList',
                component: () => import('../views/community/PostList.vue'),
                meta: { title: '社区', requiresAuth: true }
            },
            {
                path: 'posts/:id',
                name: 'PostDetail',
                component: () => import('../views/community/PostDetail.vue'),
                meta: { title: '帖子详情', requiresAuth: true }
            },
            {
                path: 'posts/:id/edit',
                name: 'PostEdit',
                component: () => import('../views/community/PostEdit.vue'),
                meta: { title: '编辑帖子', requiresAuth: true }
            }
        ]
    },
    {
        path: '/admin',
        component: () => import('../layouts/AdminLayout.vue'),
        meta: { requiresAuth: true, requiresAdmin: true },
        children: [
            {
                path: 'posts',
                name: 'AdminPosts',
                component: () => import('../views/admin/PostManagement.vue'),
                meta: { title: '帖子管理' }
            },
            {
                path: 'reports',
                name: 'AdminReports',
                component: () => import('../views/admin/ReportManagement.vue'),
                meta: { title: '举报管理' }
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(process.env.BASE_URL),
    routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    if (to.matched.some(record => record.meta.requiresAuth)) {
        if (!token) {
            next('/login')
        } else {
            if (to.matched.some(record => record.meta.requiresAdmin)) {
                const userRole = localStorage.getItem('userRole')
                if (userRole === 'ADMIN') {
                    next()
                } else {
                    next('/')
                }
            } else {
                next()
            }
        }
    } else {
        next()
    }
})

export default router

