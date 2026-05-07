import request from '@/utils/request'

// 创建答题卡
export function createAnswerSheet(examPaperId: number, studentId: number) {
  return request.post('/answer/sheet', null, { params: { examPaperId, studentId } })
}

// 提交答案
export function submitAnswer(data: { answerSheetId: number; examQuestionId: number; studentAnswer: string }) {
  return request.post('/answer', data)
}

// 提交答题卡
export function submitAnswerSheet(id: number) {
  return request.post(`/answer/sheet/${id}/submit`)
}

// 批改答题卡
export function gradeAnswerSheet(id: number, gradedBy: number) {
  return request.post(`/answer/sheet/${id}/grade`, null, { params: { gradedBy } })
}

// 获取答题卡详情
export function getAnswerSheetDetail(id: number) {
  return request.get(`/answer/sheet/${id}`)
}

// 获取试卷答题卡列表
export function getAnswerSheetList(examPaperId: number) {
  return request.get(`/answer/sheet/list/${examPaperId}`)
}

// 获取答题卡答案列表
export function getAnswerList(answerSheetId: number) {
  return request.get(`/answer/list/${answerSheetId}`)
}

// 分析班级成绩
export function analyzeClassScores(examPaperId: number, classId: number) {
  return request.post('/analysis/class', null, { params: { examPaperId, classId } })
}

// 获取成绩分析
export function getScoreAnalysis(examPaperId: number, classId: number) {
  return request.get('/analysis/class', { params: { examPaperId, classId } })
}

// 获取学生错题
export function getWrongQuestions(studentId: number) {
  return request.get(`/analysis/wrong-questions/${studentId}`)
}

// 标记纠错
export function markCorrected(studentId: number, questionId: number) {
  return request.post('/analysis/wrong-questions/corrected', null, {
    params: { studentId, questionId }
  })
}

// 获取高频错题
export function getFrequentWrongQuestions(limit: number = 20) {
  return request.get('/analysis/wrong-questions/frequent', { params: { limit } })
}