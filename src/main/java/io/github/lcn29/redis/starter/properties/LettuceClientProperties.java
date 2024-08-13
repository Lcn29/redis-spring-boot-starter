package io.github.lcn29.redis.starter.properties;

import java.time.Duration;

/**
 * Lettuce Client 配置属性
 *
 * @author canxin.li
 * @date 2024-08-13 16:12:55
 */
public class LettuceClientProperties {

    private String clientName;
    private Duration timeout = Duration.ofMillis(2000L);
    private Duration shutdownTimeout = Duration.ofMillis(100L);
    private Duration connectTimeout = Duration.ofMillis(1000L);

    private final Ssl ssl = new Ssl();

    private final Refresh refresh = new Refresh();

    private final Pool pool = new Pool();

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public Duration getShutdownTimeout() {
        return shutdownTimeout;
    }

    public void setShutdownTimeout(Duration shutdownTimeout) {
        this.shutdownTimeout = shutdownTimeout;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Ssl getSsl() {
        return ssl;
    }

    public Refresh getRefresh() {
        return refresh;
    }

    public Pool getPool() {
        return pool;
    }

    public static class Ssl {

        /**
         * Whether to enable SSL support. Enabled automatically if "bundle" is provided
         * unless specified otherwise.
         */
        private Boolean enabled = false;

        /**
         * SSL bundle name.
         */
        private String bundle;

        public boolean isEnabled() {
            return (this.enabled != null) ? this.enabled : this.bundle != null;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getBundle() {
            return this.bundle;
        }

        public void setBundle(String bundle) {
            this.bundle = bundle;
        }

    }

    public static class Refresh {

        /**
         * Whether to discover and query all cluster nodes for obtaining the
         * cluster topology. When set to false, only the initial seed nodes are
         * used as sources for topology discovery.
         */
        private boolean dynamicRefreshSources = true;

        /**
         * Cluster topology refresh period.
         */
        private Duration period;

        /**
         * Whether adaptive topology refreshing using all available refresh
         * triggers should be used.
         */
        private boolean adaptive;

        public boolean isDynamicRefreshSources() {
            return this.dynamicRefreshSources;
        }

        public void setDynamicRefreshSources(boolean dynamicRefreshSources) {
            this.dynamicRefreshSources = dynamicRefreshSources;
        }

        public Duration getPeriod() {
            return this.period;
        }

        public void setPeriod(Duration period) {
            this.period = period;
        }

        public boolean isAdaptive() {
            return this.adaptive;
        }

        public void setAdaptive(boolean adaptive) {
            this.adaptive = adaptive;
        }

    }

    public static class Pool {

        private int maxIdle = 8;
        private int minIdle = 0;
        private int maxActive = 8;
        private Duration maxWait = Duration.ofMillis(-1L);
        private Duration timeBetweenEvictionRuns;

        public Pool() {
        }

        public int getMaxIdle() {
            return this.maxIdle;
        }

        public void setMaxIdle(int maxIdle) {
            this.maxIdle = maxIdle;
        }

        public int getMinIdle() {
            return this.minIdle;
        }

        public void setMinIdle(int minIdle) {
            this.minIdle = minIdle;
        }

        public int getMaxActive() {
            return this.maxActive;
        }

        public void setMaxActive(int maxActive) {
            this.maxActive = maxActive;
        }

        public Duration getMaxWait() {
            return this.maxWait;
        }

        public void setMaxWait(Duration maxWait) {
            this.maxWait = maxWait;
        }

        public Duration getTimeBetweenEvictionRuns() {
            return this.timeBetweenEvictionRuns;
        }

        public void setTimeBetweenEvictionRuns(Duration timeBetweenEvictionRuns) {
            this.timeBetweenEvictionRuns = timeBetweenEvictionRuns;
        }
    }
}
