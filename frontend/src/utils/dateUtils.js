/**
 * 日期时间格式化工具函数
 */

/**
 * 格式化日期时间
 * @param {string|Date} date - 日期字符串或Date对象
 * @param {string} format - 格式化模式，默认为 'YYYY-MM-DD HH:mm:ss'
 * @returns {string} 格式化后的日期字符串
 */
export const formatDateTime = (date, format = 'YYYY-MM-DD HH:mm:ss') => {
  if (!date || date === null || date === undefined) {
    return ''
  }
  
  try {
    let dateObj
    
    // 处理数组格式的日期 [2023, 9, 23, 18, 30, 0]
    if (Array.isArray(date)) {
      if (date.length >= 3) {
        // Java LocalDateTime 数组格式: [年, 月, 日, 时, 分, 秒]
        const [year, month, day, hour = 0, minute = 0, second = 0] = date
        dateObj = new Date(year, month - 1, day, hour, minute, second)
      } else {
        return ''
      }
    }
    // 处理字符串格式
    else if (typeof date === 'string') {
      // 处理各种字符串格式
      dateObj = new Date(date)
    }
    // 处理Date对象
    else if (date instanceof Date) {
      dateObj = date
    }
    // 处理数字时间戳
    else if (typeof date === 'number') {
      dateObj = new Date(date)
    }
    // 处理对象格式 {year: 2023, month: 9, day: 23, ...}
    else if (typeof date === 'object' && date.year) {
      dateObj = new Date(
        date.year, 
        (date.month || date.monthValue || 1) - 1, 
        date.day || date.dayOfMonth || 1,
        date.hour || 0,
        date.minute || 0,
        date.second || 0
      )
    }
    else {
      return ''
    }
    
    // 检查日期是否有效
    if (!dateObj || isNaN(dateObj.getTime())) {
      return ''
    }
    
    const year = dateObj.getFullYear()
    const month = String(dateObj.getMonth() + 1).padStart(2, '0')
    const day = String(dateObj.getDate()).padStart(2, '0')
    const hours = String(dateObj.getHours()).padStart(2, '0')
    const minutes = String(dateObj.getMinutes()).padStart(2, '0')
    const seconds = String(dateObj.getSeconds()).padStart(2, '0')
    
    // 根据格式返回相应的字符串
    switch (format) {
      case 'YYYY-MM-DD':
        return `${year}-${month}-${day}`
      case 'YYYY-MM-DD HH:mm':
        return `${year}-${month}-${day} ${hours}:${minutes}`
      case 'YYYY-MM-DD HH:mm:ss':
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
      case 'MM-DD HH:mm':
        return `${month}-${day} ${hours}:${minutes}`
      case 'HH:mm':
        return `${hours}:${minutes}`
      default:
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    }
  } catch (error) {
    console.error('日期格式化错误:', error, '原始日期:', date)
    return ''
  }
}

/**
 * 格式化日期（不包含时间）
 * @param {string|Date} date - 日期
 * @returns {string} 格式化后的日期字符串
 */
export const formatDate = (date) => {
  return formatDateTime(date, 'YYYY-MM-DD')
}

/**
 * 格式化时间（不包含日期）
 * @param {string|Date} date - 日期时间
 * @returns {string} 格式化后的时间字符串
 */
export const formatTime = (date) => {
  return formatDateTime(date, 'HH:mm')
}

/**
 * 格式化相对时间（如：2小时前、3天前）
 * @param {string|Date} date - 日期时间
 * @returns {string} 相对时间字符串
 */
export const formatRelativeTime = (date) => {
  if (!date) return ''
  
  try {
    const dateObj = new Date(date)
    if (isNaN(dateObj.getTime())) return ''
    
    const now = new Date()
    const diff = now.getTime() - dateObj.getTime()
    const seconds = Math.floor(diff / 1000)
    const minutes = Math.floor(seconds / 60)
    const hours = Math.floor(minutes / 60)
    const days = Math.floor(hours / 24)
    const months = Math.floor(days / 30)
    const years = Math.floor(days / 365)
    
    if (seconds < 60) {
      return '刚刚'
    } else if (minutes < 60) {
      return `${minutes}分钟前`
    } else if (hours < 24) {
      return `${hours}小时前`
    } else if (days < 30) {
      return `${days}天前`
    } else if (months < 12) {
      return `${months}个月前`
    } else {
      return `${years}年前`
    }
  } catch (error) {
    console.error('相对时间格式化错误:', error)
    return ''
  }
}

/**
 * 检查日期是否为今天
 * @param {string|Date} date - 日期
 * @returns {boolean} 是否为今天
 */
export const isToday = (date) => {
  if (!date) return false
  
  try {
    const dateObj = new Date(date)
    const today = new Date()
    
    return dateObj.getFullYear() === today.getFullYear() &&
           dateObj.getMonth() === today.getMonth() &&
           dateObj.getDate() === today.getDate()
  } catch (error) {
    return false
  }
}

/**
 * 获取友好的日期时间显示
 * @param {string|Date} date - 日期时间
 * @returns {string} 友好的日期时间字符串
 */
export const formatFriendlyDateTime = (date) => {
  if (!date) return ''
  
  try {
    const dateObj = new Date(date)
    if (isNaN(dateObj.getTime())) return ''
    
    if (isToday(dateObj)) {
      return `今天 ${formatTime(dateObj)}`
    } else {
      const yesterday = new Date()
      yesterday.setDate(yesterday.getDate() - 1)
      
      if (dateObj.getFullYear() === yesterday.getFullYear() &&
          dateObj.getMonth() === yesterday.getMonth() &&
          dateObj.getDate() === yesterday.getDate()) {
        return `昨天 ${formatTime(dateObj)}`
      } else {
        return formatDateTime(dateObj, 'MM-DD HH:mm')
      }
    }
  } catch (error) {
    console.error('友好时间格式化错误:', error)
    return ''
  }
}
