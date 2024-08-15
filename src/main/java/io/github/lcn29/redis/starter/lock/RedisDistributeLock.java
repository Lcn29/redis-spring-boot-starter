package io.github.lcn29.redis.starter.lock;

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
     */
    String keyDesc();

    /**
     * 锁的 key 属性
     */
    String[] keyField() default "";

    /**
     * 锁的 value
     *
     * @return value
     */
    String lockValue() default "";

    /**
     * 锁的过期时间, 单位毫秒
     *
     * @return 过期时间
     */
    long expireTime() default 1000;

    /**
     * 获取锁的超时时间, 单位毫秒
     *
     * @return 超时时间
     */
    long acquireTimeout() default 3000;

}
