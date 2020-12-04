#!/bin/bash

VERSION=$(date "+%Y%m%d%H%M%S")
JENKINS_DIR=${WORKSPACE}/${MODULE_NAME}

echo "jenkins dir : ${JENKINS_DIR}"
echo "version : ${VERSION}"
echo "module name : ${MODULE_NAME}"

if [ 1 == 1 ];then
    echo "退出"
    exit 1
fi

cat <<EOF > Dockerfile
FROM openjdk:8-jre-alpine
COPY ${JENKINS_DIR}/target/demo-cache-ssh-0.0.1-SNAPSHOT.jar /${MODULE_NAME}.jar
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
