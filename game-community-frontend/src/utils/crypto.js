import CryptoJS from 'crypto-js'

// 加密密钥
const KEY = process.env.VUE_APP_CRYPTO_KEY || 'default-key'

/**
 * AES加密
 * @param {string} data 待加密数据
 * @returns {string}
 */
export function encrypt(data) {
    if (!data) return ''
    const dataStr = JSON.stringify(data)
    return CryptoJS.AES.encrypt(dataStr, KEY).toString()
}

/**
 * AES解密
 * @param {string} ciphertext 密文
 * @returns {*}
 */
export function decrypt(ciphertext) {
    if (!ciphertext) return null
    try {
        const bytes = CryptoJS.AES.decrypt(ciphertext, KEY)
        const decrypted = bytes.toString(CryptoJS.enc.Utf8)
        return JSON.parse(decrypted)
    } catch (error) {
        console.error('解密失败:', error)
        return null
    }
}

/**
 * MD5加密
 * @param {string} data 待加密数据
 * @returns {string}
 */
export function md5(data) {
    if (!data) return ''
    return CryptoJS.MD5(data).toString()
}

/**
 * Base64编码
 * @param {string} data 待编码数据
 * @returns {string}
 */
export function encodeBase64(data) {
    if (!data) return ''
    return CryptoJS.enc.Base64.stringify(CryptoJS.enc.Utf8.parse(data))
}

/**
 * Base64解码
 * @param {string} data 待解码数据
 * @returns {string}
 */
export function decodeBase64(data) {
    if (!data) return ''
    return CryptoJS.enc.Base64.parse(data).toString(CryptoJS.enc.Utf8)
}

/**
 * SHA256加密
 * @param {string} data 待加密数据
 * @returns {string}
 */
export function sha256(data) {
    if (!data) return ''
    return CryptoJS.SHA256(data).toString()
}

/**
 * 生成安全的随机字符串
 * @param {number} length 长度
 * @returns {string}
 */
export function generateSecureRandomString(length = 32) {
    const bytes = CryptoJS.lib.WordArray.random(length)
    return bytes.toString(CryptoJS.enc.Hex)
}