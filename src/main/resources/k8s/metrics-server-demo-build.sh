#!/bin/bash

VERSION=$(date "+%Y%m%d%H%M%S")

echo "jenkins dir : ${WORKSPACE}"
echo "version : ${VERSION}"
echo "module name : ${MODULE_NAME}"

JAR_PACKAGE_NAME=$(ls "${WORKSPACE}"/target | grep '.*\.jar$')
echo "jar_package_name : ${JAR_PACKAGE_NAME}"

cd "${WORKSPACE}"/target
rm -rf Dockerfile
rm -rf metrics-server-demo-deploy.yaml

cat <<EOF > Dockerfile
FROM openjdk:8-jre-alpine
COPY ./${JAR_PACKAGE_NAME} /${MODULE_NAME}.jar
ENTRYPOINT ["java","-jar","/${MODULE_NAME}.jar"]
EOF

echo "Dockerfile created success"

docker build -t "${MODULE_NAME}":"${VERSION}" .

echo "image build success"

sudo docker tag "${MODULE_NAME}":"${VERSION}" registry.cn-hangzhou.aliyuncs.com/ghx-docker-repo/"${MODULE_NAME}":"${VERSION}"
sudo docker push registry.cn-hangzhou.aliyuncs.com/ghx-docker-repo/"${MODULE_NAME}":"${VERSION}"
docker rmi -f registry.cn-hangzhou.aliyuncs.com/ghx-docker-repo/"${MODULE_NAME}":"${VERSION}"

echo "image push success"

cd "${WORKSPACE}"/target/classes/k8s
chmod +x metrics-server-demo-deploy-template.yaml
cp metrics-server-demo-deploy-template.yaml "${WORKSPACE}"/target/metrics-server-demo-deploy.yaml

cd "${WORKSPACE}"/target
sed -i "s/{{VERSION}}/${VERSION}/g" metrics-server-demo-deploy.yaml
sed -i "s/{{MODULE_NAME}}/${MODULE_NAME}/g" metrics-server-demo-deploy.yaml

echo metrics-server-demo-deploy.yaml
echo "开始部署到k8s中..."
kubectl apply -f metrics-server-demo-deploy.yaml
