// 临时调试函数，用于检查日期数据格式
export const debugDateTime = (date, label = '日期') => {
  console.group(`🕐 调试时间: ${label}`)
  console.log('原始值:', date)
  console.log('类型:', typeof date)
  console.log('是否为数组:', Array.isArray(date))
  console.log('是否为null/undefined:', date === null || date === undefined)
  
  if (Array.isArray(date)) {
    console.log('数组长度:', date.length)
    console.log('数组内容:', date)
  } else if (typeof date === 'object' && date !== null) {
    console.log('对象键:', Object.keys(date))
    console.log('对象内容:', date)
  }
  
  console.groupEnd()
  return date
}

// 在模板中使用: {{ debugDateTime(someDate, '注册时间') }}
