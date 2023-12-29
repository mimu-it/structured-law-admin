import request from '@/utils/request'

// 查询法律信息列表
export function listLaw(query) {
  return request({
    url: '/structured-law/law/list',
    method: 'get',
    params: query
  })
}

// 查询法律信息详细
export function getLaw(id) {
  return request({
    url: '/structured-law/law/' + id,
    method: 'get'
  })
}

// 新增法律信息
export function addLaw(data) {
  return request({
    url: '/structured-law/law',
    method: 'post',
    data: data
  })
}

// 修改法律信息
export function updateLaw(data) {
  return request({
    url: '/structured-law/law',
    method: 'put',
    data: data
  })
}

// 删除法律信息
export function delLaw(id) {
  return request({
    url: '/structured-law/law/' + id,
    method: 'delete'
  })
}
