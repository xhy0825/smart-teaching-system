# 学生画像与班级画像图表增强设计

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:writing-plans to create implementation plan after design approval.

**Goal:** 为学生画像和班级画像页面新增图表展示（雷达图、折线图、饼图、箱线图）

**Architecture:** 前后端分离，后端Service层计算图表数据并写入Response DTO，前端Vue组件使用ECharts渲染图表。保持现有API接口不变，仅在响应DTO中新增图表数据字段。

**Tech Stack:** Spring Boot 3.2.0 + MyBatis-Plus + Vue 3 + Element Plus + ECharts 5

---

## 1. 需求概述

### 1.1 学生画像图表（3个）
- **A. 成绩趋势折线图**：按考试展示学生历次成绩变化
- **B. 知识点掌握雷达图**：展示学生在各知识点上的掌握率
- **C. 错题类型分布饼图**：展示各类题型（选择题、填空题等）的错题占比

### 1.2 班级画像图表（2个）
- **A. 知识点掌握雷达图**：展示班级整体在各知识点上的掌握率
- **B. 学生成绩分布箱线图**：展示班级学生成绩的五数概括（最小、Q1、中位、Q3、最大）

---

## 2. 后端设计

### 2.1 DTO扩展

#### StudentProfileResponse 新增字段

```java
// 知识点雷达图数据
private KnowledgeRadar knowledgeRadar;

// 错题类型饼图数据
private WrongTypePie wrongTypePie;

// 内部类
@Data
public static class KnowledgeRadar {
    private List<String> points;      // 知识点名称列表（雷达图维度）
    private List<BigDecimal> scores;  // 对应掌握率（0-100）
}

@Data
public static class WrongTypePie {
    private List<String> types;        // 题型列表
    private List<Integer> counts;      // 对应错题数量
}
```

#### ClassProfileStatsResponse 新增字段

```java
// 知识点雷达图数据
private KnowledgeRadar knowledgeRadar;

// 成绩箱线图数据
private ScoreBoxplot scoreBoxplot;

// 内部类
@Data
public static class KnowledgeRadar {
    private List<String> points;
    private List<BigDecimal> scores;
}

@Data
public static class ScoreBoxplot {
    private BigDecimal min;
    private BigDecimal q1;
    private BigDecimal median;
    private BigDecimal q3;
    private BigDecimal max;
    private List<BigDecimal> outliers;  // 异常值（可选）
}
```

### 2.2 Service层改动

#### StudentProfileService

**完善 `getKnowledgePointStats` 方法** - 从数据库真实统计：
1. 查询学生错题记录（`student_wrong_question`）
2. 关联查询题目表（`question`）获取知识点
3. 按知识点聚合：总题数、正确题数
4. 计算掌握率 = 正确数 / 总题数 × 100
5. 同时填充 `knowledgeRadar` 数据

**新增 `getWrongTypeStats` 方法** - 错题类型统计：
1. 查询学生错题记录
2. 关联查询题目表获取题型（`question_type`）
3. 按题型聚合计数
4. 填充 `wrongTypePie` 数据

**修改 `getStudentProfile` 方法** - 填充新字段：
```java
// 知识点雷达图
response.setKnowledgeRadar(buildKnowledgeRadar(studentId));

// 错题类型饼图
response.setWrongTypePie(buildWrongTypePie(studentId));
```

#### ClassProfileService

**新增 `buildKnowledgeRadar` 方法** - 班级知识点雷达图：
1. 获取班级所有学生
2. 汇总所有学生的错题知识点
3. 按知识点计算班级整体掌握率
4. 返回雷达图数据

**新增 `buildScoreBoxplot` 方法** - 成绩箱线图：
1. 查询班级所有学生的已批改答题卡（`answer_sheet`，status=3）
2. 收集所有成绩到列表
3. 排序后计算：最小值、Q1（25%）、中位数（50%）、Q3（75%）、最大值
4. 可选：检测并标记异常值（< Q1 - 1.5×IQR 或 > Q3 + 1.5×IQR）
5. 优先从 `score_analysis` 表读取（若已存储），否则实时计算

**修改 `getClassStats` 方法** - 填充新字段：
```java
// 知识点雷达图
response.setKnowledgeRadar(buildKnowledgeRadar(classId));

// 成绩箱线图
response.setScoreBoxplot(buildScoreBoxplot(classId));
```

### 2.3 数据源说明

- **知识点**：来自 `question` 表的 `knowledge_points` 字段（JSON数组格式）
- **成绩数据**：来自 `answer_sheet` 表的 `total_score` 字段（status=3 已批改）
- **错题数据**：来自 `student_wrong_question` 表
- **题型**：来自 `question` 表的 `question_type` 字段

---

## 3. 前端设计

### 3.1 StudentProfile.vue 改动

**新增图表容器**：
```html
<!-- 成绩趋势折线图 -->
<el-card class="chart-card" shadow="hover">
  <template #header><span>成绩趋势</span></template>
  <div ref="trendChartRef" style="height: 300px"></div>
</el-card>

<!-- 知识点掌握雷达图 -->
<el-card class="chart-card" shadow="hover">
  <template #header><span>知识点掌握雷达图</span></template>
  <div ref="radarChartRef" style="height: 300px"></div>
</el-card>

<!-- 错题类型饼图 -->
<el-card class="chart-card" shadow="hover">
  <template #header><span>错题类型分布</span></template>
  <div ref="pieChartRef" style="height: 300px"></div>
</el-card>
```

**ECharts配置**：
- 折线图：x轴=考试名称/日期，y轴=成绩，平滑曲线
- 雷达图：indicator=知识点列表，value=掌握率，面积填充
- 饼图：各题型占比，显示百分比，环形图

### 3.2 ClassProfile.vue 改动

**新增图表容器**：
```html
<!-- 知识点掌握雷达图 -->
<el-card class="chart-card" shadow="hover">
  <template #header><span>班级知识点掌握雷达图</span></template>
  <div ref="classRadarRef" style="height: 300px"></div>
</el-card>

<!-- 学生成绩分布箱线图 -->
<el-card class="chart-card" shadow="hover">
  <template #header><span>成绩分布箱线图</span></template>
  <div ref="boxplotRef" style="height: 300px"></div>
</el-card>
```

**ECharts箱线图配置**：
- 使用 ECharts `boxplot` 系列类型
- 数据格式：`[min, Q1, median, Q3, max]`
- 可选：散点图叠加显示异常值

### 3.3 前端数据处理

- 图表初始化在 `nextTick` 中执行
- 使用 `watch` 监听数据变化，自动刷新图表
- 组件卸载时调用 `chartInstance.dispose()` 清理资源
- 图表 resize 响应式处理（可选，后续优化）

---

## 4. 数据流程

### 4.1 学生画像

```
用户选择学生
  → 前端请求 GET /api/student-profile/{studentId}
  → StudentProfileController.getProfile()
  → StudentProfileService.getStudentProfile()
      → calculateScoreStats()      // 成绩统计
      → getKnowledgePointStats()    // 知识点统计（含雷达图数据）
      → calculateWrongStats()       // 错题统计
      → getScoreTrends()           // 成绩趋势
      → buildKnowledgeRadar()     // 雷达图数据（新增）
      → buildWrongTypePie()       // 饼图数据（新增）
  → 返回 StudentProfileResponse（含图表数据）
  → 前端渲染：折线图、雷达图、饼图
```

### 4.2 班级画像

```
用户选择班级
  → 前端请求 GET /api/class-profile/{classId}/stats
  → ClassProfileController.getStats()
  → ClassProfileService.getClassStats()
      → ScoreAnalysisService.getLatestByClassId()  // 基础统计
      → buildKnowledgeMastery()                    // 知识点掌握
      → buildKnowledgeRadar()                     // 雷达图数据（新增）
      → buildScoreBoxplot()                       // 箱线图数据（新增）
  → 返回 ClassProfileStatsResponse（含图表数据）
  → 前端渲染：雷达图、箱线图、柱状图
```

---

## 5. 开发策略

### 5.1 混合方案（用户选择）

**后端数据逻辑 + 前端模拟数据同步开发**：

1. **后端**：先修改DTO添加字段，Service层先返回模拟数据（固定维度），确保接口可用
2. **前端**：基于模拟数据开发图表组件，完成ECharts集成
3. **后端**：逐步完善真实数据逻辑（知识点统计、错题类型、箱线图计算）
4. **联调**：后端真实数据就绪后，前端无缝切换

### 5.2 雷达图维度

**混合方案**：
- 前端开发阶段：使用固定维度（负数、乘法、加法、圆的周长、三角形）
- 后端就绪后：通过接口返回动态维度，前端自适应渲染

---

## 6. 测试计划

### 6.1 后端测试
- Service层单元测试：验证雷达图数据计算逻辑
- Service层单元测试：验证箱线图五数计算正确性
- Controller集成测试：验证API返回包含新字段

### 6.2 前端测试
- 手动测试：检查图表是否正确渲染
- 手动测试：检查数据更新时图表是否自动刷新
- 手动测试：检查图表容器resize行为

---

## 7. 依赖与风险

### 7.1 依赖
- ECharts 5.x（已安装）
- MyBatis-Plus（已有）
- JSON解析（fastjson2，已有）

### 7.2 风险
- **知识点字段格式**：`question`表的`knowledge_points`字段为JSON数组，需确保格式正确
- **数据量**：班级学生较多时，实时计算箱线图可能有性能问题（混合方案已缓解：优先读取预计算数据）
- **租户拦截器**：新增查询需注意`tenant_id`豁免配置（`TenantInterceptor`）

---

## 8. 后续优化（超出当前范围）

- 图表响应式resize
- 图表导出为图片
- 更多图表类型（柱状图、热力图等）
- 知识点掌握预测（基于历史数据）
