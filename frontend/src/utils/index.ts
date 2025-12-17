/**
 * 工具函数集合
 */

/**
 * 格式化时间
 */
export function formatTime(time: string | Date): string {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  if (diff < 60 * 1000) {
    return '刚刚'
  } else if (diff < 60 * 60 * 1000) {
    return `${Math.floor(diff / 60 / 1000)}分钟前`
  } else if (diff < 24 * 60 * 60 * 1000) {
    return `${Math.floor(diff / 60 / 60 / 1000)}小时前`
  } else if (diff < 7 * 24 * 60 * 60 * 1000) {
    return `${Math.floor(diff / 24 / 60 / 60 / 1000)}天前`
  } else {
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    })
  }
}

/**
 * 格式化日期
 */
export function formatDate(date: string | Date, format = 'YYYY-MM-DD'): string {
  const d = new Date(date)

  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')

  return format
    .replace('YYYY', String(year))
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 格式化手机号
 */
export function formatPhone(phone: string): string {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

/**
 * 格式化文件大小
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'

  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))

  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 防抖函数
 */
export function debounce<T extends (...args: any[]) => any>(
  func: T,
  delay: number
): (...args: Parameters<T>) => void {
  let timer: NodeJS.Timeout | null = null

  return function (...args: Parameters<T>) {
    if (timer) clearTimeout(timer)

    timer = setTimeout(() => {
      func.apply(this, args)
    }, delay)
  }
}

/**
 * 节流函数
 */
export function throttle<T extends (...args: any[]) => any>(
  func: T,
  delay: number
): (...args: Parameters<T>) => void {
  let lastTime = 0

  return function (...args: Parameters<T>) {
    const now = Date.now()

    if (now - lastTime >= delay) {
      lastTime = now
      func.apply(this, args)
    }
  }
}

/**
 * 深拷贝
 */
export function deepClone<T>(obj: T): T {
  if (obj === null || typeof obj !== 'object') {
    return obj
  }

  if (obj instanceof Date) {
    return new Date(obj.getTime()) as unknown as T
  }

  if (obj instanceof Array) {
    return obj.map(item => deepClone(item)) as unknown as T
  }

  if (typeof obj === 'object') {
    const clonedObj = {} as { [key: string]: any }
    for (const key in obj) {
      if (obj.hasOwnProperty(key)) {
        clonedObj[key] = deepClone(obj[key])
      }
    }
    return clonedObj as T
  }

  return obj
}

/**
 * 生成随机字符串
 */
export function randomString(length = 8): string {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let result = ''

  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length))
  }

  return result
}

/**
 * 生成唯一ID
 */
export function generateId(): string {
  return Date.now().toString(36) + Math.random().toString(36).substr(2)
}

/**
 * 验证手机号
 */
export function validatePhone(phone: string): boolean {
  const phoneReg = /^1[3-9]\d{9}$/
  return phoneReg.test(phone)
}

/**
 * 验证邮箱
 */
export function validateEmail(email: string): boolean {
  const emailReg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailReg.test(email)
}

/**
 * 验证密码强度
 */
export function validatePassword(password: string): {
  isValid: boolean
  strength: 'weak' | 'medium' | 'strong'
  message: string
} {
  if (password.length < 6) {
    return {
      isValid: false,
      strength: 'weak',
      message: '密码长度至少6位'
    }
  }

  if (password.length < 8) {
    return {
      isValid: true,
      strength: 'weak',
      message: '密码强度较弱'
    }
  }

  const hasLetter = /[a-zA-Z]/.test(password)
  const hasNumber = /\d/.test(password)
  const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/.test(password)

  if (hasLetter && hasNumber && hasSpecial) {
    return {
      isValid: true,
      strength: 'strong',
      message: '密码强度很强'
    }
  } else if ((hasLetter && hasNumber) || (hasLetter && hasSpecial) || (hasNumber && hasSpecial)) {
    return {
      isValid: true,
      strength: 'medium',
      message: '密码强度中等'
    }
  } else {
    return {
      isValid: true,
      strength: 'weak',
      message: '密码强度较弱'
    }
  }
}

/**
 * 获取URL参数
 */
export function getUrlParams(url: string): Record<string, string> {
  const params: Record<string, string> = {}
  const queryString = url.split('?')[1]

  if (queryString) {
    queryString.split('&').forEach(param => {
      const [key, value] = param.split('=')
      params[key] = decodeURIComponent(value)
    })
  }

  return params
}

/**
 * 设置URL参数
 */
export function setUrlParams(params: Record<string, string | number>): string {
  const searchParams = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    searchParams.append(key, String(value))
  })

  return searchParams.toString()
}

/**
 * 数组分组
 */
export function chunk<T>(array: T[], size: number): T[][] {
  const chunks: T[][] = []
  for (let i = 0; i < array.length; i += size) {
    chunks.push(array.slice(i, i + size))
  }
  return chunks
}

/**
 * 数组去重
 */
export function unique<T>(array: T[]): T[] {
  return Array.from(new Set(array))
}

/**
 * 数组交集
 */
export function intersection<T>(array1: T[], array2: T[]): T[] {
  return array1.filter(item => array2.includes(item))
}

/**
 * 数组差集
 */
export function difference<T>(array1: T[], array2: T[]): T[] {
  return array1.filter(item => !array2.includes(item))
}

/**
 * 数组并集
 */
export function union<T>(array1: T[], array2: T[]): T[] {
  return unique([...array1, ...array2])
}

/**
 * 扁平化数组
 */
export function flatten<T>(array: (T | T[])[]): T[] {
  return array.reduce<T[]>((acc, val) => {
    return acc.concat(Array.isArray(val) ? flatten(val) : val)
  }, [])
}

/**
 * 压缩图片
 */
export function compressImage(
  src: string,
  quality = 0.8,
  maxWidth = 750
): Promise<string> {
  return new Promise((resolve) => {
    uni.getImageInfo({
      src,
      success: (info) => {
        const canvas = uni.createCanvasContext('tempCanvas')

        const scale = maxWidth / info.width
        const width = info.width * scale
        const height = info.height * scale

        canvas.drawImage(src, 0, 0, width, height)
        canvas.toTempFilePath({
          quality,
          success: (res) => {
            resolve(res.tempFilePath)
          }
        })
      }
    })
  })
}
