import request from '@/utils/request'

// 生成PPT
export function generatePpt(data: any) {
  return request.post('/ppt/generate', data)
}

// 获取PPT列表
export function getPptList() {
  return request.get('/ppt')
}

// 获取PPT详情
export function getPptDetail(id: number) {
  return request.get(`/ppt/${id}`)
}

// 删除PPT
export function deletePpt(id: number) {
  return request.delete(`/ppt/${id}`)
}