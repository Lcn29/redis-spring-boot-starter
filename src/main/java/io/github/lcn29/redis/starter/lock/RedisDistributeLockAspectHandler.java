package io.github.lcn29.redis.starter.lock;

import io.github.lcn29.redis.starter.cache.RedisSupport;
import io.github.lcn29.redis.starter.exception.RedisDistributeLockException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

/**
 * RedisDistributeLockAspectHandler
 *
 * @author canxin.li
 * @date 2024-08-15 16:46:49
 */
@Aspect
public class RedisDistributeLockAspectHandler {

    private final static Logger log = LoggerFactory.getLogger(RedisDistributeLockAspectHandler.class);

    private final static ExpressionParser PARSER = new SpelExpressionParser();

    private final static StandardEvaluationContext CONTEXT = new StandardEvaluationContext();

    private final static StandardReflectionParameterNameDiscoverer DISCOVERER = new StandardReflectionParameterNameDiscoverer();


    private final RedisSupport redisSupport;

    public RedisDistributeLockAspectHandler(RedisSupport redisSupport) {
        this.redisSupport = redisSupport;
    }

    @Around("@annotation(redisDistributeLock)")
    public Object redisDistributeLockAround(ProceedingJoinPoint joinPoint, RedisDistributeLock redisDistributeLock) throws Throwable {

        if (redisDistributeLock.keyDesc().isBlank()) {
            throw new RedisDistributeLockException("RedisDistributeLock: keyDesc is blank");
        }

        String redisKey = getRedisKey(joinPoint, redisDistributeLock);
        String redisValue = getRedisValue(redisDistributeLock);

        boolean isLock = redisSupport.tryLock(RedisDistributeLockEnum.LOCK, redisValue,
                redisDistributeLock.expireTime(), redisDistributeLock.acquireTimeout(), redisKey);

        if (!isLock) {
            throw new RedisDistributeLockException("RedisDistributeLock: get lock failed");
        }

        try {
            log.info("RedisDistributeLock: get lock success, key: {}", redisKey);
            return joinPoint.proceed();
        } finally {
            redisSupport.tryUnLock(RedisDistributeLockEnum.LOCK, redisValue, redisKey);
            log.info("RedisDistributeLock: release lock success, key: {}", redisKey);
        }
    }

    /**
     * 获取切面的目标方法
     */
    private Method getMethod(ProceedingJoinPoint pjp) {

        Signature signature = pjp.getSignature();
        if (!(signature instanceof MethodSignature methodSignature)) {
            throw new RedisDistributeLockException("RedisDistributeLock: current method is not aop method");
        }

        Object target = pjp.getTarget();
        if (target == null) {
            throw new RedisDistributeLockException("RedisDistributeLock: unFound the aop target object");
        }

        try {
            return target.getClass().getMethod(
                    methodSignature.getName(),
                    methodSignature.getParameterTypes());
        } catch (NoSuchMethodException ex) {
            throw new RedisDistributeLockException("RedisDistributeLock: unFound the aop target method");
        }
    }

    /**
     * 获取 redis key
     */
    private String getRedisKey(ProceedingJoinPoint joinPoint, RedisDistributeLock redisDistributeLock) {

        String[] keyFieldArray = redisDistributeLock.keyField();
        if (keyFieldArray == null || keyFieldArray.length == 0) {
            return redisDistributeLock.keyDesc();
        }

        Method joinPointMethod = getMethod(joinPoint);
        String[] paraNameArr = DISCOVERER.getParameterNames(joinPointMethod);
        if (paraNameArr == null || paraNameArr.length == 0) {
            return redisDistributeLock.keyDesc();
        }

        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < paraNameArr.length; i++) {
            CONTEXT.setVariable(paraNameArr[i], args[i]);
        }

        Object[] fieldArray = Arrays.stream(keyFieldArray)
                .map(keyField -> PARSER.parseExpression(keyField).getValue(CONTEXT, String.class))
                .toArray();
        return String.format(redisDistributeLock.keyDesc(), fieldArray);
    }

    /**
     * 获取 redis value
     */
    private String getRedisValue(RedisDistributeLock redisDistributeLock) {
        if (StringUtils.hasText(redisDistributeLock.lockValue())) {
            return redisDistributeLock.lockValue();
        }
        return UUID.randomUUID().toString().replace("-", "");
    }

}
