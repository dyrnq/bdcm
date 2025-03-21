package com.dyrnq.bdcm;

import lombok.Data;
import org.noear.solon.annotation.BindProps;
import org.noear.solon.annotation.Configuration;

@Data
@Configuration
@BindProps(prefix = "http-proxy")
public class HttpProxy {
    private boolean enable;
    private String type;
    private String host;
    private int port;
    private String exclude;
}
