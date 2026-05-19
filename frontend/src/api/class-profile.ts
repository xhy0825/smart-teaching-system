import request from '@/utils/request'

export interface ClassProfileStats {
  baseStats: {
    avgScore: number
    maxScore: number
    minScore: number
    studentCount: number
    passRate: number
    excellentRate: number
    gradedCount: number
  }
  distribution: Array<{
    range: string
    count: number
  }>
  knowledgeMastery: Array<{
    knowledgePoint: string
    avgMasteryRate: number
    weakStudentCount: number
    level: string
  }>
  knowledgeRadar: {
    points: string[]
    scores: number[]
  }
  scoreBoxplot: {
    min: number
    q1: number
    median: number
    q3: number
    max: number
    outliers: number[]
  }
}

// 获取班级画像统计
export function getClassProfileStats(classId: number) {
  return request.get<any>(`/class-profile/${classId}/stats`)
}
