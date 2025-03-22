# BDCM

## 描述

<img src="https://a.dyrnq.com/bdcm/images/logo.png" alt="bdcm" width="300" height="300"><br/>

bdcm(binary distribution cache management)是一个二进制分发文件的缓存下载管理系统。灵感来源于<a target="_blank" href="https://github.com/DaoCloud/public-binary-files-mirror">DaoCloud/public-binary-files-mirror</a>。

## 特色

- 支持本地部署
- 支持多任务并发下载
- 支持断点续传
- 保留原始文件的目录结构
- 支持本地存储和s3存储

## 运行

see [scripts/test-docker.sh](scripts/test-docker.sh) and [dyrnq/bdcm:latest](https://hub.docker.com/r/dyrnq/bdcm/tags)

支持环境变量

| 变量名                                  | 含义                                                                | 默认值                     |
|--------------------------------------|-------------------------------------------------------------------|-------------------------|
| HTTP_PROXY_ENABLE                    | HTTP proxy switch                                                 | false                   |
| HTTP_PROXY_TYPE                      | HTTP proxy type                                                   | HTTP                    |
| HTTP_PROXY_HOST                      | HTTP proxy host                                                   |                         |
| HTTP_PROXY_PORT                      | HTTP proxy port                                                   |                         |
| HTTP_PROXY_USERNAME                  | HTTP proxy username                                               |                         |
| HTTP_PROXY_PASSWORD                  | HTTP proxy password                                               |                         |
| HTTP_PROXY_EXCLUDE                   | HTTP proxy exclude                                                |                         |
| HTTPS_PROXY_ENABLE                   | HTTPS proxy switch                                                | false                   |
| HTTPS_PROXY_TYPE                     | HTTPS proxy type                                                  | HTTP                    |
| HTTPS_PROXY_HOST                     | HTTPS proxy host                                                  |                         |
| HTTPS_PROXY_PORT                     | HTTPS proxy port                                                  |                         |
| HTTPS_PROXY_USERNAME                 | HTTPS proxy username                                              |                         |
| HTTPS_PROXY_PASSWORD                 | HTTPS proxy password                                              |                         |
| HTTPS_PROXY_EXCLUDE                  | HTTPS proxy exclude                                               |                         |
| REPO_EXTERNAL_URL                    | repo external url                                                 |                         |
| REPO_TYPE                            | 存储类型                                                              | local                   |
| REPO_LOCAL_PATH                      | 本地存储路径                                                            | $HOME/bdcm/local_repo   |
| REPO_LOCAL_LISTEN                    | 本地存储监听地址                                                          | 0.0.0.0:9980 (optional) |
| REPO_S3_ENDPOINT                     | s3存储endpoint                                                      |                         |
| REPO_S3_BUCKET                       | s3存储bucket                                                        |                         |
| REPO_S3_ACCESSKEY                    | s3存储accessKey                                                     |                         |
| REPO_S3_SECRETKEY                    | s3存储secretKey                                                     |                         |
| SERVER_PORT                          | 端口                                                                | 8080                    |
| PROJECT_HOME                         | 数据目录                                                              | $HOME/bdcm              |
| SPRING_DATABASE_TYPE                 | 可选 h2，mysql，sqlite，postgresql                                     | h2                      |
| SPRING_DATASOURCE_URL                | 数据源 url                                                           |                         |
| SPRING_DATASOURCE_USERNAME           | 数据源 username                                                      |                         |
| SPRING_DATASOURCE_PASSWORD           | 数据源 password                                                      |                         |
| JWT_SECRET                           | jwt secret                                                        |                         |
| REPO_LOCAL_ADDITIONAL_PREFIX_MAPPING | 本地存储额外的前缀映射，格式为:前缀=本地存储路径,多个用逗号分隔，例如: /foo=/foo/bar,/bar=/bar/foo |                         |

> 重要:

使用以下命令获取并替换默认的jwt secret:

```bash
docker run -it --rm --entrypoint="" dyrnq/bdcm:latest bash -c "java -cp /app/bdcm.jar cli jwt"
```


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

<!-- <img src="https://a.dyrnq.com/bdcm/images/Screenshot%202025-03-18%20at%2009-57-53 bdcm.png" alt="bdcm" width="589" height="310"> -->
<img src="https://a.dyrnq.com/bdcm/images/Screenshot%202025-03-18%20at%2009-58-13 bdcm.png" alt="bdcm" width="589" height="310">
<img src="https://a.dyrnq.com/bdcm/images/Screenshot%202025-03-18%20at%2009-58-02 bdcm.png" alt="bdcm" width="589" height="310">
