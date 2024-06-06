import request from '@/utils/request'

// 查询增量更新日志列表
export function listLog(query) {
  return request({
    url: '/structured-law/log/list',
    method: 'get',
    params: query
  })
}

// 查询增量更新日志详细
export function getLog(id) {
  return request({
    url: '/structured-law/log/' + id,
    method: 'get'
  })
}

// 新增增量更新日志
export function addLog(data) {
  return request({
    url: '/structured-law/log',
    method: 'post',
    data: data
  })
}

// 修改增量更新日志
export function updateLog(data) {
  return request({
    url: '/structured-law/log',
    method: 'put',
    data: data
  })
}

// 删除增量更新日志
export function delLog(id) {
  return request({
    url: '/structured-law/log/' + id,
    method: 'delete'
  })
}
