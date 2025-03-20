package com.dyrnq.bdcm;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.dyrnq.utils.AddressUtils;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.util.ETag;
import io.undertow.util.MimeMappings;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetSocketAddress;

/**
 * "repo" 是 "repository" 的缩写。
 * 在软件开发和版本控制的 context 中，"repository" 指的是一个存储和管理代码、文件和其他数据的中央位置。例如，Git 仓库就是一个代码 repository。
 * 因此，"repo" 是一个常用的缩写，用于指代代码仓库、软件仓库或其他类型的 repository。
 */

@Configuration
public class RepoConfig {
    final static int DEFAULT_REPO_LISTEN_PORT = 9980;
    static Logger logger = LoggerFactory.getLogger(RepoConfig.class);
    @Inject
    RepoProps repoProps;
    @Inject
    HomeDir homeDir;
    private String guessExternalUrl;

    private RepoProps repoProps() {
        return repoProps;
    }

    public String guessExternalUrl() {
        return guessExternalUrl;
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
        String externalUrl = repoProps().getExternalUrl();
        if (StrUtil.isNotEmpty(externalUrl)) {
            if (StrUtil.endWith(externalUrl, "/")) {
                guessExternalUrl = externalUrl;
            } else {
                guessExternalUrl = externalUrl + "/";
            }
        }

        if (StrUtil.equalsIgnoreCase(RepoType.LOCAL, repoProps().getType())) {
            if (StrUtil.isNotBlank(repoProps().getLocal().getListen())) {
                String defaultRepoLocalPath = StringUtils.joinWith(File.separator, homeDir.getHomeAbsolutePath(), "local_repo");
                if (StrUtil.isBlank(repoProps().getLocal().getPath())) {
                    repoProps().getLocal().setPath(defaultRepoLocalPath);
                }
                FileUtil.mkdir(new File(repoProps().getLocal().getPath()));
                InetSocketAddress inetSocketAddress = AddressUtils.parseAddress(repoProps().getLocal().getListen(), DEFAULT_REPO_LISTEN_PORT, true);
                if (StrUtil.isBlank(guessExternalUrl)) {
                    if (StrUtil.equalsAny(inetSocketAddress.getHostName(), "", "0.0.0.0")) {
                        guessExternalUrl = String.format("http://127.0.0.1:%s/", inetSocketAddress.getPort());
                    } else if (StrUtil.equals(inetSocketAddress.getHostName(), "0:0:0:0:0:0:0:0")) {
                        guessExternalUrl = String.format("http://[0000:0000:0000:0000:0000:0000:0000:0001]:%s/", inetSocketAddress.getPort());
                    }
                }
                Thread tcpThread = getThread(inetSocketAddress);
                tcpThread.start();
            }
        } else {
            if (StrUtil.isBlank(guessExternalUrl)) {
                guessExternalUrl = StrUtil.join("/", repoProps().getS3().getEndpoint(), repoProps().getS3().getBucket(), "");
            }
        }
        logger.debug("**********guessExternalUrl={}", guessExternalUrl);
    }

    @NotNull
    private Thread getThread(InetSocketAddress inetSocketAddress) {

        final int port_final = inetSocketAddress.getPort();
        final String host_final = inetSocketAddress.getHostName();
        logger.info("***************host={}, port={}", host_final, port_final);

        Thread tcpThread = new Thread(() -> {
            PathHandler path = new PathHandler();
            ResourceManager resourceManager = FileResourceManager
                    .builder().setBase(new File(repoProps().getLocal().getPath()).toPath())
                    .setETagFunction(path1 -> {
                        long lastModified = path1.toFile().lastModified();
                        long size = path1.toFile().length();
                        return new ETag(false, lastModified + "-" + size);
                    })
                    .build();
            ResourceHandler resourceHandler = new ResourceHandler(resourceManager);

            String textContentTypeUTF8 = "text/plain; charset=utf-8";
            String[] textTypes = new String[]{"txt", "cfg", "md", "log", "conf", "properties", "ini", "sh", "bat", "java", "js", "css", "xml", "json", "yaml", "yml", "sql", "service"};
            MimeMappings.Builder builder = MimeMappings.builder(true);
            for (String textType : textTypes) {
                builder.addMapping(textType, textContentTypeUTF8);
            }
            resourceHandler.setMimeMappings(builder.build());
            resourceHandler.setDirectoryListingEnabled(true);
            path.addPrefixPath("/", resourceHandler);

            String additionalPrefixMapping = repoProps.getLocal().getAdditionalPrefixMapping();

            if (StrUtil.isNotBlank(additionalPrefixMapping)) {
                String[] split = StringUtils.split(additionalPrefixMapping, ",");
                for (String s : split) {
                    String[] split1 = s.split("=");
                    if (split1.length == 2) {
                        String addPrefix = split1[0];
                        String addPath = split1[1];

                        if (StrUtil.isBlank(addPrefix) || StrUtil.equalsIgnoreCase(addPrefix, "/")) {
                            logger.warn("addPrefix is invalid: {}", addPrefix);
                            continue;
                        }
                        ResourceManager resourceManagerAdd = FileResourceManager
                                .builder().setBase(new File(addPath).toPath())
                                .setETagFunction(path2 -> {
                                    long lastModified = path2.toFile().lastModified();
                                    long size = path2.toFile().length();
                                    return new ETag(false, lastModified + "-" + size);
                                })
                                .build();
                        ResourceHandler resourceHandlerAdd = new ResourceHandler(resourceManagerAdd);
                        resourceHandlerAdd.setMimeMappings(builder.build());
                        resourceHandlerAdd.setDirectoryListingEnabled(true);
                        path.addPrefixPath(addPrefix, resourceHandlerAdd);
                    } else {
                        logger.warn("additionalPrefixMapping is invalid: {}", s);
                    }
                }
            }


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
