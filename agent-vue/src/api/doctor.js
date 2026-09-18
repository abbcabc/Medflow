import request from '@/utils/request'
export function getDoctorPage(params) {
  return request({
    url: '/api/api/doctor/list',
    method: 'get',
    params
  })
}
export function addDoctor(data) {
  return request({
    url: '/api/api/doctor/add',
    method: 'post',
    data
  })
}
export function updateDoctor(data) {
  return request({
    url: '/api/api/doctor/update',
    method: 'post',
    data
  })
}
export function deleteDoctor(id) {
  return request({
    url: `/api/api/doctor/delete/${id}`,
    method: 'get'
  })
}
// 新增：获取医生当前接诊的患者列表
export function getDoctorCurrentPatients(doctorId) {
  return request({
    url: `/api/api/doctor/appointment/current-reception`,
    method: 'get',
    params: { doctorId }
  })
}
// 新增：获取指定患者的预约/病历记录
export function getPatientRecords(doctorId, patientId) {
  return request({
    url: `/api/api/doctor/appointment/patient-records`,
    method: 'get',
    params: { doctorId, patientId }
  })
}