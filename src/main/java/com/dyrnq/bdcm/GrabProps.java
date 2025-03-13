package com.dyrnq.bdcm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.noear.solon.annotation.BindProps;
import org.noear.solon.annotation.Configuration;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Configuration
@BindProps(prefix="grab")
public class GrabProps {
    private HostAndPort httpProxy = new HostAndPort();
    private HostAndPort httpsProxy = new HostAndPort();

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
}
