package com.dyrnq.bdcm;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
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

    public String guessExternalUrl() {
        String type = repoProps().getType();
        String externalUrl = repoProps().getExternalUrl();
        RepoProps.RepoS3 s3 = repoProps().getS3();
        RepoProps.RepoLocal local = repoProps().getLocal();
        if (StrUtil.isNotEmpty(externalUrl)) {
            return externalUrl;
        } else {
            if (StrUtil.equalsIgnoreCase(RepoType.LOCAL, type)) {
                return "http://"+StrUtil.replace(local.getListen(), "0.0.0.0", "127.0.0.1")+"/";
            } else {

                return StrUtil.join("/", s3.getEndpoint(), s3.getBucket(), "");

            }
        }
    }

    private void info() {
        RepoProps repo = ObjectUtil.cloneByStream(repoProps());
        if (repo.getS3() != null) {
            if (repo.getS3().getAccessKey() != null) {
                repo.getS3().setAccessKey("******");
            }
            if (repo.getS3().getSecretKey() != null) {
                repo.getS3().setSecretKey("******");
            }
        }
        logger.info("***************repoType={}", JSONUtil.toJsonStr(repo));
        logger.debug("***************repoType={}", JSONUtil.toJsonStr(repoProps()));
    }

    @Init
    public void init() {
        info();
        if (StrUtil.equalsIgnoreCase(RepoType.LOCAL, repoProps().getType())) {
            if (StrUtil.isNotBlank(repoProps().getLocal().getListen())) {
                if (StrUtil.isNotBlank(repoProps().getLocal().getPath())) {
                    File file = new File(repoProps().getLocal().getPath());
                    FileUtil.mkdir(new File(repoProps().getLocal().getPath()));
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
