package io.github.lcn29.redis.starter.config;

import io.github.lcn29.redis.starter.properties.ClusterModelProperties;
import io.github.lcn29.redis.starter.properties.CustomRedisProperties;
import io.github.lcn29.redis.starter.properties.LettuceClientProperties;
import io.github.lcn29.redis.starter.properties.StandaloneModelProperties;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnThreading;
import org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.thread.Threading;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.ssl.SslOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * LettuceConnectionConfiguration 配置
 *
 * @author canxin.li
 * @date 2024-08-13 14:41:43
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RedisClient.class)
public class LettuceConnectionConfiguration {

    private final CustomRedisProperties redisProperties;

    public LettuceConnectionConfiguration(CustomRedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(ClientResources.class)
    public DefaultClientResources lettuceClientResources(ObjectProvider<ClientResourcesBuilderCustomizer> customizers) {
        DefaultClientResources.Builder builder = DefaultClientResources.builder();
        customizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean(RedisConnectionFactory.class)
    @ConditionalOnThreading(Threading.PLATFORM)
    public LettuceConnectionFactory redisConnectionFactory(
            ClientResources clientResources,
            ObjectProvider<SslBundles> sslBundles,
            ObjectProvider<LettuceClientConfigurationBuilderCustomizer> builderCustomizers) {

        return createConnectionFactory(clientResources, sslBundles, builderCustomizers);
    }

    @Bean
    @ConditionalOnMissingBean(RedisConnectionFactory.class)
    @ConditionalOnThreading(Threading.VIRTUAL)
    public LettuceConnectionFactory redisConnectionFactoryVirtualThreads(
            ClientResources clientResources,
            ObjectProvider<SslBundles> sslBundles,
            ObjectProvider<LettuceClientConfigurationBuilderCustomizer> builderCustomizers) {

        LettuceConnectionFactory lettuceConnectionFactory = createConnectionFactory(clientResources, sslBundles, builderCustomizers);
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("redis-");
        executor.setVirtualThreads(true);
        lettuceConnectionFactory.setExecutor(executor);
        return lettuceConnectionFactory;
    }


    /**
     * 创建连接工厂
     *
     * @param clientResources    客户端资源
     * @param sslBundles         SSL 配置
     * @param builderCustomizers 客户端配置定制器
     * @return 连接工厂
     */
    private LettuceConnectionFactory createConnectionFactory(
            ClientResources clientResources,
            ObjectProvider<SslBundles> sslBundles,
            ObjectProvider<LettuceClientConfigurationBuilderCustomizer> builderCustomizers) {

        // 客户端配置
        LettuceClientConfiguration clientConfiguration = createLettucePoolingClientConfiguration(clientResources, builderCustomizers, sslBundles);

        // 集群模式
        if (redisProperties.getClusterModel() != null) {
            RedisClusterConfiguration clusterConfiguration = getClusterConfiguration();
            if (clusterConfiguration == null) {
                throw new BeanCreationException("Cluster configuration missing");
            }
            return new LettuceConnectionFactory(clusterConfiguration, clientConfiguration);
        }

        RedisStandaloneConfiguration standaloneConfig = getStandaloneConfig();
        if (standaloneConfig == null) {
            throw new BeanCreationException("Standalone configuration missing");
        }
        // 单节点模式
        return new LettuceConnectionFactory(standaloneConfig, clientConfiguration);
    }

    /**
     * 创建 LettucePoolingClientConfiguration
     *
     * @param clientResources    客户端资源
     * @param builderCustomizers 客户端配置定制器
     * @param sslBundles         SSL 配置
     * @return LettucePoolingClientConfiguration
     */
    private LettucePoolingClientConfiguration createLettucePoolingClientConfiguration(
            ClientResources clientResources,
            ObjectProvider<LettuceClientConfigurationBuilderCustomizer> builderCustomizers,
            ObjectProvider<SslBundles> sslBundles
    ) {

        LettuceClientProperties clientConfig = redisProperties.getClient();

        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder builder = LettucePoolingClientConfiguration.builder();

        if (StringUtils.hasText(clientConfig.getClientName())) {
            builder.clientName(clientConfig.getClientName());
        }

        builder.commandTimeout(clientConfig.getTimeout());

        if (clientConfig.getShutdownTimeout() != null && clientConfig.getShutdownTimeout().toMillis() > 0L) {
            builder.shutdownTimeout(clientConfig.getShutdownTimeout());
        }

        if (clientConfig.getSsl().isEnabled()) {
            builder.useSsl();
        }
        builder.clientOptions(createClientOptions(clientConfig, sslBundles));
        builder.poolConfig(createGenericObjectPoolConfig(clientConfig));
        builder.clientResources(clientResources);

        builderCustomizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
        return builder.build();
    }

    /**
     * 创建客户端配置
     *
     * @param clientConfig 客户端配置
     * @param sslBundles   SSL 配置
     * @return 客户端配置
     */
    private ClientOptions createClientOptions(LettuceClientProperties clientConfig, ObjectProvider<SslBundles> sslBundles) {
        ClientOptions.Builder builder = createClientOptionsBuilder(clientConfig);
        Duration connectTimeout = clientConfig.getConnectTimeout();
        if (connectTimeout != null) {
            builder.socketOptions(SocketOptions.builder().connectTimeout(connectTimeout).build());
        }
        if (clientConfig.getSsl().isEnabled() && clientConfig.getSsl().getBundle() != null) {
            sslBundles.ifAvailable((bundles) -> {
                SslBundle sslBundle = bundles.getBundle(clientConfig.getSsl().getBundle());
                io.lettuce.core.SslOptions.Builder sslOptionsBuilder = io.lettuce.core.SslOptions.builder();
                sslOptionsBuilder.keyManager(sslBundle.getManagers().getKeyManagerFactory());
                sslOptionsBuilder.trustManager(sslBundle.getManagers().getTrustManagerFactory());
                SslOptions sslOptions = sslBundle.getOptions();
                if (sslOptions.getCiphers() != null) {
                    sslOptionsBuilder.cipherSuites(sslOptions.getCiphers());
                }
                if (sslOptions.getEnabledProtocols() != null) {
                    sslOptionsBuilder.protocols(sslOptions.getEnabledProtocols());
                }
                builder.sslOptions(sslOptionsBuilder.build());
            });
        }
        return builder.timeoutOptions(TimeoutOptions.enabled()).build();
    }

    /**
     * 创建对象池配置
     *
     * @param clientConfig 客户端配置
     * @return 对象池配置
     */
    private GenericObjectPoolConfig<Object> createGenericObjectPoolConfig(LettuceClientProperties clientConfig) {
        GenericObjectPoolConfig<Object> genericObjectPoolConfig = new GenericObjectPoolConfig<>();
        genericObjectPoolConfig.setMaxIdle(clientConfig.getPool().getMaxIdle());
        genericObjectPoolConfig.setMinIdle(clientConfig.getPool().getMinIdle());
        genericObjectPoolConfig.setMaxTotal(clientConfig.getPool().getMaxActive());
        genericObjectPoolConfig.setMaxWait(clientConfig.getPool().getMaxWait());
        Optional.ofNullable(clientConfig.getPool().getTimeBetweenEvictionRuns())
                .ifPresent(genericObjectPoolConfig::setTimeBetweenEvictionRuns);
        return genericObjectPoolConfig;
    }

    /**
     * 初始化客户端配置构建器
     *
     * @param clientConfig 客户端配置
     * @return 客户端配置构建器
     */
    private ClientOptions.Builder createClientOptionsBuilder(LettuceClientProperties clientConfig) {

        if (redisProperties.getClusterModel() == null) {
            return ClientOptions.builder();
        }

        ClusterClientOptions.Builder builder = ClusterClientOptions.builder();
        LettuceClientProperties.Refresh refreshProperties = clientConfig.getRefresh();
        ClusterTopologyRefreshOptions.Builder refreshBuilder = ClusterTopologyRefreshOptions.builder()
                .dynamicRefreshSources(refreshProperties.isDynamicRefreshSources());
        if (refreshProperties.getPeriod() != null) {
            refreshBuilder.enablePeriodicRefresh(refreshProperties.getPeriod());
        }
        if (refreshProperties.isAdaptive()) {
            refreshBuilder.enableAllAdaptiveRefreshTriggers();
        }
        return builder.topologyRefreshOptions(refreshBuilder.build());
    }

    /**
     * 获取 Redis 集群配置
     *
     * @return Redis 集群配置
     */
    protected final RedisClusterConfiguration getClusterConfiguration() {

        ClusterModelProperties clusterProperties = redisProperties.getClusterModel();
        if (clusterProperties == null) {
            return null;
        }
        List<String> nodeList = clusterProperties.getNodes();
        if (CollectionUtils.isEmpty(nodeList)) {
            return null;
        }
        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration(nodeList);
        Optional.ofNullable(clusterProperties.getMaxRedirects()).ifPresent(redisClusterConfiguration::setMaxRedirects);
        Optional.ofNullable(clusterProperties.getUsername()).ifPresent(redisClusterConfiguration::setUsername);
        Optional.ofNullable(clusterProperties.getPassword()).ifPresent(redisClusterConfiguration::setPassword);
        return redisClusterConfiguration;
    }

    /**
     * 获取 Redis 单节点的配置
     *
     * @return Redis 单节点配置
     */
    private RedisStandaloneConfiguration getStandaloneConfig() {

        StandaloneModelProperties standaloneModelProperties = redisProperties.getStandaloneModel();
        if (standaloneModelProperties == null) {
            return null;
        }

        RedisStandaloneConfiguration redisStandaloneConfigurationConfig = new RedisStandaloneConfiguration();
        redisStandaloneConfigurationConfig.setHostName(standaloneModelProperties.getHost());
        redisStandaloneConfigurationConfig.setPort(standaloneModelProperties.getPort());
        Optional.ofNullable(standaloneModelProperties.getUsername()).ifPresent(redisStandaloneConfigurationConfig::setUsername);
        Optional.ofNullable(standaloneModelProperties.getPassword()).ifPresent(redisStandaloneConfigurationConfig::setPassword);
        return redisStandaloneConfigurationConfig;
    }

}
