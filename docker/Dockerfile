FROM __BASE_IMAGE__

ARG S6_OVERLAY_VERSION

ENV \
DEBIAN_FRONTEND=noninteractive \
LANG=en_US.utf8 \
S6_OVERLAY_VERSION=${S6_OVERLAY_VERSION:-3.1.5.0} \
S6_KEEP_ENV=1 \
S6_BEHAVIOUR_IF_STAGE2_FAILS=2 \
S6_SERVICES_GRACETIME=3000 \
S6_KILL_GRACETIME=3000 \
S6_CMD_WAIT_FOR_SERVICES=1

RUN \
set -eux; \
apt-get clean && \
apt-get update && \
apt-get -y upgrade && \
apt-get install -yq \
locales \
ca-certificates \
curl \
openssh-client \
psmisc \
procps \
iproute2 \
tree \
libfreetype6-dev \
fontconfig \
unzip \
less \
xz-utils \
gosu \
fonts-noto-cjk \
ttf-wqy-microhei \
wget \
jq \
&& \
localedef -i en_US -c -f UTF-8 -A /usr/share/locale/locale.alias en_US.UTF-8 && \
( if id ubuntu 2>/dev/null; then  userdel -r ubuntu; fi ) && \
groupadd -g 1000 app && useradd -d /app -u 1000 -g 1000 -m -s /bin/bash app; \
rm -rf /var/lib/apt/lists/*; \
ARCH="$(uname -m)"; \
case "${ARCH}" in \
   aarch64|arm64) \
     s6overlayArch="aarch64"; \
     ;; \
   armhf|armv7l) \
     s6overlayArch="armhf"; \
     c_rehash /etc/ssl/certs; \
     ;; \
   ppc64el|ppc64le) \
     s6overlayArch="ppc64le"; \
     ;; \
   amd64|x86_64) \
     s6overlayArch="x86_64"; \
     ;; \
   *) \
     echo "Unsupported arch: ${ARCH}"; \
     exit 1; \
     ;; \
esac; \
curl --retry 3 -fsSL https://github.com/just-containers/s6-overlay/releases/download/v${S6_OVERLAY_VERSION}/s6-overlay-${s6overlayArch}.tar.xz | tar -Jxpv -C /; \
curl --retry 3 -fsSL https://github.com/just-containers/s6-overlay/releases/download/v${S6_OVERLAY_VERSION}/s6-overlay-noarch.tar.xz | tar -Jxpv -C /; \
curl --retry 3 -fsSL https://github.com/just-containers/s6-overlay/releases/download/v${S6_OVERLAY_VERSION}/s6-overlay-symlinks-noarch.tar.xz | tar -Jxpv -C /;



WORKDIR /app

VOLUME [ "/app/bdcm" ]

COPY target/*.jar /app/bdcm.jar
COPY docker/rootfs /
EXPOSE 8080
EXPOSE 9980



STOPSIGNAL SIGTERM

ENTRYPOINT ["/docker-entrypoint.sh"]
