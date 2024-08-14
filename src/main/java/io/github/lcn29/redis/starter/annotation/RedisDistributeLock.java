package io.github.lcn29.redis.starter.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Redis 分布式锁注解
 *
 * @author canxin.li
 * @date 2024-08-14 17:23:29
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisDistributeLock {

    /**
     * 锁的 key
     *
     * @return key
     */
    String key();

    /**
     * 锁的过期时间, 单位毫秒
     *
     * @return 过期时间
     */
    long expireTime() default 30000;

    /**
     * 获取锁的超时时间, 单位毫秒
     *
     * @return 超时时间
     */
    long timeout() default 10000;
}
