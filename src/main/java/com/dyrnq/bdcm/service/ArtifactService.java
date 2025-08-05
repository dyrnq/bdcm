package com.dyrnq.bdcm.service;

import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.dyrnq.bdcm.*;
import com.dyrnq.bdcm.dso.ArtJobLogMapper;
import com.dyrnq.bdcm.dso.ArtJobMapper;
import com.dyrnq.bdcm.dso.ArtifactMapper;
import com.dyrnq.bdcm.model.ArtJob;
import com.dyrnq.bdcm.model.ArtJobLog;
import com.dyrnq.bdcm.model.Artifact;
import com.dyrnq.utils.IDUtils;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.wood.annotation.Db;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ArtifactService {

    static Logger logger = LoggerFactory.getLogger(ArtifactService.class);

    @Inject
    HomeDir homeDir;

    @Db
    ArtifactMapper artifactMapper;

    @Db
    ArtJobMapper artJobMapper;

    @Db
    ArtJobLogMapper artJobLogMapper;

    @Inject
    RepoProps repoProps;

    @Inject
    ThreadPoolUtils threadPoolUtils;

    @Inject
    GrabProps grabProps;

    @Inject
    HttpProxy httpProxy;

    @Inject
    HttpsProxy httpsProxy;

    private HttpProxy getHttpProxy() {
        return this.httpProxy;
    }

    private RepoProps repoProps() {
        return repoProps;
    }

    // 下载接口
    public String download(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            log.info("未传入任何ID");
            return "未传入任何ID";
        }
        // 检查yaml路径下是否有预留文件夹.没有则创建
        // 循环调用线程池进行下载任务。
        for (Long id : ids) {
            threadPoolUtils.execute(() -> download(id));
        }
        return "下载任务已提交";
    }

    public String download(Long id) {
        return download(id, null, 0);
    }


    protected Tuple2<Proxy, okhttp3.Authenticator> buildProxy(String fileURL) {
        Proxy proxy = null;
        okhttp3.Authenticator proxyAuthenticator = null;
        if (getHttpProxy().isEnable()) {
            if (getHttpProxy().getPort() == 0) {
                logger.warn("port is 0, skip proxy");
            } else {
                Proxy.Type type = Proxy.Type.HTTP;
                if (StrUtil.isNotBlank(getHttpProxy().getType())) {
                    try {
                        type = Proxy.Type.valueOf(getHttpProxy().getType().toUpperCase());
                    } catch (Exception ignore) {
                    }
                }
                proxy = new Proxy(type, new InetSocketAddress(getHttpProxy().getHost(), getHttpProxy().getPort()));
            }
            String[] excludeHosts = StringUtils.splitByWholeSeparator(getHttpProxy().getExclude(), ",");
            for (String excludeHost : excludeHosts) {
                String host = "";
                try {
                    URL url = new URL(fileURL);
                    host = url.getHost();
                    if (host.contains(excludeHost)) {
                        proxy = null;
                        break;
                    }
                } catch (Exception e) {
                    proxy = null;
                    break;
                }

            }
            String username = getHttpProxy().getUsername();
            String password = getHttpProxy().getPassword();

            if (StrUtil.isNotBlank(username)) {
                proxyAuthenticator = (route, response) -> {
                    String credential = Credentials.basic(username, password);
                    return response.request().newBuilder()
                            .header("Proxy-Authorization", credential)
                            .build();
                };
            }

        }
        return Tuple.tuple(proxy, proxyAuthenticator);
    }

    protected void unlockArtifact(Long id, Tuple2<Long, String> tuple, int status) {
        Artifact artifact = new Artifact();
        artifact.setId(id);
        artifact.setLock(0);
        artifact.setFinalStatus(status);
        if (tuple != null) {
            artifact.setFileSize(tuple.v1);
            artifact.setEtag(tuple.v2);
        }
        artifactMapper.updateById(artifact, true);
    }

    private String getPathFromURL(String fileURL) {
        String fileURLRemoved = null;
        if (Strings.CS.contains(fileURL, "//")) {
            fileURLRemoved = fileURL.split("//")[1];
        } else {
            fileURLRemoved = fileURL;
        }
        return URLDecoder.decode(fileURLRemoved, Charset.defaultCharset());
    }

    // 单文件下载
    public String download(Long id, Long artJobId, int retryCount) {
        // 根据id获取URL
        Artifact artifact = artifactMapper.selectById(id);
        if (artifact == null) {
            log.info("未找到ID为{}的文件", id);
            return "未找到ID为{}的文件";
        }
        if (StringUtils.isEmpty(artifact.getUrl())) {
            log.info("ID为{}的文件未配置URL", id);
            return "ID为{}的文件未配置URL";
        }
        if (artifact.getFinalStatus() != null && artifact.getFinalStatus() == 1) {
            log.info("ID为{}的文件已下载完成", id);
            return "ID为{}的文件已下载完成";
        }
        // 查看任务锁状态，如果是下载中，驳回下载请求
        if (artJobId != null || (artifact.getLock() == null || artifact.getLock() == 0)) {
            // 通过URL执行下载任务。
            Long jobId = null;

            ArtJob job = null;
            if (artJobId == null) {
                // 提交部分下载记录
                jobId = IDUtils.getLongID();
                job = new ArtJob();
                job.setId(jobId);
                job.setStatus(0);
                job.setBeginTime(new Date());
                job.setArtId(artifact.getId());
                job.setUserId("1");
                artJobMapper.insert(job, false);
            } else {
                job = new ArtJob();
                jobId = artJobId;
                job.setId(jobId);
            }
            info(jobId, "jobId={}, 开始执行文件 {} 的下载任务, retry次数 {}", jobId, artifact.getUrl(), retryCount);
            // 加锁
            artifact.setLock(1);
            artifact.setBeginLock(new Date());
            artifact.setCurrentJobId(jobId);
            artifactMapper.updateById(artifact, true);

            String fileURL = artifact.getUrl();
            String rawUrl = getPathFromURL(fileURL);
            String saveFilePath;
            // 根据存储模式选择文件的存储路径
            if (repoProps.getType().equals(RepoType.LOCAL)) {
                saveFilePath = StringUtils.joinWith(File.separator, repoProps().getLocal().getPath(), StrUtil.startWith(rawUrl, "/") ? rawUrl.substring(1) : rawUrl);
            } else {
                saveFilePath = StringUtils.joinWith(File.separator, homeDir.getTmpAbsolutePath(), StrUtil.startWith(rawUrl, "/") ? rawUrl.substring(1) : rawUrl);
            }
            // 设置代理

            Tuple2<Long, String> tuple = null;
            Tuple2<Proxy, okhttp3.Authenticator> proxyTuple = buildProxy(fileURL);
            Proxy proxy = proxyTuple.v1;
            okhttp3.Authenticator proxyAuthenticator = proxyTuple.v2;
            try {
                tuple = downloadFileWithResume(fileURL, saveFilePath, proxy, proxyAuthenticator, jobId, artifact.getEtag());
                job.setEndTime(new Date());
                job.setStatus(1);
                job.setProgress("100%");
                artJobMapper.updateById(job, true);
                unlockArtifact(id, tuple, job.getStatus());
            } catch (Exception e) {
                error(jobId, e);

                ThreadUtil.safeSleep(grabProps.getRetryInterval());
                retryCount = retryCount + 1;
                if (retryCount < grabProps.getRetry()) {
                    download(id, jobId, retryCount);
                } else {
                    job.setStatus(2);
                    artJobMapper.updateById(job, true);
                    unlockArtifact(id, tuple, job.getStatus());
                }
            }
            return "下载任务执行完毕";
        } else {
            log.info("该任务下载功能暂被占用，请稍候。id={}, url={}", id, artifact.getUrl());
            return "该任务下载功能暂被占用，请稍候。";
        }
    }


    public Tuple2<Long, String> downloadFileWithResume(String fileURL, String saveFilePath, Proxy proxy, okhttp3.Authenticator proxyAuthenticator, Long jobId, String eTagPersistence) throws Exception {
        File file = new File(saveFilePath);
        String rawUrl = getPathFromURL(fileURL);

        long existingFileSize = 0;

        long remoteFileSize = -1;
        String eTag = "";
        Tuple2<Long, String> tuple = null;

        try {
            tuple = getRemoteFileSize(fileURL, proxy, proxyAuthenticator);
        } catch (Exception ignore) {

        }
        if (tuple != null) {
            remoteFileSize = tuple.v1;
            eTag = tuple.v2;
        }

        String progressFilePath = StringUtils.joinWith(File.separator, homeDir.getTmpAbsolutePath(), StringUtils.replace(rawUrl, "/", "__") + ".progress"); // 进度文件
        File progressFile = new File(progressFilePath); // 进度文件

        // 如果文件已存在，检查文件大小是否与远程文件大小一致
        if (file.exists()) {
            existingFileSize = file.length();

            if (remoteFileSize == file.length()) {
                info(jobId, "该文件已存在且完整，无需重新下载, jobId={}, saveFilePath={}, eTag={}", jobId, saveFilePath, eTag);
                return tuple; // 文件已存在且完整，直接返回
            } else {
                info(jobId, "文件已存在但不完整，继续下载, jobId={}, saveFilePath={} remoteFileSize={}, existingFileSize={}, eTag={}", jobId, saveFilePath, remoteFileSize, existingFileSize, eTag);
            }
            if(StringUtils.isNoneBlank(eTagPersistence)) {
                if (!Strings.CI.equals(eTag, eTagPersistence)) {
                    existingFileSize = 0;
                    info(jobId, "对比eTag不等，开启强制下载, jobId={}, saveFilePath={} remoteFileSize={}, existingFileSize={}, eTag={}，eTagPersistence={}", jobId, saveFilePath, remoteFileSize, existingFileSize, eTag, eTagPersistence);
                }
            }
        }

        // 如果进度文件存在，读取已下载的字节数
//        if (progressFile.exists()) {
//            try (FileInputStream fis = new FileInputStream(progressFile)) {
//                byte[] bytes = new byte[Long.BYTES];
//                fis.read(bytes);
//                existingFileSize = bytesToLong(bytes);
//                logger.info("文件已存在但不完整，继续下载, id={}, saveFilePath={}, 从{}开始下载", id, saveFilePath, formatFileSize(existingFileSize));
//            }
//        }

        // 检查并创建父目录
        File parentDir = file.getParentFile();

        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        if (proxy != null) {
            info(jobId, "jobId={}, 使用代理下载, proxy={}", jobId, proxy);
        } else {
            info(jobId, "jobId={}, 不使用代理下载", jobId);
        }
        // 创建 OkHttpClient
        OkHttpClient client = createOkHttpClient(proxy, proxyAuthenticator);

        // 创建请求
        Request request = createRequest(fileURL, existingFileSize);

        // 发送请求并获取响应
        Response response = null;
        try {
            response = client.newCall(request).execute();

            // 处理 416 错误
            if (response.code() == 416) {
                info(jobId, "服务器返回 416 错误，从头开始下载");
                existingFileSize = 0;
                request = createRequest(fileURL, existingFileSize); // 重新创建请求
                response = client.newCall(request).execute(); // 重新发送请求
            }

            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response.code() + " " + response.message());
            }

            // 获取文件总大小
            long fileSize = existingFileSize + response.body().contentLength();
            info(jobId, "jobId={}, 文件总大小={}, url={}", jobId, formatFileSize(fileSize), fileURL);

            try (InputStream inputStream = response.body().byteStream();
                 RandomAccessFile outputFile = new RandomAccessFile(file, "rw")) {

                outputFile.seek(existingFileSize);

                byte[] buffer = new byte[1024];
                int bytesRead;
                long totalBytesRead = existingFileSize;
                long time = System.currentTimeMillis();

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputFile.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;

                    // 更新进度文件
                    updateProgressFile(progressFile, totalBytesRead);

                    // 计算并显示下载进度
                    double progress = (double) totalBytesRead / fileSize * 100;
                    long now = System.currentTimeMillis();
                    if (now - time > 1000) {
                        time = now;
                        ArtJob artJob = new ArtJob();
                        String progressStr = String.format("%.2f", progress);
                        artJob.setProgress(progressStr + "%");
                        artJob.setId(jobId);
                        artJobMapper.updateById(artJob, true);
                    }
                }
                boolean deleted = false;
                // 下载完成后删除进度文件
                if (progressFile.exists()) {
                    try {
                        deleted = progressFile.delete();
                    } catch (Exception ignored) {

                    }
                }

                if (repoProps.getType().equals(RepoType.S3)) {
                    // 上传到S3
                    uploadObjectS3(saveFilePath, rawUrl);
                }

                info(jobId, "jobId={}, 文件总大小={}, url={}, 文件下载完成={}, delete progressFile={}", jobId, formatFileSize(fileSize), fileURL, saveFilePath, deleted);

            }
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response);
                IOUtils.closeQuietly(response.body());
            }
        }
        return tuple;
    }

    private OkHttpClient createOkHttpClient() {
        return createOkHttpClient(null, null);
    }

    private OkHttpClient createOkHttpClient(Proxy proxy) {
        return createOkHttpClient(proxy, null);
    }
    /**
     * 创建 OkHttpClient
     */
    private OkHttpClient createOkHttpClient(Proxy proxy, okhttp3.Authenticator proxyAuthenticator) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .followRedirects(true)
//                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .followSslRedirects(true)
                .connectTimeout(grabProps.getConnectTimeout(), TimeUnit.MILLISECONDS) // 连接超时
                .readTimeout(grabProps.getReadTimeout(), TimeUnit.MILLISECONDS); // 读取超时

        if (proxy != null) {
            builder.proxy(proxy); // 设置代理
            if (proxyAuthenticator != null) {
                builder.proxyAuthenticator(proxyAuthenticator);
            }
        }


        return builder.build();
    }

    /**
     * 创建请求
     */
    private Request createRequest(String fileURL, long existingFileSize) {

        List<GrabProps.KeyVal> httpHeaders = grabProps.getHttpHeaders();

        Request.Builder requestBuilder = new Request.Builder().url(fileURL);
        requestBuilder.addHeader("Range", "bytes=" + existingFileSize + "-");
        if (httpHeaders != null && !httpHeaders.isEmpty()) {
            for (GrabProps.KeyVal header : httpHeaders) {
                requestBuilder.addHeader(header.getName(), header.getValue());
            }
        }
        return requestBuilder.build();
    }

    /**
     * 获取远程文件的大小
     */
    private Tuple2<Long, String> getRemoteFileSize(String fileURL, Proxy proxy, okhttp3.Authenticator proxyAuthenticator) throws IOException {
        Tuple2<Long, String> tuple2 = null;
        long contentLength = -1;
        String eTag = "";

        OkHttpClient client = createOkHttpClient(proxy, proxyAuthenticator);
        Request request = new Request.Builder()
                .url(fileURL)
                .addHeader("User-Agent", "curl/7.88.1")
                .head() // 使用 HEAD 请求获取文件大小
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (response.isSuccessful()) {


                try {
                    contentLength = Long.parseLong(Objects.requireNonNull(response.header("Content-Length")));
                } catch (Exception ignored) {

                }
                try {
//
//                    ETag机制同时支持强校验和弱校验。它们通过ETag标识符的开头是否存在“W/”来区分，如：
//
//                    "123456789"   -- 一个强ETag验证符
//                    W/"123456789"  -- 一个弱ETag验证符
//                    强校验的ETag匹配要求两个资源内容的每个字节需完全相同，包括所有其他实体字段（如Content-Language）不发生变化。强ETag允许重新装配和缓存部分响应，以及字节范围请求。弱校验的ETag匹配要求两个资源在语义上相等，这意味着在实际情况下它们可以互换，而且缓存副本也可以使用。不过这些资源不需要每个字节相同，因此弱ETag不适合字节范围请求。当Web服务器无法生成强ETag的时候，比如动态生成的内容，弱ETag就可能发挥作用了。

                    eTag = Objects.requireNonNull(response.header("ETag"));
                    if (StringUtils.startsWith(eTag, "W/")) {
                        eTag = StringUtils.substring(eTag, 2);
                    }
                    eTag = StringUtils.replace(eTag, "\"", "");
                } catch (Exception ignored) {

                }
                tuple2 = Tuple.tuple(contentLength, eTag);

            } else {
                throw new IOException("请求失败: " + response.code() + " " + response.message());
            }
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response);
                IOUtils.closeQuietly(response.body());
            }
        }
        return tuple2;
    }

    /**
     * 更新进度文件
     */
    private void updateProgressFile(File progressFile, long totalBytesRead) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(progressFile)) {
            fos.write(longToBytes(totalBytesRead));
        }
    }

    /**
     * 将 long 转换为字节数组
     */
    private byte[] longToBytes(long value) {
        byte[] bytes = new byte[Long.BYTES];
        for (int i = 0; i < Long.BYTES; i++) {
            bytes[i] = (byte) (value >> (8 * i));
        }
        return bytes;
    }

    /**
     * 将字节数组转换为 long
     */
    private long bytesToLong(byte[] bytes) {
        long value = 0;
        for (int i = 0; i < Long.BYTES; i++) {
            value |= ((long) bytes[i] & 0xFF) << (8 * i);
        }
        return value;
    }

    /**
     * 格式化文件大小（将字节转换为更易读的单位，如 KB、MB、GB）
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }

    private void uploadObjectS3(String localFilePath, String object) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String accessKey = repoProps.getS3().getAccessKey();
        String secretKey = repoProps.getS3().getSecretKey();
        String bucket = repoProps.getS3().getBucket();
        MinioClient minioClient = MinioClient.builder()
                .endpoint(repoProps.getS3().getEndpoint())
                .credentials(accessKey, secretKey)
                .build();
        minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket(bucket)
                        .object(object)
                        .filename(localFilePath)
                        .build());
    }


    /**
     * 下载log记录
     */

    private void error(Long jobId, Exception e) {
        ArtJobLog artJobLog = new ArtJobLog();
        artJobLog.setId(IDUtils.getLongID());
        artJobLog.setLog(e.getMessage());
        artJobLog.setArtJobId(jobId);
        logger.error(e.getMessage(), e);
        artJobLogMapper.insert(artJobLog, false);
    }

    private void info(Long jobId, String s, Object... objects) {
        ArtJobLog artJobLog = new ArtJobLog();
        artJobLog.setId(IDUtils.getLongID());
        artJobLog.setLog(MessageFormatter.arrayFormat(s, objects).getMessage());
        artJobLog.setArtJobId(jobId);
        logger.info(s, objects);
        artJobLogMapper.insert(artJobLog, false);
    }
}