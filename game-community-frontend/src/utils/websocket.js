import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import store from '@/store';
import { getToken } from '@/utils/auth';
import { ElMessage } from 'element-plus';

class WebSocketService {
    constructor() {
        this.stompClient = null;
        this.connected = false;
        this.retryCount = 0;
        this.maxRetries = 5;
        this.retryInterval = 5000;
        this.subscriptions = new Map();
        this.messageQueue = [];
    }

    connect() {
        if (this.connected) return;

        const socket = new SockJS(process.env.VUE_APP_WS_URL);
        this.stompClient = Stomp.over(socket);

        // 配置
        this.stompClient.reconnect_delay = this.retryInterval;
        this.stompClient.debug = process.env.NODE_ENV === 'development' ? console.log : null;

        const headers = {
            Authorization: `Bearer ${getToken()}`
        };

        this.stompClient.connect(
            headers,
            this.onConnected.bind(this),
            this.onError.bind(this)
        );
    }

    onConnected() {
        this.connected = true;
        this.retryCount = 0;

        // 订阅必要的通道
        this.subscribeToUserChannel();

        // 处理消息队列
        this.processMessageQueue();

        console.log('WebSocket连接成功');
    }

    onError(error) {
        console.error('WebSocket连接错误:', error);
        this.handleConnectionError();
    }

    handleConnectionError() {
        this.connected = false;
        if (this.retryCount < this.maxRetries) {
            this.retryCount++;
            console.log(`WebSocket重连尝试 ${this.retryCount}/${this.maxRetries}`);
            setTimeout(() => this.connect(), this.retryInterval);
        } else {
            ElMessage.error('WebSocket连接失败，请刷新页面重试');
        }
    }

    subscribeToUserChannel() {
        const userId = store.getters['user/userId'];
        if (!userId) return;

        // 订阅个人通知
        this.subscribe(
            `/user/${userId}/queue/notifications`,
            this.handleNotification.bind(this)
        );

        // 订阅系统通知
        this.subscribe(
            '/topic/announcements',
            this.handleAnnouncement.bind(this)
        );
    }

    handleNotification(message) {
        const notification = JSON.parse(message.body);
        store.dispatch('notification/addNotification', notification);

        ElMessage({
            message: notification.content,
            type: 'info',
            duration: 3000
        });
    }

    handleAnnouncement(message) {
        const announcement = JSON.parse(message.body);
        ElMessage({
            message: announcement.content,
            type: 'warning',
            duration: 0,
            showClose: true
        });
    }

    subscribe(destination, callback) {
        if (!this.connected) {
            this.messageQueue.push({ type: 'subscribe', destination, callback });
            return;
        }

        if (this.subscriptions.has(destination)) {
            this.subscriptions.get(destination).unsubscribe();
        }

        const subscription = this.stompClient.subscribe(destination, callback);
        this.subscriptions.set(destination, subscription);
    }

    send(destination, data) {
        if (!this.connected) {
            this.messageQueue.push({ type: 'send', destination, data });
            return;
        }

        this.stompClient.send(destination, {}, JSON.stringify(data));
    }

    processMessageQueue() {
        while (this.messageQueue.length > 0) {
            const message = this.messageQueue.shift();
            if (message.type === 'subscribe') {
                this.subscribe(message.destination, message.callback);
            } else if (message.type === 'send') {
                this.send(message.destination, message.data);
            }
        }
    }

    disconnect() {
        if (this.stompClient) {
            Object.values(this.subscriptions).forEach(sub => sub.unsubscribe());
            this.subscriptions.clear();
            this.stompClient.disconnect();
            this.connected = false;
        }
    }
}

export const webSocketService = new WebSocketService();

export function initWebSocket() {
    if (store.getters['user/isAuthenticated']) {
        webSocketService.connect();
    }
}

export function handleWebSocketAuth(to, from) {
    const wasAuthenticated = from.matched.some(record => record.meta.requiresAuth);
    const willBeAuthenticated = to.matched.some(record => record.meta.requiresAuth);

    if (!wasAuthenticated && willBeAuthenticated) {
        webSocketService.connect();
    } else if (wasAuthenticated && !willBeAuthenticated) {
        webSocketService.disconnect();
    }
}