import random, json, os
random.seed(42)
out = open("backend/src/main/resources/db/mock-data.sql", "w", encoding="utf-8")
def w(s=""): out.write(s+"\n")
def esc(s): return str(s).replace("'", "''") if s is not None else ""

w("-- ============================================================")
w("-- 模拟数据 - 用于测试和预览系统")
w("-- 生成日期: 2026-05-09")
w("-- ============================================================")
w("")

# ===== 姓氏和名字 =====
SN=["张","李","王","刘","陈","杨","赵","黄","周","吴","徐","孙","胡","朱","高","林","何","郭","马","罗"]
MN=["明","华","强","伟","军","勇","杰","涛","磊","鹏","宇","浩","辉","波","峰"]
FN=["芳","娟","敏","静","丽","燕","艳","玲","婷","雪","梅","红","霞","琳","萍"]
def mkname(i): s=SN[i%len(SN)]; return s+MN[(i//2)%len(MN)] if i%2==0 else s+FN[(i//2)%len(FN)]

def ct(cid): return 1 if cid<=9 else (2 if cid<=18 else 3)

# ===== 租户/学校/年级/班级 =====
w("REPLACE INTO tenant (id,name,code,ai_provider,status) VALUES (1,'测试学校','TEST_SCHOOL','CLOUD',1),(2,'示范中学','DEMO_SCHOOL','CLOUD',1),(3,'实验学校','EXP_SCHOOL','PRIVATE',1);")
w("REPLACE INTO school (id,tenant_id,name,address) VALUES (1,1,'测试学校','北京市海淀区测试路1号'),(2,2,'示范中学','上海市浦东新区示范街2号'),(3,3,'实验学校','广州市天河区实验大道3号');")
grades=[]
for sid in range(1,4):
    for seq,nm in enumerate(["七年级","八年级","九年级"],1): gid=(sid-1)*3+seq; grades.append(f"({gid},{sid},'{nm}',2,{seq})")
w("REPLACE INTO grade (id,school_id,name,level,sequence) VALUES "+", ".join(grades)+";")

CL=[(1,1,"一班",40),(2,1,"二班",40),(3,1,"三班",37),(4,2,"一班",40),(5,2,"二班",40),(6,2,"三班",36),(7,3,"一班",40),(8,3,"二班",40),(9,3,"三班",37),(10,4,"一班",37),(11,4,"二班",37),(12,4,"三班",37),(13,5,"一班",37),(14,5,"二班",37),(15,5,"三班",37),(16,6,"一班",37),(17,6,"二班",37),(18,6,"三班",37),(19,7,"一班",37),(20,7,"二班",37),(21,7,"三班",37),(22,8,"一班",37),(23,8,"二班",37),(24,8,"三班",37),(25,9,"一班",37),(26,9,"二班",37),(27,9,"三班",37),(28,9,"四班",37),(29,9,"五班",37),(30,9,"六班",35)]
w("REPLACE INTO class (id,grade_id,name,student_count) VALUES "+", ".join([f"({c[0]},{c[1]},'{c[2]}',{c[3]})" for c in CL])+";")
w("")

# ===== 学生 1000+ =====
w("-- 创建学生")
stu_batch=[]; sid=1
for cid,gid,cn,cnt in CL:
    t=ct(cid)
    for i in range(cnt):
        g=1 if sid%2==1 else 2; stu_batch.append(f"({sid},{t},{cid},'{mkname(sid)}','2024{sid:04d}',{g},1,0)"); sid+=1
for i in range(0,len(stu_batch),100):
    chunk=stu_batch[i:i+100]; sep=";" if i+100>=len(stu_batch) else ","
    w("REPLACE INTO student (id,tenant_id,class_id,name,student_no,gender,status,deleted) VALUES "+", ".join(chunk)+sep)
TOT_STU=sid-1
w("")

# ===== 角色 =====
w("-- 用户模块")
w("REPLACE INTO role (id,tenant_id,name,code,description,deleted) VALUES (4,1,'家长','PARENT','家长角色',0),(5,2,'管理员','ADMIN','系统管理员',0),(6,2,'教师','TEACHER','教师角色',0),(7,2,'学生','STUDENT','学生角色',0),(8,2,'家长','PARENT','家长角色',0),(9,3,'管理员','ADMIN','系统管理员',0),(10,3,'教师','TEACHER','教师角色',0),(11,3,'学生','STUDENT','学生角色',0),(12,3,'家长','PARENT','家长角色',0);")

# ===== 用户 =====
PWD="$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJBo4lP3a2e"
US=[(2,1,'admin1',PWD,'管理员1',1,0),(3,2,'admin2',PWD,'管理员2',1,0),(4,3,'admin3',PWD,'管理员3',1,0)]
for i in range(10): US.append((5+i,1,f'teacher{i+1:02d}',PWD,f"{mkname(i+100)}老师",1,0))
for i in range(10): US.append((15+i,2,f'teacher{i+11:02d}',PWD,f"{mkname(i+200)}老师",1,0))
for i in range(10): US.append((25+i,3,f'teacher{i+21:02d}',PWD,f"{mkname(i+300)}老师",1,0))
for i in range(17): US.append((35+i,1,f'student{i+1:03d}',PWD,mkname(i+1),1,0))
w("REPLACE INTO sys_user (id,tenant_id,username,password,real_name,status,deleted) VALUES "+", ".join([f"({u[0]},{u[1]},'{u[2]}','{u[3]}','{u[4]}',{u[5]},{u[6]})" for u in US])+";")

# ===== 用户角色关联 =====
UR=[]; urid=1
UR+=[(urid,2,1)]; urid+=1; UR+=[(urid,3,5)]; urid+=1; UR+=[(urid,4,9)]; urid+=1
for i in range(10): UR+=[(urid,5+i,2)]; urid+=1
for i in range(10): UR+=[(urid,15+i,6)]; urid+=1
for i in range(10): UR+=[(urid,25+i,10)]; urid+=1
for i in range(17): UR+=[(urid,35+i,3)]; urid+=1
w("REPLACE INTO user_role (id,user_id,role_id) VALUES "+", ".join([f"({u[0]},{u[1]},{u[2]})" for u in UR])+";")

# ===== 角色权限关联 =====
RP=[]; rpid=1
for p in [7,9]: RP+=[(rpid,4,p)]; rpid+=1
for p in range(1,11): RP+=[(rpid,5,p)]; rpid+=1
for p in [4,5,6,7,8,9,10]: RP+=[(rpid,6,p)]; rpid+=1
for p in [7,9]: RP+=[(rpid,7,p)]; rpid+=1
for p in [7,9]: RP+=[(rpid,8,p)]; rpid+=1
for p in range(1,11): RP+=[(rpid,9,p)]; rpid+=1
for p in [4,5,6,7,8,9,10]: RP+=[(rpid,10,p)]; rpid+=1
for p in [7,9]: RP+=[(rpid,11,p)]; rpid+=1
for p in [7,9]: RP+=[(rpid,12,p)]; rpid+=1
w("REPLACE INTO role_permission (id,role_id,permission_id) VALUES "+", ".join([f"({r[0]},{r[1]},{r[2]})" for r in RP])+";")
w("")

# ===== 题库 =====
w("-- 试卷模块")
w("REPLACE INTO question_bank (id,tenant_id,name,subject,grade_level,description,is_public,deleted,created_by) VALUES (5,1,'初中数学校验题库','MATH',2,'初中数学校验题',1,0,2),(6,2,'初中数学题库','MATH',2,'初中数学练习题',1,0,3),(7,2,'初中物理题库','PHYSICS',2,'初中物理练习题',1,0,3),(8,2,'初中化学题库','CHEMISTRY',2,'初中化学练习题',1,0,3),(9,2,'初中英语题库','ENGLISH',2,'初中英语练习题',1,0,3),(10,3,'初中数学题库','MATH',2,'初中数学练习题',1,0,4),(11,3,'初中物理题库','PHYSICS',2,'初中物理练习题',1,0,4),(12,3,'初中化学题库','CHEMISTRY',2,'初中化学练习题',1,0,4),(13,3,'初中英语题库','ENGLISH',2,'初中英语练习题',1,0,4);")
w("")

# ===== 题目生成器 =====
Q_BANKS = [
    (1,'MATH'),(2,'PHYSICS'),(3,'CHEMISTRY'),(4,'ENGLISH'),  # init-data banks
    (5,'MATH'),(6,'MATH'),(7,'PHYSICS'),(8,'CHEMISTRY'),(9,'ENGLISH'),
    (10,'MATH'),(11,'PHYSICS'),(12,'CHEMISTRY'),(13,'ENGLISH')
]
# 每题库题数分配 (与plan一致, 但bank 1-4 已有部分题目)
# init-data: bank1 有 5 题 (id 1-5)
# 需要补充: bank1 95题, bank2 67题, bank3 67题, bank4 67题
# bank5 100题, bank6 100题, bank7 67题, bank8 67题, bank9 67题
# bank10 100题, bank11 67题, bank12 67题, bank13 67题
# 总计: 95+67+67+67+100+100+67+67+67+100+67+67+67 = 997, 加上init-data 5题 = 1002题
BANK_CNT = {1:95, 2:67, 3:67, 4:67, 5:100, 6:100, 7:67, 8:67, 9:67, 10:100, 11:67, 12:67, 13:67}

# 题目内容池
MATH_CH=[("下列哪个数是负数？",'{"A":"-1","B":"0","C":"1","D":"2"}','A','负数小于0，-1是负数。','["负数"]'),("计算: (-3)+5=",'{"A":"2","B":"-2","C":"8","D":"-8"}','A','(-3)+5=2','["有理数加减"]'),("绝对值最小的数是",'{"A":"-1","B":"0","C":"1","D":"-2"}','B','0的绝对值是0，是最小的。','["绝对值"]'),("计算: 2×3+4=",'{"A":"8","B":"10","C":"12","D":"14"}','B','先算乘法: 2×3=6，再加4得10。','["乘法","加法"]'),("化简: 3a+2a=",'{"A":"5a","B":"6a","C":"a","D":"5a²"}','A','同类项合并，系数相加。','["代数式化简"]'),("方程x+3=7的解是",'{"A":"3","B":"4","C":"7","D":"10"}','B','移项得x=7-3=4','["一元一次方程"]'),("下列数中最大的是",'{"A":"0.5","B":"1/3","C":"0.4","D":"0.45"}','A','0.5=1/2，大于其他三个数。','["有理数比较"]'),("(-2)³的值是",'{"A":"-6","B":"6","C":"-8","D":"8"}','C','(-2)³=-2×-2×-2=-8','["乘方运算"]'),("三角形的内角和是",'{"A":"90°","B":"180°","C":"270°","D":"360°"}','B','三角形内角和定理为180度。','["三角形性质"]'),("圆的周长公式是",'{"A":"πr","B":"2πr","C":"πr²","D":"2r"}','B','圆的周长C=2πr=πd','["圆的性质"]')]
PHY_CH=[("光在真空中的传播速度约为",'{"A":"3×10⁶m/s","B":"3×10⁸m/s","C":"3×10⁴m/s","D":"3×10²m/s"}','B','光速约为3×10⁸m/s。','["光学基础"]'),("力的单位是",'{"A":"千克","B":"牛顿","C":"焦耳","D":"瓦特"}','B','力的国际单位是牛顿(N)。','["力学基础"]'),("物体保持静止或匀速直线运动状态的性质叫",'{"A":"惯性","B":"重力","C":"弹力","D":"摩擦力"}','A','惯性是物体保持原有运动状态的性质。','["牛顿第一定律"]'),("下列属于可再生能源的是",'{"A":"煤","B":"石油","C":"太阳能","D":"天然气"}','C','太阳能可以持续从自然界获得。','["能源分类"]'),("声音在空气中的传播速度约为",'{"A":"340m/s","B":"1500m/s","C":"3×10⁸m/s","D":"34m/s"}','A','15°C时声速约为340m/s。','["声学基础"]'),("压强的单位是",'{"A":"N","B":"Pa","C":"J","D":"W"}','B','压强的国际单位是帕斯卡(Pa)。','["压强"]'),("杠杆平衡条件是",'{"A":"F₁=F₂","B":"F₁L₁=F₂L₂","C":"F₁+L₁=F₂+L₂","D":"F₁/F₂=L₁/L₂"}','B','杠杆平衡条件：动力×动力臂=阻力×阻力臂。','["简单机械"]')]
CHE_CH=[("水的化学式是",'{"A":"HO","B":"H₂O","C":"H₂O₂","D":"OH"}','B','水由两个氢原子和一个氧原子组成。','["化学式"]'),("空气中含量最多的气体是",'{"A":"氧气","B":"氮气","C":"二氧化碳","D":"稀有气体"}','B','氮气约占空气体积的78%。','["空气成分"]'),("酸雨的pH值",'{"A":"大于7","B":"等于7","C":"小于7","D":"等于5.6"}','C','酸雨的pH值小于5.6，呈酸性。','["酸碱性质"]'),("铁在氧气中燃烧生成",'{"A":"FeO","B":"Fe₂O₃","C":"Fe₃O₄","D":"Fe(OH)₃"}','C','3Fe+2O₂=Fe₃O₄','["金属燃烧"]'),("下列物质属于单质的是",'{"A":"水","B":"二氧化碳","C":"氧气","D":"盐酸"}','C','单质是由同种元素组成的纯净物。','["物质分类"]'),("实验室制取氧气的常用方法是",'{"A":"电解水","B":"加热高锰酸钾","C":"燃烧","D":"分解碳酸钙"}','B','加热高锰酸钾是实验室制氧常用方法。','["氧气制取"]')]
ENG_CH=[("'Apple'的中文意思是",'{"A":"香蕉","B":"苹果","C":"橙子","D":"葡萄"}','B','Apple意为苹果。','["基础词汇"]'),("'Good morning'的意思是",'{"A":"晚上好","B":"下午好","C":"早上好","D":"再见"}','C','Good morning用于早上问候。','["日常用语"]'),("选择正确的单词填空: I ___ a student.",'{"A":"am","B":"is","C":"are","D":"be"}','A','I后面用am。','["be动词"]'),("'How are you?'的恰当回答是",'{"A":"Thank you","B":"Fine, thank you","C":"Goodbye","D":"Hello"}','B','"How are you?"通常回答"Fine, thank you."。','["日常对话"]'),("'Book'的复数形式是",'{"A":"books","B":"bookes","C":"booking","D":"book"}','A','一般情况下名词复数加-s。','["名词复数"]'),("选择正确的介词: I go to school ___ bus.",'{"A":"by","B":"on","C":"in","D":"at"}','A','"by bus"表示乘坐公交车。','["介词用法"]'),("'What is your name?'的意思是",'{"A":"你几岁了","B":"你叫什么名字","C":"你好吗","D":"再见"}','B','What is your name?询问对方姓名。','["基础句型"]')]

# 各科目题型池
SUBJ_POOLS = {
    'MATH': {'CHOICE': MATH_CH, 'FILL': [("圆的周长公式是C=____","2πr或πd","圆的周长等于直径乘π。","圆的周长"),("(-3)²=____","9","负数的平方是正数。","乘方运算"),("化简: 2x+3x=____","5x","同类项合并。","代数式化简"),("直角三角形两锐角之和为____度","90","直角三角形两锐角互余。","三角形性质"),("若a=3,b=4,则a+b=____","7","简单加法。","有理数加法")], 'JUDGE': [("三角形的内角和是180度。","正确","三角形内角和定理。","三角形"),("0是最小的自然数。","正确","自然数从0开始。","自然数"),("负数没有平方根。","正确","实数范围内负数无平方根。","平方根"),("所有偶数都是合数。","错误","2是偶数但不是合数。","合数"),("平行四边形的对角线相等。","错误","矩形对角线相等，一般平行四边形不一定。","平行四边形")], 'CALCULATION': [("解方程: x+5=12","x=7","移项得x=12-5=7","方程"),("计算: 3²+4²","25","3²=9,4²=16,9+16=25","乘方运算"),("求值: 当a=2,b=3时，2a+b的值","7","2×2+3=7","代数式求值"),("计算: (5-3)×4","8","先算括号:5-3=2,2×4=8","混合运算"),("解方程: 2x=10","x=5","两边同除以2得x=5","方程")]},
    'PHYSICS': {'CHOICE': PHY_CH, 'FILL': [("力的国际单位是____","牛顿","力的单位是牛顿(N)。","力学基础"),("光在真空中的速度约为____m/s","3×10⁸","光速约为3×10⁸m/s。","光学基础"),("标准大气压约为____Pa","1.01×10⁵","标准大气压约为101325Pa。","压强"),("声音在15°C空气中的传播速度约为____m/s","340","声速约340m/s。","声学基础"),("物体所受重力与质量的关系式为G=____","mg","重力G=质量m×重力加速度g","重力")], 'JUDGE': [("光在真空中速度最快。","正确","真空中光速最大。","光速"),("物体的质量随位置变化。","错误","质量不随位置变化，重量会变化。","质量与重量"),("声音不能在真空中传播。","正确","声音传播需要介质。","声波传播"),("摩擦力总是阻碍物体运动。","错误","摩擦力有时也是动力。","摩擦力"),("同种电荷相互排斥。","正确","电荷相互作用规律。","静电")], 'CALCULATION': [("计算: 质量为2kg的物体所受重力(g取10N/kg)","20N","G=mg=2×10=20N","重力计算"),("计算: 速度为5m/s的物体在2s内通过的路程","10m","s=vt=5×2=10m","匀速运动"),("计算: 密度为2g/cm³、体积为5cm³的物体质量","10g","m=ρV=2×5=10g","密度计算"),("计算: 功率为100W的电器工作10s消耗的电能","1000J","W=Pt=100×10=1000J","电功计算"),("计算: 电阻为10Ω的导体两端电压为5V时的电流","0.5A","I=U/R=5/10=0.5A","欧姆定律")]},
    'CHEMISTRY': {'CHOICE': CHE_CH, 'FILL': [("水的化学式是____","H₂O","水由氢和氧组成。","化学式"),("空气中含量最多的气体是____","氮气","氮气约占78%。","空气成分"),("氧气的化学式是____","O₂","氧气分子由两个氧原子组成。","化学式"),("实验室制取氧气的常用方法是加热____","高锰酸钾","2KMnO₄=K₂MnO₄+MnO₂+O₂↑","氧气制取"),("铁锈的主要成分是____","Fe₂O₃","铁锈主要成分是氧化铁。","金属腐蚀")], 'JUDGE': [("水是一种化合物。","正确","H₂O由两种元素组成。","化合物"),("铁在潮湿空气中易生锈。","正确","铁生锈需要水和氧气。","金属腐蚀"),("所有金属都能与酸反应。","错误","铜、银等不活泼金属不能与稀酸反应。","金属活动性"),("催化剂能改变反应速率。","正确","催化剂改变反应速率，本身不消耗。","催化剂"),("pH=7的溶液一定呈中性。","错误","25°C时pH=7为中性，温度变化时中性点变化。","pH值")], 'CALCULATION': [("计算: 2mol水的质量","36g","m=2×18=36g","摩尔质量"),("计算: 5g氢气完全燃烧需要氧气的质量","40g","2H₂+O₂=2H₂O, 4:32=5:x, x=40g","化学方程式计算"),("计算: 100g 10%的NaCl溶液中NaCl的质量","10g","100×10%=10g","溶液浓度"),("计算: 2L 0.5mol/L的NaOH溶液中NaOH的物质的量","1mol","n=cV=0.5×2=1mol","物质的量浓度"),("计算: 标准状况下11.2L氧气的物质的量","0.5mol","n=V/Vm=11.2/22.4=0.5mol","气体摩尔体积")]},
    'ENGLISH': {'CHOICE': ENG_CH, 'FILL': [("'Hello'的中文意思是____","你好","Hello用于打招呼。","基础词汇"),("I am的中文意思是____","我是","I am表示我是。","基础句型"),("'Thank you'的中文意思是____","谢谢你","Thank you表示感谢。","日常用语"),("'Goodbye'的中文意思是____","再见","Goodbye用于告别。","日常用语"),("'What'的中文意思是____","什么","What用于询问。","疑问词")], 'JUDGE': [("'I'在句中总是大写。","正确","人称代词I永远大写。","书写规范"),("英语中名词都有复数形式。","错误","不可数名词没有复数。","名词"),("'Good morning'可用于下午。","错误","Good morning仅用于上午。","日常用语"),("英语句子首字母需要大写。","正确","句子首字母大写是基本规则。","书写规范"),("'Sheep'的单复数同形。","正确","Sheep单复数都是sheep。","名词复数")], 'CALCULATION': [("翻译: I love English.","我爱英语。","I love English.意为我爱英语。","翻译"),("翻译: She is a student.","她是一名学生。","She is a student.意为她是一名学生。","翻译"),("翻译: How old are you?","你几岁了？","How old are you?询问年龄。","翻译"),("翻译: My name is Tom.","我的名字是汤姆。","My name is Tom.介绍自己的名字。","翻译"),("翻译: Nice to meet you.","很高兴见到你。","Nice to meet you.用于初次见面。","翻译")]}
}

TYPE_RATIO = ['CHOICE']*5 + ['FILL']*3 + ['JUDGE']*1 + ['CALCULATION']*1
DIFF_RATIO = [1]*4 + [2]*4 + [3]*2

qid = 6  # init-data 有 1-5
all_questions = []  # (qid, bank_id, subject, type, difficulty, content, options, answer, analysis, knowledge)

for bank_id, subject in Q_BANKS:
    cnt = BANK_CNT[bank_id]
    pool = SUBJ_POOLS[subject]
    for i in range(cnt):
        qtype = TYPE_RATIO[(bank_id + i) % len(TYPE_RATIO)]
        diff = DIFF_RATIO[(bank_id * 3 + i) % len(DIFF_RATIO)]
        items = pool[qtype]
        item = items[i % len(items)]
        if qtype == 'CHOICE':
            content, options, answer, analysis, kp = item
        else:
            content, answer, analysis, kp = item
            options = None
        all_questions.append((qid, bank_id, subject, qtype, diff, content, options, answer, analysis, f'["{kp}"]'))
        qid += 1

# 分批写入题目 (每批200条)
for i in range(0, len(all_questions), 200):
    chunk = all_questions[i:i+200]
    vals = []
    for q in chunk:
        opt = "NULL" if q[6] is None else f"'{q[6]}'"
        vals.append(f"({q[0]},{q[1]},'{q[2]}','{q[3]}',{q[4]},'{esc(q[5])}',{opt},'{esc(q[7])}','{esc(q[8])}','{esc(q[9])}','MANUAL',1)")
    w("REPLACE INTO question (id,bank_id,subject,question_type,difficulty,content,options,answer,answer_analysis,knowledge_points,source,created_by) VALUES "+", ".join(vals)+";")
w(f"-- 共生成 {len(all_questions)} 条题目 (接续init-data id=5, 当前最大id={qid-1})")
w("")

# ===== 试卷模板 =====
w("-- 创建试卷模板")
ET=[]
for tid in range(1,4):
    base = 1 if tid==1 else (5 if tid==2 else 9)
    subjects = ['MATH','PHYSICS','CHEMISTRY','ENGLISH']
    names = ['初中数学单元测试模板','初中物理单元测试模板','初中化学单元测试模板','初中英语单元测试模板']
    for j,(subj,nm) in enumerate(zip(subjects,names),0):
        eid = base + j
        # 英语模板用简答题替代计算题
        if subj == 'ENGLISH':
            struct = '[{"section":"选择题","type":"CHOICE","count":10,"scoreEach":4},{"section":"填空题","type":"FILL","count":5,"scoreEach":4},{"section":"简答题","type":"CALCULATION","count":3,"scoreEach":12}]'
        else:
            struct = '[{"section":"选择题","type":"CHOICE","count":10,"scoreEach":4},{"section":"填空题","type":"FILL","count":5,"scoreEach":4},{"section":"计算题","type":"CALCULATION","count":3,"scoreEach":12}]'
        ET.append(f"({eid},{tid},'{nm}','{subj}',100,45,'{struct}',{tid},0)")
w("REPLACE INTO exam_template (id,tenant_id,name,subject,total_score,time_limit,structure,created_by,deleted) VALUES "+", ".join(ET)+";")
w("")

# ===== 试卷 (每班级1张数学测试) =====
w("-- 创建试卷")
EP=[]; EQ=[]; AS=[]; ANS=[]; SA=[]
eid=1; eqid=1; asid=1; anid=1; said=1

# 仅生成数学试卷，每班级1张，使用数学模板 (tid=1 用模板1, tid=2 用模板5, tid=3 用模板9)
for cid, gid, cname, cnt in CL:
    tid = ct(cid)
    tmpl = 1 if tid==1 else (5 if tid==2 else 9)
    grade_id = gid
    title = f"{cname}数学测试"
    # 试卷 id
    EP.append(f"({eid},{tid},{tmpl},'{title}','MATH',{grade_id},{cid},100,45,4,{tid},'2026-04-15 08:00:00',0)")
    # 从对应租户的数学题库选30题
    math_banks = [1,5,10] if tid==1 else ([6] if tid==2 else [10])
    # 简化: 每个租户只用第一个数学题库
    bank = math_banks[0]
    # 选30题
    qids_for_paper = [q[0] for q in all_questions if q[1]==bank]
    if len(qids_for_paper) < 30:
        # 如果不够，从所有数学题中选
        qids_for_paper = [q[0] for q in all_questions if q[2]=='MATH']
    selected = qids_for_paper[:30]
    # 前10题选择题4分，中间5题填空题4分，后3题计算题12分 (但这里统一每题4分简化，或按模板)
    # 模板: 10道选择(4分), 5道填空(4分), 3道计算(12分) = 30题... 不对，10+5+3=18题
    # 等等，模板定义的是section，不是每题。为了简化，30题每题约3.33分
    for seq, qidx in enumerate(selected, 1):
        score = 4 if seq <= 20 else 5
        EQ.append((eqid, eid, qidx, seq, score))
        eqid += 1
    eid += 1

w("REPLACE INTO exam_paper (id,tenant_id,template_id,title,subject,grade_id,class_id,total_score,time_limit,status,created_by,published_at,deleted) VALUES "+", ".join(EP)+";")
w("")
_eq_strs = [f"({e[0]},{e[1]},{e[2]},{e[3]},{e[4]})" for e in EQ]
w("REPLACE INTO exam_question (id,exam_paper_id,question_id,sequence,score) VALUES "+", ".join(_eq_strs)+";")
w("")

# ===== 答题卡 =====
w("-- 创建答题卡")
for cid, gid, cname, cnt in CL:
    tid = ct(cid)
    ep_id = [c for c in CL].index((cid,gid,cname,cnt)) + 1  # 试卷id 1-30
    for i in range(cnt):
        stu_id = sum(c[3] for c in CL[:CL.index((cid,gid,cname,cnt))]) + i + 1
        # 分数正态分布 60-100, 均值80, 标准差10
        score = int(random.gauss(80, 10))
        score = max(60, min(100, score))
        AS.append((asid, tid, ep_id, stu_id, score))
        asid += 1

_as_strs = [f"({a[0]},{a[1]},{a[2]},{a[3]},3,{a[4]},'2026-04-15 09:00:00','2026-04-15 10:00:00',{a[1]+3},0)" for a in AS]
w("REPLACE INTO answer_sheet (id,tenant_id,exam_paper_id,student_id,status,total_score,submit_time,grading_time,graded_by,deleted) VALUES "+", ".join(_as_strs)+";")
w("")

# ===== 答案详情 =====
w("-- 创建答案详情")
# 每答题卡对应试卷的每道题
for as_item in AS:
    as_id = as_item[0]
    ep_id = as_item[2]
    stu_total = as_item[4]
    # 找到该试卷的题目
    paper_qs = [(eq[0], eq[2], eq[4]) for eq in EQ if eq[1] == ep_id]  # (eqid, question_id, score)
    for eqid, qid_p, qscore in paper_qs:
        # 随机正确率 70%
        correct = random.random() < 0.7
        is_c = 1 if correct else 0
        got = qscore if correct else (0 if random.random() < 0.7 else round(qscore * random.uniform(0.1, 0.5), 1))
        got = round(got, 1)
        # 查找题目答案
        q_info = None
        for q in all_questions:
            if q[0] == qid_p:
                q_info = q
                break
        stu_ans = q_info[7] if q_info else "A"
        if not correct:
            # 错误时给错误答案
            if q_info and q_info[6] and q_info[6].startswith('{'):
                opts = ['A','B','C','D']
                opts.remove(q_info[7])
                stu_ans = random.choice(opts)
            else:
                stu_ans = "错误答案"
        ai_ana = "答案正确" if correct else f"答案错误，正确答案是{q_info[7] if q_info else 'A'}"
        ANS.append(f"({anid},{as_id},{eqid},'{esc(stu_ans)}',{is_c},{got},{got},NULL,'{esc(ai_ana)}','2026-04-15 10:00:00')")
        anid += 1

# 分批写入答案 (每批500条)
for i in range(0, len(ANS), 500):
    chunk = ANS[i:i+500]
    w("REPLACE INTO answer (id,answer_sheet_id,exam_question_id,student_answer,is_correct,score,ai_score,manual_score,ai_analysis,graded_at) VALUES "+", ".join(chunk)+";")
w(f"-- 共生成 {len(ANS)} 条答案记录")
w("")

# ===== 成绩分析 =====
w("-- 创建成绩分析")
for cid, gid, cname, cnt in CL:
    ep_id = CL.index((cid,gid,cname,cnt)) + 1
    # 获取该班级该试卷的所有答题卡分数
    scores = []
    for as_item in AS:
        if as_item[2] == ep_id:
            scores.append(float(as_item[4]))
    if scores:
        avg = round(sum(scores)/len(scores), 1)
        mx = max(scores)
        mn = min(scores)
        pass_rate = round(sum(1 for s in scores if s >= 60)/len(scores), 4)
        exc_rate = round(sum(1 for s in scores if s >= 90)/len(scores), 4)
    else:
        avg, mx, mn, pass_rate, exc_rate = 80, 100, 60, 0.85, 0.35
    qa = {"q1":{"avg":round(avg*0.04,1),"correct_rate":0.85}}
    SA.append(f"({said},{ep_id},{cid},{avg},{mx},{mn},{pass_rate},{exc_rate},'{json.dumps(qa, ensure_ascii=False)}')")
    said += 1

w("REPLACE INTO score_analysis (id,exam_paper_id,class_id,avg_score,max_score,min_score,pass_rate,excellent_rate,question_analysis) VALUES "+", ".join(SA)+";")
w("")

# ===== 学生错题记录 =====
w("-- 创建学生错题记录")
SWQ=[]; swqid=1
# 从答案中筛选 is_correct=0 的记录，每个学生最多5条
stu_wrong = {}
for ans in ANS:
    parts = ans.strip("()").split(",")
    is_c = int(parts[4])
    if is_c == 0:
        as_id = int(parts[1])
        eq_id = int(parts[2])
        # 找到学生id
        stu_id = None
        for as_item in AS:
            if as_item[0] == as_id:
                stu_id = as_item[3]
                break
        # 找到题目id
        qid_val = None
        for eq in EQ:
            if eq[0] == eq_id:
                qid_val = eq[2]
                break
        # 找到试卷id
        ep_val = None
        for eq in EQ:
            if eq[0] == eq_id:
                ep_val = eq[1]
                break
        if stu_id and qid_val and stu_id not in stu_wrong:
            stu_wrong[stu_id] = []
        if stu_id and qid_val and len(stu_wrong.get(stu_id, [])) < 5:
            stu_wrong[stu_id].append((qid_val, ep_val))

for stu_id, wqs in stu_wrong.items():
    for qid_val, ep_val in wqs:
        cnt = random.randint(1, 3)
        SWQ.append(f"({swqid},{stu_id},{qid_val},{ep_val},{cnt},'2026-04-15 10:00:00',NULL)")
        swqid += 1

w("REPLACE INTO student_wrong_question (id,student_id,question_id,exam_paper_id,wrong_count,last_wrong_at,corrected_at) VALUES "+", ".join(SWQ)+";")
w(f"-- 共生成 {len(SWQ)} 条错题记录")
w("")

w("-- ============================================================")
w("-- 模拟数据生成完毕")
w(f"-- 学生: {TOT_STU} 人, 题目: {len(all_questions)} 道, 试卷: {eid-1} 张, 答题卡: {asid-1} 张, 答案: {len(ANS)} 条, 错题: {len(SWQ)} 条")
w("-- ============================================================")

out.close()
print("mock-data.sql generated successfully")
print(f"Students: {TOT_STU}, Questions: {len(all_questions)}, Papers: {eid-1}, AnswerSheets: {asid-1}, Answers: {len(ANS)}, WrongQuestions: {len(SWQ)}")
