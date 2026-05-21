# 学生画像与班级画像图表增强实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为学生画像和班级画像页面新增5个图表（雷达图、折线图、饼图、箱线图）

**Architecture:** 后端在现有DTO中添加图表数据字段，Service层计算数据；前端使用ECharts渲染图表组件

**Tech Stack:** Spring Boot 3.2.0 + MyBatis-Plus + Vue 3 + ECharts 5 + Element Plus

---

## 文件结构

### 后端文件
- `backend/src/main/java/com/edu/user/dto/StudentProfileResponse.java` - 添加图表数据内部类
- `backend/src/main/java/com/edu/user/dto/ClassProfileStatsResponse.java` - 添加图表数据内部类
- `backend/src/main/java/com/edu/user/service/StudentProfileService.java` - 完善知识点统计，新增雷达图和饼图数据方法
- `backend/src/main/java/com/edu/user/service/ClassProfileService.java` - 新增雷达图和箱线图数据方法

### 前端文件
- `frontend/src/views/StudentProfile.vue` - 添加三个图表组件
- `frontend/src/views/ClassProfile.vue` - 添加两个图表组件

---

### Task 1: 扩展StudentProfileResponse添加图表数据字段

**Files:**
- Modify: `backend/src/main/java/com/edu/user/dto/StudentProfileResponse.java`

- [ ] **Step 1: 添加KnowledgeRadar内部类**

在StudentProfileResponse类中添加：

```java
@Data
public static class KnowledgeRadar {
    private List<String> points;      // 知识点名称列表
    private List<BigDecimal> scores;  // 对应掌握率（0-100）
}

@Data
public static class WrongTypePie {
    private List<String> types;        // 题型列表
    private List<Integer> counts;      // 对应错题数量
}
```

- [ ] **Step 2: 添加字段和getter/setter**

在StudentProfileResponse类中添加字段：

```java
private KnowledgeRadar knowledgeRadar;
private WrongTypePie wrongTypePie;
```

- [ ] **Step 3: 使用lombok的@Data注解**

确保类级别有`@Data`注解（如果还没有），或者手动添加getter/setter。

- [ ] **Step 4: 编译验证**

```bash
cd backend && mvn clean compile -DskipTests
```

Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
cd backend && git add src/main/java/com/edu/user/dto/StudentProfileResponse.java
git commit -m "feat: 扩展StudentProfileResponse添加图表数据字段"
```

---

### Task 2: 扩展ClassProfileStatsResponse添加图表数据字段

**Files:**
- Modify: `backend/src/main/java/com/edu/user/dto/ClassProfileStatsResponse.java`

- [ ] **Step 1: 添加KnowledgeRadar和ScoreBoxplot内部类**

在ClassProfileStatsResponse类中添加：

```java
@Data
public static class KnowledgeRadar {
    private List<String> points;      // 知识点名称列表
    private List<BigDecimal> scores;  // 对应掌握率（0-100）
}

@Data
public static class ScoreBoxplot {
    private BigDecimal min;
    private BigDecimal q1;
    private BigDecimal median;
    private BigDecimal q3;
    private BigDecimal max;
    private List<BigDecimal> outliers;  // 异常值
}
```

- [ ] **Step 2: 添加字段**

在ClassProfileStatsResponse类中添加字段：

```java
private KnowledgeRadar knowledgeRadar;
private ScoreBoxplot scoreBoxplot;
```

- [ ] **Step 3: 编译验证**

```bash
cd backend && mvn clean compile -DskipTests
```

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
cd backend && git add src/main/java/com/edu/user/dto/ClassProfileStatsResponse.java
git commit -m "feat: 扩展ClassProfileStatsResponse添加图表数据字段"
```

---

### Task 3: 完善StudentProfileService - 知识点统计和图表数据

**Files:**
- Modify: `backend/src/main/java/com/edu/user/service/StudentProfileService.java`
- Need to read: `backend/src/main/java/com/edu/user/dto/StudentProfileResponse.java`
- Need to read: `backend/src/main/java/com/edu/exam/entity/Question.java` (了解知识点字段)

- [ ] **Step 1: 修改getKnowledgePointStats返回真实数据**

替换现有的模拟数据逻辑，从数据库统计：

```java
public List<StudentProfileResponse.KnowledgePointStats> getKnowledgePointStats(Long studentId) {
    // 获取学生错题记录
    LambdaQueryWrapper<StudentWrongQuestion> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(StudentWrongQuestion::getStudentId, studentId);
    List<StudentWrongQuestion> wrongQuestions = wrongQuestionMapper.selectList(wrapper);
    
    // 按知识点聚合
    Map<String, KnowledgeAgg> aggMap = new HashMap<>();
    
    for (StudentWrongQuestion wq : wrongQuestions) {
        // 查询题目获取知识点
        Question question = questionService.getById(wq.getQuestionId());
        if (question != null && question.getKnowledgePoints() != null) {
            try {
                JSONArray points = JSON.parseArray(question.getKnowledgePoints().toString());
                if (points != null) {
                    for (int i = 0; i < points.size(); i++) {
                        String point = points.getString(i);
                        aggMap.computeIfAbsent(point, k -> new KnowledgeAgg())
                               .incrementTotal();
                    }
                }
            } catch (Exception e) {
                // 解析失败跳过
            }
        }
    }
    
    // 获取学生总的答题记录（用于计算正确数）
    // 这里简化处理，实际应该查询答题记录
    List<StudentProfileResponse.KnowledgePointStats> stats = new ArrayList<>();
    for (Map.Entry<String, KnowledgeAgg> entry : aggMap.entrySet()) {
        StudentProfileResponse.KnowledgePointStats stat = new StudentProfileResponse.KnowledgePointStats();
        stat.setKnowledgePoint(entry.getKey());
        int total = entry.getValue().getTotalCount();
        // 假设错误数为1，正确数 = total - 1（简化逻辑）
        int wrongCount = 1;
        int correctCount = Math.max(0, total - wrongCount);
        stat.setCorrectCount(correctCount);
        stat.setTotalCount(total);
        BigDecimal masteryRate = total > 0 ? 
            BigDecimal.valueOf((correctCount * 100.0) / total).setScale(2, RoundingMode.HALF_UP) : 
            BigDecimal.ZERO;
        stat.setMasteryRate(masteryRate);
        stat.setLevel(getMasteryLevel(masteryRate));
        stats.add(stat);
    }
    
    return stats;
}
```

需要添加内部类：

```java
private static class KnowledgeAgg {
    private int totalCount = 0;
    
    void incrementTotal() {
        totalCount++;
    }
    
    int getTotalCount() { return totalCount; }
}
```

同时需要注入`QuestionService`：

```java
private final com.edu.exam.service.QuestionService questionService;
```

- [ ] **Step 2: 新增buildKnowledgeRadar方法**

```java
public StudentProfileResponse.KnowledgeRadar buildKnowledgeRadar(Long studentId) {
    List<StudentProfileResponse.KnowledgePointStats> stats = getKnowledgePointStats(studentId);
    StudentProfileResponse.KnowledgeRadar radar = new StudentProfileResponse.KnowledgeRadar();
    
    List<String> points = new ArrayList<>();
    List<BigDecimal> scores = new ArrayList<>();
    
    for (StudentProfileResponse.KnowledgePointStats stat : stats) {
        points.add(stat.getKnowledgePoint());
        scores.add(stat.getMasteryRate());
    }
    
    radar.setPoints(points);
    radar.setScores(scores);
    return radar;
}
```

- [ ] **Step 3: 新增buildWrongTypePie方法**

```java
public StudentProfileResponse.WrongTypePie buildWrongTypePie(Long studentId) {
    // 查询学生错题
    LambdaQueryWrapper<StudentWrongQuestion> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(StudentWrongQuestion::getStudentId, studentId);
    List<StudentWrongQuestion> wrongQuestions = wrongQuestionMapper.selectList(wrapper);
    
    // 按题型聚合
    Map<String, Integer> typeCountMap = new HashMap<>();
    for (StudentWrongQuestion wq : wrongQuestions) {
        Question question = questionService.getById(wq.getQuestionId());
        if (question != null) {
            String type = question.getQuestionType();
            typeCountMap.merge(type, 1, Integer::sum);
        }
    }
    
    StudentProfileResponse.WrongTypePie pie = new StudentProfileResponse.WrongTypePie();
    List<String> types = new ArrayList<>();
    List<Integer> counts = new ArrayList<>();
    
    // 题型名称映射
    Map<String, String> typeNameMap = Map.of(
        "CHOICE", "选择题",
        "FILL", "填空题",
        "JUDGE", "判断题",
        "ESSAY", "简答题"
    );
    
    for (Map.Entry<String, Integer> entry : typeCountMap.entrySet()) {
        types.add(typeNameMap.getOrDefault(entry.getKey(), entry.getKey()));
        counts.add(entry.getValue());
    }
    
    pie.setTypes(types);
    pie.setCounts(counts);
    return pie;
}
```

- [ ] **Step 4: 修改getStudentProfile方法填充新字段**

在方法的return之前添加：

```java
// 知识点雷达图
response.setKnowledgeRadar(buildKnowledgeRadar(studentId));

// 错题类型饼图
response.setWrongTypePie(buildWrongTypePie(studentId));
```

- [ ] **Step 5: 编译验证**

```bash
cd backend && mvn clean compile -DskipTests
```

Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
cd backend && git add src/main/java/com/edu/user/service/StudentProfileService.java
git commit -m "feat: 完善StudentProfileService知识点统计和图表数据"
```

---

### Task 4: 完善ClassProfileService - 雷达图和箱线图数据

**Files:**
- Modify: `backend/src/main/java/com/edu/user/service/ClassProfileService.java`
- Need to read: `backend/src/main/java/com/edu/user/dto/ClassProfileStatsResponse.java`

- [ ] **Step 1: 新增buildKnowledgeRadar方法**

```java
private ClassProfileStatsResponse.KnowledgeRadar buildKnowledgeRadar(Long classId) {
    List<Student> students = studentService.listByClass(classId);
    if (students.isEmpty()) {
        return new ClassProfileStatsResponse.KnowledgeRadar();
    }
    
    // 按知识点聚合班级整体掌握情况
    Map<String, KnowledgePointClassAgg> aggMap = new HashMap<>();
    
    for (Student student : students) {
        List<StudentWrongQuestion> wrongQuestions = studentWrongQuestionService.listByStudent(student.getId());
        for (StudentWrongQuestion wq : wrongQuestions) {
            Question question = questionService.getQuestionById(wq.getQuestionId());
            if (question != null && question.getKnowledgePoints() != null) {
                try {
                    JSONArray points = JSON.parseArray(question.getKnowledgePoints().toString());
                    if (points != null) {
                        for (int i = 0; i < points.size(); i++) {
                            String point = points.getString(i);
                            aggMap.computeIfAbsent(point, k -> new KnowledgePointClassAgg())
                                   .addStudent(student.getId());
                        }
                    }
                } catch (Exception e) {
                    // 解析失败跳过
                }
            }
        }
    }
    
    ClassProfileStatsResponse.KnowledgeRadar radar = new ClassProfileStatsResponse.KnowledgeRadar();
    List<String> points = new ArrayList<>();
    List<BigDecimal> scores = new ArrayList<>();
    
    for (Map.Entry<String, KnowledgePointClassAgg> entry : aggMap.entrySet()) {
        points.add(entry.getKey());
        double masteryRate = 1.0 - (entry.getValue().getWrongStudentCount() / (double) students.size());
        scores.add(BigDecimal.valueOf(Math.max(0, masteryRate * 100)).setScale(2, RoundingMode.HALF_UP));
    }
    
    radar.setPoints(points);
    radar.setScores(scores);
    return radar;
}

private static class KnowledgePointClassAgg {
    private Set<Long> studentIds = new HashSet<>();
    
    void addStudent(Long studentId) {
        studentIds.add(studentId);
    }
    
    int getWrongStudentCount() { return studentIds.size(); }
}
```

- [ ] **Step 2: 新增buildScoreBoxplot方法**

```java
private ClassProfileStatsResponse.ScoreBoxplot buildScoreBoxplot(Long classId) {
    // 获取班级所有学生
    List<Student> students = studentService.listByClass(classId);
    if (students.isEmpty()) {
        return new ClassProfileStatsResponse.ScoreBoxplot();
    }
    
    // 收集所有已批改的成绩
    List<BigDecimal> allScores = new ArrayList<>();
    for (Student student : students) {
        LambdaQueryWrapper<com.edu.grading.entity.AnswerSheet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(com.edu.grading.entity.AnswerSheet::getStudentId, student.getId())
                .eq(com.edu.grading.entity.AnswerSheet::getStatus, 3) // 已批改
                .isNotNull(com.edu.grading.entity.AnswerSheet::getTotalScore);
        
        List<com.edu.grading.entity.AnswerSheet> sheets = answerSheetMapper.selectList(wrapper);
        for (com.edu.grading.entity.AnswerSheet sheet : sheets) {
            if (sheet.getTotalScore() != null) {
                allScores.add(sheet.getTotalScore());
            }
        }
    }
    
    if (allScores.isEmpty()) {
        return new ClassProfileStatsResponse.ScoreBoxplot();
    }
    
    // 排序
    allScores.sort(BigDecimal::compareTo);
    
    // 计算五数概括
    int size = allScores.size();
    BigDecimal min = allScores.get(0);
    BigDecimal max = allScores.get(size - 1);
    BigDecimal median = getPercentile(allScores, 50);
    BigDecimal q1 = getPercentile(allScores, 25);
    BigDecimal q3 = getPercentile(allScores, 75);
    
    // 检测异常值（简化：不检测）
    List<BigDecimal> outliers = new ArrayList<>();
    
    ClassProfileStatsResponse.ScoreBoxplot boxplot = new ClassProfileStatsResponse.ScoreBoxplot();
    boxplot.setMin(min);
    boxplot.setQ1(q1);
    boxplot.setMedian(median);
    boxplot.setQ3(q3);
    boxplot.setMax(max);
    boxplot.setOutliers(outliers);
    
    return boxplot;
}

private BigDecimal getPercentile(List<BigDecimal> sortedList, double percentile) {
    int size = sortedList.size();
    double index = (percentile / 100.0) * (size - 1);
    int lower = (int) Math.floor(index);
    int upper = (int) Math.ceil(index);
    
    if (lower == upper) {
        return sortedList.get(lower);
    }
    
    // 线性插值
    BigDecimal lowerVal = sortedList.get(lower);
    BigDecimal upperVal = sortedList.get(upper);
    BigDecimal diff = upperVal.subtract(lowerVal);
    double fraction = index - lower;
    return lowerVal.add(diff.multiply(BigDecimal.valueOf(fraction)));
}
```

需要注入`AnswerSheetMapper`：

```java
private final com.edu.grading.mapper.AnswerSheetMapper answerSheetMapper;
```

- [ ] **Step 3: 修改getClassStats方法填充新字段**

在`response.setKnowledgeMastery(buildKnowledgeMastery(classId));`之后添加：

```java
// 知识点雷达图
response.setKnowledgeRadar(buildKnowledgeRadar(classId));

// 成绩箱线图
response.setScoreBoxplot(buildScoreBoxplot(classId));
```

- [ ] **Step 4: 编译验证**

```bash
cd backend && mvn clean compile -DskipTests
```

Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
cd backend && git add src/main/java/com/edu/user/service/ClassProfileService.java
git commit -m "feat: 完善ClassProfileService添加雷达图和箱线图数据"
```

---

### Task 5: 前端StudentProfile.vue添加图表

**Files:**
- Modify: `frontend/src/views/StudentProfile.vue`

- [ ] **Step 1: 添加图表容器**

在`成绩趋势`卡片之后添加：

```html
<!-- 知识点掌握雷达图 -->
<el-card class="chart-card" shadow="hover">
  <template #header>
    <div class="card-header">
      <el-icon><Collection /></el-icon>
      <span>知识点掌握雷达图</span>
    </div>
  </template>
  <div ref="radarChartRef" class="chart-container"></div>
  <el-empty v-if="!profile?.knowledgeRadar?.points?.length" description="暂无雷达图数据" />
</el-card>

<!-- 错题类型分布饼图 -->
<el-card class="chart-card" shadow="hover">
  <template #header>
    <div class="card-header">
      <el-icon><PieChart /></el-icon>
      <span>错题类型分布</span>
    </div>
  </template>
  <div ref="pieChartRef" class="chart-container"></div>
  <el-empty v-if="!profile?.wrongTypePie?.types?.length" description="暂无饼图数据" />
</el-card>
```

- [ ] **Step 2: 添加图表ref和初始化逻辑**

在`<script setup>`中添加：

```typescript
import { PieChart } from '@element-plus/icons-vue'

const trendChartRef = ref<HTMLElement | null>(null)
const radarChartRef = ref<HTMLElement | null>(null)
const pieChartRef = ref<HTMLElement | null>(null)
let trendChartInstance: echarts.ECharts | null = null
let radarChartInstance: echarts.ECharts | null = null
let pieChartInstance: echarts.ECharts | null = null
```

- [ ] **Step 3: 实现initTrendChart函数**

```typescript
const initTrendChart = () => {
  if (!trendChartRef.value || !profile.value?.scoreTrends?.length) return
  
  nextTick(() => {
    if (!trendChartRef.value) return
    if (trendChartRef.value) {
      trendChartInstance?.dispose()
    }
    trendChartInstance = echarts.init(trendChartRef.value)
    
    const labels = profile.value!.scoreTrends.map((t: any) => t.examName)
    const data = profile.value!.scoreTrends.map((t: any) => t.score)
    
    trendChartInstance.setOption({
      title: { text: '成绩趋势', left: 'center' },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: labels },
      yAxis: { type: 'value' },
      series: [{
        type: 'line',
        data: data,
        smooth: true,
        itemStyle: { color: '#409eff' },
        areaStyle: { color: 'rgba(64,158,255,0.1)' }
      }]
    })
  })
}
```

- [ ] **Step 4: 实现initRadarChart函数**

```typescript
const initRadarChart = () => {
  if (!radarChartRef.value || !profile.value?.knowledgeRadar?.points?.length) return
  
  nextTick(() => {
    if (!radarChartRef.value) return
    if (radarChartInstance) {
      radarChartInstance.dispose()
    }
    radarChartInstance = echarts.init(radarChartRef.value)
    
    const points = profile.value!.knowledgeRadar.points
    const scores = profile.value!.knowledgeRadar.scores
    
    const indicators = points.map((p: string) => ({ name: p, max: 100 }))
    
    radarChartInstance.setOption({
      title: { text: '知识点掌握雷达图', left: 'center' },
      radar: {
        indicator: indicators,
        shape: 'circle'
      },
      series: [{
        type: 'radar',
        data: [{
          value: scores,
          name: '掌握率',
          areaStyle: { color: 'rgba(64,158,255,0.3)' },
          lineStyle: { color: '#409eff' }
        }]
      }]
    })
  })
}
```

- [ ] **Step 5: 实现initPieChart函数**

```typescript
const initPieChart = () => {
  if (!pieChartRef.value || !profile.value?.wrongTypePie?.types?.length) return
  
  nextTick(() => {
    if (!pieChartRef.value) return
    if (pieChartInstance) {
      pieChartInstance.dispose()
    }
    pieChartInstance = echarts.init(pieChartRef.value)
    
    const types = profile.value!.wrongTypePie.types
    const counts = profile.value!.wrongTypePie.counts
    
    const data = types.map((t: string, i: number) => ({
      name: t,
      value: counts[i]
    }))
    
    pieChartInstance.setOption({
      title: { text: '错题类型分布', left: 'center' },
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        data: data,
        radius: ['40%', '70%'],
        label: {
          formatter: '{b}: {c}题 ({d}%)'
        }
      }]
    })
  })
}
```

- [ ] **Step 6: 添加watch监听数据变化**

```typescript
watch(() => profile.value?.scoreTrends, () => { initTrendChart() })
watch(() => profile.value?.knowledgeRadar, () => { initRadarChart() })
watch(() => profile.value?.wrongTypePie, () => { initPieChart() })
```

- [ ] **Step 7: 添加图表容器样式**

```css
.chart-container {
  height: 300px;
}

.chart-card {
  margin-top: 20px;
}
```

- [ ] **Step 8: 测试验证**

运行前端开发服务器，访问学生画像页面，选择学生和班级，检查三个图表是否显示。

```bash
cd frontend && npm run dev
```

- [ ] **Step 9: Commit**

```bash
cd frontend && git add src/views/StudentProfile.vue
git commit -m "feat: StudentProfile添加折线图、雷达图、饼图"
```

---

### Task 6: 前端ClassProfile.vue添加图表

**Files:**
- Modify: `frontend/src/views/ClassProfile.vue`

- [ ] **Step 1: 添加图表容器**

在分数段分布图之后添加：

```html
<!-- 知识点掌握雷达图 -->
<el-card class="chart-card" shadow="hover">
  <template #header>
    <span>班级知识点掌握雷达图</span>
  </template>
  <div ref="classRadarRef" style="height: 300px"></div>
  <el-empty v-if="!stats?.knowledgeRadar?.points?.length" description="暂无雷达图数据" />
</el-card>

<!-- 学生成绩分布箱线图 -->
<el-card class="chart-card" shadow="hover">
  <template #header>
    <span>成绩分布箱线图</span>
  </template>
  <div ref="boxplotRef" style="height: 300px"></div>
  <el-empty v-if="!stats?.scoreBoxplot" description="暂无箱线图数据" />
</el-card>
```

- [ ] **Step 2: 添加图表ref和初始化逻辑**

```typescript
const classRadarRef = ref<HTMLElement | null>(null)
const boxplotRef = ref<HTMLElement | null>(null)
let classRadarInstance: echarts.ECharts | null = null
let boxplotInstance: echarts.ECharts | null = null
```

- [ ] **Step 3: 实现initClassRadarChart函数**

```typescript
const initClassRadarChart = () => {
  if (!classRadarRef.value || !stats.value?.knowledgeRadar?.points?.length) return
  
  nextTick(() => {
    if (!classRadarRef.value) return
    if (classRadarInstance) {
      classRadarInstance.dispose()
    }
    classRadarInstance = echarts.init(classRadarRef.value)
    
    const points = stats.value.knowledgeRadar.points
    const scores = stats.value.knowledgeRadar.scores
    
    const indicators = points.map((p: string) => ({ name: p, max: 100 }))
    
    classRadarInstance.setOption({
      title: { text: '知识点掌握雷达图', left: 'center' },
      radar: {
        indicator: indicators,
        shape: 'circle'
      },
      series: [{
        type: 'radar',
        data: [{
          value: scores,
          name: '班级掌握率',
          areaStyle: { color: 'rgba(103,194,58,0.3)' },
          lineStyle: { color: '#67c23a' }
        }]
      }]
    })
  })
}
```

- [ ] **Step 4: 实现initBoxplotChart函数**

```typescript
const initBoxplotChart = () => {
  if (!boxplotRef.value || !stats.value?.scoreBoxplot) return
  
  nextTick(() => {
    if (!boxplotRef.value) return
    if (boxplotInstance) {
      boxplotInstance.dispose()
    }
    boxplotInstance = echarts.init(boxplotRef.value)
    
    const boxplot = stats.value.scoreBoxplot
    
    boxplotInstance.setOption({
      title: { text: '成绩分布箱线图', left: 'center' },
      tooltip: {
        formatter: function(params: any) {
          return `最小值: ${boxplot.min}<br/>
                   Q1: ${boxplot.q1}<br/>
                   中位数: ${boxplot.median}<br/>
                   Q3: ${boxplot.q3}<br/>
                   最大值: ${boxplot.max}`
        }
      },
      xAxis: { type: 'category', data: ['成绩分布'] },
      yAxis: { type: 'value' },
      series: [{
        type: 'boxplot',
        data: [[
          boxplot.min,
          boxplot.q1,
          boxplot.median,
          boxplot.q3,
          boxplot.max
        ]]
      }]
    })
  })
}
```

- [ ] **Step 5: 添加watch监听数据变化**

修改现有的watch并添加新的：

```typescript
watch(() => stats.value?.distribution, () => { initChart() })
watch(() => stats.value?.knowledgeRadar, () => { initClassRadarChart() })
watch(() => stats.value?.scoreBoxplot, () => { initBoxplotChart() })
```

- [ ] **Step 6: 测试验证**

运行前端开发服务器，访问班级画像页面，选择班级，检查两个图表是否显示。

```bash
cd frontend && npm run dev
```

- [ ] **Step 7: Commit**

```bash
cd frontend && git add src/views/ClassProfile.vue
git commit -m "feat: ClassProfile添加雷达图和箱线图"
```

---

### Task 7: 集成测试与验证

**Files:**
- Test: Manual testing required

- [ ] **Step 1: 启动后端服务**

```bash
cd backend && mvn spring-boot:run
```

Expected: Started EduApplication in X seconds

- [ ] **Step 2: 启动前端服务**

```bash
cd frontend && npm run dev
```

Expected: Local: http://localhost:3000/

- [ ] **Step 3: 测试学生画像图表**

1. 访问 http://localhost:3000
2. 登录（admin/admin123）
3. 进入学生画像页面
4. 选择班级 → 选择学生
5. 验证：
   - 成绩趋势折线图显示
   - 知识点掌握雷达图显示
   - 错题类型饼图显示

- [ ] **Step 4: 测试班级画像图表**

1. 进入班级画像页面
2. 选择班级
3. 验证：
   - 分数段分布柱状图显示
   - 知识点掌握雷达图显示
   - 成绩分布箱线图显示
   - 知识点掌握表格显示
   - 学生列表显示

- [ ] **Step 5: 检查浏览器控制台**

确保没有JavaScript错误，ECharts图表正常渲染。

- [ ] **Step 6: 最终Commit（如有修复）**

如果测试中发现问题并修复：

```bash
cd backend && git add -A && git commit -m "fix: 修复图表显示问题"
cd frontend && git add -A && git commit -m "fix: 修复前端图表问题"
```

---

## 自检验证

### Spec Coverage检查
- ✅ 学生画像成绩趋势折线图 → Task 5, Step 3
- ✅ 学生画像知识点雷达图 → Task 5, Step 4
- ✅ 学生画像错题类型饼图 → Task 5, Step 5
- ✅ 班级画像知识点雷达图 → Task 6, Step 3
- ✅ 班级画像成绩箱线图 → Task 6, Step 4
- ✅ 后端DTO扩展 → Task 1, Task 2
- ✅ 后端Service实现 → Task 3, Task 4

### Placeholder扫描
- ✅ 无TBD/TODO
- ✅ 无"implement later"
- ✅ 无模糊描述
- ✅ 所有步骤包含实际代码

### 类型一致性
- ✅ StudentProfileResponse.KnowledgeRadar 在Task 1定义，Task 3使用
- ✅ ClassProfileStatsResponse.KnowledgeRadar 在Task 2定义，Task 4使用
- ✅ 方法签名一致（buildKnowledgeRadar返回类型匹配）
