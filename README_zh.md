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

see [scripts/test-docker.sh](scripts/test-docker.sh)

支持环境变量

| 变量名                        | 含义                            | 默认值                   |
|----------------------------|-------------------------------|-----------------------|
| HTTP_PROXY_ENABLE          | http proxy switch             | true                  |
| HTTP_PROXY_HOST            | http proxy host               |                       |
| HTTP_PROXY_PORT            | http proxy port               |                       |
| HTTP_PROXY_EXCLUDE         | http proxy exclude            |                       |
| HTTPS_PROXY_ENABLE         | https proxy switch            | true                  |
| HTTPS_PROXY_HOST           | https proxy host              |                       |
| HTTPS_PROXY_PORT           | https proxy port              |                       |
| HTTPS_PROXY_EXCLUDE        | https proxy exclude           |                       |
| REPO_EXTERNAL_URL          | repo external url             |                       |
| REPO_TYPE                  | 存储类型                          | local                 |
| REPO_LOCAL_PATH            | 本地存储路径                        | $HOME/bdcm/local_repo |
| REPO_LOCAL_LISTEN          | 本地存储监听地址                      | 0.0.0.0:9980,也可以不配置   |
| REPO_S3_ENDPOINT           | s3存储endpoint                  |                       |
| REPO_S3_BUCKET             | s3存储bucket                    |                       |
| REPO_S3_ACCESSKEY          | s3存储accessKey                 |                       |
| REPO_S3_SECRETKEY          | s3存储secretKey                 |                       |
| SERVER_PORT                | 端口                            | 8080                  |
| PROJECT_HOME               | 数据目录                          | $HOME/bdcm            |
| SPRING_DATASOURCE_TYPE     | 可选 h2，mysql，sqlite，postgresql | h2                    |
| SPRING_DATASOURCE_URL      | 数据源 url                       |                       |
| SPRING_DATASOURCE_USERNAME | 数据源 username                  |                       |
| SPRING_DATASOURCE_PASSWORD | 数据源 password                  |                       |

使用例子

```bash
(
dist_server="http://127.0.0.1:9980"
DIST_URL="https://archive.apache.org/dist/zookeeper/zookeeper-3.7.1/apache-zookeeper-3.7.1-bin.tar.gz"
DIST_URL="${DIST_URL/https:\/\//${dist_server}/}"
echo "${DIST_URL}"
curl -fSL# --remote-name "${DIST_URL}"
ls -l |grep zookeeper
)
```