import dayjs from 'dayjs'

// 格式化日期
export function formatDate(date, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!date) return ''
  return dayjs(date).format(format)
}

// 格式化时间
export function formatTime(date, format = 'HH:mm') {
  if (!date) return ''
  return dayjs(date).format(format)
}

// 获取相对时间
export function getRelativeTime(date) {
  if (!date) return ''
  return dayjs(date).fromNow()
}

// 判断是否是今天
export function isToday(date) {
  if (!date) return false
  return dayjs(date).isSame(dayjs(), 'day')
}

// 判断是否是本周
export function isThisWeek(date) {
  if (!date) return false
  return dayjs(date).isSame(dayjs(), 'week')
}

// 获取日期范围
export function getDateRange(start, end, format = 'YYYY-MM-DD') {
  const dates = []
  let current = dayjs(start)
  const endDate = dayjs(end)
  
  while (current.isBefore(endDate) || current.isSame(endDate)) {
    dates.push(current.format(format))
    current = current.add(1, 'day')
  }
  
  return dates
}

// 计算两个日期的差值
export function getDaysDiff(start, end) {
  return dayjs(end).diff(dayjs(start), 'day')
}

// 添加时间
export function addTime(date, amount, unit = 'day') {
  return dayjs(date).add(amount, unit).toDate()
}

// 减去时间
export function subtractTime(date, amount, unit = 'day') {
  return dayjs(date).subtract(amount, unit).toDate()
}
