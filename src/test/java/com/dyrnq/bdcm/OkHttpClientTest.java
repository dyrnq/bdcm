package com.dyrnq.bdcm;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.concurrent.TimeUnit;

public class OkHttpClientTest {
    public static void main(String[] args) throws Exception {

        String[] urls = new String[]{
                "https://mirrors.ustc.edu.cn/debian-cd/current/amd64/iso-dvd/debian-12.10.0-amd64-DVD-1.iso",
                "https://mirrors.ustc.edu.cn/apache/ant/binaries/apache-ant-1.9.16-bin.tar.gz",
                "https://mirrors.ustc.edu.cn/ubuntu-cloud-images/noble/current/noble-server-cloudimg-amd64.img",
                "https://mirrors.huaweicloud.com/ubuntu-cloud-images/noble/20250304/noble-server-cloudimg-arm64.img"
        };

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .connectTimeout(100000, TimeUnit.MILLISECONDS) // 连接超时
                .readTimeout(100000, TimeUnit.MILLISECONDS);

        OkHttpClient client = builder.build();

        for (String url : urls) {

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "curl/7.81.0")
                    .head()
                    .build();

            Response response = client.newCall(request).execute();

            String eTag = response.header("ETag");
            String contentLength = response.header("Content-Length");
            System.out.println("-------------->url: " + url);
            System.out.println("ETag: " + eTag);
            System.out.println("Content Length: " + contentLength);
        }
    }

}

