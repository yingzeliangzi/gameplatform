import SockJS from 'sockjs-client'
import { Stomp } from '@stomp/stompjs'
import { ElMessage } from 'element-plus'
import store from '@/store'

class WebSocketClient {
    constructor() {
        this.stompClient = null
        this.connected = false
        this.subscriptions = new Map()
    }

    connect() {
        const socket = new SockJS(`${process.env.VUE_APP_BASE_API}/ws`)
        this.stompClient = Stomp.over(socket)

        this.stompClient.connect(
            {},
            () => {
                this.connected = true
                this.subscribeToNotifications()
            },
            (error) => {
                console.error('WebSocket连接失败:', error)
                setTimeout(() => this.connect(), 5000)
            }
        )
    }

    disconnect() {
        if (this.stompClient) {
            this.stompClient.disconnect()
            this.connected = false
        }
    }

    subscribeToNotifications() {
        const userId = store.state.user.id

        // 订阅新通知
        this.subscribe(
            `/user/${userId}/topic/notifications`,
            (message) => {
                const notification = JSON.parse(message.body)
                store.dispatch('notification/addNotification', notification)
                ElMessage({
                    message: notification.title,
                    type: 'info',
                    duration: 3000,
                    onClick: () => {
                        if (notification.targetUrl) {
                            router.push(notification.targetUrl)
                        }
                    }
                })
            }
        )

        // 订阅未读消息数
        this.subscribe(
            `/user/${userId}/topic/notifications/unread`,
            (message) => {
                const unreadCount = JSON.parse(message.body)
                store.commit('notification/SET_UNREAD_COUNT', unreadCount)
            }
        )
    }

    subscribe(destination, callback) {
        if (!this.connected) return

        if (this.subscriptions.has(destination)) {
            this.subscriptions.get(destination).unsubscribe()
        }

        const subscription = this.stompClient.subscribe(destination, callback)
        this.subscriptions.set(destination, subscription)
    }

    unsubscribe(destination) {
        if (this.subscriptions.has(destination)) {
            this.subscriptions.get(destination).unsubscribe()
            this.subscriptions.delete(destination)
        }
    }
}

export const wsClient = new WebSocketClient()