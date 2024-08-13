package io.github.lcn29.redis.starter.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.StandardCharsets;

/**
 * Redis Auto Config
 *
 * @author lcn29
 * @date 2024-08-12 21:57:25
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(LettuceConnectionConfiguration.class)
public class RedisTemplateConfiguration {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory, StringRedisSerializer stringRedisSerializer, Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer) {

        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 指定 key 的序列化方式
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);

        // 指定 value 的序列化方式
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory, StringRedisSerializer stringRedisSerializer, Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer) {

        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(redisConnectionFactory);

        // 指定 key 的序列化方式
        stringRedisTemplate.setKeySerializer(stringRedisSerializer);
        stringRedisTemplate.setHashKeySerializer(stringRedisSerializer);

        // 指定 value 的序列化方式
        stringRedisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        stringRedisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        stringRedisTemplate.afterPropertiesSet();
        return stringRedisTemplate;
    }

    /**
     * 获取 StringRedisSerializer, 用来序列化 redis key
     *
     * @return StringRedisSerializer
     */
    @Bean
    public StringRedisSerializer stringRedisSerializer() {
        return new StringRedisSerializer(StandardCharsets.UTF_8);
    }

    /**
     * 获取 Jackson2JsonRedisSerializer, 用来序列化 redis Value
     *
     * @return Jackson2JsonRedisSerializer
     */
    @Bean
    public Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer() {
        return new Jackson2JsonRedisSerializer<>(redisObjectMapper(), Object.class);
    }

    /**
     * 获取 Jackson2JsonRedisSerializer 需要的 ObjectMapper
     *
     * @return ObjectMapper
     */
    private ObjectMapper redisObjectMapper() {

        ObjectMapper redisObjectMapper = new ObjectMapper();
        // 序列化的范围和修饰符的范围 All 全部情况都序列化
        redisObjectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 序列化的类的配置, EVERYTHING 所有类型, 包含 final 修饰的类
        redisObjectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.EVERYTHING, JsonTypeInfo.As.PROPERTY);
        // 遇到未知属性不失败
        redisObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 序列化中时间的格式的属性处理
        redisObjectMapper.registerModule(new JavaTimeModule());
        // 属性驼峰命名
        redisObjectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return redisObjectMapper;
    }
}
