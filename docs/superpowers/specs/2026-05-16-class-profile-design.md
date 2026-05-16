# 班级画像功能设计

## 概述

在现有学生画像基础上，新增"班级画像"功能，展示选定班级的整体统计数据和学生列表。页面结合聚合统计与学生明细两种视角。

## 后端设计

### 新增接口

`ClassProfileController.java`（包：`com.edu.user.controller`）

| 接口 | 说明 |
|------|------|
| `GET /api/class-profile/{classId}/stats` | 返回班级聚合统计 |

### 返回 DTO 结构

```java
class ClassProfileStats {
    BaseStats baseStats;           // 基础统计
    List<ScoreRange> distribution;  // 分数段分布
    List<KnowledgeMastery> knowledgeMastery; // 知识点掌握分布
}

class BaseStats {
    BigDecimal avgScore;    // 平均分
    BigDecimal maxScore;    // 最高分
    BigDecimal minScore;    // 最低分
    int studentCount;       // 参考人数
    BigDecimal passRate;    // 及格率
    BigDecimal excellentRate; // 优秀率
    int gradedCount;        // 已批改数
}

class ScoreRange {
    String range;   // "90+", "80-89", "70-79", "60-69", "<60"
    int count;      // 该分数段人数
}

class KnowledgeMastery {
    String knowledgePoint;   // 知识点
    BigDecimal avgMasteryRate; // 班级平均掌握率
    int weakStudentCount;      // 掌握率<60%的人数
    String level;             // 掌握等级（优秀/良好/一般/薄弱）
}
```

### 数据来源

- `baseStats` + `distribution`：复用 `ScoreAnalysisService` 查询 `score_analysis` 表，或基于 `answer_sheet` + `answer` 表实时计算
- `knowledgeMastery`：基于 `student_wrong_question` + `question.knowledge_points` 聚合
- 学生列表：复用现有 `GET /api/student-profile/class/{classId}`

## 前端设计

### 新增文件

- `frontend/src/views/ClassProfile.vue` — 班级画像页面
- `frontend/src/api/class-profile.ts` — 班级画像 API（1个新接口）

### 页面结构

1. **班级选择**：顶部下拉框选择班级
2. **聚合统计区域**（3行卡片）：
   - 行1: 平均分 | 最高分 | 最低分 | 参考人数
   - 行2: 及格率 | 优秀率 | 已批改数
   - 行3: 分数段分布图（柱状图，5个分数段）
3. **知识点掌握分布表格**：知识点 | 班级平均掌握率 | 薄弱人数 | 掌握等级
4. **学生列表区域**：搜索框 + 排序，表格展示：
   - 列：姓名 | 学号 | 平均分 | 最高分 | 最低分 | 排名 | 错题数 | 操作（查看画像）
   - "查看画像"跳转到 `/student-profile?studentId=xxx`

### 路由与菜单

- 路由路径：`/class-profile`
- 菜单项：在 Layout 侧边栏新增"班级画像"，位于"学生画像"下方
- 复用现有 `getStudentProfilesByClass(classId)` API 获取学生列表

## 数据流

```
用户选择班级
  → 调用 GET /api/class-profile/{classId}/stats → 展示聚合统计
  → 调用 GET /api/student-profile/class/{classId} → 展示学生列表
  → 点击"查看画像" → 跳转到 /student-profile?studentId=xxx
```

## 自审结果

- 无 TBD/TODO 占位符
- 后端复用现有接口，新增1个统计接口
- 前端新增1个页面和1个API文件
- 范围明确，适合单次实现
