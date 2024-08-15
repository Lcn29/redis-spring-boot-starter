package io.github.lcn29.redis.starter.exception;

/**
 * RedisDistributeLockException
 *
 * @author canxin.li
 * @date 2024-08-15 16:50:53
 */
public class RedisDistributeLockException extends RuntimeException {

    public RedisDistributeLockException(final String errorMessage, final Object... args) {
        super(String.format(errorMessage, args));
    }

    public RedisDistributeLockException(final Exception cause) {
        super(cause);
    }

    public RedisDistributeLockException(final Throwable cause) {
        super(cause);
    }

}
