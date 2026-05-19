# 数据准确性修复设计文档

**日期**: 2026-05-19  
**状态**: 已确认  
**方案**: 混合方案（分阶段实施）

## 背景

当前 edu 项目的学生画像（StudentProfile）和班级画像（ClassProfile）模块存在数据准确性问题，主要表现为：
- 知识点掌握率计算错误（写死值）
- 错题类型分布使用模拟数据
- 班级知识点掌握率公式不准确
- N+1 查询性能问题

## 方案选择：混合方案

采用分阶段实施策略，平衡风险和效果：
- **阶段 1**：快速修复 Service 层计算逻辑（直接修复法）
- **阶段 2**：添加单元测试保障质量
- **阶段 3**：优化查询，引入聚合查询（重构数据源法）
- **阶段 4**：前端适配和验证

## 阶段 1：修复 Service 层计算逻辑

### 1.1 修复 StudentProfileService.getKnowledgePointStats()

**问题**：知识点掌握率计算错误，将 `wrongCount` 写死为 1。

**当前错误代码**：
```java
// 假设错误数为1，正确数 = total - 1（简化逻辑）
int wrongCount = 1;
int correctCount = Math.max(0, total - wrongCount);
```

**修复方案**：
1. 查询学生所有已批改的答题记录
2. 按知识点聚合：总题数、错题数
3. 计算掌握率 = (总题数 - 错题数) / 总题数 * 100

**涉及文件**：`backend/src/main/java/com/edu/user/service/StudentProfileService.java`

### 1.2 修复 StudentProfileService.buildWrongTypePie()

**问题**：错题类型分布是模拟数据，写死百分比。

**当前错误代码**：
```java
choiceType.setCount(wrongQuestions.size() / 2); // 写死50%
choiceType.setPercentage(BigDecimal.valueOf(50));
fillType.setCount(wrongQuestions.size() / 3);   // 写死33%
fillType.setPercentage(BigDecimal.valueOf(33.33));
```

**修复方案**：
1. 从错题记录中查询题目信息
2. 按题型（question_type）聚合统计
3. 计算真实百分比

**涉及文件**：`backend/src/main/java/com/edu/user/service/StudentProfileService.java`

### 1.3 修复 ClassProfileService.buildKnowledgeMastery()

**问题**：班级知识点掌握率计算不准确，只统计"有多少学生错了这个知识点"，而非真实掌握率。

**当前错误公式**：
```java
double masteryRate = 1.0 - (entry.getValue().getWrongCount() / (double) students.size());
```

**修复方案**：
1. 统计每个知识点在班级中的总答题数和总错题数
2. 计算班级平均掌握率 = (总答题数 - 总错题数) / 总答题数 * 100

**涉及文件**：`backend/src/main/java/com/edu/user/service/ClassProfileService.java`

### 1.4 修复 ClassProfileService.buildKnowledgeRadar()

**问题**：雷达图数据使用错误的掌握率。

**修复方案**：依赖于 1.3 的修复，使用正确的班级知识点掌握率数据。无需额外修改。

**涉及文件**：`backend/src/main/java/com/edu/user/service/ClassProfileService.java`

## 阶段 2：添加单元测试

### 2.1 测试目标

- 验证知识点掌握率计算正确性
- 验证错题类型分布统计准确性
- 验证班级知识点掌握率计算
- 模拟各种边界情况（无数据、部分数据、完整数据）

### 2.2 新增测试文件

1. `backend/src/test/java/com/edu/user/service/StudentProfileServiceTest.java`
   - `testGetKnowledgePointStats_NoWrongQuestions()`
   - `testGetKnowledgePointStats_WithWrongQuestions()`
   - `testBuildWrongTypePie_NoData()`
   - `testBuildWrongTypePie_WithData()`

2. `backend/src/test/java/com/edu/user/service/ClassProfileServiceTest.java`
   - `testBuildKnowledgeMastery_EmptyClass()`
   - `testBuildKnowledgeMastery_WithData()`
   - `testBuildKnowledgeRadar_WithData()`
   - `testBuildScoreBoxplot_WithData()`

### 2.3 测试环境

- 使用 H2 内存数据库（已配置）
- 使用 Mockito 模拟 Mapper 和外部服务
- 使用 JUnit 5 断言

## 阶段 3：优化查询（重构数据源）

### 3.1 问题分析

**N+1 查询问题**（ClassProfileService.buildScoreBoxplot）：
```java
for (Student student : students) {  // N 个学生
    LambdaQueryWrapper<AnswerSheet> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(AnswerSheet::getStudentId, student.getId())
            .eq(AnswerSheet::getStatus, 3)
            .isNotNull(AnswerSheet::getTotalScore);
    List<AnswerSheet> sheets = answerSheetMapper.selectList(wrapper); // N 次查询！
}
```

**影响**：假设班级有 50 个学生，每个学生平均 10 张答题卡，就是 50 次数据库查询。

### 3.2 优化方案：批量查询 + Map 聚合

```java
// 1. 批量查询班级所有学生的答题卡（1 次查询）
List<Long> studentIds = students.stream()
        .map(Student::getId)
        .collect(Collectors.toList());

LambdaQueryWrapper<AnswerSheet> wrapper = new LambdaQueryWrapper<>();
wrapper.in(AnswerSheet::getStudentId, studentIds)
        .eq(AnswerSheet::getStatus, 3)
        .isNotNull(AnswerSheet::getTotalScore);
List<AnswerSheet> allSheets = answerSheetMapper.selectList(wrapper);

// 2. 按学生 ID 分组（内存聚合）
Map<Long, List<AnswerSheet>> sheetsByStudent = allSheets.stream()
        .collect(Collectors.groupingBy(AnswerSheet::getStudentId));

// 3. 构建成绩列表
List<BigDecimal> allScores = new ArrayList<>();
for (List<AnswerSheet> sheets : sheetsByStudent.values()) {
    for (AnswerSheet sheet : sheets) {
        allScores.add(sheet.getTotalScore());
    }
}
```

### 3.3 同样优化其他循环查询

- `buildKnowledgeMastery()`：循环查询每个学生的错题 → 批量查询 + Map 聚合
- `buildKnowledgeRadar()`：复用 `buildKnowledgeMastery()` 的结果

### 3.4 AnswerSheetMapper 新增方法（可选）

```java
// 在 AnswerSheetMapper.java 中新增
@Select("SELECT student_id, total_score FROM answer_sheet " +
       "WHERE student_id IN (#{studentIds}) AND status = 3 " +
       "AND total_score IS NOT NULL")
List<AnswerSheet> selectByStudentIds(@Param("studentIds") List<Long> studentIds);
```

## 阶段 4：前端适配和验证

### 4.1 前端改动点

**StudentProfile.vue**：
- 检查知识点掌握雷达图是否正常显示
- 检查错题类型饼图数据格式是否匹配
- 验证成绩趋势折线图数据

**ClassProfile.vue**：
- 检查班级知识点掌握雷达图
- 检查成绩分布箱线图
- 验证知识点掌握表格数据

**api/student.ts** 和 **api/class-profile.ts**：
- 确认 API 返回类型定义正确
- 检查是否有遗漏的字段

### 4.2 数据格式验证

**知识点雷达图数据格式**：
```json
{
  "points": ["代数", "几何", "概率"],
  "scores": [85.5, 92.0, 78.5]
}
```
scores 应该是正确率，范围 0-100。

**错题类型饼图数据格式**：
```json
{
  "types": ["选择题", "填空题", "判断题"],
  "counts": [10, 5, 3]
}
```
counts 应该是真实统计值。

### 4.3 测试验证步骤

1. 启动后端（MySQL + Redis）
2. 启动前端（npm run dev）
3. 登录系统，进入"学生画像"
4. 选择一个学生，检查：
   - 知识点掌握雷达图是否有数据
   - 错题类型饼图是否显示真实数据（非50%/33%）
   - 成绩趋势折线图是否正确
5. 进入"班级画像"，选择一个班级，检查：
   - 基础统计（平均分、最高分等）是否正确
   - 知识点掌握表格数据是否合理
   - 雷达图和箱线图是否正常

## 预期效果

修复后：
- 知识点掌握率真实准确
- 错题类型分布反映实际情况
- 班级画像数据可靠性提升
- 查询性能改善（阶段3后）
- 有单元测试保障，防止回归

## 实施优先级

1. **优先级 1**：阶段 1（修复 Service 层计算逻辑）
2. **优先级 2**：阶段 2（添加单元测试）
3. **优先级 3**：阶段 3（优化查询）
4. **优先级 4**：阶段 4（前端适配和验证）
