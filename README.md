# BDCM

## Description

<img src="https://a.dyrnq.com/bdcm/images/logo.png" alt="bdcm" width="300" height="300">

BDCM (Binary Distribution Cache Management) is a binary distribution file cache download management system. It was inspired by <a target="_blank" href="https://github.com/DaoCloud/public-binary-files-mirror">DaoCloud/public-binary-files-mirror</a>.

## Features

* Supports local deployment
* Supports multiple task concurrent downloads
* Supports breakpoint resume
* Preserves the original file's directory structure
* Supports local storage and S3 storage

## Running

see [scripts/test-docker.sh](scripts/test-docker.sh)

Supports environment variables

| Variable Name                | Meaning                            | Default Value           |
|-----------------------------|-----------------------------------|-------------------------|
| HTTP_PROXY_ENABLE           | HTTP proxy switch                 | false                    |
| HTTP_PROXY_HOST             | HTTP proxy host                   |                         |
| HTTP_PROXY_PORT             | HTTP proxy port                   |                         |
| HTTP_PROXY_EXCLUDE          | HTTP proxy exclude                |                         |
| HTTPS_PROXY_ENABLE          | HTTPS proxy switch                | false                    |
| HTTPS_PROXY_HOST            | HTTPS proxy host                  |                         |
| HTTPS_PROXY_PORT            | HTTPS proxy port                  |                         |
| HTTPS_PROXY_EXCLUDE         | HTTPS proxy exclude               |                         |
| REPO_EXTERNAL_URL           | Repository external URL          |                         |
| REPO_TYPE                   | Storage type                      | local                  |
| REPO_LOCAL_PATH             | Local storage path                | $HOME/bdcm/local_repo  |
| REPO_LOCAL_LISTEN           | Local storage listen address     | 0.0.0.0:9980 (optional) |
| REPO_S3_ENDPOINT            | S3 storage endpoint               |                         |
| REPO_S3_BUCKET              | S3 storage bucket                 |                         |
| REPO_S3_ACCESSKEY           | S3 storage access key            |                         |
| REPO_S3_SECRETKEY           | S3 storage secret key            |                         |
| SERVER_PORT                 | Server port                       | 8080                   |
| PROJECT_HOME                | Data directory                    | $HOME/bdcm             |
| SPRING_DATASOURCE_TYPE      | Database type (h2, mysql, sqlite, postgresql) | h2                    |
| SPRING_DATASOURCE_URL       | Database URL                      |                         |
| SPRING_DATASOURCE_USERNAME  | Database username                 |                         |
| SPRING_DATASOURCE_PASSWORD  | Database password                 |                         |

usage:

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