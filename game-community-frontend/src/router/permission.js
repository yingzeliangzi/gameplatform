import router from '@/router';
import store from '@/store';
import NProgress from 'nprogress';
import 'nprogress/nprogress.css';

// NProgress 配置
NProgress.configure({ showSpinner: false });

// 不需要重定向的白名单
const whiteList = ['/login', '/register', '/home', '/games', '/posts'];

router.beforeEach(async (to, from, next) => {
    // 开始进度条
    NProgress.start();

    // 设置页面标题
    document.title = to.meta.title ? `${to.meta.title} - 游戏社区` : '游戏社区';

    // 确定用户是否已登录
    const hasToken = store.getters['user/token'];

    if (hasToken) {
        if (to.path === '/login') {
            // 如果已登录，重定向到首页
            next({ path: '/' });
            NProgress.done();
        } else {
            // 确定用户是否已获取其用户信息
            const hasUserInfo = store.getters['user/hasUserInfo'];

            if (hasUserInfo) {
                next();
            } else {
                try {
                    // 获取用户信息
                    await store.dispatch('user/getUserInfo');

                    // 生成可访问的路由表
                    const accessRoutes = await store.dispatch('permission/generateRoutes');

                    // 动态添加可访问的路由
                    accessRoutes.forEach(route => {
                        router.addRoute(route);
                    });

                    // 确保addRoutes完整的hack方法
                    next({ ...to, replace: true });
                } catch (error) {
                    // 移除 token 并跳转到登录页面重新登录
                    await store.dispatch('user/resetToken');
                    Message.error(error.message || '出现错误，请重新登录');
                    next(`/login?redirect=${to.path}`);
                    NProgress.done();
                }
            }
        }
    } else {
        if (whiteList.indexOf(to.path) !== -1) {
            // 在免登录白名单中，直接进入
            next();
        } else {
            // 其他没有访问权限的页面将重定向到登录页面
            next(`/login?redirect=${to.path}`);
            NProgress.done();
        }
    }
});

router.afterEach(() => {
    // 结束进度条
    NProgress.done();
});