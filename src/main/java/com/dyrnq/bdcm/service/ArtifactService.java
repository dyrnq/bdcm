package com.dyrnq.bdcm.service;

import com.dyrnq.bdcm.HomeDir;
import com.dyrnq.bdcm.dso.ArtJobMapper;
import com.dyrnq.bdcm.dso.ArtifactMapper;
import com.dyrnq.bdcm.model.ArtJob;
import com.dyrnq.bdcm.model.Artifact;
import com.dyrnq.utils.IDUtils;
import com.dyrnq.utils.ThreadPoolUtils;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.wood.annotation.Db;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

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

    @Inject("${repo.local.path}")

    String folder;

    //下载接口
    public String download(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            log.info("未传入任何ID");
            return "未传入任何ID";
        }
        //检查yaml路径下是否有预留文件夹.没有则创建
        String folderName = folder;
        Path path = Paths.get(folderName);
        if (!Files.exists(path)) {
            try {
                // 如果不存在，创建文件夹
                Files.createDirectories(path);
                log.info("文件夹创建成功: {}", path);
            } catch (Exception e) {
                log.error("创建文件夹时出错: {}", e.getMessage());
            }
        } else {
            log.info("文件夹已存在:{}", path);
        }
        //循环调用线程池进行下载任务。
        for (Long id : ids) {
            ThreadPoolUtils.execute(() -> {
                downLoad(id);
            });
        }

        return "下载任务已提交";
    }

    //单文件下载
    private String downLoad(Long id) {
        //根据id获取URL
        Artifact artifact = artifactMapper.selectById(id);
        //查看任务锁状态，如果是下载中，驳回下载请求
        if (artifact.getLock() == null || artifact.getLock() == 0) {
            //通过URL执行下载任务。
            log.info("开始执行文件 {} 的下载任务...", artifact.getName());
            //加锁
            artifact.setLock(1);
            artifact.setBeginLock(new Date());
            artifactMapper.updateById(artifact, false);
            //提交部分下载记录
            ArtJob job = new ArtJob();
            Long jobId = IDUtils.getLongID();
            job.setId(jobId);
            job.setStatus(0);
            job.setBeginTime(new Date());
            job.setArtId(artifact.getId());
            job.setUserId("1");
            artJobMapper.insert(job, false);
            String fileURL = artifact.getUrl();
            String rawUrl = fileURL.split("//")[1];
            String saveFilePath = folder + "/" + rawUrl;
            // 设置代理
            Proxy proxy = null;

            try {
                downloadFileWithResume(fileURL, saveFilePath, proxy);
                job.setEndTime(new Date());
                job.setStatus(1);
            } catch (IOException e) {
                log.error(e.getMessage());
                job.setEndTime(new Date());
                job.setStatus(2);
            }
            //补足下载结果与完成时间。
            artJobMapper.updateById(job, false);
            //更新任务信息表
            artifact.setLock(0);
            artifact.setCurrentJobId(job.getId());
            artifact.setFinalStatus(job.getStatus());
            artifactMapper.updateById(artifact, false);
            return "下载任务执行完毕";
        } else {
            log.info("该任务下载功能暂被占用，请稍候。");
            return "该任务下载功能暂被占用，请稍候。";
        }
    }

    public void downloadFileWithResume(String fileURL, String saveFilePath, Proxy proxy) throws IOException {
        File file = new File(saveFilePath);
        File progressFile = new File(homeDir.getTmpAbsolutePath() + ".progress"); // 进度文件
        long existingFileSize = 0;

        // 如果文件已存在，检查文件大小是否与远程文件大小一致
        if (file.exists()) {
            long remoteFileSize = getRemoteFileSize(fileURL, null); // 先尝试不使用代理
            if (remoteFileSize == -1) {
                remoteFileSize = getRemoteFileSize(fileURL, proxy); // 如果失败，尝试使用代理
            }
            if (remoteFileSize == file.length()) {
                logger.info("该文件已存在且完整，无需重新下载");
                return; // 文件已存在且完整，直接返回
            } else {
                logger.info("文件已存在但不完整，继续下载");
                existingFileSize = file.length();
            }
        }

        // 如果进度文件存在，读取已下载的字节数
        if (progressFile.exists()) {
            try (FileInputStream fis = new FileInputStream(progressFile)) {
                byte[] bytes = new byte[Long.BYTES];
                fis.read(bytes);
                existingFileSize = bytesToLong(bytes);
                logger.info("检测到未完成的下载，继续从 " + formatFileSize(existingFileSize) + " 开始下载");
            }
        }

        // 检查并创建父目录
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        // 测试连接，决定是否使用代理
        HttpURLConnection connection = null;
        boolean useProxy = false;

        // 先尝试不使用代理
        try {
            connection = createConnection(fileURL, null, existingFileSize);
            connection.setConnectTimeout(5000); // 设置连接超时时间为 5 秒
            connection.setReadTimeout(5000); // 设置读取超时时间为 5 秒
            connection.connect();
            logger.info("直接连接成功，无需使用代理");
        } catch (IOException e) {
            logger.info("直接连接失败，尝试使用代理...");
            useProxy = true;
        }

        // 如果直接连接失败，使用代理

        connection = createConnection(fileURL, proxy, existingFileSize);


        // 获取文件总大小
        long fileSize = connection.getContentLengthLong() + existingFileSize;
        logger.info("文件总大小: " + formatFileSize(fileSize));

        // 检查服务器是否支持断点续传
        if (connection.getResponseCode() != HttpURLConnection.HTTP_PARTIAL) {
            logger.info("服务器不支持断点续传，从头开始下载");
            existingFileSize = 0;
            connection.disconnect();
            connection = createConnection(fileURL, useProxy ? proxy : null, existingFileSize);
            fileSize = connection.getContentLengthLong(); // 重新获取文件大小
        }

        try (InputStream inputStream = connection.getInputStream();
             RandomAccessFile outputFile = new RandomAccessFile(file, "rw")) {

            outputFile.seek(existingFileSize);

            byte[] buffer = new byte[1024];
            int bytesRead;
            long totalBytesRead = existingFileSize;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputFile.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;

                // 更新进度文件
                updateProgressFile(progressFile, totalBytesRead);

                // 计算并显示下载进度
                double progress = (double) totalBytesRead / fileSize * 100;
                logger.info("下载进度: %.2f%% (%s/%s)%n",
                        progress,
                        formatFileSize(totalBytesRead),
                        formatFileSize(fileSize));
            }

            System.out.println("文件下载完成: " + saveFilePath);

            // 下载完成后删除进度文件
            if (progressFile.exists()) {
                progressFile.delete();
                System.out.println("进度文件已删除");
            }
        } finally {
            connection.disconnect();
        }
    }

    /**
     * 创建 HttpURLConnection 连接
     */
    private HttpURLConnection createConnection(String fileURL, Proxy proxy, long existingFileSize) throws IOException {
        URL url = new URL(fileURL);
        HttpURLConnection connection;
        if (proxy != null) {
            connection = (HttpURLConnection) url.openConnection(proxy);
        } else {
            connection = (HttpURLConnection) url.openConnection();
        }
        connection.setRequestProperty("Range", "bytes=" + existingFileSize + "-");
        return connection;
    }

    /**
     * 获取远程文件的大小
     */
    private long getRemoteFileSize(String fileURL, Proxy proxy) throws IOException {
        URL url = new URL(fileURL);
        HttpURLConnection connection;
        if (proxy != null) {
            connection = (HttpURLConnection) url.openConnection(proxy);
        } else {
            connection = (HttpURLConnection) url.openConnection();
        }
        connection.setRequestMethod("HEAD"); // 使用 HEAD 请求获取文件大小
        connection.setConnectTimeout(5000); // 设置连接超时时间为 5 秒
        connection.setReadTimeout(5000); // 设置读取超时时间为 5 秒
        long fileSize = connection.getContentLengthLong();
        connection.disconnect();
        return fileSize;
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

}
