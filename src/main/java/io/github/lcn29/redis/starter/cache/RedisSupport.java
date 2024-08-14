package io.github.lcn29.redis.starter.cache;

import io.github.lcn29.redis.starter.cache.batch.RedisBatchGetOp;
import io.github.lcn29.redis.starter.cache.batch.RedisBatchSetOp;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Redis 支持类
 *
 * @author canxin.li
 * @date 2024-08-14 11:06:26
 */
public class RedisSupport {

    private final static String DEFAULT_LOCK_VALUE = "RedisLock";

    private final StringRedisSerializer stringRedisSerializer;
    private final Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer;
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisSupport(RedisTemplate<String, Object> redisTemplate,
                        StringRedisSerializer stringRedisSerializer,
                        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer) {
        this.stringRedisSerializer = stringRedisSerializer;
        this.jackson2JsonRedisSerializer = jackson2JsonRedisSerializer;
        this.redisTemplate = redisTemplate;
    }

    public boolean lock(RedisKeyDesc redisKeyDesc, Object redisValue, Long expireTime, Object... params) {

        return Boolean.TRUE.equals(redisTemplate.execute((RedisCallback<Boolean>) connection -> {

            byte[] keySerializeByte = stringRedisSerializer.serialize(generateRedisKey(redisKeyDesc, params));
            byte[] valueSerializeByte = jackson2JsonRedisSerializer.serialize(redisValue);
            if (keySerializeByte == null || valueSerializeByte == null) {
                return false;
            }
            return connection.stringCommands()
                    .set(keySerializeByte, valueSerializeByte, Expiration.from(expireTime, TimeUnit.MILLISECONDS),
                            RedisStringCommands.SetOption.SET_IF_ABSENT);
        }));
    }

    public boolean tryLock(RedisKeyDesc redisKeyDesc, Object redisValue, Long expireTime, Long lockTryTime, Object... params) {

        // TODO
        return Boolean.TRUE.equals(redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            byte[] keySerializeByte = stringRedisSerializer.serialize(generateRedisKey(redisKeyDesc, params));
            byte[] valueSerializeByte = jackson2JsonRedisSerializer.serialize(redisValue);
            if (keySerializeByte == null || valueSerializeByte == null) {
                return false;
            }
            return connection.stringCommands()
                    .set(keySerializeByte, valueSerializeByte, Expiration.from(expireTime, TimeUnit.MILLISECONDS),
                            RedisStringCommands.SetOption.SET_IF_ABSENT);
        }));
    }

    public boolean unlock(RedisKeyDesc redisKeyDesc, Object... params) {

        return Boolean.TRUE.equals(redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            byte[] keySerializeByte = stringRedisSerializer.serialize(generateRedisKey(redisKeyDesc, params));
            if (keySerializeByte == null) {
                return false;
            }
            Long delNum = connection.keyCommands().del(keySerializeByte);
            return delNum != null && delNum > 0;
        }));
    }

    public boolean tryUnLock(RedisKeyDesc redisKeyDesc, Object lockValue, Object... params) {
        return Boolean.TRUE.equals(redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            byte[] keySerializeByte = stringRedisSerializer.serialize(generateRedisKey(redisKeyDesc, params));
            if (keySerializeByte == null) {
                return false;
            }
            byte[] bytes = connection.stringCommands().get(keySerializeByte);
            Object deserialize = jackson2JsonRedisSerializer.deserialize(bytes);
            if (lockValue != deserialize) {
                return false;
            }
            Long delNum = connection.keyCommands().del(keySerializeByte);
            return delNum != null && delNum > 0;
        }));
    }

    /**
     * 判断 key 是否存在
     *
     * @param redisKeyDesc 缓存 key 描述
     * @param params       拼接 key 的参数
     * @return 是否存在 true 存在, false 不存在
     */
    public boolean isExists(RedisKeyDesc redisKeyDesc, Object... params) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(generateRedisKey(redisKeyDesc, params)));
    }

    /**
     * 判断 key 是否过期
     *
     * @param redisKeyDesc 缓存 key 描述
     * @param params       拼接 key 的参数
     * @return 是否过期 true 过期, false 未过期
     */
    public boolean isExpire(RedisKeyDesc redisKeyDesc, Object... params) {
        Long expire = redisTemplate.opsForValue().getOperations().getExpire(generateRedisKey(redisKeyDesc, params));
        return expire == null || expire <= 0;
    }

    /**
     * 设置 key 过期
     *
     * @param redisKeyDesc 缓存 key 描述
     * @param params       拼接 key 的参数
     */
    public void expire(RedisKeyDesc redisKeyDesc, Object... params) {
        expire(redisKeyDesc, redisKeyDesc.expireTime(), params);
    }

    /**
     * 设置 key 过期
     *
     * @param redisKeyDesc     缓存 key 描述
     * @param customExpireTime 自定义过期时间
     * @param params           拼接 key 的参数
     */
    public void expire(RedisKeyDesc redisKeyDesc, long customExpireTime, Object... params) {
        redisTemplate.opsForValue()
                .getOperations()
                .expire(generateRedisKey(redisKeyDesc, params), customExpireTime, TimeUnit.MILLISECONDS);
    }

    /**
     * 获取缓存
     *
     * @param redisKeyDesc 缓存 key 描述
     * @param params       拼接 key 的参数
     * @return RedisValue
     */
    public <T> T get(RedisKeyDesc redisKeyDesc, Object... params) {
        Object redisValue = redisTemplate.opsForValue()
                .get(generateRedisKey(redisKeyDesc, params));
        return objectCast(redisValue);
    }

    /**
     * 批量获取缓存
     *
     * @param redisBatchSetOpList 操作列表
     * @return RedisValue
     */
    public <T> List<T> multiGet(List<RedisBatchGetOp> redisBatchSetOpList) {

        if (CollectionUtils.isEmpty(redisBatchSetOpList)) {
            return Collections.emptyList();
        }

        List<String> redisKeyList = redisBatchSetOpList.stream()
                .map(getOp -> generateRedisKey(getOp.getRedisKeyDesc(), getOp.getParams()))
                .toList();

        List<Object> redisValueList = redisTemplate.opsForValue().multiGet(redisKeyList);
        if (CollectionUtils.isEmpty(redisValueList)) {
            return Collections.emptyList();
        }
        List<T> returnValue = new ArrayList<>(redisValueList.size());
        redisValueList.stream().filter(Objects::nonNull).forEach(item -> returnValue.add(objectCast(item)));
        return returnValue;
    }

    /**
     * 设置缓存
     *
     * @param redisKeyDesc 缓存 key 描述
     * @param redisValue   缓存 value
     * @param params       拼接 key 的参数
     */
    public void set(RedisKeyDesc redisKeyDesc, Object redisValue, Object... params) {
        setWithCustomExpireTime(redisKeyDesc, redisValue, redisKeyDesc.expireTime(), params);
    }

    /**
     * 设置缓存
     *
     * @param redisKeyDesc     缓存 key 描述
     * @param redisValue       缓存 value
     * @param customExpireTime 自定义过期时间
     * @param params           拼接 key 的参数
     */
    public void setWithCustomExpireTime(RedisKeyDesc redisKeyDesc, Object redisValue, long customExpireTime, Object... params) {
        if (redisValue == null || customExpireTime < 0) {
            return;
        }
        String redisKey = generateRedisKey(redisKeyDesc, params);
        if (customExpireTime == RedisKeyDesc.NO_EXPIRE_MARK) {
            redisTemplate.opsForValue().set(redisKey, redisValue);
            return;
        }
        redisTemplate.opsForValue().set(redisKey, redisValue, customExpireTime, TimeUnit.MILLISECONDS);
    }

    /**
     * 批量设置缓存
     *
     * @param redisBatchSetOpList 缓存对象列表
     */
    public void batchSet(List<RedisBatchSetOp> redisBatchSetOpList) {

        if (CollectionUtils.isEmpty(redisBatchSetOpList)) {
            return;
        }

        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {


            for (RedisBatchSetOp redisBatchSetOp : redisBatchSetOpList) {

                byte[] keySerializeByte = stringRedisSerializer.serialize(generateRedisKey(redisBatchSetOp.getRedisKeyDesc(), redisBatchSetOp.getParams()));
                byte[] valueSerializeByte = jackson2JsonRedisSerializer.serialize(redisBatchSetOp.getValue());

                if (keySerializeByte == null || valueSerializeByte == null) {
                    continue;
                }

                Long expireTime = redisBatchSetOp.expireTime();
                if (expireTime == RedisKeyDesc.NO_EXPIRE_MARK) {
                    connection.stringCommands().set(keySerializeByte, valueSerializeByte);
                } else {
                    connection.stringCommands().set(
                            keySerializeByte,
                            valueSerializeByte,
                            Expiration.from(expireTime, TimeUnit.MILLISECONDS),
                            RedisStringCommands.SetOption.UPSERT);
                }
            }
            return null;
        });
    }

    /**
     * 设置缓存，如果 key 存在则更新过期时间
     *
     * @param redisKeyDesc 缓存 key 描述
     * @param redisValue   缓存 value
     * @param params       拼接 key 的参数
     */
    public void setOrExpireIfExist(RedisKeyDesc redisKeyDesc, Object redisValue, Object... params) {
        setOrExpireIfExist(redisKeyDesc, redisValue, redisKeyDesc.expireTime(), params);
    }

    /**
     * 设置缓存，如果 key 存在则更新过期时间
     *
     * @param redisKeyDesc     缓存 key 描述
     * @param redisValue       缓存 value
     * @param customExpireTime 自定义过期时间`
     * @param params           拼接 key 的参数
     */
    public void setOrExpireIfExist(RedisKeyDesc redisKeyDesc, Object redisValue, long customExpireTime, Object... params) {
        if (redisValue == null || customExpireTime <= 0) {
            return;
        }
        redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            byte[] keySerializeByte = stringRedisSerializer.serialize(generateRedisKey(redisKeyDesc, params));
            if (keySerializeByte == null) {
                return false;
            }

            // 检查 key 是否存在
            Boolean exists = connection.keyCommands().exists(keySerializeByte);
            if (exists != null && exists) {
                // 如果 key 存在，只更新过期时间
                connection.keyCommands().pExpire(keySerializeByte, customExpireTime);
                return true;
            }

            byte[] valueSerializeByte = jackson2JsonRedisSerializer.serialize(redisValue);

            if (valueSerializeByte == null) {
                return false;
            }

            connection.stringCommands().set(
                    keySerializeByte,
                    valueSerializeByte,
                    Expiration.from(customExpireTime, TimeUnit.MILLISECONDS),
                    RedisStringCommands.SetOption.UPSERT);
            return true;
        });
    }

    /**
     * 随机过期时间
     *
     * @param baseTime        基础的时间
     * @param randomTimeRange 随机的时间范围
     * @return 需要的随机时间
     */
    public static long randomExpireTime(Long baseTime, long randomTimeRange) {
        return baseTime + ThreadLocalRandom.current().nextLong(randomTimeRange);
    }

    /**
     * 获取 Redis key
     *
     * @param redisKeyDesc key 描述
     * @param params       拼接 key 的参数
     * @return Redis key
     */
    private String generateRedisKey(RedisKeyDesc redisKeyDesc, Object... params) {
        return redisKeyDesc.redisKeys(params);
    }

    /**
     * 对象强制转换
     *
     * @param obj 转换的类型
     * @return 转换后的对象
     */
    @SuppressWarnings("unchecked")
    private <T> T objectCast(Object obj) {
        if (obj == null) {
            return null;
        }
        return (T) obj;
    }
}
