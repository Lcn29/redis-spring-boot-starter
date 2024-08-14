package io.github.lcn29.redis.starter.cache.batch;

import io.github.lcn29.redis.starter.cache.RedisKeyDesc;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * batch get operation
 *
 * @author canxin.li
 * @date 2024-08-14 09:59:29
 */
public class RedisBatchGetOp {

    @NonNull
    private final RedisKeyDesc redisKeyDesc;

    @Nullable
    private Object[] params;

    public RedisBatchGetOp(@NonNull RedisKeyDesc redisKeyDesc) {
        this.redisKeyDesc = redisKeyDesc;
    }

    public RedisBatchGetOp(@NonNull RedisKeyDesc redisKeyDesc, @Nullable Object... params) {
        this.redisKeyDesc = redisKeyDesc;
        this.params = params;
    }

    @NonNull
    public RedisKeyDesc getRedisKeyDesc() {
        return redisKeyDesc;
    }

    @Nullable
    public Object[] getParams() {
        return params;
    }
}
