import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';
import store from '@/store';
import { getToken } from '@/utils/auth';
import router from '@/router';

// 创建axios实例
const service = axios.create({
    baseURL: process.env.VUE_APP_BASE_API,
    timeout: 15000,
    headers: {
        'Content-Type': 'application/json;charset=utf-8'
    },
    // 允许跨域带cookie
    withCredentials: true
});

// 请求拦截器
service.interceptors.request.use(
    config => {
        const token = getToken();
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        // 添加时间戳防止缓存
        if (config.method === 'get') {
            config.params = {
                ...config.params,
                _t: Date.now()
            };
        }
        return config;
    },
    error => {
        console.error('Request error:', error);
        return Promise.reject(error);
    }
);

// 响应拦截器
service.interceptors.response.use(
    response => {
        const res = response.data;

        // 二进制数据直接返回
        if (response.config.responseType === 'blob' || response.config.responseType === 'arraybuffer') {
            return response;
        }

        // 处理业务码
        if (res.code !== '200') {
            showError(res.message);

            // Token问题处理
            if (res.code === 'UNAUTHORIZED') {
                handleUnauthorized();
                return Promise.reject(new Error('登录已过期，请重新登录'));
            }

            return Promise.reject(new Error(res.message || 'Error'));
        }

        return res.data;
    },
    error => {
        handleError(error);
        return Promise.reject(error);
    }
);

// 显示错误信息
function showError(message) {
    ElMessage({
        message,
        type: 'error',
        duration: 3000
    });
}

// 处理未授权情况
function handleUnauthorized() {
    // 防止多个请求同时触发多个提示
    if (!window._isShowingUnauthorized) {
        window._isShowingUnauthorized = true;
        ElMessageBox.confirm(
            '登录状态已过期，是否重新登录？',
            '提示',
            {
                confirmButtonText: '重新登录',
                cancelButtonText: '取消',
                type: 'warning'
            }
        ).then(() => {
            store.dispatch('user/resetToken').then(() => {
                router.push(`/login?redirect=${router.currentRoute.value.fullPath}`);
            });
        }).finally(() => {
            window._isShowingUnauthorized = false;
        });
    }
}

// 处理错误
function handleError(error) {
    const message = error.response?.data?.message || error.message || '请求失败';
    showError(message);

    if (error.response) {
        switch (error.response.status) {
            case 401:
                handleUnauthorized();
                break;
            case 403:
                router.push('/403');
                break;
            case 404:
                router.push('/404');
                break;
            case 500:
                router.push('/500');
                break;
        }
    }
}

// 导出实例
export default service;