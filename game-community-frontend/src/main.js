import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import './assets/styles/index.scss'
import './permission'

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

app.mount('#app')