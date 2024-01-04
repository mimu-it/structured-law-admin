import request from '@/utils/request'

// 查询关联文件列表
export function listOtherFile(query) {
  return request({
    url: '/structured-law/other-file/list',
    method: 'get',
    params: query
  })
}

// 查询关联文件详细
export function getOtherFile(id) {
  return request({
    url: '/structured-law/other-file/' + id,
    method: 'get'
  })
}

// 新增关联文件
export function addOtherFile(data) {
  return request({
    url: '/structured-law/other-file',
    method: 'post',
    data: data
  })
}

// 修改关联文件
export function updateOtherFile(data) {
  return request({
    url: '/structured-law/other-file',
    method: 'put',
    data: data
  })
}

// 删除关联文件
export function delOtherFile(id) {
  return request({
    url: '/structured-law/other-file/' + id,
    method: 'delete'
  })
}
