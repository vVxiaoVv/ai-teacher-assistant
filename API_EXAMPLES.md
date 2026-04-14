# 视频分析接口调用示例

## 接口信息

- **接口地址**: `POST /api/video-analysis/analyze`
- **服务端口**: `8080`
- **Content-Type**: `application/json`

## 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| videoUrl | String | 是 | 视频文件的URL地址 |

## 响应参数

| 参数名 | 类型 | 说明 |
|--------|------|------|
| formattedMessage | String | 格式化后的消息内容（已去除冗余标识符，自动换行） |
| rawMessage | String | 原始消息内容 |

---

## 调用示例

### 1. cURL 命令

```bash
curl --location 'http://localhost:8080/api/video-analysis/analyze' \
--header 'Content-Type: application/json' \
--data '{
    "videoUrl": "https://static0.xesimg.com/guardian-online/teacherTalk1.MP4"
}'
```

### 2. JavaScript (Fetch API)

```javascript
const videoUrl = 'https://static0.xesimg.com/guardian-online/teacherTalk1.MP4';

fetch('http://localhost:8080/api/video-analysis/analyze', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
    },
    body: JSON.stringify({
        videoUrl: videoUrl
    })
})
.then(response => response.json())
.then(data => {
    console.log('格式化后的消息:', data.formattedMessage);
    console.log('原始消息:', data.rawMessage);
})
.catch(error => {
    console.error('请求失败:', error);
});
```

### 3. JavaScript (Axios)

```javascript
const axios = require('axios');

const videoUrl = 'https://static0.xesimg.com/guardian-online/teacherTalk1.MP4';

axios.post('http://localhost:8080/api/video-analysis/analyze', {
    videoUrl: videoUrl
}, {
    headers: {
        'Content-Type': 'application/json'
    }
})
.then(response => {
    console.log('格式化后的消息:', response.data.formattedMessage);
    console.log('原始消息:', response.data.rawMessage);
})
.catch(error => {
    console.error('请求失败:', error.response?.data || error.message);
});
```

### 4. Python (requests)

```python
import requests
import json

url = "http://localhost:8080/api/video-analysis/analyze"

payload = {
    "videoUrl": "https://static0.xesimg.com/guardian-online/teacherTalk1.MP4"
}

headers = {
    "Content-Type": "application/json"
}

response = requests.post(url, json=payload, headers=headers)

if response.status_code == 200:
    data = response.json()
    print("格式化后的消息:", data.get("formattedMessage"))
    print("原始消息:", data.get("rawMessage"))
else:
    print(f"请求失败: {response.status_code}")
    print(response.text)
```

### 5. Java (Spring RestTemplate)

```java
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

RestTemplate restTemplate = new RestTemplate();

String url = "http://localhost:8080/api/video-analysis/analyze";

HttpHeaders headers = new HttpHeaders();
headers.setContentType(MediaType.APPLICATION_JSON);

Map<String, String> requestBody = new HashMap<>();
requestBody.put("videoUrl", "https://static0.xesimg.com/guardian-online/teacherTalk1.MP4");

HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

Map<String, Object> responseBody = response.getBody();
System.out.println("格式化后的消息: " + responseBody.get("formattedMessage"));
System.out.println("原始消息: " + responseBody.get("rawMessage"));
```

### 6. Java (OkHttp)

```java
import okhttp3.*;
import com.google.gson.Gson;
import java.io.IOException;

public class VideoAnalysisClient {
    public static void main(String[] args) throws IOException {
        OkHttpClient client = new OkHttpClient();
        
        String json = "{\"videoUrl\":\"https://static0.xesimg.com/guardian-online/teacherTalk1.MP4\"}";
        
        RequestBody body = RequestBody.create(
            json, 
            MediaType.parse("application/json; charset=utf-8")
        );
        
        Request request = new Request.Builder()
            .url("http://localhost:8080/api/video-analysis/analyze")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                System.out.println("响应: " + responseBody);
            } else {
                System.out.println("请求失败: " + response.code());
            }
        }
    }
}
```

### 7. Postman 配置

**请求方式**: `POST`

**URL**: `http://localhost:8080/api/video-analysis/analyze`

**Headers**:
```
Content-Type: application/json
```

**Body** (选择 raw -> JSON):
```json
{
    "videoUrl": "https://static0.xesimg.com/guardian-online/teacherTalk1.MP4"
}
```

---

## 请求示例

```json
{
    "videoUrl": "https://static0.xesimg.com/guardian-online/teacherTalk1.MP4"
}
```

---

## 响应示例

### 成功响应 (200 OK)

```json
{
    "formattedMessage": "根据视频内容分析，以下是针对老师试讲的详细评估：\n\n1. 发音方面\n在视频的第3分钟到第5分钟，老师存在以下发音问题：\n- 部分词语发音不够清晰\n- 语速略快，建议适当放慢\n\n2. 表情和教态\n整体表现良好，但在第8分钟时表情略显紧张，建议：\n- 保持自然微笑\n- 增加与学生眼神交流\n\n3. 课堂设计\n知识性：9分\n逻辑思路：8分\n互动性：7分\n\n总体评价：课堂设计合理，但在互动环节可以进一步加强。",
    "rawMessage": "根据视频内容分析，以下是针对老师试讲的详细评估：\n\n**1. 发音方面**\n在视频的第3分钟到第5分钟，老师存在以下发音问题：\n- 部分词语发音不够清晰\n- 语速略快，建议适当放慢\n\n**2. 表情和教态**\n整体表现良好，但在第8分钟时表情略显紧张，建议：\n- 保持自然微笑\n- 增加与学生眼神交流\n\n**3. 课堂设计**\n知识性：9分\n逻辑思路：8分\n互动性：7分\n\n总体评价：课堂设计合理，但在互动环节可以进一步加强。"
}
```

### 错误响应 (400 Bad Request)

当 `videoUrl` 为空时：

```json
{
    "timestamp": "2024-01-15T10:30:00.000+00:00",
    "status": 400,
    "error": "Bad Request",
    "message": "视频URL不能为空",
    "path": "/api/video-analysis/analyze"
}
```

### 错误响应 (500 Internal Server Error)

当调用大模型API失败时：

```json
{
    "timestamp": "2024-01-15T10:30:00.000+00:00",
    "status": 500,
    "error": "Internal Server Error",
    "message": "调用视频分析API失败: ...",
    "path": "/api/video-analysis/analyze"
}
```

---

## 注意事项

1. **视频URL格式**: 必须是可公开访问的视频文件URL，支持常见的视频格式（MP4、AVI等）
2. **响应时间**: 由于需要调用大模型API进行视频分析，响应时间可能较长（通常需要几十秒到几分钟）
3. **超时设置**: 建议客户端设置较长的超时时间（建议5分钟以上）
4. **错误处理**: 请妥善处理网络错误、超时错误等异常情况
5. **CORS配置**: 如果前端跨域调用，需要确保后端已配置CORS

---

## 测试视频URL示例

```json
{
    "videoUrl": "https://static0.xesimg.com/guardian-online/teacherTalk1.MP4"
}
```

---

## 完整调用流程

1. 客户端发送POST请求，包含视频URL
2. 服务端接收请求，验证参数
3. 服务端调用大模型API进行视频分析
4. 服务端格式化返回结果
5. 客户端接收响应，展示分析结果

---

## 前端集成示例 (uni-app)

```javascript
// pages/video-analysis/index.vue
export default {
  data() {
    return {
      videoUrl: '',
      loading: false,
      result: null
    }
  },
  methods: {
    async analyzeVideo() {
      if (!this.videoUrl) {
        uni.showToast({
          title: '请输入视频URL',
          icon: 'none'
        });
        return;
      }
      
      this.loading = true;
      try {
        const response = await uni.request({
          url: 'http://localhost:8080/api/video-analysis/analyze',
          method: 'POST',
          header: {
            'Content-Type': 'application/json'
          },
          data: {
            videoUrl: this.videoUrl
          },
          timeout: 300000 // 5分钟超时
        });
        
        if (response.statusCode === 200) {
          this.result = response.data;
          uni.showToast({
            title: '分析完成',
            icon: 'success'
          });
        } else {
          throw new Error(response.data.message || '请求失败');
        }
      } catch (error) {
        uni.showToast({
          title: error.message || '分析失败，请重试',
          icon: 'none'
        });
      } finally {
        this.loading = false;
      }
    }
  }
}
```

