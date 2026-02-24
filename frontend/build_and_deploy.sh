#!/bin/bash
# -----------------------------------------
# React 项目自动构建 + Docker 打包 + 自动上传到服务器并导入
# 作者: Han Ding
# 日期: 2026-2-11
# 用法:
# 确保有执行权限: chmod +x build_and_deploy.sh
# 确保.pem 文件权限正确: chmod 400 [pem路径]
# ./build_and_deploy.sh [镜像名] [版本号] [pem路径] [服务器IP] [服务器用户] [远程目录]
# 示例:
# ./build_and_deploy.sh my-react-app v1.0 ~/.ssh/mykey.pem 192.168.1.100 ubuntu /home/ubuntu
# -----------------------------------------

set -e  # 遇到错误立即退出

# 参数读取
IMAGE_NAME=${1:-my-react-app}
IMAGE_TAG=${2:-latest}
PEM_PATH=${3}
SERVER_IP=${4}
SERVER_USER=${5:-ubuntu}
REMOTE_PATH=${6:-/home/${SERVER_USER}}
HOST_PORT=${7:-80}
CONTAINER_NAME=${IMAGE_NAME}

# 校验参数
if [ -z "$PEM_PATH" ] || [ -z "$SERVER_IP" ]; then
  echo "❌ 请提供 .pem 路径 和 服务器 IP！"
  echo "示例: ./build_and_deploy.sh myapp v1.0 ~/.ssh/my.pem 1.2.3.4 ubuntu /home/ubuntu"
  exit 1
fi

# 检查 Dockerfile
if [ ! -f Dockerfile ]; then
  echo "❌ 未找到 Dockerfile，请确保在项目根目录执行此脚本。"
  exit 1
fi

# 检查 React 项目
if [ ! -f package.json ]; then
  echo "❌ 未找到 package.json，请确保这是一个 React 项目目录。"
  exit 1
fi

# 1️⃣ 构建 React 项目
echo "📦 正在构建 React 项目..."
pnpm install
pnpm run build
echo "✅ React 构建完成。"

# 2️⃣ 构建 Docker 镜像
FULL_IMAGE="${IMAGE_NAME}:${IMAGE_TAG}"
echo "🐳 开始构建 Docker 镜像: ${FULL_IMAGE}"
docker build --platform linux/amd64 -t ${FULL_IMAGE} .
echo "✅ Docker 镜像构建完成。"

# 3️⃣ 导出镜像
EXPORT_FILE="${IMAGE_NAME}_${IMAGE_TAG}.tar"
echo "📦 正在导出镜像为: ${EXPORT_FILE}"
docker save -o ${EXPORT_FILE} ${FULL_IMAGE}
echo "✅ 镜像导出完成。"

# 4️⃣ 上传镜像文件到服务器
echo "🚀 正在上传镜像到服务器 ${SERVER_USER}@${SERVER_IP}:${REMOTE_PATH}"
scp -i ${PEM_PATH} -o StrictHostKeyChecking=no ${EXPORT_FILE} ${SERVER_USER}@${SERVER_IP}:${REMOTE_PATH}/

# 5️⃣ 登录服务器执行远程操作
ssh -i ${PEM_PATH} -o StrictHostKeyChecking=no ${SERVER_USER}@${SERVER_IP} << EOF
set -e

REMOTE_TAR="${REMOTE_PATH}/${EXPORT_FILE}"
CONTAINER_NAME="${CONTAINER_NAME}"
FULL_IMAGE="${FULL_IMAGE}"

# 停止同名容器（如果存在）
if docker ps -a --format '{{.Names}}' | grep -Eq "^${CONTAINER_NAME}\$"; then
  echo "🛑 停止并删除已存在容器..."
  docker stop ${CONTAINER_NAME} || true
  docker rm ${CONTAINER_NAME} || true
fi

# 删除旧镜像
if docker images --format '{{.Repository}}:{{.Tag}}' | grep -Eq "^${FULL_IMAGE}\$"; then
  echo "🗑 删除旧镜像 ${FULL_IMAGE}..."
  docker rmi ${FULL_IMAGE} || true
fi

echo "📦 正在导入 Docker 镜像..."
docker load -i "\$REMOTE_TAR"
echo "✅ 镜像已导入成功：${FULL_IMAGE}"

# 删除远程 tar 文件
echo "🗑 删除远程 tar 文件..."
rm -f "\$REMOTE_TAR"
echo "✅ tar 文件已删除。"

# 自动运行镜像
echo "🚀 启动容器 ${CONTAINER_NAME}，映射端口 ${HOST_PORT}:80..."
docker run -d --name ${CONTAINER_NAME} -p ${HOST_PORT}:80 --platform linux/amd64 ${FULL_IMAGE}
echo "✅ 容器已启动：${CONTAINER_NAME}"
docker ps | grep ${CONTAINER_NAME}

EOF

# 6️⃣ 清理本地临时文件（可选）
rm -f ${EXPORT_FILE}

echo "🎉 全部完成！镜像已上传并导入到服务器。"
