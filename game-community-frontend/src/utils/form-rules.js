import {
    isValidEmail,
    isValidPhone,
    isValidUsername,
    isValidIdCard,
    validatePassword
} from './validate'

/**
 * 公共表单验证规则
 */
export const commonRules = {
    // 必填规则
    required: {
        required: true,
        message: '此项不能为空',
        trigger: ['blur', 'change']
    },

    // 用户名规则
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' },
        {
            validator: (_, value, callback) => {
                if (!isValidUsername(value)) {
                    callback(new Error('用户名只能包含字母、数字和下划线'))
                } else {
                    callback()
                }
            },
            trigger: 'blur'
        }
    ],

    // 密码规则
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 8, message: '密码长度不能小于8位', trigger: 'blur' },
        {
            validator: (_, value, callback) => {
                const result = validatePassword(value)
                if (!result.valid) {
                    callback(new Error(result.message.join(',')))
                } else {
                    callback()
                }
            },
            trigger: 'blur'
        }
    ],

    // 确认密码规则
    confirmPassword: (password) => [
        { required: true, message: '请再次输入密码', trigger: 'blur' },
        {
            validator: (_, value, callback) => {
                if (value !== password) {
                    callback(new Error('两次输入的密码不一致'))
                } else {
                    callback()
                }
            },
            trigger: 'blur'
        }
    ],

    // 邮箱规则
    email: [
        { required: true, message: '请输入邮箱地址', trigger: 'blur' },
        {
            validator: (_, value, callback) => {
                if (!isValidEmail(value)) {
                    callback(new Error('请输入正确的邮箱地址'))
                } else {
                    callback()
                }
            },
            trigger: 'blur'
        }
    ],

    // 手机号规则
    phone: [
        { required: true, message: '请输入手机号码', trigger: 'blur' },
        {
            validator: (_, value, callback) => {
                if (!isValidPhone(value)) {
                    callback(new Error('请输入正确的手机号码'))
                } else {
                    callback()
                }
            },
            trigger: 'blur'
        }
    ],

    // 身份证号规则
    idCard: [
        { required: true, message: '请输入身份证号', trigger: 'blur' },
        {
            validator: (_, value, callback) => {
                if (!isValidIdCard(value)) {
                    callback(new Error('请输入正确的身份证号'))
                } else {
                    callback()
                }
            },
            trigger: 'blur'
        }
    ],

    // 验证码规则
    verifyCode: [
        { required: true, message: '请输入验证码', trigger: 'blur' },
        { len: 6, message: '验证码长度为6位', trigger: 'blur' }
    ]
}

/**
 * 自定义表单验证规则
 */
export const customRules = {
    // 金额规则
    amount: [
        { required: true, message: '请输入金额', trigger: 'blur' },
        { pattern: /^[0-9]+(.[0-9]{1,2})?$/, message: '请输入正确的金额', trigger: 'blur' }
    ],

    // URL规则
    url: [
        { required: true, message: '请输入URL', trigger: 'blur' },
        { type: 'url', message: '请输入正确的URL地址', trigger: 'blur' }
    ],

    // 整数规则
    integer: [
        { required: true, message: '请输入数字', trigger: 'blur' },
        { pattern: /^[0-9]*$/, message: '请输入整数', trigger: 'blur' }
    ]
}

/**
 * 生成自定义验证规则
 * @param {Object} config 验证配置
 * @returns {Array} 验证规则数组
 */
export function generateRules(config) {
    const rules = []

    // 必填验证
    if (config.required) {
        rules.push({
            required: true,
            message: config.requiredMessage || '此项不能为空',
            trigger: config.trigger || ['blur', 'change']
        })
    }

    // 长度验证
    if (config.min !== undefined || config.max !== undefined) {
        rules.push({
            min: config.min,
            max: config.max,
            message: config.lengthMessage || `长度在 ${config.min} 到 ${config.max} 个字符`,
            trigger: 'blur'
        })
    }

    // 正则验证
    if (config.pattern) {
        rules.push({
            pattern: config.pattern,
            message: config.patternMessage || '格式不正确',
            trigger: 'blur'
        })
    }

    // 自定义验证函数
    if (config.validator) {
        rules.push({
            validator: config.validator,
            trigger: config.trigger || 'blur'
        })
    }

    return rules
}