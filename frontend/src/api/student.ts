import request from '@/utils/request'

// 获取学生画像
export function getStudentProfile(studentId: number) {
  return request.get(`/student-profile/${studentId}`)
}

// 获取班级学生画像列表
export function getStudentProfilesByClass(classId: number) {
  return request.get(`/student-profile/class/${classId}`)
}

// 获取知识点掌握情况
export function getKnowledgePoints(studentId: number) {
  return request.get(`/student-profile/${studentId}/knowledge-points`)
}

// 获取成绩趋势
export function getScoreTrends(studentId: number) {
  return request.get(`/student-profile/${studentId}/score-trends`)
}

// 更新学生兴趣爱好
export function updateInterests(studentId: number, data: { interests?: string, talents?: string, learningStyle?: string }) {
  return request.put(`/student-profile/${studentId}/interests`, null, { params: data })
}