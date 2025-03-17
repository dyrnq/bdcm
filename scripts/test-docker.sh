#!/usr/bin/env bash
# 删除 旧的 bdcm 容器

name="bdcm"
image="dyrnq/bdcm:latest"

docker rm -f "${name}" >/dev/null 2>&1
docker pull "${image}"


if [ "$(id -u)" = 0 ]; then
  mkdir -p "${HOME}"/bdcm
  chown -R 1000:1000 "${HOME}"/bdcm
else
  mkdir -p "${HOME}"/bdcm
fi





# 启动 新的 bdcm 容器
docker run \
--name "${name}" \
-d \
--restart always \
-v "${HOME}"/bdcm:/app/bdcm \
-e TZ="Asia/Shanghai" \
-e HTTP_PROXY_ENABLE=true \
-e HTTP_PROXY_HOST=192.168.6.111 \
-e HTTP_PROXY_PORT=8118 \
-e HTTP_PROXY_EXCLUDE="127.0.0.1,mirrors.ustc.edu.cn,localhost,192.168.66.0/24" \
-e REPO_EXTERNAL_URL="http://192.168.66.122:15680" \
-e SERVER_SESSION_TIMEOUT=172800 \
-e JWT_SECRET="IDP32XTulsVIUZU+srFEUC9Lhu1wV+nd8iCJPoPA2zSFVAtWhCgpMEymxy5wFAZKMB9yROX31UjDzjwL66r1RA==" \
-p 15580:8080 \
-p 15680:9980 \
-e JAVA_OPTS="-server -Xms512m -Xmx512m -Djava.awt.headless=true -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai -Djava.net.preferIPv4Stack=true" \
"${image}"
