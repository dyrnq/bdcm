package com.dyrnq.bdcm;

import cn.hutool.core.util.StrUtil;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

@Configuration
public class RepoConfig {
    static Logger logger = LoggerFactory.getLogger(RepoConfig.class);
    @Inject("${repo.type}")
    String repoType;
    @Inject("${repo.local.path}")
    String repoLocalPath;
    @Inject("${repo.local.listen}")
    String repoLocalListen;

    @Init
    public void init() {
        if (StrUtil.equalsIgnoreCase("local", repoType)) {
            if (StrUtil.isNotBlank(repoLocalListen)) {
                Thread tcpThread = getThread(repoLocalListen);
                tcpThread.start();
                logger.info("***************repoType={},repoLocalPath={},{}监听已开启, 监听端口 {}",
                        repoType,
                        repoLocalPath,
                        tcpThread.getName(),
                        repoLocalListen);
            }
        }
    }

    @NotNull
    private Thread getThread(String repoLocalListen) {

        String[] listenArray = StringUtils.splitByWholeSeparator(repoLocalListen, ":");
        int port = 9888;
        String host = "0.0.0.0";

        if (listenArray.length >= 2) {
            try {
                port = Integer.parseInt(listenArray[1]);
            } catch (Exception e) {
                logger.warn(e.getMessage());
            }
            host = listenArray[0];
        } else {
            try {
                port = Integer.parseInt(listenArray[0]);
            } catch (Exception e) {
                logger.warn(e.getMessage());
            }
        }
        final int port_final = port;
        final String host_final = host;


        Thread tcpThread = new Thread(() -> {
            PathHandler path = new PathHandler();
            ResourceHandler resourceHandler = new ResourceHandler(new FileResourceManager(new File(repoLocalPath), 100));
            resourceHandler.setDirectoryListingEnabled(true);

            path.addPrefixPath("/", resourceHandler);

            Undertow server = Undertow.builder()
                    .addHttpListener(port_final, host_final)
                    .setHandler(path)
                    .build();

            server.start();
        });
        tcpThread.setName("static-server");
        return tcpThread;
    }
}
