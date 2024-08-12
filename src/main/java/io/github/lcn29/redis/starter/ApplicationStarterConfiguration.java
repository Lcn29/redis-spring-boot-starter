package io.github.lcn29.redis.starter;

import io.github.lcn29.redis.starter.config.CustomRedisAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 应用配置
 *
 * @author canxin.li
 * @date 2024-05-28 20:16:38
 */
@Configuration
@Import(value = {CustomRedisAutoConfiguration.class})
@EnableAutoConfiguration(exclude = {RedisAutoConfiguration.class})
public class ApplicationStarterConfiguration {
}
