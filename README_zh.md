# BDCM

## 描述

<img src="https://a.dyrnq.com/bdcm/images/logo.png" alt="bdcm" width="300" height="300">

bdcm(binary distribution cache management)是一个二进制分发文件的缓存下载管理系统。灵感来源于<a target="_blank" href="https://github.com/DaoCloud/public-binary-files-mirror">DaoCloud/public-binary-files-mirror</a>。

## 特色

- 支持本地部署
- 支持多任务并发下载
- 支持断点续传
- 保留原始文件的目录结构
- 支持本地存储和s3存储

## 运行

支持环境变量

| 变量名                 | 含义                  | 默认值  |
|---------------------|---------------------|------|
| HTTP_PROXY_ENABLE   | http proxy switch   | true |
| HTTP_PROXY_HOST     | http proxy host     |      |
| HTTP_PROXY_PORT     | http proxy port     |      |
| HTTP_PROXY_EXCLUDE  | http proxy exclude  |      |
| HTTPS_PROXY_ENABLE  | https proxy switch  | true |
| HTTPS_PROXY_HOST    | https proxy host    |      |
| HTTPS_PROXY_PORT    | https proxy port    |      |
| HTTPS_PROXY_EXCLUDE | https proxy exclude |      |
| REPO_EXTERNAL_URL   |  repo external url  |      |

jvm参数

| 参数名                 | 含义            | 默认值                 |
|---------------------|---------------|---------------------|
| -Xms                | 最小堆内存         | 1024m               |
| -Xmx                | 最大堆内存         | 1024m               |
| -Drepo.type         | 存储类型          | local               |
| -Drepo.local.path   | 本地存储路径        |                     |
| -Drepo.local.listen | 本地存储监听地址      | 0.0.0.0:9980,也可以不配置 |
| -Drepo.s3.endpoint  | s3存储endpoint  |                     |
| -Drepo.s3.bucket    | s3存储bucket    |                     |
| -Drepo.s3.accessKey | s3存储accessKey |                     |
| -Drepo.s3.secretKey | s3存储secretKey |                     |



参数描述 (都是可选的）

| 参数                           | 含义                            | 默认值        |
|------------------------------|-------------------------------|------------|
| --server.port                | 端口                            | 8080       |
| --project.home               | 数据目录                          | $HOME/bdcm |
| --spring.database.type       | 可选 h2，mysql，sqlite，postgresql | h2         |
| --spring.datasource.url      | 数据源 url                       |            |
| --spring.datasource.username | 数据源 username                  |            |
| --spring.datasource.password | 数据源 password                  |            |