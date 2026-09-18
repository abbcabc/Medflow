import request from '@/utils/request'

// 后台查询预约列表（支持筛选）
export function getAdminAppointmentList(params) {
  return request({
    url: '/api/api/appointments/list',
    method: 'get',
    params
  })
}

// 后台修改预约状态
export function updateAppointmentStatus(appointmentId, status) {
  return request({
    url: `/api/api/appointments/status/${appointmentId}`,
    method: 'put',
    params: { status }
  })
}

// 后台查询预约详情
export function getAppointmentDetail(appointmentId) {
  return request({
    url: `/api/api/appointments/${appointmentId}`,
    method: 'get',
  })
}

// 生成测试预约数据
export function generateTestAppointments(count = 5) {
  return request({
    url: '/api/api/appointments/generate-test',
    method: 'post',
    params: { count }
  })
}