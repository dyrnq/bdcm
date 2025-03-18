package com.dyrnq.bdcm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.noear.solon.annotation.BindProps;
import org.noear.solon.annotation.Configuration;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Configuration
@BindProps(prefix = "grab")
public class GrabProps {
    private HostAndPort httpProxy = new HostAndPort();
    private HostAndPort httpsProxy = new HostAndPort();
    private ThreadPool threadPool = new ThreadPool();
    private List<KeyVal> httpHeaders;
    private int retry;
    private int retryInterval;
    private int connectTimeout;
    private int readTimeout;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static
    public class KeyVal {
        String name;
        String value;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static
    public class HostAndPort {
        private boolean enable;
        private String host;
        private int port;
        private String exclude;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static
    public class ThreadPool {
        private String corePoolSize;
        private String maxPoolSize;
        private int queueCapacity;
        private int keepAliveSeconds;
    }
}
