package io.github.lcn29.redis.starter.properties;

/**
 * 单节点配置
 *
 * @author canxin.li
 * @date 2024-08-13 16:13:45
 */
public class StandaloneModelProperties {

    private String host;
    private int port = 6379;
    private String username;
    private String password;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

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
}
