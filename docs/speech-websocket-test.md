# 语音识别 WebSocket 测试指南

## Postman WebSocket 测试

### 1. 连接配置

**WebSocket URL:**
```
ws://speech-internal.tal.com/v1/ws
```

**Headers 尝试（逐一测试）:**

| Header Name | Value |
|-------------|-------|
| api-key | 300000775:568f52f31bf0782018145f7dc56c3afb |
| Authorization | Basic MzAwMDAwNzc1OjU2OGY1MmYzMWJmMDc4MjAxODE0NWY3ZGM1NmMzYWZi |
| X-NLS-Token | 568f52f31bf0782018145f7dc56c3afb |
| appkey | 300000775 |

### 2. StartRecognition 消息

连接成功后，发送以下 JSON 消息开始识别：

```json
{
    "header": {
        "message_id": "3a0a75093d1948cf826b292fd9bd05d9",
        "task_id": "ed3a0019bebb416e94fbc1979a738553",
        "namespace": "SpeechRecognizer",
        "name": "StartRecognition",
        "appkey": "300000775"
    },
    "payload": {
        "cluster": "azure",
        "format": "pcm"
    },
    "context": {
        "sdk": {
            "name": "postman-test",
            "version": "1.0.0",
            "language": "javascript"
        },
        "app": {},
        "system": {},
        "device": {},
        "network": {},
        "geography": {},
        "bridge": {},
        "custom": {}
    }
}
```

### 3. StopRecognition 消息

停止识别时发送：

```json
{
    "header": {
        "message_id": "b0a799c88c454c638425465030415376",
        "task_id": "ed3a0019bebb416e94fbc1979a738553",
        "namespace": "SpeechRecognizer",
        "name": "StopRecognition",
        "appkey": "300000775"
    },
    "context": {
        "sdk": {
            "name": "postman-test",
            "version": "1.0.0",
            "language": "javascript"
        }
    }
}
```

---

## cURL 测试 WebSocket 握手

使用 wscat 工具测试（需要先安装: `npm install -g wscat`）：

```bash
# 方式1: 使用 api-key header
wscat -c "ws://speech-internal.tal.com/v1/ws" -H "api-key: 300000775:568f52f31bf0782018145f7dc56c3afb"

# 方式2: 使用 Authorization Basic
wscat -c "ws://speech-internal.tal.com/v1/ws" -H "Authorization: Basic MzAwMDAwNzc1OjU2OGY1MmYzMWJmMDc4MjAxODE0NWY3ZGM1NmMzYWZi"

# 方式3: 使用 X-NLS-Token
wscat -c "ws://speech-internal.tal.com/v1/ws" -H "X-NLS-Token: 568f52f31bf0782018145f7dc56c3afb"

# 方式4: URL 参数
wscat -c "ws://speech-internal.tal.com/v1/ws?api-key=300000775:568f52f31bf0782018145f7dc56c3afb"
```

---

## Postman Collection 导入

将以下 JSON 保存为 `speech-ws-test.postman_collection.json` 并导入 Postman：

```json
{
    "info": {
        "name": "Speech WebSocket Test",
        "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
    },
    "item": [
        {
            "name": "WebSocket Connection Test",
            "request": {
                "method": "GET",
                "header": [
                    {
                        "key": "api-key",
                        "value": "300000775:568f52f31bf0782018145f7dc56c3afb"
                    },
                    {
                        "key": "X-NLS-Token",
                        "value": "568f52f31bf0782018145f7dc56c3afb"
                    },
                    {
                        "key": "appkey",
                        "value": "300000775"
                    }
                ],
                "url": {
                    "raw": "ws://speech-internal.tal.com/v1/ws",
                    "protocol": "ws",
                    "host": ["speech-internal", "tal", "com"],
                    "path": ["v1", "ws"]
                }
            }
        }
    ]
}
```

---

## 测试步骤

1. **打开 Postman** → 新建 WebSocket Request
2. **输入 URL**: `ws://speech-internal.tal.com/v1/ws`
3. **添加 Headers**（在 Headers 标签页）:
   - `api-key`: `300000775:568f52f31bf0782018145f7dc56c3afb`
4. **点击 Connect**
5. **观察结果**:
   - 如果连接成功，状态会显示 "Connected"
   - 如果返回 401，说明认证方式不对，尝试其他 Header 组合

---

## Base64 编码说明

`Authorization: Basic` 的值是 api-key 的 Base64 编码：

```
原文: 300000775:568f52f31bf0782018145f7dc56c3afb
Base64: MzAwMDAwNzc1OjU2OGY1MmYzMWJmMDc4MjAxODE0NWY3ZGM1NmMzYWZi
```




