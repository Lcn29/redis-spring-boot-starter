package io.github.lcn29.redis.starter.cache;

import java.util.Objects;

/**
 * Redis Key 描述接口
 *
 * @author canxin.li
 * @date 2024-08-14 09:55:33
 */
public interface RedisKeyDesc {

    long NO_EXPIRE_MARK = 0L;

    /**
     * 用户自定义 Redis key 描述
     *
     * @return key 的描述
     */
    String keyDesc();

    /**
     * 获取过期时间, 单位毫秒
     *
     * @return 过期时间
     */
    long expireTime();

    /**
     * 获取 Redis key
     *
     * @return Redis key
     */
    default String redisKeys(Object... params) {
        if (Objects.isNull(params) || params.length == 0) {
            return keyDesc();
        }
        return String.format(keyDesc(), params);
    }

    /**
     * 是否永不过期
     *
     * @return 是否永不过期
     */
    default boolean notExpire() {
        return NO_EXPIRE_MARK == expireTime();
    }


}
