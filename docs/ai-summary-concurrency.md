# 滚动摘要：并发协调器

实现类：`RedisSummaryConcurrencyCoordinator`（`ai.chat.summary-enabled=true` 时装配）

## 机制概览

| 步骤 | Redis 操作 |
|------|------------|
| 加锁 | `SET ai:summary:lock:{sid} {token} NX EX 60` |
| 捕获 batch | `RENAME ai:hot:{sid} ai:hot:snapshot:{sid}`，count 置 0 |
| 摘要成功 | `DEL snapshot`，`count = LLEN hot` |
| 摘要失败 | batch prepend 回 hot，`DEL snapshot` |
| 释放锁 | Lua：仅 token 匹配时 `DEL lock` |

## 配置

```yaml
ai:
  chat:
    summary-enabled: true
    summary-batch-size: 20
    session-ttl-days: 7
```

Legacy 模式（`summary-enabled: false`）使用 `ai.chat-history.storage: mysql|redis`。

执行 `src/main/resources/db/ai_session_summaries.sql` 后启用摘要模式。
