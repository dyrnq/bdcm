#!/usr/bin/env bash
# shellcheck disable=SC2086


iface="${iface:-enp0s8}"

wait4x_image="${wait4x_image:-atkrad/wait4x:2.14}"
mysql5_image="${mysql5_image:-mysql:5.7.41}"
mysql8_image="${mysql8_image:-mysql:8.0.23}"
pg_image="${pg_image:-postgres:12.14}"
adminer_image="${adminer_image:-adminer:4.8.1}"
minio_image="${minio_image:-minio/minio:RELEASE.2025-02-28T09-55-16Z}"
nginx_image="${nginx_image:-nginx:1.26.3}"




while [ $# -gt 0 ]; do
    case "$1" in
        --iface|-i)
            iface="$2"
            shift
            ;;
        --*)
            echo "Illegal option $1"
            ;;
    esac
    shift $(( $# > 0 ? 1 : 0 ))
done

ip4=$(/sbin/ip -o -4 addr list "${iface}" | awk '{print $4}' |cut -d/ -f1 | head -n1);


command_exists() {
    command -v "$@" > /dev/null 2>&1
}



fun_add_mynet(){

docker network inspect mynet &>/dev/null || docker network create --subnet 172.18.0.0/16 --gateway 172.18.0.1 --driver bridge mynet

}



fun_install_misc(){
docker rm -f mysql57 2>/dev/null || true
docker rm -f mysql8 2>/dev/null || true
docker rm -f postgres12 2>/dev/null || true
docker rm -f adminer 2>/dev/null || true
docker rm -f minio 2>/dev/null || true

mkdir -p $HOME/var/lib/mysql
docker run -d --name mysql57 \
--restart always \
--network mynet \
-e MYSQL_ROOT_PASSWORD=666666 \
-v $HOME/var/lib/mysql:/var/lib/mysql \
-p 3306:3306 \
${mysql5_image} --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci --default-time-zone=+8:00

mkdir -p $HOME/var/lib/mysql8
docker run -d --name mysql8 \
--restart always \
--network mynet \
-e MYSQL_ROOT_PASSWORD=666666 \
-v $HOME/var/lib/mysql8:/var/lib/mysql \
-p 13306:3306 \
${mysql8_image} --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci --default-time-zone=+8:00 --innodb-dedicated-server=on


mkdir -p $HOME/var/lib/postgresql/data

docker run -d --name postgres12 \
--restart always \
--network mynet \
-e POSTGRES_PASSWORD=666666 \
-p 5432:5432 \
-v $HOME/var/lib/postgresql/data:/var/lib/postgresql/data \
${pg_image}


docker run -d --name=adminer --restart always --network mynet -p 18080:8080 ${adminer_image}



}

fun_initdb(){

docker run --net host --rm --name='wait4x' ${wait4x_image} mysql root:666666@tcp\(127.0.0.1:3306\)/mysql --interval 1s --timeout 360s && \
docker run -it --rm --network mynet  ${mysql5_image} mysql --host mysql57 --user root --password=666666 --loose-default-character-set=utf8 -e "CREATE DATABASE if not exists bdcm DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;show databases;"


docker run --net host --rm --name='wait4x' ${wait4x_image} mysql root:666666@tcp\(127.0.0.1:13306\)/mysql --interval 1s --timeout 360s && \
docker run -it --rm --network mynet  ${mysql8_image} mysql --host mysql8 --user root --password=666666 -e "CREATE DATABASE if not exists bdcm DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;show databases;"


docker run --net host --rm --name='wait4x' ${wait4x_image} postgresql 'postgres://postgres:666666@127.0.0.1:5432/postgres?sslmode=disable' --interval 1s --timeout 360s && \
docker exec -i postgres12 bash <<'EOF'
if [ $(psql -tA --username "postgres" -c "select count(1) from pg_database where datname='bdcm'") = "0" ]; then
    psql -v ON_ERROR_STOP=1 --username "postgres" --no-password -c "create database bdcm with encoding='utf8' TEMPLATE template0;"
fi
psql -v ON_ERROR_STOP=1 --username "postgres" --no-password -c "\l";
EOF
}


fun_install_minio(){
docker rm -f minio 2>/dev/null || true
mkdir -p $HOME/minio/data
docker run -d \
--name=minio \
--restart always \
--network host \
-e "MINIO_ROOT_USER=minioadmin" \
-e "MINIO_ROOT_PASSWORD=minioadmin" \
-v $HOME/minio/data:/data \
${minio_image} server /data --address ":19000" --console-address ":19001"



sleep 5s;

docker run -i --rm --network host --entrypoint '' minio/mc bash<<EOF
mc alias set myminio http://localhost:19000 minioadmin minioadmin;
mc admin user svcacct add --access-key "vUR3oLMF5ds8gWCP" --secret-key "odWFIZukYrw9dY0G5ezDKMZWbhU0S4oD" myminio minioadmin 2>/dev/null || true;
EOF

docker run -i --rm --network host --entrypoint '' minio/mc bash<<EOF
mc alias set myminio http://localhost:19000 minioadmin minioadmin;
mc mb myminio/my-bucket
mc anonymous set download myminio/my-bucket
EOF

}

fun_nginx_static(){

port=9980
name=nginx-$port
docker rm -f $name 2>/dev/null 1>/dev/null || true
cat >$HOME/nginx-$port.conf<<EOF
server {
    listen 80 reuseport backlog=8192 so_keepalive=10m:30s:10;
    keepalive_requests 30000;
    keepalive_time 30m;
    keepalive_timeout 150s;


    location / {
        root /usr/share/nginx/html;
        index index.html index.htm;
        autoindex on;
        autoindex_exact_size on;
        autoindex_localtime on;
        types {
            text/plain yaml;
            text/plain md;
            text/plain yml;
            text/plain conf;
            text/plain properties;
            text/plain service;
            text/plain h;
            text/plain c;
            text/plain sed;
            text/plain sh;
            text/plain xml;
            text/html html;
        }
    }
}
EOF

docker \
run -d \
--name $name \
--restart always \
--network mynet \
--add-host=host.docker.internal:host-gateway \
-p $port:80 \
-v $HOME/nginx-$port.conf:/etc/nginx/conf.d/default.conf \
${nginx_image}
}

fun_nginx_proxy_minio(){
port=9982
name=nginx-$port
docker rm -f $name 2>/dev/null 1>/dev/null || true
cat >$HOME/nginx-$port.conf<<EOF
upstream backend {
   server host.docker.internal:19000;
   keepalive 32;
   keepalive_requests 1000;
   keepalive_time 1h;
   keepalive_timeout 120s;
}

server {
    listen 80 reuseport backlog=8192 so_keepalive=10m:30s:10;
    keepalive_requests 30000;
    keepalive_time 30m;
    keepalive_timeout 150s;
    location / {
      rewrite ^(/.*)\$ /my-bucket\$1 break;
      proxy_pass http://backend;
      proxy_http_version 1.1;
      proxy_set_header Connection "";
      proxy_set_header Host \$host;
      proxy_set_header X-Real-IP \$remote_addr;
      proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
      proxy_set_header X-Forwarded-Proto \$scheme;
      proxy_set_header X-Forwarded-Port  \$server_port;
      proxy_set_header X-Forwarded-Host  \$host;
      proxy_hide_header X-Protocol-Hide;
      proxy_pass_header Server;
      proxy_busy_buffers_size 512k;
      proxy_buffers 8 512k;
      proxy_buffer_size 256k;
      proxy_read_timeout 1800;
      proxy_connect_timeout 1800;
      proxy_send_timeout 1800;
      client_max_body_size 50M;
      proxy_next_upstream error timeout invalid_header http_500 http_502 http_503 http_504;
      proxy_ssl_protocols TLSv1 TLSv1.1 TLSv1.2 TLSv1.3;
      proxy_ssl_ciphers HIGH:!aNULL:!MD5;
      proxy_ssl_verify off;
      proxy_set_header cookie \$http_cookie;
      chunked_transfer_encoding off;
      port_in_redirect off;
      absolute_redirect off;
      proxy_socket_keepalive on;
    }
}
EOF

docker \
run -d \
--name $name \
--restart always \
--network mynet \
--add-host=host.docker.internal:host-gateway \
-p $port:80 \
-v $HOME/nginx-$port.conf:/etc/nginx/conf.d/default.conf \
${nginx_image}
}



fun_add_mynet
fun_install_misc
fun_initdb
fun_install_minio
fun_nginx_static
fun_nginx_proxy_minio

