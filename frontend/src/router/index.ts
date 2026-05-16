import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'question-bank',
        name: 'QuestionBank',
        component: () => import('@/views/QuestionBank.vue'),
        meta: { title: '题库管理' }
      },
      {
        path: 'question',
        name: 'Question',
        component: () => import('@/views/Question.vue'),
        meta: { title: '题目管理' }
      },
      {
        path: 'exam-template',
        name: 'ExamTemplate',
        component: () => import('@/views/ExamTemplate.vue'),
        meta: { title: '试卷模板' }
      },
      {
        path: 'exam-paper',
        name: 'ExamPaper',
        component: () => import('@/views/ExamPaper.vue'),
        meta: { title: '试卷管理' }
      },
      {
        path: 'exam-generate',
        name: 'ExamGenerate',
        component: () => import('@/views/ExamGenerate.vue'),
        meta: { title: '试卷生成' }
      },
      {
        path: 'grading',
        name: 'Grading',
        component: () => import('@/views/Grading.vue'),
        meta: { title: '试卷批改' }
      },
      {
        path: 'score-analysis',
        name: 'ScoreAnalysis',
        component: () => import('@/views/ScoreAnalysis.vue'),
        meta: { title: '成绩分析' }
      },
      {
        path: 'wrong-questions',
        name: 'WrongQuestions',
        component: () => import('@/views/WrongQuestions.vue'),
        meta: { title: '错题记录' }
      },
      {
        path: 'student-profile',
        name: 'StudentProfile',
        component: () => import('@/views/StudentProfile.vue'),
        meta: { title: '学生画像' }
      },
      {
        path: 'class-profile',
        name: 'ClassProfile',
        component: () => import('@/views/ClassProfile.vue'),
        meta: { title: '班级画像' }
      },
      {
        path: 'ppt-maker',
        name: 'PptMaker',
        component: () => import('@/views/PptMaker.vue'),
        meta: { title: 'PPT制作' }
      },
      {
        path: 'user-manage',
        name: 'UserManage',
        component: () => import('@/views/UserManage.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'class-manage',
        name: 'ClassManage',
        component: () => import('@/views/ClassManage.vue'),
        meta: { title: '班级管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router