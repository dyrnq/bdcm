#!/usr/bin/env bash
set -Eeo pipefail

base_image="${base_image:-eclipse-temurin:21-jdk-noble}"
version="${version:-}";
push="${push:-false}"
repo="${repo:-dyrnq}"
image_name="${image_name:-bdcm}"
platforms="${platforms:-linux/amd64,linux/arm64/v8}"
curl_opts="${curl_opts:-}"
S6_OVERLAY_VERSION=3.2.0.2

while [ $# -gt 0 ]; do
    case "$1" in
        --base-image|--base)
            base_image="$2"
            shift
            ;;
        --version|--ver)
            version="$2"
            shift
            ;;
        --push)
            push="$2"
            shift
            ;;
        --curl-opts)
            curl_opts="$2"
            shift
            ;;
        --platforms)
            platforms="$2"
            shift
            ;;
        --repo)
            repo="$2"
            shift
            ;;
        --image-name|--image)
            image_name="$2"
            shift
            ;;
        --*)
            echo "Illegal option $1"
            ;;
    esac
    shift $(( $# > 0 ? 1 : 0 ))
done



# # https://docs.docker.com/build/building/multi-platform/
# docker run --privileged --rm tonistiigi/binfmt --install all
# # https://github.com/docker/buildx/issues/208
# docker run --privileged --rm multiarch/qemu-user---reset -p yes
# docker buildx create --name mbuilder --use 2>/dev/null || docker buildx use mbuilder
# docker buildx inspect --bootstrap


#chmod +x ./docker/rootfs/etc/services.d/bdcm/run
chmod +x ./docker/rootfs/docker-entrypoint.sh
chmod +x ./docker/rootfs/etc/s6-overlay/s6-rc.d/bdcm/run


line="${base_image}"
dockerfile="${line}_Dockerfile"


latest_tag="--tag $repo/$image_name:latest"

sed -e "s@__BASE_IMAGE__@$line@g" ./docker/Dockerfile > ./docker/${dockerfile}


docker buildx build \
--platform ${platforms} \
--output "type=image,push=$push" \
--file ./docker/${dockerfile} . \
--build-arg S6_OVERLAY_VERSION="${S6_OVERLAY_VERSION}" \
${latest_tag}

rm -rf ./docker/${dockerfile}
