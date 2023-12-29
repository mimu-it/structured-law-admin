import request from '@/utils/request'

// 查询法库目录列表
export function listCategory(query) {
  return request({
    url: '/structured-law/category/list',
    method: 'get',
    params: query
  })
}


// 列举所有
export function listAllCategory(query) {
  return request({
    url: '/structured-law/category/list-all',
    method: 'get',
    params: query
  })
}

// 查询法库目录详细
export function getCategory(id) {
  return request({
    url: '/structured-law/category/' + id,
    method: 'get'
  })
}

// 新增法库目录
export function addCategory(data) {
  return request({
    url: '/structured-law/category',
    method: 'post',
    data: data
  })
}

// 修改法库目录
export function updateCategory(data) {
  return request({
    url: '/structured-law/category',
    method: 'put',
    data: data
  })
}

// 删除法库目录
export function delCategory(id) {
  return request({
    url: '/structured-law/category/' + id,
    method: 'delete'
  })
}
