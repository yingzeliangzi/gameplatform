import { format, formatDistanceToNow } from 'date-fns'
import { zhCN } from 'date-fns/locale'

/**
 * 格式化时间为相对时间
 * @param {Date|string|number} date 日期
 * @returns {string}
 */
export function formatTime(date) {
    return formatDistanceToNow(new Date(date), {
        addSuffix: true,
        locale: zhCN
    })
}

/**
 * 格式化日期时间
 * @param {Date|string|number} date 日期
 * @param {string} formatStr 格式化字符串
 * @returns {string}
 */
export function formatDateTime(date, formatStr = 'yyyy-MM-dd HH:mm:ss') {
    return format(new Date(date), formatStr)
}

/**
 * 格式化日期
 * @param {Date|string|number} date 日期
 * @returns {string}
 */
export function formatDate(date) {
    return format(new Date(date), 'yyyy-MM-dd')
}