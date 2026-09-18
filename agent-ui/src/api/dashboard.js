import request from '@/utils/request'

// 仪表盘统计数据
export function getDashboardStat() {
  return request({
    url: '/api/admin/dashboard/stat',
    method: 'get'
  })
}

// 近7天预约趋势
export function getDashboardTrend() {
  return request({
    url: '/api/admin/dashboard/trend',
    method: 'get'
  })
}

// 最新5条预约
export function getLatestAppointments(limit = 5) {
  return request({
    url: '/api/admin/dashboard/latest-appointments',
    method: 'get',
    params: {
      limit: limit 
    }
  })
}