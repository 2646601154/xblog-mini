#!/bin/bash
# ============================================================
# Xblog-mini 一键构建脚本
# 用途：编译后端 + 构建前端 → 复制到 docker/ 目录
# 使用：chmod +x build.sh && ./build.sh
# ============================================================

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
DOCKER_DIR="$PROJECT_ROOT/docker"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Xblog-mini 一键构建${NC}"
echo -e "${GREEN}========================================${NC}"

# ============================================================
# 1. 构建后端 JAR
# ============================================================
echo -e "\n${YELLOW}[1/3] 构建后端 JAR...${NC}"
cd "$PROJECT_ROOT/blog-api"

# 检查 Maven Wrapper
if [ ! -f "./mvnw" ]; then
    echo -e "${RED}错误: 未找到 blog-api/mvnw${NC}"
    exit 1
fi

# 设置数据库密码（构建时需要，但不影响打包）
export DB_PASSWORD="${DB_PASSWORD:-123456}"

echo "执行: ./mvnw clean package -DskipTests"
./mvnw clean package -DskipTests

# 复制 JAR 到 docker 目录
JAR_FILE=$(find target -name "*.jar" -not -name "*-sources.jar" | head -1)
if [ -z "$JAR_FILE" ]; then
    echo -e "${RED}错误: 未找到生成的 JAR 文件${NC}"
    exit 1
fi

cp "$JAR_FILE" "$DOCKER_DIR/blog-api-0.0.1-SNAPSHOT.jar"
echo -e "${GREEN}✓ 后端 JAR 已复制到 docker/${NC}"

# ============================================================
# 2. 构建前端 blog-web
# ============================================================
echo -e "\n${YELLOW}[2/3] 构建 blog-web...${NC}"
cd "$PROJECT_ROOT/blog-web"

# 检查 pnpm
if ! command -v pnpm &> /dev/null; then
    echo -e "${RED}错误: 未找到 pnpm，请先安装: npm install -g pnpm${NC}"
    exit 1
fi

echo "执行: pnpm install && pnpm build"
pnpm install
pnpm build

# 复制构建产物到 docker 目录
rm -rf "$DOCKER_DIR/dist-web"
cp -r dist "$DOCKER_DIR/dist-web"
echo -e "${GREEN}✓ blog-web 构建产物已复制到 docker/dist-web/${NC}"

# ============================================================
# 3. 构建前端 blog-admin
# ============================================================
echo -e "\n${YELLOW}[3/3] 构建 blog-admin...${NC}"
cd "$PROJECT_ROOT/blog-admin"

echo "执行: pnpm install && pnpm build"
pnpm install
pnpm build

# 复制构建产物到 docker 目录
rm -rf "$DOCKER_DIR/dist-admin"
cp -r dist "$DOCKER_DIR/dist-admin"
echo -e "${GREEN}✓ blog-admin 构建产物已复制到 docker/dist-admin/${NC}"

# ============================================================
# 完成
# ============================================================
echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}  构建完成！${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "构建产物位置："
echo "  - docker/blog-api-0.0.1-SNAPSHOT.jar"
echo "  - docker/dist-web/"
echo "  - docker/dist-admin/"
echo ""
echo "下一步："
echo "  cd docker"
echo "  docker-compose up -d --build"
echo ""
