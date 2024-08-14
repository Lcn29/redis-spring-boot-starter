package io.github.lcn29.redis.starter.cache.batch;

import io.github.lcn29.redis.starter.cache.RedisKeyDesc;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Redis 批量设置操作
 *
 * @author canxin.li
 * @date 2024-08-14 10:00:47
 */
public class RedisBatchSetOp {

    @NonNull
    private final RedisKeyDesc redisKeyDesc;

    @NonNull
    private final Object value;

    @Nullable
    private Object[] params;

    @Nullable
    private Long customExpireTime;

    public RedisBatchSetOp(@NonNull RedisKeyDesc redisKeyDesc,
                           @NonNull Object value) {
        this.redisKeyDesc = redisKeyDesc;
        this.value = value;
    }

    public RedisBatchSetOp(@NonNull RedisKeyDesc redisKeyDesc,
                           @NonNull Object value,
                           @Nullable Object... params) {
        this(redisKeyDesc, value);
        this.params = params;
    }

    public RedisBatchSetOp(@NonNull RedisKeyDesc redisKeyDesc,
                           @NonNull Object value,
                           @Nullable Long customExpireTime,
                           @Nullable Object... params) {
        this(redisKeyDesc, value, params);
        this.customExpireTime = customExpireTime;
    }

    public RedisBatchSetOp(@NonNull RedisKeyDesc redisKeyDesc,
                           @NonNull Object value,
                           @NonNull Long customExpireTime) {
        this.redisKeyDesc = redisKeyDesc;
        this.value = value;
        this.customExpireTime = customExpireTime;
    }

    public Long expireTime() {
        return customExpireTime != null ? customExpireTime : redisKeyDesc.expireTime();
    }

    @NonNull
    public RedisKeyDesc getRedisKeyDesc() {
        return redisKeyDesc;
    }

    @Nullable
    public Object[] getParams() {
        return params;
    }

    @NonNull
    public Object getValue() {
        return value;
    }

}
