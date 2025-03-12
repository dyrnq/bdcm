package com.dyrnq.bdcm;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
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

/**
 * "repo" 是 "repository" 的缩写。
 * 在软件开发和版本控制的 context 中，"repository" 指的是一个存储和管理代码、文件和其他数据的中央位置。例如，Git 仓库就是一个代码 repository。
 * 因此，"repo" 是一个常用的缩写，用于指代代码仓库、软件仓库或其他类型的 repository。
 */

@Configuration
public class RepoConfig {
    static Logger logger = LoggerFactory.getLogger(RepoConfig.class);
    @Inject
    RepoProps repoProps;

    private RepoProps repoProps() {
        return repoProps;
    }

    @Init
    public void init() {
        RepoProps repoProps = repoProps();
        if (repoProps.getS3() != null) {
            if (repoProps.getS3().getAccessKey() != null) {
                repoProps.getS3().setAccessKey("******");
            }
            if (repoProps.getS3().getSecretKey() != null) {
                repoProps.getS3().setSecretKey("******");
            }
        }
        logger.info("***************repoType={}", JSONUtil.toJsonStr(repoProps));
        if (StrUtil.equalsIgnoreCase("local", repoProps().getType())) {
            if (StrUtil.isNotBlank(repoProps().getLocal().getListen())) {
                if (StrUtil.isNotBlank(repoProps().getLocal().getPath())) {
                    File file = new File(repoProps().getLocal().getPath());
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                }
                Thread tcpThread = getThread(repoProps().getLocal().getListen());
                tcpThread.start();
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
            ResourceHandler resourceHandler = new ResourceHandler(new FileResourceManager(new File(repoProps().getLocal().getPath()), 100));
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
