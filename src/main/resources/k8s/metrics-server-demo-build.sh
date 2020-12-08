#!/bin/bash

VERSION=$(date "+%Y%m%d%H%M%S")

echo "jenkins dir : ${WORKSPACE}"
echo "version : ${VERSION}"
echo "module name : ${MODULE_NAME}"

JAR_PACKAGE_NAME=$(ls "${WORKSPACE}"/target | grep '.*\.jar$')
echo "jar_package_name : ${JAR_PACKAGE_NAME}"

cd "${WORKSPACE}"/target
cat <<EOF > Dockerfile
FROM openjdk:8-jre-alpine
COPY ./${JAR_PACKAGE_NAME} /${MODULE_NAME}.jar
ENTRYPOINT ["java","-jar","/${MODULE_NAME}.jar"]
EOF

echo "Dockerfile created success"


docker build -t "${MODULE_NAME}":"${VERSION}" .

if [ 1 == 1 ];then
    echo "退出"
    exit 1
fi

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
