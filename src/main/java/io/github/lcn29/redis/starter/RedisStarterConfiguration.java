package io.github.lcn29.redis.starter;

import io.github.lcn29.redis.starter.config.LettuceConnectionConfiguration;
import io.github.lcn29.redis.starter.config.RedisTemplateConfiguration;
import io.github.lcn29.redis.starter.properties.CustomRedisProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 应用配置
 *
 * @author canxin.li
 * @date 2024-05-28 20:16:38
 */
@Configuration
@EnableConfigurationProperties(CustomRedisProperties.class)
@Import(value = {LettuceConnectionConfiguration.class, RedisTemplateConfiguration.class})
public class RedisStarterConfiguration {

    private final static Logger logger = LoggerFactory.getLogger(RedisStarterConfiguration.class);

    static {
        logger.info("RedisStarterConfiguration Init ...");
    }

}
