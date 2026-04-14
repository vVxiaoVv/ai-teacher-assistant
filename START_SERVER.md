# 服务端启动指南

## 前置条件

### 1. 启动MySQL服务

**方式一：使用brew services（推荐）**
```bash
brew services start mysql
```

**方式二：手动启动**
```bash
mysql.server start
```

**方式三：如果MySQL安装在系统路径**
```bash
sudo /usr/local/mysql/support-files/mysql.server start
```

### 2. 检查MySQL服务状态
```bash
# 检查端口
lsof -i :3306

# 测试连接
mysql -u root -p
```

### 3. 创建数据库（如果不存在）
```bash
mysql -u root -p
```

然后在MySQL命令行中执行：
```sql
CREATE DATABASE IF NOT EXISTS ai_teacher DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

或者执行项目中的初始化脚本：
```bash
mysql -u root -p < database/init.sql
```

## 配置数据库连接

编辑 `src/main/resources/application.yml`，配置数据库用户名和密码：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_teacher?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: your_password  # 修改为你的MySQL密码
```

## 启动服务

### 方式一：使用Maven（开发环境推荐）
```bash
cd /Users/tal/Documents/code/project/ai-teacher-assistant
mvn spring-boot:run
```

### 方式二：使用打包后的JAR（生产环境）
```bash
# 1. 打包
mvn clean package -DskipTests

# 2. 运行
java -jar target/ai-teacher-assistant-0.0.1-SNAPSHOT.jar
```

### 方式三：后台运行（带日志）
```bash
# 创建日志目录
mkdir -p logs

# 后台运行并输出日志
nohup java -jar target/ai-teacher-assistant-0.0.1-SNAPSHOT.jar > logs/app.log 2>&1 &

# 查看日志
tail -f logs/app.log
```

## 验证服务启动

### 1. 检查端口
```bash
lsof -i :8080
```

### 2. 测试接口
```bash
# 测试历史记录接口
curl http://localhost:8080/api/video-correction/history

# 测试视频分析历史接口
curl http://localhost:8080/api/video-analysis/history
```

## 常见问题

### 1. 数据库连接失败
- 检查MySQL服务是否运行：`lsof -i :3306`
- 检查数据库用户名密码是否正确
- 检查数据库`ai_teacher`是否存在

### 2. 端口被占用
```bash
# 查找占用8080端口的进程
lsof -i :8080

# 杀死进程
kill -9 <PID>
```

### 3. 编译错误
```bash
# 清理并重新编译
mvn clean compile
```

## 服务信息

- **服务端口**：8080
- **数据库**：MySQL localhost:3306/ai_teacher
- **API基础路径**：`/api`

## 快速启动命令

```bash
# 一键启动（需要先启动MySQL）
cd /Users/tal/Documents/code/project/ai-teacher-assistant
mvn spring-boot:run
```

