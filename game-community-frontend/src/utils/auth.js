const TokenKey = 'game_platform_token'
const UserRoleKey = 'game_platform_user_role'

export function getToken() {
    return localStorage.getItem(TokenKey)
}

export function setToken(token) {
    return localStorage.setItem(TokenKey, token)
}

export function removeToken() {
    return localStorage.removeItem(TokenKey)
}

export function setUserRole(role) {
    return localStorage.setItem(UserRoleKey, role)
}

export function getUserRole() {
    return localStorage.getItem(UserRoleKey)
}

export function removeUserRole() {
    return localStorage.removeItem(UserRoleKey)
}

// 检查用户是否有权限访问某个路由
export function hasPermission(route, roles) {
    if (route.meta && route.meta.roles) {
        return roles.some(role => route.meta.roles.includes(role))
    }
    return true
}