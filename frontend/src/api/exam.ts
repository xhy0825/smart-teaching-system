import request from '@/utils/request'

// 创建试卷模板
export function createTemplate(data: any) {
  return request.post('/exam-template', data)
}

// 获取模板列表
export function getTemplateList() {
  return request.get('/exam-template')
}

// 按学科获取模板
export function getTemplateListBySubject(subject: string) {
  return request.get(`/exam-template/subject/${subject}`)
}

// 获取模板详情
export function getTemplateDetail(id: number) {
  return request.get(`/exam-template/${id}`)
}

// 更新模板
export function updateTemplate(id: number, data: any) {
  return request.put(`/exam-template/${id}`, data)
}

// 删除模板
export function deleteTemplate(id: number) {
  return request.delete(`/exam-template/${id}`)
}

// 创建试卷
export function createPaper(data: any) {
  return request.post('/exam-paper', data)
}

// AI生成试卷
export function generatePaper(data: any) {
  return request.post('/exam-paper/generate', data)
}

// 获取试卷列表
export function getPaperList() {
  return request.get('/exam-paper')
}

// 获取班级试卷
export function getPaperListByClass(classId: number) {
  return request.get(`/exam-paper/class/${classId}`)
}

// 获取试卷详情
export function getPaperDetail(id: number) {
  return request.get(`/exam-paper/${id}`)
}

// 更新试卷
export function updatePaper(id: number, data: any) {
  return request.put(`/exam-paper/${id}`, data)
}

// 发布试卷
export function publishPaper(id: number) {
  return request.put(`/exam-paper/${id}/publish`)
}

// 删除试卷
export function deletePaper(id: number) {
  return request.delete(`/exam-paper/${id}`)
}

// 添加题目到试卷
export function addQuestionToPaper(paperId: number, questionId: number, sequence: number, score: number) {
  return request.post(`/exam-paper/${paperId}/question`, null, {
    params: { questionId, sequence, score }
  })
}