package io.github.lcn29.redis.starter.properties;

import java.util.List;

/**
 * 集群模式配置
 *
 * @author canxin.li
 * @date 2024-08-13 16:14:09
 */
public class ClusterModelProperties {

    private String username;

    private String password;

    /**
     * 格式: IP:PORT
     */
    private List<String> nodes;

    /**
     * 集群中执行命令时要遵循的最大重定向数
     */
    private Integer maxRedirects;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    public Integer getMaxRedirects() {
        return maxRedirects;
    }

    public void setMaxRedirects(Integer maxRedirects) {
        this.maxRedirects = maxRedirects;
    }
}
