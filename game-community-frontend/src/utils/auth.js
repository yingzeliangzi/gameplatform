import Cookies from 'js-cookie'

const TokenKey = 'game_platform_token'
const UserInfoKey = 'game_platform_user'

export function getToken() {
    return Cookies.get(TokenKey)
}

export function setToken(token) {
    return Cookies.set(TokenKey, token)
}

export function removeToken() {
    return Cookies.remove(TokenKey)
}

export function getUserInfo() {
    const userInfo = Cookies.get(UserInfoKey)
    return userInfo ? JSON.parse(userInfo) : null
}

export function setUserInfo(userInfo) {
    return Cookies.set(UserInfoKey, JSON.stringify(userInfo))
}

export function removeUserInfo() {
    return Cookies.remove(UserInfoKey)
}

export function clearAuth() {
    removeToken()
    removeUserInfo()
}