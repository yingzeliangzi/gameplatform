module.exports = {
    root: true, // 作用的根目录
    env: {
        browser: true, // 浏览器环境
        es2021: true // 支持 ES2021 语法
    },
    extends: [
        'eslint:recommended', // 推荐的 ESLint 规则
        'plugin:vue/vue3-essential', // Vue 3 基本规则
    ],
    parserOptions: {
        ecmaVersion: 'latest', // 最新 ES 版本
        sourceType: 'module' // 模块类型
    },
    plugins: [
        'vue', // Vue 插件
    ],
    rules: {
        // 自定义规则，例如：
        'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off', // 生产环境禁用 console
        'no-debugger': process.env.NODE_ENV === 'production' ? 'warn' : 'off', // 生产环境禁用 debugger
        // ... 其他自定义规则
    }
};