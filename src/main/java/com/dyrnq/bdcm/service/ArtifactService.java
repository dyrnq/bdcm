package com.dyrnq.bdcm.service;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.dyrnq.bdcm.GrabProps;
import com.dyrnq.bdcm.HomeDir;
import com.dyrnq.bdcm.RepoProps;
import com.dyrnq.bdcm.RepoType;
import com.dyrnq.bdcm.dso.ArtJobLogMapper;
import com.dyrnq.bdcm.dso.ArtJobMapper;
import com.dyrnq.bdcm.dso.ArtifactMapper;
import com.dyrnq.bdcm.model.ArtJob;
import com.dyrnq.bdcm.model.ArtJobLog;
import com.dyrnq.bdcm.model.Artifact;
import com.dyrnq.utils.IDUtils;
import com.dyrnq.utils.ThreadPoolUtils;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
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
    GrabProps grabProps;

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
            ThreadPoolUtils.execute(() -> download(id));
        }
        return "下载任务已提交";
    }

    public String download(Long id) {
        return download(id, null, 0);
    }


    // 单文件下载
    public String download(Long id, Long artJobId, int retryCount) {
        // 根据id获取URL
        Artifact artifact = artifactMapper.selectById(id);
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
            info(jobId, "jobId={}, 开始执行文件 {} 的下载任务...", jobId, artifact.getUrl());
            // 加锁
            artifact.setLock(1);
            artifact.setBeginLock(new Date());
            artifact.setCurrentJobId(jobId);
            artifactMapper.updateById(artifact, false);

            String fileURL = artifact.getUrl();
            String rawUrl = fileURL.split("//")[1];
            String saveFilePath;
            // 根据存储模式选择文件的存储路径
            if (repoProps.getType().equals(RepoType.LOCAL)) {
                saveFilePath = StringUtils.joinWith(File.separator, repoProps().getLocal().getPath(), StrUtil.startWith(rawUrl, "/") ? rawUrl.substring(1) : rawUrl);
            } else {
                saveFilePath = StringUtils.joinWith(File.separator, homeDir.getTmpAbsolutePath(), StrUtil.startWith(rawUrl, "/") ? rawUrl.substring(1) : rawUrl);
            }
            // 设置代理
            Proxy proxy = null;

            if (grabProps.getHttpProxy().isEnable()) {
                if (grabProps.getHttpProxy().getPort() == 0) {
                    logger.warn("port is 0, skip proxy");
                } else {
                    proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(grabProps.getHttpProxy().getHost(), grabProps.getHttpProxy().getPort()));
                }
                String[] excludeHosts = StringUtils.splitByWholeSeparator(grabProps.getHttpProxy().getExclude(), ",");
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

            }

            try {
                downloadFileWithResume(fileURL, saveFilePath, proxy, jobId);
                job.setEndTime(new Date());
                job.setStatus(1);
                job.setProgress("100%");
            } catch (Exception e) {
                error(jobId, e);

                ThreadUtil.safeSleep(grabProps.getRetryInterval());
                retryCount = retryCount + 1;
                if (retryCount < grabProps.getRetry()) {
                    download(id, jobId, retryCount);
                } else {
                    job.setEndTime(new Date());
                    job.setStatus(2);
                }
            }

            // 补足下载结果与完成时间。
            artJobMapper.updateById(job, false);
            // 更新任务信息表
            artifact.setLock(0);
            artifact.setFinalStatus(job.getStatus());
            artifactMapper.updateById(artifact, false);
            return "下载任务执行完毕";
        } else {
            log.info("该任务下载功能暂被占用，请稍候。id={}, url={}", id, artifact.getUrl());
            return "该任务下载功能暂被占用，请稍候。";
        }
    }

    public void downloadFileWithResume(String fileURL, String saveFilePath, Proxy proxy, Long jobId) throws Exception {
        File file = new File(saveFilePath);
        String rawUrl = fileURL.split("//")[1];
        String progressFilePath = StringUtils.joinWith(File.separator, homeDir.getTmpAbsolutePath(), StringUtils.replace(rawUrl, "/", "__") + ".progress"); // 进度文件
        File progressFile = new File(progressFilePath); // 进度文件
        long existingFileSize = 0;

        // 如果文件已存在，检查文件大小是否与远程文件大小一致
        if (file.exists()) {
            existingFileSize = file.length();
            long remoteFileSize = -1;
            try {
                remoteFileSize = getRemoteFileSize(fileURL, proxy);
            } catch (Exception ignore) {

            }
//            if (remoteFileSize == -1) {
//                remoteFileSize = getRemoteFileSize(fileURL, proxy); // 如果失败，尝试使用代理
//            }
            if (remoteFileSize == file.length()) {
                info(jobId, "该文件已存在且完整，无需重新下载,jobId={}, saveFilePath={}", jobId, saveFilePath);
                return; // 文件已存在且完整，直接返回
            } else {
                info(jobId, "文件已存在但不完整，继续下载,jobId={}, saveFilePath={} remoteFileSize={}, existingFileSize={}", jobId, saveFilePath, remoteFileSize, existingFileSize);
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
            info(jobId, "使用代理下载, proxy={}", proxy);
        } else {
            info(jobId, "不使用代理下载");
        }
        // 创建 OkHttpClient
        OkHttpClient client = createOkHttpClient(proxy);

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
                        artJobMapper.updateById(artJob, false);
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
                    uploadObjectS3(saveFilePath, fileURL.split("//")[1]);
                }

                info(jobId, "jobId={}, 文件总大小={}, url={}, 文件下载完成={}, delete progressFile={}", jobId, formatFileSize(fileSize), fileURL, saveFilePath, deleted);

            }
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (Exception ignored) {
            }
            try {
                if (response != null) {
                    if (response.body() != null) {
                        response.body().close();
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 创建 OkHttpClient
     */
    private OkHttpClient createOkHttpClient(Proxy proxy) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .connectTimeout(grabProps.getConnectTimeout(), TimeUnit.MILLISECONDS) // 连接超时
                .readTimeout(grabProps.getReadTimeout(), TimeUnit.MILLISECONDS); // 读取超时

        if (proxy != null) {
            builder.proxy(proxy); // 设置代理
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
        if (httpHeaders != null && httpHeaders.size() > 0) {
            for (GrabProps.KeyVal header : httpHeaders) {
                requestBuilder.addHeader(header.getName(), header.getValue());
            }
        }
        return requestBuilder.build();
    }

    /**
     * 获取远程文件的大小
     */
    private long getRemoteFileSize(String fileURL, Proxy proxy) throws IOException {
        OkHttpClient client = createOkHttpClient(proxy);
        Request request = new Request.Builder()
                .url(fileURL)
                .head() // 使用 HEAD 请求获取文件大小
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (response.isSuccessful()) {

                String contentLength = response.header("Content-Length");
                if (contentLength != null) {
                    return Long.parseLong(contentLength);
                } else {
                    return -1;
                }
            } else {
                throw new IOException("请求失败: " + response.code() + " " + response.message());
            }
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (Exception ignored) {
            }
            try {
                if (response != null) {
                    if (response.body() != null) {
                        response.body().close();
                    }
                }
            } catch (Exception ignored) {
            }
        }
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