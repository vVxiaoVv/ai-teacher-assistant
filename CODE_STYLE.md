# 代码风格规范（基于renren-fast）

本文档定义了项目开发中应遵循的代码风格和编程规范，参考renren-fast框架的最佳实践。

## 一、项目结构

```
src/main/java/com/moke/assistant/
├── common/                    # 公共模块
│   ├── annotation/           # 自定义注解
│   ├── aspect/              # 切面处理
│   ├── exception/           # 异常处理
│   ├── utils/               # 工具类
│   └── R.java               # 统一响应格式
├── config/                   # 配置类
├── controller/              # 控制器层
├── service/                 # 服务层接口
│   └── impl/               # 服务层实现
├── entity/                  # 实体类
├── repository/             # 数据访问层（JPA Repository）
└── dto/                    # 数据传输对象
```

## 二、统一响应格式

### 2.1 使用R类统一返回

所有Controller方法必须使用`R`类作为返回类型，不使用`ResponseEntity`。

**正确示例：**
```java
@PostMapping("/analyze")
public R analyzeVideo(@RequestBody VideoAnalysisRequest request) {
    VideoAnalysisResponse response = videoAnalysisService.analyzeVideo(request.getVideoUrl());
    return R.ok().put("data", response);
}
```

**错误示例：**
```java
@PostMapping("/analyze")
public ResponseEntity<VideoAnalysisResponse> analyzeVideo(@RequestBody VideoAnalysisRequest request) {
    // ...
    return ResponseEntity.ok(response);
}
```

### 2.2 R类的使用方式

```java
// 成功响应（无数据）
return R.ok();

// 成功响应（带消息）
return R.ok("操作成功");

// 成功响应（带数据）
return R.ok().put("data", result);

// 成功响应（多个数据）
return R.ok().put("list", list).put("total", total);

// 错误响应
return R.error("错误信息");

// 错误响应（带错误码）
return R.error(400, "参数错误");
```

## 三、异常处理

### 3.1 使用ServiceException抛出业务异常

**正确示例：**
```java
if (request.getVideoUrl() == null || request.getVideoUrl().trim().isEmpty()) {
    throw new ServiceException("视频URL不能为空");
}
```

**错误示例：**
```java
if (request.getVideoUrl() == null || request.getVideoUrl().trim().isEmpty()) {
    throw new IllegalArgumentException("视频URL不能为空");
}
```

### 3.2 异常处理机制

- 所有业务异常使用`ServiceException`
- 参数验证异常使用`IllegalArgumentException`（会被GlobalExceptionHandler转换为400错误）
- 系统异常会被GlobalExceptionHandler统一捕获并返回友好提示

## 四、Controller层规范

### 4.1 基本规范

1. **类注释**：每个Controller类必须有类注释
2. **方法注释**：每个public方法必须有JavaDoc注释
3. **参数验证**：在Controller层进行基本参数验证
4. **统一返回**：所有方法返回`R`类型

**示例：**
```java
/**
 * 视频分析控制器
 */
@RestController
@RequestMapping("/api/video-analysis")
public class VideoAnalysisController {

    /**
     * 分析视频内容
     * 
     * @param request 包含视频URL的请求对象
     * @return 格式化后的分析结果
     */
    @PostMapping("/analyze")
    public R analyzeVideo(@RequestBody VideoAnalysisRequest request) {
        if (request.getVideoUrl() == null || request.getVideoUrl().trim().isEmpty()) {
            throw new ServiceException("视频URL不能为空");
        }
        
        VideoAnalysisResponse response = videoAnalysisService.analyzeVideo(request.getVideoUrl().trim());
        return R.ok().put("data", response);
    }
}
```

### 4.2 RESTful API设计

- GET：查询操作
- POST：创建操作
- PUT：更新操作
- DELETE：删除操作

## 五、Service层规范

### 5.1 接口和实现分离

- Service接口定义在`service`包下
- Service实现定义在`service/impl`包下
- 使用接口注入，不使用实现类注入

**正确示例：**
```java
private final VideoAnalysisService videoAnalysisService;

public VideoAnalysisController(VideoAnalysisService videoAnalysisService) {
    this.videoAnalysisService = videoAnalysisService;
}
```

### 5.2 方法命名规范

- 查询单个：`getXxx`、`findXxx`
- 查询列表：`listXxx`、`getXxxList`
- 创建：`createXxx`、`saveXxx`
- 更新：`updateXxx`
- 删除：`deleteXxx`、`removeXxx`

## 六、实体类规范

### 6.1 使用Lombok简化代码

```java
@Entity
@Table(name = "video_analysis_record")
@Data
public class VideoAnalysisRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "video_url", nullable = false, length = 500)
    private String videoUrl;
    
    // ...
}
```

### 6.2 字段命名规范

- 使用驼峰命名
- 布尔类型使用`is`前缀（Lombok会自动处理）
- 时间字段统一使用`LocalDateTime`

## 七、DTO规范

### 7.1 DTO命名

- 请求DTO：`XxxRequest`
- 响应DTO：`XxxResponse`
- 查询DTO：`XxxQueryDto`
- 列表项DTO：`XxxItemDto`

### 7.2 使用Lombok

所有DTO类使用`@Data`注解：

```java
@Data
public class VideoAnalysisRequest {
    private String videoUrl;
}
```

## 八、工具类规范

### 8.1 工具类设计原则

- 工具类方法必须是静态方法
- 工具类应该是无状态的
- 工具类应该有清晰的注释

### 8.2 常用工具类

- `R`：统一响应格式
- `PageUtils`：分页工具
- `Constant`：常量定义
- `Query`：查询参数封装

## 九、日志规范

### 9.1 日志级别

- ERROR：系统错误、异常信息
- WARN：警告信息、潜在问题
- INFO：重要业务流程、关键操作
- DEBUG：调试信息（生产环境关闭）

### 9.2 日志格式

```java
private static final Logger logger = LoggerFactory.getLogger(ClassName.class);

logger.info("开始处理视频分析，videoUrl: {}", videoUrl);
logger.error("视频分析失败: {}", e.getMessage(), e);
```

## 十、代码注释

### 10.1 类注释

```java
/**
 * 视频分析控制器
 */
@RestController
public class VideoAnalysisController {
}
```

### 10.2 方法注释

```java
/**
 * 分析视频内容
 * 
 * @param request 包含视频URL的请求对象
 * @return 格式化后的分析结果
 */
public R analyzeVideo(@RequestBody VideoAnalysisRequest request) {
}
```

## 十一、常量定义

### 11.1 使用Constant类

所有常量定义在`Constant`类中：

```java
public class Constant {
    public static final String TASK_STATUS_PROCESSING = "PROCESSING";
    public static final String TASK_STATUS_DONE = "DONE";
    public static final int DEFAULT_PAGE_SIZE = 10;
}
```

### 11.2 避免魔法值

**错误示例：**
```java
if ("PROCESSING".equals(status)) {
    // ...
}
```

**正确示例：**
```java
if (Constant.TASK_STATUS_PROCESSING.equals(status)) {
    // ...
}
```

## 十二、代码格式

### 12.1 缩进和空格

- 使用4个空格缩进（不使用Tab）
- 运算符前后有空格
- 方法参数之间用空格分隔

### 12.2 大括号位置

```java
// 推荐
if (condition) {
    // code
}

// 不推荐
if (condition)
{
    // code
}
```

## 十三、命名规范

### 13.1 类命名

- 类名使用大驼峰（PascalCase）
- Controller：`XxxController`
- Service：`XxxService`、`XxxServiceImpl`
- Entity：`Xxx`（实体名）
- DTO：`XxxDto`、`XxxRequest`、`XxxResponse`

### 13.2 方法命名

- 方法名使用小驼峰（camelCase）
- 布尔方法使用`is`、`has`、`can`等前缀

### 13.3 变量命名

- 变量名使用小驼峰
- 常量使用全大写下划线分隔：`MAX_SIZE`
- 集合变量使用复数形式：`userList`、`itemMap`

## 十四、最佳实践

### 14.1 参数验证

- Controller层进行基本参数验证
- 复杂验证使用Bean Validation（`@Valid`、`@NotNull`等）

### 14.2 异常处理

- 不要捕获异常后什么都不做
- 记录异常日志
- 返回友好的错误提示

### 14.3 代码复用

- 提取公共方法
- 使用工具类
- 避免重复代码

### 14.4 性能优化

- 避免在循环中进行数据库查询
- 合理使用缓存
- 注意N+1查询问题

## 十五、总结

遵循以上规范可以：
1. 提高代码可读性和可维护性
2. 统一团队开发风格
3. 减少代码审查时间
4. 提高开发效率

所有新代码必须遵循本规范，现有代码在重构时逐步调整。

