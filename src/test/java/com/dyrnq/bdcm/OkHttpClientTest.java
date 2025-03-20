package com.dyrnq.bdcm;

import cn.hutool.core.util.StrUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.io.IOUtils;

import java.util.concurrent.TimeUnit;

public class OkHttpClientTest {
    public static void main(String[] args) throws Exception {

        String[] urls = new String[]{
                "https://mirrors.ustc.edu.cn/debian-cd/current/amd64/iso-dvd/debian-12.10.0-amd64-DVD-1.iso",
                "https://mirrors.ustc.edu.cn/apache/ant/binaries/apache-ant-1.9.16-bin.tar.gz",
                "https://mirrors.ustc.edu.cn/ubuntu-cloud-images/noble/current/noble-server-cloudimg-amd64.img",
                "https://mirrors.huaweicloud.com/ubuntu-cloud-images/noble/20250304/noble-server-cloudimg-arm64.img",
                "https://dl.min.io/client/mc/release/linux-amd64/mc",
                "https://gitee.com/layui/layui/releases/download/v2.10.0/layui-v2.10.0.zip",
        };

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .connectTimeout(100000, TimeUnit.MILLISECONDS) // 连接超时
                .readTimeout(100000, TimeUnit.MILLISECONDS);

        OkHttpClient client = builder.build();
        String userAgent = "curl/7.81.0";
        for (String url : urls) {

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", userAgent)
                    .head()
                    .build();

            Response response = null;
            ResponseBody responseBody = null;

            try {
                response = client.newCall(request).execute();
                responseBody = response.body();
                if (!response.isSuccessful() || StrUtil.isBlank(response.header("Content-Length"))) {
                    System.out.println(response.code());
                    request = new Request.Builder()
                            .url(url)
                            .addHeader("User-Agent", userAgent)
                            .get()
                            .build();

                    response = client.newCall(request).execute();
                    responseBody = response.body();

                }
                String eTag = response.header("ETag");
                String contentLength = response.header("Content-Length");
                System.out.printf("-------------->url: %s %s %n", url, response.code());
                System.out.println("ETag: " + eTag);
                System.out.println("Content Length: " + contentLength);
            } finally {
                if (response != null) {
                    IOUtils.closeQuietly(response);
                    IOUtils.closeQuietly(responseBody);
                }
            }
        }
    }

}

