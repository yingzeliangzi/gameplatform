export default [
    {
        path: '/403',
        name: '403',
        component: () => import('@/views/error/403.vue'),
        meta: { title: '403 - 访问被拒绝' }
    },
    {
        path: '/404',
        name: '404',
        component: () => import('@/views/error/404.vue'),
        meta: { title: '404 - 页面不存在' }
    },
    {
        path: '/500',
        name: '500',
        component: () => import('@/views/error/500.vue'),
        meta: { title: '500 - 服务器错误' }
    },
    {
        path: '/:pathMatch(.*)*',
        redirect: '/404'
    }
]