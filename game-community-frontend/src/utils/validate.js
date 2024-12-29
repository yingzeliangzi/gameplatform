/**
 * URL验证
 * @param {string} url
 * @returns {boolean}
 */
export function isValidURL(url) {
    const reg = /^(https?|ftp):\/\/([a-zA-Z0-9.-]+(:[a-zA-Z0-9.&%$-]+)*@)*((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]?)(\.(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])){3}|([a-zA-Z0-9-]+\.)*[a-zA-Z0-9-]+\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(:[0-9]+)*(\/($|[a-zA-Z0-9.,?'\\+&%$#=~_-]+))*$/
    return reg.test(url)
}

/**
 * 邮箱验证
 * @param {string} email
 * @returns {boolean}
 */
export function isValidEmail(email) {
    const reg = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
    return reg.test(email)
}

/**
 * 手机号验证
 * @param {string} phone
 * @returns {boolean}
 */
export function isValidPhone(phone) {
    const reg = /^1[3-9]\d{9}$/
    return reg.test(phone)
}

/**
 * 密码强度验证
 * @param {string} password
 * @returns {object} 包含验证结果和强度等级
 */
export function validatePassword(password) {
    const result = {
        valid: false,
        level: 0,  // 0-4级别,对应弱到强
        message: []
    }

    if (!password || password.length < 8) {
        result.message.push('密码长度不能小于8位')
        return result
    }

    // 检查包含字母
    if (/[a-zA-Z]/.test(password)) {
        result.level++
    } else {
        result.message.push('密码需要包含字母')
    }

    // 检查包含数字
    if (/\d/.test(password)) {
        result.level++
    } else {
        result.message.push('密码需要包含数字')
    }
}


/**
 * URL验证
 * @param {string} url
 * @returns {boolean}
 */
export function isValidURL(url) {
    const reg = /^(https?|ftp):\/\/([a-zA-Z0-9.-]+(:[a-zA-Z0-9.&%$-]+)*@)*((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]?)(\.(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])){3}|([a-zA-Z0-9-]+\.)*[a-zA-Z0-9-]+\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(:[0-9]+)*(\/($|[a-zA-Z0-9.,?'\\+&%$#=~_-]+))*$/
    return reg.test(url)
}

/**
 * 邮箱验证
 * @param {string} email
 * @returns {boolean}
 */
export function isValidEmail(email) {
    const reg = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
    return reg.test(email)
}

/**
 * 手机号验证
 * @param {string} phone
 * @returns {boolean}
 */
export function isValidPhone(phone) {
    const reg = /^1[3-9]\d{9}$/
    return reg.test(phone)
}

/**
 * 身份证号验证
 * @param {string} idCard
 * @returns {boolean}
 */
export function isValidIdCard(idCard) {
    const reg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/
    return reg.test(idCard)
}

/**
 * 密码强度验证
 * @param {string} password
 * @returns {object} 包含验证结果和强度等级
 */
export function validatePassword(password) {
    const result = {
        valid: false,
        level: 0,  // 0-4级别,对应弱到强
        message: []
    }

    if (!password || password.length < 8) {
        result.message.push('密码长度不能小于8位')
        return result
    }

    // 检查包含字母
    if (/[a-zA-Z]/.test(password)) {
        result.level++
    } else {
        result.message.push('密码需要包含字母')
    }

    // 检查包含数字
    if (/\d/.test(password)) {
        result.level++
    } else {
        result.message.push('密码需要包含数字')
    }

    // 检查包含特殊字符
    if (/[!@#$%^&*(),.?":{}|<>]/.test(password)) {
        result.level++
    } else {
        result.message.push('密码需要包含特殊字符')
    }

    // 检查大小写混合
    if (/[a-z]/.test(password) && /[A-Z]/.test(password)) {
        result.level++
    } else {
        result.message.push('密码需要包含大小写字母')
    }

    result.valid = result.level >= 3 && password.length >= 8
    return result
}

/**
 * 用户名验证
 * @param {string} username
 * @returns {boolean}
 */
export function isValidUsername(username) {
    // 3-20位字母、数字、下划线
    const reg = /^[a-zA-Z0-9_]{3,20}$/
    return reg.test(username)
}

/**
 * IPv4地址验证
 * @param {string} ip
 * @returns {boolean}
 */
export function isValidIPv4(ip) {
    const reg = /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/
    return reg.test(ip)
}

/**
 * 日期验证
 * @param {string} date YYYY-MM-DD格式
 * @returns {boolean}
 */
export function isValidDate(date) {
    const reg = /^\d{4}-\d{2}-\d{2}$/
    if (!reg.test(date)) return false

    const d = new Date(date)
    const year = d.getFullYear()
    const month = d.getMonth() + 1
    const day = d.getDate()

    if (isNaN(year) || isNaN(month) || isNaN(day)) return false

    if (month < 1 || month > 12) return false
    if (day < 1 || day > 31) return false

    return true
}

/**
 * 金额验证(两位小数)
 * @param {string|number} amount
 * @returns {boolean}
 */
export function isValidAmount(amount) {
    const reg = /^[0-9]+(.[0-9]{1,2})?$/
    return reg.test(amount)
}

/**
 * 中文字符验证
 * @param {string} str
 * @returns {boolean}
 */
export function isChineseChar(str) {
    const reg = /^[\u4e00-\u9fa5]+$/
    return reg.test(str)
}

/**
 * 文件大小限制验证
 * @param {File} file 文件对象
 * @param {number} maxSize 最大大小(MB)
 * @returns {boolean}
 */
export function isValidFileSize(file, maxSize) {
    return file.size / 1024 / 1024 <= maxSize
}

/**
 * 文件类型验证
 * @param {File} file 文件对象
 * @param {Array<string>} types 允许的文件类型
 * @returns {boolean}
 */
export function isValidFileType(file, types) {
    const extension = file.name.split('.').pop().toLowerCase()
    return types.includes(extension)
}