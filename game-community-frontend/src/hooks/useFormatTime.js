import { formatDistanceToNow, format } from 'date-fns'
import { zhCN } from 'date-fns/locale'

export function useFormatTime() {
    // 相对时间格式化
    const formatTime = (time) => {
        if (!time) return ''
        return formatDistanceToNow(new Date(time), {
            addSuffix: true,
            locale: zhCN
        })
    }

    // 标准日期时间格式化
    const formatDateTime = (time, formatStr = 'yyyy-MM-dd HH:mm:ss') => {
        if (!time) return ''
        return format(new Date(time), formatStr)
    }

    // 日期格式化
    const formatDate = (time, formatStr = 'yyyy-MM-dd') => {
        if (!time) return ''
        return format(new Date(time), formatStr)
    }

    // 短时间格式化 (例如: 15:30)
    const formatShortTime = (time) => {
        if (!time) return ''
        return format(new Date(time), 'HH:mm')
    }

    // 友好时间格式化
    // 今天: 15:30
    // 昨天: 昨天 15:30
    // 前天: 前天 15:30
    // 其他: 2024-01-01
    const formatFriendlyTime = (time) => {
        if (!time) return ''
        const date = new Date(time)
        const now = new Date()
        const diff = now.getDate() - date.getDate()

        if (diff === 0 && date.getMonth() === now.getMonth() && date.getFullYear() === now.getFullYear()) {
            return format(date, 'HH:mm')
        }
        if (diff === 1 && date.getMonth() === now.getMonth() && date.getFullYear() === now.getFullYear()) {
            return `昨天 ${format(date, 'HH:mm')}`
        }
        if (diff === 2 && date.getMonth() === now.getMonth() && date.getFullYear() === now.getFullYear()) {
            return `前天 ${format(date, 'HH:mm')}`
        }
        return format(date, 'yyyy-MM-dd')
    }

    return {
        formatTime,
        formatDateTime,
        formatDate,
        formatShortTime,
        formatFriendlyTime
    }
}