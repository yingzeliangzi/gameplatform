import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import store from '@/store'
import { getToken } from '@/utils/auth'
import router from '@/router'

// 创建axios实例
const service = axios.create({
    baseURL: process.env.VUE_APP_BASE_API || '/api',
    timeout: 15000,
    headers: {
        'Content-Type': 'application/json;charset=utf-8'
    }
})

// 请求拦截器
service.interceptors.request.use(
    config => {
        const token = getToken()
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`
        }
        return config
    },
    error => {
        console.error('Request error:', error)
        return Promise.reject(error)
    }
)

// 响应拦截器
service.interceptors.response.use(
    response => {
        const res = response.data

        // 如果是二进制数据直接返回
        if (response.config.responseType === 'blob' || response.config.responseType === 'arraybuffer') {
            return response.data
        }

        // 如果没有code字段，说明是直接返回的数据
        if (res.code === undefined) {
            return res
        }

        // 处理业务码
        if (res.code !== 200) {
            // Token 过期或无效
            if (res.code === 401) {
                handleUnauthorized()
                return Promise.reject(new Error('登录已过期，请重新登录'))
            }
            // 其他业务错误
            const errMsg = res.message || '请求失败'
            ElMessage.error(errMsg)
            return Promise.reject(new Error(errMsg))
        }

        return res.data
    },
    error => {
        const { response } = error
        let message = '请求失败'

        if (response) {
            // 网络请求存在响应
            switch (response.status) {
                case 400:
                    message = '请求错误'
                    break
                case 401:
                    handleUnauthorized()
                    message = '登录已过期，请重新登录'
                    break
                case 403:
                    message = '拒绝访问'
                    break
                case 404:
                    message = '请求地址不存在'
                    break
                case 500:
                    message = '服务器内部错误'
                    break
                default:
                    message = response.data?.message || '请求失败'
            }
        } else if (error.request) {
            // 网络请求已发出但未收到响应
            message = '网络请求超时'
        } else {
            // 请求配置出错
            message = '请求配置错误'
        }

        ElMessage.error(message)
        return Promise.reject(error)
    }
)

// 处理未授权情况
function handleUnauthorized() {
    // 防止多个请求同时触发多个提示
    if (!window._isShowingUnauthorized) {
        window._isShowingUnauthorized = true
        ElMessageBox.confirm(
            '登录状态已过期，请重新登录',
            '提示',
            {
                confirmButtonText: '重新登录',
                cancelButtonText: '取消',
                type: 'warning'
            }
        ).then(() => {
            store.dispatch('auth/logout').then(() => {
                router.push(`/login?redirect=${router.currentRoute.value.fullPath}`)
            })
        }).finally(() => {
            window._isShowingUnauthorized = false
        })
    }
}

export default service