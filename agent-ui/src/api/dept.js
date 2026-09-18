import request from '@/utils/request'

// 分页查询
export function getDeptPage(params) {
  return request({
    url: '/api/dept/list',   // ✅ 这里必须是 list，不是 page！
    method: 'get',
    params
  })
}

// 新增
export function addDept(data) {
  return request({
    url: '/api/dept/add',
    method: 'post',
    data
  })
}

// 修改
export function updateDept(data) {
  return request({
    url: '/api/dept/update',
    method: 'post',
    data
  })
}

// 删除
export function deleteDept(id) {
  return request({
    url: `/api/dept/delete/${id}`,
    method: 'get'
  })
}