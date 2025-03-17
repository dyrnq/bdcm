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

Supports environment variables

| Variable Name       | Meaning                 | Default Value |
|---------------------|-------------------------|---------------|
| HTTP_PROXY_ENABLE   | HTTP proxy switch       | true          |
| HTTP_PROXY_HOST     | HTTP proxy host         |               |
| HTTP_PROXY_PORT     | HTTP proxy port         |               |
| HTTP_PROXY_EXCLUDE  | HTTP proxy exclude      |               |
| HTTPS_PROXY_ENABLE  | HTTPS proxy switch      | true          |
| HTTPS_PROXY_HOST    | HTTPS proxy host        |               |
| HTTPS_PROXY_PORT    | HTTPS proxy port        |               |
| HTTPS_PROXY_EXCLUDE | HTTPS proxy exclude     |               |
| REPO_EXTERNAL_URL   | Repository external URL |               |

JVM parameters

| Parameter Name      | Meaning                      | Default Value                   |
|---------------------|------------------------------|---------------------------------|
| -Xms                | Minimum heap memory          | 1024m                           |
| -Xmx                | Maximum heap memory          | 1024m                           |
| -Drepo.type         | Storage type                 | local                           |
| -Drepo.local.path   | Local storage path           |                                 |
| -Drepo.local.listen | Local storage listen address | 0.0.0.0:9980, or not configured |
| -Drepo.s3.endpoint  | S3 storage endpoint          |                                 |
| -Drepo.s3.bucket    | S3 storage bucket            |                                 |
| -Drepo.s3.accessKey | S3 storage access key        |                                 |
| -Drepo.s3.secretKey | S3 storage secret key        |                                 |

Parameter description (all optional)

| Parameter                    | Meaning                                 | Default Value |
|------------------------------|-----------------------------------------|---------------|
| --server.port                | Port                                    | 8080          |
| --project.home               | Data directory                          | $HOME/bdcm    |
| --spring.database.type       | Optional: h2, mysql, sqlite, postgresql | h2            |
| --spring.datasource.url      | Data source URL                         |               |
| --spring.datasource.username | Data source username                    |               |
| --spring.datasource.password | Data source password                    |               |