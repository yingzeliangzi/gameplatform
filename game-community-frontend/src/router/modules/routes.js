export const constantRoutes = [
    {
        path: '/login',
        component: () => import('@/views/auth/Login.vue'),
        meta: { title: '登录', public: true }
    },
    {
        path: '/register',
        component: () => import('@/views/auth/Register.vue'),
        meta: { title: '注册', public: true }
    },
    {
        path: '/reset-password',
        component: () => import('@/views/auth/ResetPassword.vue'),
        meta: { title: '重置密码', public: true }
    },
    {
        path: '/',
        component: () => import('@/layout/index.vue'),
        redirect: '/home',
        children: [
            {
                path: 'home',
                component: () => import('@/views/Home.vue'),
                meta: { title: '首页', icon: 'home', public: true }
            }
        ]
    }
];

export const asyncRoutes = [
    {
        path: '/admin',
        component: () => import('@/layout/index.vue'),
        meta: { title: '管理后台', icon: 'setting', roles: ['admin'] },
        children: [
            {
                path: 'dashboard',
                component: () => import('@/views/admin/Dashboard.vue'),
                meta: { title: '控制台', icon: 'dashboard' }
            },
            {
                path: 'users',
                component: () => import('@/views/admin/Users.vue'),
                meta: { title: '用户管理', icon: 'user' }
            },
            {
                path: 'games',
                component: () => import('@/views/admin/Games.vue'),
                meta: { title: '游戏管理', icon: 'game' }
            }
        ]
    },
    {
        path: '/user',
        component: () => import('@/layout/index.vue'),
        meta: { title: '个人中心' },
        children: [
            {
                path: 'profile',
                component: () => import('@/views/user/Profile.vue'),
                meta: { title: '个人资料', icon: 'user' }
            },
            {
                path: 'settings',
                component: () => import('@/views/user/Settings.vue'),
                meta: { title: '账号设置', icon: 'setting' }
            }
        ]
    }
];