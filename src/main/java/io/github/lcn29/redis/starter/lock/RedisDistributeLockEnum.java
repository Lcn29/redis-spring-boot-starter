package io.github.lcn29.redis.starter.lock;

import io.github.lcn29.redis.starter.cache.RedisKeyDesc;

/**
 * RedisDistributeLockEnum
 *
 * @author canxin.li
 * @date 2024-08-15 17:38:34
 */
public enum RedisDistributeLockEnum implements RedisKeyDesc {

    LOCK("%s", 0L);

    private final String key;

    private final Long expireTime;

    RedisDistributeLockEnum(String key, Long expireTime) {
        this.key = key;
        this.expireTime = expireTime;
    }

    @Override
    public String keyDesc() {
        return key;
    }

    @Override
    public long expireTime() {
        return expireTime;
    }
}
