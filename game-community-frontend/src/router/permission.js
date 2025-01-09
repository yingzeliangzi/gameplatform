import router from '@/router';
import store from '@/store';
import NProgress from 'nprogress';
import 'nprogress/nprogress.css';
import { getToken } from '@/utils/auth';
import { ElMessage } from 'element-plus';
import { handleWebSocketAuth } from '@/utils/websocket';

// NProgress配置
NProgress.configure({
    showSpinner: false,
    minimum: 0.1,
    easing: 'ease',
    speed: 500
});

// 白名单路由
const whiteList = [
    '/login',
    '/register',
    '/reset-password',
    '/404',
    '/403',
    '/500'
];

// 路由前置守卫
router.beforeEach(async(to, from, next) => {
    // 开始进度条
    NProgress.start();

    // 设置页面标题
    document.title = to.meta.title ? `${to.meta.title} - 游戏社区` : '游戏社区';

    // 获取token
    const hasToken = getToken();

    if (hasToken) {
        if (to.path === '/login') {
            // 已登录且要跳转的页面是登录页
            next({ path: '/' });
            NProgress.done();
        } else {
            // 判断是否有用户信息
            const hasUserInfo = store.getters['user/hasUserInfo'];

            if (hasUserInfo) {
                // 验证权限
                if (hasPermission(to)) {
                    handleWebSocketAuth(to, from);
                    next();
                } else {
                    next('/403');
                }
            } else {
                try {
                    // 获取用户信息
                    await store.dispatch('user/getUserInfo');

                    // 生成可访问路由表
                    const accessRoutes = await store.dispatch('permission/generateRoutes');

                    // 动态添加可访问路由
                    accessRoutes.forEach(route => {
                        router.addRoute(route);
                    });

                    // 处理WebSocket认证
                    handleWebSocketAuth(to, from);

                    // hack: 确保addRoutes完成
                    next({ ...to, replace: true });
                } catch (error) {
                    // 清除token并跳转登录页
                    await store.dispatch('user/resetToken');
                    ElMessage.error(error?.message || '认证失败，请重新登录');
                    next(`/login?redirect=${to.path}`);
                    NProgress.done();
                }
            }
        }
    } else {
        // 未登录
        if (whiteList.includes(to.path) || isPublicRoute(to)) {
            // 白名单或公开路由，直接进入
            next();
        } else {
            // 重定向到登录页
            next(`/login?redirect=${to.path}`);
            NProgress.done();
        }
    }
});

// 路由后置守卫
router.afterEach(() => {
    NProgress.done();
});

// 验证路由权限
function hasPermission(route) {
    if (route.meta && route.meta.roles) {
        const userRoles = store.getters['user/roles'];
        return userRoles.some(role => route.meta.roles.includes(role));
    }
    return true;
}

// 判断是否为公开路由
function isPublicRoute(route) {
    if (route.meta && route.meta.public) {
        return true;
    }
    if (route.matched) {
        return route.matched.some(record => record.meta && record.meta.public);
    }
    return false;
}