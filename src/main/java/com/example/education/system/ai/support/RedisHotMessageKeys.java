package com.example.education.system.ai.support;

/**
 * Redis 热消息与摘要协调相关的 Key 命名规范。
 */
public final class RedisHotMessageKeys {

    private RedisHotMessageKeys() {
    }

    public static final String HOT_PREFIX = "ai:hot:";
    public static final String COUNT_PREFIX = "ai:hot:count:";
    public static final String SNAPSHOT_PREFIX = "ai:hot:snapshot:";
    public static final String LOCK_PREFIX = "ai:summary:lock:";

    //
    public static String hot(String sessionId) {
        return HOT_PREFIX + sessionId;
    }

    //记录对话次数
    public static String count(String sessionId) {
        return COUNT_PREFIX + sessionId;
    }

    //快照
    public static String snapshot(String sessionId) {
        return SNAPSHOT_PREFIX + sessionId;
    }

    //锁
    public static String lock(String sessionId) {
        return LOCK_PREFIX + sessionId;
    }
}
