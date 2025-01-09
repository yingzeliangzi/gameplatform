import { createRouter, createWebHistory } from 'vue-router';
import Layout from '@/layout/index.vue';

const routes = [
    {
        path: '/redirect',
        component: Layout,
        hidden: true,
        children: [
            {
                path: '/redirect/:path(.*)',
                component: () => import('@/views/redirect/index.vue')
            }
        ]
    },
    {
        path: '/login',
        component: () => import('@/views/auth/Login.vue'),
        hidden: true
    },
    {
        path: '/register',
        component: () => import('@/views/auth/Register.vue'),
        hidden: true
    },
    {
        path: '/',
        component: Layout,
        redirect: '/home',
        children: [
            {
                path: 'home',
                component: () => import('@/views/home/index.vue'),
                name: 'Home',
                meta: { title: '首页', icon: 'home', affix: true }
            }
        ]
    },
    {
        path: '/games',
        component: Layout,
        children: [
            {
                path: '',
                component: () => import('@/views/games/index.vue'),
                name: 'Games',
                meta: { title: '游戏库', icon: 'game' }
            },
            {
                path: ':id',
                component: () => import('@/views/games/detail.vue'),
                name: 'GameDetail',
                meta: { title: '游戏详情', activeMenu: '/games' },
                hidden: true
            }
        ]
    },
    {
        path: '/posts',
        component: Layout,
        children: [
            {
                path: '',
                component: () => import('@/views/posts/index.vue'),
                name: 'Posts',
                meta: { title: '社区', icon: 'message' }
            },
            {
                path: ':id',
                component: () => import('@/views/posts/detail.vue'),
                name: 'PostDetail',
                meta: { title: '帖子详情', activeMenu: '/posts' },
                hidden: true
            },
            {
                path: 'create',
                component: () => import('@/views/posts/create.vue'),
                name: 'CreatePost',
                meta: { title: '发帖', activeMenu: '/posts' },
                hidden: true
            }
        ]
    },
    {
        path: '/events',
        component: Layout,
        children: [
            {
                path: '',
                component: () => import('@/views/events/index.vue'),
                name: 'Events',
                meta: { title: '活动', icon: 'event' }
            },
            {
                path: ':id',
                component: () => import('@/views/events/detail.vue'),
                name: 'EventDetail',
                meta: { title: '活动详情', activeMenu: '/events' },
                hidden: true
            }
        ]
    },
    {
        path: '/user',
        component: Layout,
        hidden: true,
        children: [
            {
                path: 'profile',
                component: () => import('@/views/user/profile.vue'),
                name: 'Profile',
                meta: { title: '个人中心' }
            },
            {
                path: 'settings',
                component: () => import('@/views/user/settings.vue'),
                name: 'Settings',
                meta: { title: '设置' }
            }
        ]
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes,
    scrollBehavior: (to, from, savedPosition) => {
        if (savedPosition) {
            return savedPosition;
        }
        return { top: 0 };
    }
});

export default router;