import SockJS from 'sockjs-client'
import { Stomp } from '@stomp/stompjs'
import store from '@/store'
import { ElMessage } from 'element-plus'

class WebSocketService {
    constructor() {
        this.stompClient = null
        this.connected = false
        this.retryCount = 0
        this.maxRetries = 5
        this.subscriptions = new Map()
    }

    connect() {
        const socket = new SockJS(`${process.env.VUE_APP_BASE_API}/ws`)
        this.stompClient = Stomp.over(socket)

        // 禁用调试日志
        this.stompClient.debug = null

        const headers = {
            Authorization: `Bearer ${store.getters['auth/token']}`
        }

        this.stompClient.connect(
            headers,
            () => {
                this.connected = true
                this.retryCount = 0
                this.subscribeToNotifications()
                console.log('WebSocket连接成功')
            },
            (error) => {
                console.error('WebSocket连接错误:', error)
                this.handleConnectionError()
            }
        )
    }

    handleConnectionError() {
        this.connected = false
        if (this.retryCount < this.maxRetries) {
            this.retryCount++
            console.log(`WebSocket重连尝试 ${this.retryCount}/${this.maxRetries}`)
            setTimeout(() => this.connect(), 5000)
        } else {
            ElMessage.error('WebSocket连接失败，请刷新页面重试')
        }
    }

    disconnect() {
        if (this.stompClient) {
            this.unsubscribeAll()
            this.stompClient.disconnect()
            this.connected = false
        }
    }

    subscribeToNotifications() {
        const userId = store.getters['auth/userInfo']?.id
        if (!userId) return

        // 订阅个人通知
        this.subscribe(
            `/user/${userId}/queue/notifications`,
            (message) => {
                const notification = JSON.parse(message.body)
                store.dispatch('notification/addNotification', notification)

                ElMessage({
                    message: notification.content,
                    type: 'info',
                    duration: 3000,
                    showClose: true
                })
            }
        )

        // 订阅系统公告
        this.subscribe(
            '/topic/announcements',
            (message) => {
                const announcement = JSON.parse(message.body)
                ElMessage({
                    message: announcement.content,
                    type: 'warning',
                    duration: 0,
                    showClose: true
                })
            }
        )
    }

    subscribe(destination, callback) {
        if (!this.connected) {
            console.warn('WebSocket未连接')
            return
        }

        if (this.subscriptions.has(destination)) {
            this.subscriptions.get(destination).unsubscribe()
        }

        const subscription = this.stompClient.subscribe(destination, callback)
        this.subscriptions.set(destination, subscription)
        return subscription
    }

    unsubscribe(destination) {
        const subscription = this.subscriptions.get(destination)
        if (subscription) {
            subscription.unsubscribe()
            this.subscriptions.delete(destination)
        }
    }

    unsubscribeAll() {
        this.subscriptions.forEach(subscription => subscription.unsubscribe())
        this.subscriptions.clear()
    }

    send(destination, data) {
        if (!this.connected) {
            console.warn('WebSocket未连接')
            return
        }
        this.stompClient.send(destination, {}, JSON.stringify(data))
    }
}

export const webSocketService = new WebSocketService()

// 在Vue应用启动时初始化WebSocket
export function initWebSocket() {
    if (store.getters['auth/isAuthenticated']) {
        webSocketService.connect()
    }
}

// 在路由守卫中管理WebSocket连接
export function handleWebSocketAuth(to, from) {
    const wasAuthenticated = from.matched.some(record => record.meta.requiresAuth)
    const willBeAuthenticated = to.matched.some(record => record.meta.requiresAuth)

    if (!wasAuthenticated && willBeAuthenticated) {
        webSocketService.connect()
    } else if (wasAuthenticated && !willBeAuthenticated) {
        webSocketService.disconnect()
    }
}