package com.example.education.system.ai.service.impl;

import com.example.education.system.ai.model.ChatMessage;
import com.example.education.system.ai.config.AiSummaryConstants;
import com.example.education.system.ai.service.RedisHotMessageStore;
import com.example.education.system.ai.service.SummaryConcurrencyCoordinator;
import com.example.education.system.ai.support.RedisHotMessageKeys;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 基于 Redis 的滚动摘要并发协调器。
 *
 * <h3>核心机制</h3>
 * <ul>
 *   <li><b>分布式锁</b>：{@code SET ai:summary:lock:{sid} NX EX 60}，释放时用 Lua 校验 token</li>
 *   <li><b>RENAME 快照</b>：{@code RENAME ai:hot:{sid} ai:hot:snapshot:{sid}}，摘要期间新消息写入新的 hot List</li>
 *   <li><b>失败回滚</b>：将 snapshot 中的 batch prepend 回 hot，并同步 count</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = AiSummaryConstants.SUMMARY_ENABLED_PROPERTY, havingValue = AiSummaryConstants.SUMMARY_ENABLED_VALUE)
public class RedisSummaryConcurrencyCoordinator implements SummaryConcurrencyCoordinator {

    private static final Logger log = LoggerFactory.getLogger(RedisSummaryConcurrencyCoordinator.class);

    private static final Duration LOCK_TTL = Duration.ofSeconds(60);

    /**
     * 仅释放当前线程持有的锁，防止误删其他实例的锁。
     */
    private static final DefaultRedisScript<Long> RELEASE_LOCK_SCRIPT = new DefaultRedisScript<>(
            """
                    if redis.call('get', KEYS[1]) == ARGV[1] then
                        return redis.call('del', KEYS[1])
                    else
                        return 0
                    end
                    """,
            Long.class
    );

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisHotMessageStore redisHotMessageStore;

    /** 当前异步任务线程持有的锁 token（每个 triggerIfNeeded 在单线程内完成） */
    private final ThreadLocal<String> lockTokens = new ThreadLocal<>();


    @Override
    public boolean tryLock(String sessionId) {
        String token = UUID.randomUUID().toString();
        Boolean acquired = stringRedisTemplate.opsForValue().setIfAbsent(
                RedisHotMessageKeys.lock(sessionId),//对sessionId进行加锁
                token,//谁加的锁
                LOCK_TTL//加多长时间
        );
        if (Boolean.TRUE.equals(acquired)) {
            lockTokens.set(token);
            log.debug("获取摘要锁成功, sessionId={}", sessionId);
            return true;
        }
        log.debug("获取摘要锁失败, sessionId={}", sessionId);
        return false;
    }

    @Override
    public void releaseLock(String sessionId) {
        String token = lockTokens.get();
        if (token == null) {
            return;
        }
        try {
            stringRedisTemplate.execute(
                    RELEASE_LOCK_SCRIPT,
                    Collections.singletonList(RedisHotMessageKeys.lock(sessionId)),
                    token
            );
        } finally {
            lockTokens.remove();
        }
    }

    /*
    *对一次分支进行总结
    *
    * */
    @Override
    public List<ChatMessage> captureBatchForSummary(String sessionId) {
        // 若上次摘要失败遗留 snapshot，先合并回 hot 再重新捕获
        recoverStaleSnapshotIfNeeded(sessionId);

        String hotKey = RedisHotMessageKeys.hot(sessionId);
        String snapshotKey = RedisHotMessageKeys.snapshot(sessionId);

        if (!Boolean.TRUE.equals(stringRedisTemplate.hasKey(hotKey))) {
            return List.of();
        }

        Long hotSize = stringRedisTemplate.opsForList().size(hotKey);
        if (hotSize == null || hotSize == 0) {
            return List.of();
        }

        // RENAME：hot → snapshot；之后 saveMessage 会创建新的 hot List
        stringRedisTemplate.rename(hotKey, snapshotKey);
        redisHotMessageStore.refreshAllTtls(sessionId);

        List<ChatMessage> batch = redisHotMessageStore.loadSnapshot(sessionId);
        // 本批次已移入 snapshot，活跃 count 从 0 重新累计
        redisHotMessageStore.resetCountAfterCapture(sessionId);

        log.info("已捕获摘要快照, sessionId={}, batchSize={}", sessionId, batch.size());
        return batch;
    }

    /*
    *
    * */
    @Override
    public void onSummarySuccess(String sessionId, List<ChatMessage> summarizedBatch) {
        redisHotMessageStore.deleteSnapshot(sessionId);
        // 摘要期间可能已有新消息写入 hot，count 应与 hot 实际长度一致
        redisHotMessageStore.syncCountWithHotList(sessionId);
        log.info("摘要成功, 已清理快照, sessionId={}, summarizedSize={}",
                sessionId, summarizedBatch == null ? 0 : summarizedBatch.size());
    }

    @Override
    public void onSummaryFailure(String sessionId, List<ChatMessage> batch) {
        if (batch != null && !batch.isEmpty()) {
            // snapshot 内容 prepend 到 hot 前面；hot 中可能已有摘要期间新产生的消息
            redisHotMessageStore.prependToHot(sessionId, batch);
        } else if (redisHotMessageStore.hasSnapshot(sessionId)) {
            List<ChatMessage> snapshot = redisHotMessageStore.loadSnapshot(sessionId);
            redisHotMessageStore.prependToHot(sessionId, snapshot);
        }
        redisHotMessageStore.deleteSnapshot(sessionId);
        redisHotMessageStore.syncCountWithHotList(sessionId);
        log.warn("摘要失败, 已回滚快照, sessionId={}, batchSize={}",
                sessionId, batch == null ? 0 : batch.size());
    }

    @Override
    public boolean isSummarizing(String sessionId) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(RedisHotMessageKeys.lock(sessionId)))
                || redisHotMessageStore.hasSnapshot(sessionId);
    }

    /**
     * 处理上次崩溃/失败遗留的 snapshot：合并回 hot 后继续正常流程。
     */
    private void recoverStaleSnapshotIfNeeded(String sessionId) {
        if (!redisHotMessageStore.hasSnapshot(sessionId)) {
            return;
        }
        List<ChatMessage> stale = redisHotMessageStore.loadSnapshot(sessionId);
        if (!stale.isEmpty()) {
            redisHotMessageStore.prependToHot(sessionId, stale);
            log.warn("发现遗留 snapshot，已合并回 hot, sessionId={}, size={}", sessionId, stale.size());
        }
        redisHotMessageStore.deleteSnapshot(sessionId);
        redisHotMessageStore.syncCountWithHotList(sessionId);
    }
}
