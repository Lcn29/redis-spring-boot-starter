package io.github.lcn29.redis.starter.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Redis Config
 *
 * @author canxin.li
 * @date 2024-08-13 15:59:07
 */
@ConfigurationProperties(prefix = "custom.redis")
public class CustomRedisProperties {

    @NestedConfigurationProperty
    private final LettuceClientProperties client = new LettuceClientProperties();

    @NestedConfigurationProperty
    private StandaloneModelProperties standaloneModel;

    @NestedConfigurationProperty
    private ClusterModelProperties clusterModel;

    public LettuceClientProperties getClient() {
        return client;
    }

    public StandaloneModelProperties getStandaloneModel() {
        return standaloneModel;
    }

    public ClusterModelProperties getClusterModel() {
        return clusterModel;
    }

    public void setStandaloneModel(StandaloneModelProperties standaloneModel) {
        this.standaloneModel = standaloneModel;
    }

    public void setClusterModel(ClusterModelProperties clusterModel) {
        this.clusterModel = clusterModel;
    }
}
