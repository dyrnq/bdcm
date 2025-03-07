#!/usr/bin/env bash
mirrors="http://192.168.6.130:5000"
# echo "${mirrors}"
while [ $# -gt 0 ]; do
    case "$1" in
        --mirrors|-m|-M)
            mirrors="$2"
            shift
            ;;
        --*)
            echo "Illegal option $1"
            ;;
    esac
    shift $(( $# > 0 ? 1 : 0 ))
done


IFS=',' read -r -a mirrors_arr <<< ${mirrors}

echo "current mirrors:"
jq -r ".\"registry-mirrors\"" /etc/docker/daemon.json

mirrors_json=""
for element in "${mirrors_arr[@]}" ;do
    mirrors_json=${mirrors_json}"\""${element}\"","
done
mirrors_json="[${mirrors_json%?}]"

echo "new mirrors:"

echo "${mirrors_json}"
echo ""
echo "update mirrors..."

jq ".\"registry-mirrors\" = ${mirrors_json}" /etc/docker/daemon.json > /etc/docker/daemon.json.tmp && mv /etc/docker/daemon.json.tmp /etc/docker/daemon.json


echo "current daemon.json:"
cat </etc/docker/daemon.json

echo ""
echo "restart docker..."

systemctl daemon-reload
systemctl restart docker