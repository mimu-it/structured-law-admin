import request from '@/utils/request'


export function merge(idArray) {
    let idsStr = idArray.join(",");
    return request({
        url: '/structured-law/incremental-update/merge/' + idsStr,
        method: 'put'
    })
}


// 查询法律信息列表
export function listLaw(query) {
    return request({
        url: '/structured-law/incremental-update/list',
        method: 'get',
        params: query
    })
}

// 列举所有
export function listAllCategory(query) {
    return request({
        url: '/structured-law/incremental-update/list-category',
        method: 'get',
        params: query
    })
}


// 查询法律信息详细
export function getLaw(id) {
    return request({
        url: '/structured-law/incremental-update/' + id,
        method: 'get'
    })
}

// 新增法律信息
export function addLaw(data) {
    return request({
        url: '/structured-law/incremental-update',
        method: 'post',
        data: data
    })
}

// 修改法律信息
export function updateLaw(data) {
    return request({
        url: '/structured-law/incremental-update',
        method: 'put',
        data: data
    })
}

// 删除法律信息
export function delLaw(id) {
    return request({
        url: '/structured-law/incremental-update/' + id,
        method: 'delete'
    })
}


// 查询法律条款列表
export function listProvision(query) {
    return request({
        url: '/structured-law/incremental-update/provision/list',
        method: 'get',
        params: query
    })
}

// 查询法律条款详细
export function getProvision(id) {
    return request({
        url: '/structured-law/incremental-update/provision/' + id,
        method: 'get'
    })
}

// 新增法律条款
export function addProvision(data) {
    return request({
        url: '/structured-law/incremental-update/provision',
        method: 'post',
        data: data
    })
}

// 修改法律条款
export function updateProvision(data) {
    return request({
        url: '/structured-law/incremental-update/provision',
        method: 'put',
        data: data
    })
}

// 删除法律条款
export function delProvision(id) {
    return request({
        url: '/structured-law/incremental-update/provision/' + id,
        method: 'delete'
    })
}

