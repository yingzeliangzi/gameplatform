import { format, formatDistance } from 'date-fns'
import { zhCN } from 'date-fns/locale'

/**
 * 日期格式化
 * @param {(Date|string|number)} date 日期
 * @param {string} formatStr 格式化模板
 * @returns {string}
 */
export function formatDate(date, formatStr = 'yyyy-MM-dd HH:mm:ss') {
    if (!date) return ''
    return format(new Date(date), formatStr)
}

/**
 * 相对时间
 * @param {(Date|string|number)} date 日期
 * @returns {string}
 */
export function formatRelativeTime(date) {
    if (!date) return ''
    return formatDistance(new Date(date), new Date(), { addSuffix: true, locale: zhCN })
}

/**
 * 金额格式化
 * @param {(number|string)} amount 金额
 * @param {number} decimals 小数位数
 * @returns {string}
 */
export function formatMoney(amount, decimals = 2) {
    if (!amount) return '0.00'
    return Number(amount).toFixed(decimals)
        .replace(/\d{1,3}(?=(\d{3})+(\.\d*)?$)/g, '$&,')
}

/**
 * 文件大小格式化
 * @param {number} bytes 字节
 * @returns {string}
 */
export function formatFileSize(bytes) {
    if (!bytes) return '0 B'
    const units = ['B', 'KB', 'MB', 'GB', 'TB', 'PB']
    const exp = Math.floor(Math.log(bytes) / Math.log(1024))
    return `${(bytes / Math.pow(1024, exp)).toFixed(2)} ${units[exp]}`
}

/**
 * 随机字符串
 * @param {number} length 长度
 * @returns {string}
 */
export function randomString(length = 32) {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
    let result = ''
    for (let i = 0; i < length; i++) {
        result += chars.charAt(Math.floor(Math.random() * chars.length))
    }
    return result
}

/**
 * 深拷贝
 * @param {*} obj 源对象
 * @returns {*}
 */
export function deepClone(obj) {
    if (obj === null || typeof obj !== 'object') return obj
    const clone = Array.isArray(obj) ? [] : {}
    for (const key in obj) {
        if (Object.prototype.hasOwnProperty.call(obj, key)) {
            clone[key] = deepClone(obj[key])
        }
    }
    return clone
}

/**
 * 防抖函数
 * @param {function} func 函数
 * @param {number} wait 延迟时间(ms)
 * @param {boolean} immediate 是否立即执行
 * @returns {function}
 */
export function debounce(func, wait = 300, immediate = false) {
    let timeout
    return function(...args) {
        const later = () => {
            timeout = null
            if (!immediate) func.apply(this, args)
        }
        const callNow = immediate && !timeout
        clearTimeout(timeout)
        timeout = setTimeout(later, wait)
        if (callNow) func.apply(this, args)
    }
}

/**
 * 节流函数
 * @param {function} func 函数
 * @param {number} wait 延迟时间(ms)
 * @returns {function}
 */
export function throttle(func, wait = 300) {
    let timeout = null
    let previous = 0

    return function(...args) {
        const now = Date.now()
        if (!previous) previous = now
        if (now - previous >= wait) {
            func.apply(this, args)
            previous = now
        } else {
            clearTimeout(timeout)
            timeout = setTimeout(() => {
                func.apply(this, args)
                previous = now
            }, wait)
        }
    }
}

/**
 * URL参数解析
 * @param {string} url URL字符串
 * @returns {object}
 */
export function parseQuery(url) {
    const query = {}
    const search = url.split('?')[1]
    if (!search) return query

    const pairs = search.split('&')
    pairs.forEach(pair => {
        const [key, value] = pair.split('=')
        query[decodeURIComponent(key)] = decodeURIComponent(value || '')
    })

    return query
}

/**
 * 下载文件
 * @param {Blob} blob 文件blob
 * @param {string} filename 文件名
 */
export function downloadFile(blob, filename) {
    const link = document.createElement('a')
    link.href = window.URL.createObjectURL(new Blob([blob]))
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(link.href)
}

/**
 * 判断是否为移动设备
 * @returns {boolean}
 */
export function isMobile() {
    return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
}

/**
 * 生成唯一ID
 * @returns {string}
 */
export function generateUUID() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        const r = Math.random() * 16 | 0
        const v = c === 'x' ? r : (r & 0x3 | 0x8)
        return v.toString(16)
    })
}
