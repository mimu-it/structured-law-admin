import request from '@/utils/request'

// 查询法律条款列表
export function listProvision(query) {
  return request({
    url: '/structured-law/provision/list',
    method: 'get',
    params: query
  })
}

// 查询法律条款详细
export function getProvision(id) {
  return request({
    url: '/structured-law/provision/' + id,
    method: 'get'
  })
}

// 新增法律条款
export function addProvision(data) {
  return request({
    url: '/structured-law/provision',
    method: 'post',
    data: data
  })
}

// 修改法律条款
export function updateProvision(data) {
  return request({
    url: '/structured-law/provision',
    method: 'put',
    data: data
  })
}

// 删除法律条款
export function delProvision(id) {
  return request({
    url: '/structured-law/provision/' + id,
    method: 'delete'
  })
}


export function buildElasticSearchIndex() {
  return request({
    url: '/structured-law/portal/init',
    method: 'put'
  })
}


export function deleteElasticSearchIndex() {
  return request({
    url: '/structured-law/portal/deleteIndex',
    method: 'put'
  })
}

export function backup() {
  return request({
    url: '/structured-law/portal/backup',
    method: 'put'
  })
}

export function sync() {
  return request({
    url: '/structured-law/portal/sync',
    method: 'put'
  })
}

export function syncProgress() {
  return request({
    url: '/structured-law/portal/sync-progress',
    method: 'put'
  })
}
