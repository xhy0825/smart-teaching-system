import request from '@/utils/request'

// 创建题库
export function createBank(data: any) {
  return request.post('/question-bank', data)
}

// 获取题库列表
export function getBankList() {
  return request.get('/question-bank')
}

// 按学科获取题库
export function getBankListBySubject(subject: string) {
  return request.get(`/question-bank/subject/${subject}`)
}

// 获取题库详情
export function getBankDetail(id: number) {
  return request.get(`/question-bank/${id}`)
}

// 更新题库
export function updateBank(id: number, data: any) {
  return request.put(`/question-bank/${id}`, data)
}

// 删除题库
export function deleteBank(id: number) {
  return request.delete(`/question-bank/${id}`)
}

// 创建题目
export function createQuestion(data: any) {
  return request.post('/question', data)
}

// 获取题库题目列表
export function getQuestionList(bankId: number) {
  return request.get(`/question/bank/${bankId}`)
}

// 查询题目
export function queryQuestions(subject: string, type?: string, difficulty?: number) {
  return request.get('/question/query', { params: { subject, type, difficulty } })
}

// 获取题目详情
export function getQuestionDetail(id: number) {
  return request.get(`/question/${id}`)
}

// 更新题目
export function updateQuestion(id: number, data: any) {
  return request.put(`/question/${id}`, data)
}

// 删除题目
export function deleteQuestion(id: number) {
  return request.delete(`/question/${id}`)
}