import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import './assets/styles/index.scss'
import './permission'  // 路由权限控制
import { initWebSocket } from '@/utils/websocket'

const app = createApp(App)

// 注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

// 全局错误处理
app.config.errorHandler = (err, vm, info) => {
    console.error('Global Error:', err, info)
    // 可以在这里添加错误上报逻辑
}

app.use(store)
    .use(router)
    .use(ElementPlus, {
        size: 'default',
        zIndex: 3000
    })
    .mount('#app')

// 初始化 WebSocket 连接
if (store.getters['auth/isAuthenticated']) {
    initWebSocket()
}