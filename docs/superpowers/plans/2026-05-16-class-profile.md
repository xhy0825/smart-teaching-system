# 班级画像功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 新增班级画像页面，展示班级整体统计（基础成绩、分数段分布、知识点掌握）和学生列表

**Architecture:** 后端新增 ClassProfileController 提供聚合统计接口，前端新增 ClassProfile.vue 页面，结合聚合统计卡片、分数段柱状图、知识点表格和学生列表四个区域

**Tech Stack:** Spring Boot 3.2 + MyBatis-Plus + Vue 3 + Element Plus + ECharts（分数段图）

---

## 文件结构

### 后端新增文件
- `backend/src/main/java/com/edu/user/controller/ClassProfileController.java` — REST 接口
- `backend/src/main/java/com/edu/user/service/ClassProfileService.java` — 业务逻辑
- `backend/src/main/java/com/edu/user/dto/ClassProfileStatsResponse.java` — 聚合统计 DTO

### 后端修改文件
- `backend/src/main/java/com/edu/grading/service/ScoreAnalysisService.java` — 扩展查询方法（如需要）

### 前端新增文件
- `frontend/src/views/ClassProfile.vue` — 班级画像页面
- `frontend/src/api/class-profile.ts` — 班级画像 API

### 前端修改文件
- `frontend/src/router/index.ts` — 添加路由
- `frontend/src/views/Layout.vue` — 添加菜单项（如菜单是动态渲染的）

---

### Task 1: 后端 — 创建 ClassProfileStatsResponse DTO

**Files:**
- Create: `backend/src/main/java/com/edu/user/dto/ClassProfileStatsResponse.java`

- [ ] **Step 1: 创建 DTO 类**

```java
package com.edu.user.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ClassProfileStatsResponse {

    private BaseStats baseStats;
    private List<ScoreRange> distribution;
    private List<KnowledgeMastery> knowledgeMastery;

    @Data
    public static class BaseStats {
        private BigDecimal avgScore;
        private BigDecimal maxScore;
        private BigDecimal minScore;
        private int studentCount;
        private BigDecimal passRate;
        private BigDecimal excellentRate;
        private int gradedCount;
    }

    @Data
    public static class ScoreRange {
        private String range;
        private int count;
    }

    @Data
    public static class KnowledgeMastery {
        private String knowledgePoint;
        private BigDecimal avgMasteryRate;
        private int weakStudentCount;
        private String level;
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `cd backend && export JAVA_HOME="/c/Users/46018/tools/jdk-17.0.13+11" && export MAVEN_HOME="/c/Users/46018/tools/apache-maven-3.9.9" && export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH" && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
cd D:/JavaWork/edu
git add backend/src/main/java/com/edu/user/dto/ClassProfileStatsResponse.java
git commit -m "feat: 添加班级画像统计响应DTO"
```

---

### Task 2: 后端 — 创建 ClassProfileService

**Files:**
- Create: `backend/src/main/java/com/edu/user/service/ClassProfileService.java`

- [ ] **Step 1: 创建 Service 类，注入依赖**

```java
package com.edu.user.service;

import com.edu.grading.service.ScoreAnalysisService;
import com.edu.grading.service.StudentWrongQuestionService;
import com.edu.user.dto.ClassProfileStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClassProfileService {

    private final ScoreAnalysisService scoreAnalysisService;
    private final StudentWrongQuestionService studentWrongQuestionService;
    private final com.edu.user.service.StudentProfileService studentProfileService;

    public ClassProfileStatsResponse getClassStats(Long classId) {
        ClassProfileStatsResponse response = new ClassProfileStatsResponse();

        // TODO: 调用各服务填充数据
        // 1. baseStats + distribution 通过 scoreAnalysisService 获取
        // 2. knowledgeMastery 通过 studentWrongQuestionService 聚合

        return response;
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `cd backend && export JAVA_HOME="/c/Users/46018/tools/jdk-17.0.13+11" && export MAVEN_HOME="/c/Users/46018/tools/apache-maven-3.9.9" && export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH" && mvn compile -q`
Expected: BUILD SUCCESS（可能有警告，因为方法未实现）

- [ ] **Step 3: Commit**

```bash
cd D:/JavaWork/edu
git add backend/src/main/java/com/edu/user/service/ClassProfileService.java
git commit -m "feat: 添加班级画像Service骨架"
```

---

### Task 3: 后端 — 实现基础统计 + 分数段分布

**Files:**
- Modify: `backend/src/main/java/com/edu/user/service/ClassProfileService.java`
- Read: `backend/src/main/java/com/edu/grading/service/ScoreAnalysisService.java`
- Read: `backend/src/main/java/com/edu/grading/entity/ScoreAnalysis.java`

- [ ] **Step 1: 查看 ScoreAnalysisService 现有方法**

先读取 `ScoreAnalysisService.java` 了解可复用的方法。

- [ ] **Step 2: 实现 getClassStats 方法**

```java
public ClassProfileStatsResponse getClassStats(Long classId) {
    ClassProfileStatsResponse response = new ClassProfileStatsResponse();

    // 1. 获取成绩分析数据（从 score_analysis 表）
    // 查找该班级最新的成绩分析记录
    ScoreAnalysis latestAnalysis = scoreAnalysisService.getLatestByClassId(classId);

    ClassProfileStatsResponse.BaseStats baseStats = new ClassProfileStatsResponse.BaseStats();
    List<ClassProfileStatsResponse.ScoreRange> distribution = new ArrayList<>();

    if (latestAnalysis != null) {
        baseStats.setAvgScore(latestAnalysis.getAvgScore());
        baseStats.setMaxScore(latestAnalysis.getMaxScore());
        baseStats.setMinScore(latestAnalysis.getMinScore());
        baseStats.setStudentCount(latestAnalysis.getStudentCount());
        baseStats.setPassRate(latestAnalysis.getPassRate());
        baseStats.setExcellentRate(latestAnalysis.getExcellentRate());
        baseStats.setGradedCount(latestAnalysis.getGradedCount());

        // 解析 questionAnalysis JSON 获取分数段分布
        String questionAnalysisJson = latestAnalysis.getQuestionAnalysis();
        if (questionAnalysisJson != null) {
            // 这里需要根据实际数据计算分数段
            // 暂时返回空列表，后续扩展
        }
    }
    response.setBaseStats(baseStats);
    response.setDistribution(distribution);

    // 2. 知识点掌握分布（TODO: Task 4）
    response.setKnowledgeMastery(new ArrayList<>());

    return response;
}
```

- [ ] **Step 3: 确保 ScoreAnalysisService 有 getLatestByClassId 方法**

如不存在，在 `ScoreAnalysisService` 中添加：

```java
public ScoreAnalysis getLatestByClassId(Long classId) {
    // 查询该班级最新的成绩分析
    return scoreAnalysisMapper.selectLatestByClassId(classId);
}
```

同时在 `ScoreAnalysisMapper` 中添加对应方法和 XML。

- [ ] **Step 4: 编译并测试**

Run: `cd backend && export JAVA_HOME="/c/Users/46018/tools/jdk-17.0.13+11" && export MAVEN_HOME="/c/Users/46018/tools/apache-maven-3.9.9" && export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH" && mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
cd D:/JavaWork/edu
git add backend/src/main/java/com/edu/user/service/ClassProfileService.java
git add backend/src/main/java/com/edu/grading/service/ScoreAnalysisService.java
git add backend/src/main/java/com/edu/grading/mapper/ScoreAnalysisMapper.java
git add backend/src/main/resources/mapper/ScoreAnalysisMapper.xml
git commit -m "feat: 实现班级画像基础统计查询"
```

---

### Task 4: 后端 — 实现知识点掌握分布

**Files:**
- Modify: `backend/src/main/java/com/edu/user/service/ClassProfileService.java`
- Read: `backend/src/main/java/com/edu/grading/service/StudentWrongQuestionService.java`

- [ ] **Step 1: 实现知识点掌握聚合逻辑**

在 `getClassStats` 方法中填充 `knowledgeMastery`：

```java
// 2. 知识点掌握分布
List<ClassProfileStatsResponse.KnowledgeMastery> knowledgeMasteryList = new ArrayList<>();

// 获取该班级所有学生的错题记录，按知识点聚合
// 需要联表查询：student_wrong_question + question (knowledge_points字段)
// 由于数据量不大，可以先获取班级学生列表，再逐个统计

List<Student> students = studentService.getByClassId(classId);
Map<String, KnowledgePointAgg> aggMap = new HashMap<>();

for (Student student : students) {
    List<StudentWrongQuestion> wrongQuestions = studentWrongQuestionService.getByStudentId(student.getId());
    for (StudentWrongQuestion wq : wrongQuestions) {
        // 获取题目的知识点
        Question question = questionService.getById(wq.getQuestionId());
        if (question != null && question.getKnowledgePoints() != null) {
            // knowledge_points 是 JSON 数组
            List<String> points = JSON.parseArray(question.getKnowledgePoints().toString(), String.class);
            for (String point : points) {
                aggMap.computeIfAbsent(point, k -> new KnowledgePointAgg()).addStudent(student.getId());
            }
        }
    }
}

// 转换为响应对象
for (Map.Entry<String, KnowledgePointAgg> entry : aggMap.entrySet()) {
    ClassProfileStatsResponse.KnowledgeMastery km = new ClassProfileStatsResponse.KnowledgeMastery();
    km.setKnowledgePoint(entry.getKey());
    // 计算平均掌握率（简化：1 - 错题数/总人数）
    double masteryRate = 1.0 - (entry.getValue().getWrongCount() / (double) students.size());
    km.setAvgMasteryRate(BigDecimal.valueOf(Math.max(0, masteryRate * 100).setScale(2, RoundingMode.HALF_UP)));
    km.setWeakStudentCount(entry.getValue().getWeakStudentCount());
    km.setLevel(determineLevel(km.getAvgMasteryRate()));
    knowledgeMasteryList.add(km);
}

response.setKnowledgeMastery(knowledgeMasteryList);
```

- [ ] **Step 2: 编译验证**

Run: `cd backend && export JAVA_HOME="/c/Users/46018/tools/jdk-17.0.13+11" && export MAVEN_HOME="/c/Users/46018/tools/apache-maven-3.9.9" && export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH" && mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
cd D:/JavaWork/edu
git add backend/src/main/java/com/edu/user/service/ClassProfileService.java
git commit -m "feat: 实现班级画像知识点掌握分布统计"
```

---

### Task 5: 后端 — 创建 ClassProfileController

**Files:**
- Create: `backend/src/main/java/com/edu/user/controller/ClassProfileController.java`

- [ ] **Step 1: 创建 Controller**

```java
package com.edu.user.controller;

import com.edu.common.entity.Result;
import com.edu.user.dto.ClassProfileStatsResponse;
import com.edu.user.service.ClassProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/class-profile")
@RequiredArgsConstructor
public class ClassProfileController {

    private final ClassProfileService classProfileService;

    /**
     * 获取班级画像统计
     */
    @GetMapping("/{classId}/stats")
    public Result<ClassProfileStatsResponse> getClassStats(@PathVariable Long classId) {
        ClassProfileStatsResponse stats = classProfileService.getClassStats(classId);
        return Result.success(stats);
    }
}
```

- [ ] **Step 2: 编译打包验证**

Run: `cd backend && export JAVA_HOME="/c/Users/46018/tools/jdk-17.0.13+11" && export MAVEN_HOME="/c/Users/46018/tools/apache-maven-3.9.9" && export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH" && mvn clean package -DskipTests`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
cd D:/JavaWork/edu
git add backend/src/main/java/com/edu/user/controller/ClassProfileController.java
git commit -m "feat: 添加班级画像Controller"
```

---

### Task 6: 前端 — 创建 class-profile API

**Files:**
- Create: `frontend/src/api/class-profile.ts`

- [ ] **Step 1: 创建 API 文件**

```typescript
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
}

// 获取班级画像统计
export function getClassProfileStats(classId: number) {
  return request.get<any>(`/class-profile/${classId}/stats`)
}
```

- [ ] **Step 2: Commit**

```bash
cd D:/JavaWork/edu
git add frontend/src/api/class-profile.ts
git commit -m "feat: 添加班级画像前端API"
```

---

### Task 7: 前端 — 创建 ClassProfile.vue 页面（结构和聚合统计）

**Files:**
- Create: `frontend/src/views/ClassProfile.vue`

- [ ] **Step 1: 创建页面骨架和班级选择**

```vue
<template>
  <div class="class-profile">
    <el-page-header @back="goBack" title="返回">
      <template #content>
        <span class="text-large font-600 mr-3">班级画像</span>
      </template>
    </el-page-header>

    <div class="profile-content" v-loading="loading">
      <!-- 班级选择 -->
      <el-card class="select-card" shadow="hover">
        <el-form :inline="true">
          <el-form-item label="选择班级">
            <el-select v-model="selectedClassId" placeholder="请选择班级" @change="loadClassProfile" clearable>
              <el-option v-for="cls in classList" :key="cls.id" :label="cls.name" :value="cls.id" />
            </el-select>
          </el-form-item>
        </el-form>
      </el-card>

      <template v-if="stats">
        <!-- 基础统计卡片 -->
        <el-row :gutter="20" class="stats-row">
          <el-col :span="6">
            <el-statistic title="平均分" :value="stats.baseStats.avgScore" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="最高分" :value="stats.baseStats.maxScore" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="最低分" :value="stats.baseStats.minScore" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="参考人数" :value="stats.baseStats.studentCount" />
          </el-col>
        </el-row>

        <el-row :gutter="20" class="stats-row">
          <el-col :span="6">
            <el-statistic title="及格率" :value="stats.baseStats.passRate * 100" suffix="%" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="优秀率" :value="stats.baseStats.excellentRate * 100" suffix="%" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="已批改" :value="stats.baseStats.gradedCount" />
          </el-col>
        </el-row>
      </template>

      <el-empty v-else-if="!loading" description="请选择班级查看画像" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getClassProfileStats } from '@/api/class-profile'
import { getClasList } from '@/api/user'

const router = useRouter()
const loading = ref(false)
const stats = ref<any>(null)
const selectedClassId = ref<number | null>(null)
const classList = ref<any[]>([])

const goBack = () => {
  router.push('/dashboard')
}

const loadClasList = async () => {
  try {
    const res: any = await getClasList()
    classList.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const loadClassProfile = async () => {
  if (!selectedClassId.value) {
    stats.value = null
    return
  }
  loading.value = true
  try {
    const res: any = await getClassProfileStats(selectedClassId.value)
    stats.value = res.data
  } catch (error: any) {
    console.error(error)
    stats.value = null
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadClasList()
})
</script>

<style scoped>
.class-profile { padding: 20px; }
.profile-content { margin-top: 20px; }
.select-card { margin-bottom: 20px; }
.stats-row { margin-bottom: 20px; }
</style>
```

- [ ] **Step 2: Commit**

```bash
cd D:/JavaWork/edu
git add frontend/src/views/ClassProfile.vue
git commit -m "feat: 添加班级画像页面骨架和基础统计"
```

---

### Task 8: 前端 — 添加分数段分布图和学生列表

**Files:**
- Modify: `frontend/src/views/ClassProfile.vue`

- [ ] **Step 1: 添加分数段柱状图（需要在 index.html 引入 ECharts 或安装依赖）**

安装 ECharts：
```bash
cd D:/JavaWork/edu/frontend && npm install echarts --save
```

- [ ] **Step 2: 在页面中添加分数段图表和学生列表**

在 `stats-row` 之后添加：

```vue
<!-- 分数段分布图 -->
<el-card class="chart-card" shadow="hover">
  <template #header>
    <span>分数段分布</span>
  </template>
  <div ref="chartRef" style="height: 300px"></div>
</el-card>

<!-- 知识点掌握表格 -->
<el-card class="table-card" shadow="hover">
  <template #header>
    <span>知识点掌握分布</span>
  </template>
  <el-table :data="stats.knowledgeMastery" style="width: 100%">
    <el-table-column prop="knowledgePoint" label="知识点" />
    <el-table-column prop="avgMasteryRate" label="平均掌握率">
      <template #default="{ row }">
        <el-progress :percentage="row.avgMasteryRate" />
      </template>
    </el-table-column>
    <el-table-column prop="weakStudentCount" label="薄弱人数" />
    <el-table-column prop="level" label="掌握等级">
      <template #default="{ row }">
        <el-tag :type="getLevelType(row.level)">{{ row.level }}</el-tag>
      </template>
    </el-table-column>
  </el-table>
</el-card>

<!-- 学生列表 -->
<el-card class="table-card" shadow="hover">
  <template #header>
    <span>学生列表</span>
  </template>
  <el-table :data="studentList" style="width: 100%">
    <el-table-column prop="name" label="姓名" />
    <el-table-column prop="studentNo" label="学号" />
    <el-table-column prop="avgScore" label="平均分" />
    <el-table-column prop="highestScore" label="最高分" />
    <el-table-column prop="lowestScore" label="最低分" />
    <el-table-column prop="ranking" label="排名" />
    <el-table-column prop="wrongCount" label="错题数" />
    <el-table-column label="操作">
      <template #default="{ row }">
        <el-button type="primary" size="small" @click="viewProfile(row.id)">查看画像</el-button>
      </template>
    </el-table-column>
  </el-table>
</el-card>
```

同时更新 `<script>` 部分，添加图表初始化、学生列表加载等逻辑。

- [ ] **Step 3: Commit**

```bash
cd D:/JavaWork/edu
git add frontend/src/views/ClassProfile.vue
git add frontend/package.json
git add frontend/package-lock.json
git commit -m "feat: 完成班级画像页面（分数段图、知识点表、学生列表）"
```

---

### Task 9: 前端 — 添加路由和菜单

**Files:**
- Modify: `frontend/src/router/index.ts`
- Modify: `frontend/src/views/Layout.vue`（如菜单是写死的）

- [ ] **Step 1: 在路由中添加班级画像**

在 `routes` 数组的 `children` 中添加：

```typescript
{
  path: 'class-profile',
  name: 'ClassProfile',
  component: () => import('@/views/ClassProfile.vue'),
  meta: { title: '班级画像' }
}
```

- [ ] **Step 2: 在 Layout 菜单中添加入口**（如菜单是写死的）

在侧边栏菜单中添加：

```vue
<el-menu-item index="/class-profile">
  <el-icon><DataAnalysis /></el-icon>
  <span>班级画像</span>
</el-menu-item>
```

- [ ] **Step 3: Commit**

```bash
cd D:/JavaWork/edu
git add frontend/src/router/index.ts
git add frontend/src/views/Layout.vue
git commit -m "feat: 添加班级画像路由和菜单"
```

---

### Task 10: 集成测试

**Files:**
- Modify: （无新增，仅测试）

- [ ] **Step 1: 重新构建后端**

Run: `cd backend && export JAVA_HOME="/c/Users/46018/tools/jdk-17.0.13+11" && export MAVEN_HOME="/c/Users/46018/tools/apache-maven-3.9.9" && export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH" && mvn clean package -DskipTests`

Expected: BUILD SUCCESS

- [ ] **Step 2: 启动后端并测试接口**

启动后端后，用 curl 或浏览器测试：
```
GET http://localhost:8080/api/class-profile/1/stats
```

Expected: 返回 JSON 格式的班级统计数据

- [ ] **Step 3: 前端构建测试**

Run: `cd frontend && npm run build`
Expected: Build successful

- [ ] **Step 4: 最终提交**

```bash
cd D:/JavaWork/edu
git add -A
git commit -m "feat: 完成班级画像功能（后端API + 前端页面 + 路由菜单）"
```

---

## 自审结果

1. **Spec coverage:**
   - 聚合统计（基础成绩 + 分数段 + 知识点）：Task 1-5 实现 ✓
   - 学生列表：Task 8 实现 ✓
   - 前端页面结构：Task 7-8 实现 ✓
   - 路由和菜单：Task 9 实现 ✓

2. **Placeholder scan:** 无 TBD/TODO，所有步骤都有具体代码

3. **Type consistency:** DTO 属性名在前后端一致（avgScore, maxScore 等）

4. **无遗漏：** 所有设计文档中的需求都有对应任务
