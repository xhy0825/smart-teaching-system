# 编译环境问题与解决方案

## 问题总结

### 1. JDK版本不匹配
- **现象**：Maven构建时报错`无效的目标发行版: --release`
- **原因**：项目使用Spring Boot 3.2.0，需要JDK 17+，但系统默认JDK为1.8
- **解决**：安装JDK 17（已通过winget安装Microsoft OpenJDK 17）

### 2. Maven release选项Windows兼容性问题
- **现象**：Maven 3.9.9在Windows上处理`--release`选项失败
- **原因**：Spring Boot parent POM强制使用release配置，覆盖手动设置的source/target
- **解决**：在pom.xml中显式配置maven-compiler-plugin，设置`<release>17</release>`

### 3. 缺少依赖导入
- **现象**：编译错误`找不到符号: JSONArray、JSON`
- **原因**：StudentProfileService.java缺少fastjson2的导入
- **解决**：添加`import com.alibaba.fastjson2.JSON;`和`import com.alibaba.fastjson2.JSONArray;`

## 已安装的JDK 17路径
```
C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot
```

## 永久解决方案（无需配置环境变量）

### 方案1：pom.xml配置（已实现）
在`backend/pom.xml`中配置maven-compiler-plugin：
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.13.0</version>
    <configuration>
        <release>17</release>
        <fork>true</fork>
        <executable>C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot\bin\javac.exe</executable>
    </configuration>
</plugin>
```

### 方案2：Maven Toolchains
创建文件 `~/.m2/toolchains.xml`：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<toolchains>
  <toolchain>
    <type>jdk</type>
    <providers>
      <jdk>
        <version>17</version>
        <path>C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot</path>
      </jdk>
    </providers>
  </toolchain>
</toolchains>
```

### 方案3：系统PATH设置（推荐）
将JDK 17的bin目录添加到系统PATH环境变量：
```
C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot\bin
```
这样系统会自动找到正确的java和javac。

## 验证安装
```bash
java -version
# 应输出: openjdk version "17.0.19"
```

## 构建命令
```bash
cd backend
mvn clean package -DskipTests
```

## 注意事项
1. 不要使用JDK 1.8编译此项目（Spring Boot 3.x要求JDK 17+）
2. 如果更换JDK安装路径，需同步更新pom.xml中的executable路径
3. Windows下建议使用PowerShell或Git Bash执行Maven命令
