import request from '@/utils/request'

/**
 * 条件查询排班列表
 * @param {Object} data - 查询条件（JSON格式）
 * @returns {Promise}
 */
export function getScheduleList(data) {
  return request({
    url: '/api/admin/doctorSchedule/list',
    method: 'post',
    data
  })
}


/**
 * 新增排班
 * @param {Object} data - 排班数据
 * @returns {Promise}
 */
export function addSchedule(data) {
  return request({
    url: '/api/admin/doctorSchedule/add',
    method: 'post',
    data
  })
}

/**
 * 编辑排班
 * @param {Object} data - 排班数据
 * @returns {Promise}
 */
export function editSchedule(data) {
  return request({
    url: '/api/admin/doctorSchedule/update',
    method: 'put',
    data
  })
}

/**
 * 删除排班
 * @param {Number} id - 排班ID
 * @returns {Promise}
 */
export function deleteSchedule(id) {
  return request({
    url: `/api/admin/doctorSchedule/delete/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除排班
 * @param {Array} ids - 排班ID数组
 * @returns {Promise}
 */
export function batchDeleteSchedule(ids) {
  return request({
    url: '/api/admin/doctorSchedule/batch/delete',
    method: 'delete',
    data: ids
  })
}

/**
 * 一键排班
 * @param {Object} data - 一键排班参数
 * @returns {Promise}
 */
export function oneKeySchedule(data) {
  return request({
    url: '/api/admin/doctorSchedule/oneKey',
    method: 'post',
    data
  })
}

/**
 * 批量修改排班
 * @param {Object} data - 批量修改参数
 * @returns {Promise}
 */
export function batchUpdateSchedule(data) {
  return request({
    url: '/api/admin/doctorSchedule/batch/update',
    method: 'put',
    data
  })
}

/**
 * 分页查询排班列表
 * @param {Number} pageNum - 页码（默认1）
 * @param {Number} pageSize - 每页条数（默认20）
 * @param {Object} data - 查询条件
 * @returns {Promise}
 */
export function pageScheduleList(pageNum = 1, pageSize = 20, data) {
  return request({
    url: '/api/admin/doctorSchedule/pageList',
    method: 'post',
    params: { pageNum, pageSize }, // 路径参数
    data // 请求体参数
  })
}

/**
 * 按ID查询排班详情
 * @param {Number} id - 排班ID
 * @returns {Promise}
 */
export function getScheduleById(id) {
  return request({
    url: `/api/admin/doctorSchedule/get/${id}`,
    method: 'get'
  })
}