import axios from 'axios'
import { ElMessage } from 'element-plus'
import store from '@/store'
import { getToken } from '@/utils/auth'
import { handleError } from '@/utils'

// 创建 axios 实例
const request = axios.create({
    baseURL: process.env.VUE_APP_BASE_API || '/api',
    timeout: 15000
})

// 请求拦截器
request.interceptors.request.use(
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
request.interceptors.response.use(
    response => {
        const res = response.data

        // 如果响应的是文件流
        if (response.config.responseType === 'blob') {
            return response
        }

        // 如果接口返回错误码
        if (res.code && res.code !== 200) {
            // 401: 未登录或Token过期
            if (res.code === 401) {
                MessageBox.confirm('登录状态已过期，请重新登录', '系统提示', {
                    confirmButtonText: '重新登录',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    store.dispatch('auth/resetToken').then(() => {
                        location.reload()
                    })
                })
            }
            return Promise.reject(new Error(res.message || 'Error'))
        }
        return res
    },
    error => {
        if (error.response) {
            const { status } = error.response

            // 处理 401 未授权
            if (status === 401) {
                // Token 过期，登出处理
                store.dispatch('auth/resetAuth')
                location.reload()
                return
            }

            // 处理 403 权限不足
            if (status === 403) {
                ElMessage.error('没有权限进行此操作')
                return Promise.reject(error)
            }
        }

        handleError(error)
        return Promise.reject(error)
    }
)

export default request