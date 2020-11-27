#!/bin/bash

if [ ! $# -eq 1 ];then
    echo "请输入版本号"
    exit 1
fi

version="$1"

cat <<EOF > Dockerfile
FROM openjdk:8-jre-alpine
COPY demo-cache-ssh-0.0.1-SNAPSHOT.jar /cache-demo.jar
ENTRYPOINT ["java","-jar","/cache-demo.jar"]
EOF

echo "Dockerfile created success"

docker build -t cache-demo:${version} .

echo "image build success"
rm -rf Dockerfile

sudo docker tag cache-demo:${version} registry.cn-hangzhou.aliyuncs.com/ghx-docker-repo/demo-cache:${version}
sudo docker push registry.cn-hangzhou.aliyuncs.com/ghx-docker-repo/demo-cache:${version}
docker rmi -f registry.cn-hangzhou.aliyuncs.com/ghx-docker-repo/demo-cache:${version}

echo "image push success"

cp cache-demo-deployment-template.yaml cache-demo-deployment.yaml
sed -i "s/{{version}}/${version}/g" cache-demo-deployment.yaml

echo "$(cat cache-demo-deployment.yaml)"


kubectl apply -f cache-demo-deployment.yaml