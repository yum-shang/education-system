#!/bin/bash
# ============================================================
# Ubuntu 22.04 Docker + Docker Compose 安装脚本（国内网络适配）
# 优先使用阿里云镜像，失败则回退到系统自带的 docker.io
# ============================================================
set -e

GREEN='\033[0;32m'; YELLOW='\033[1;33m'; NC='\033[0m'
log() { echo -e "${GREEN}[INFO]${NC} $1"; }
warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }

# 卸载旧版本
log "清理旧版本..."
for pkg in docker.io docker-doc docker-compose docker-compose-v2 podman-docker containerd runc; do
    sudo apt remove -y $pkg 2>/dev/null || true
done

# 安装依赖
log "安装依赖..."
sudo apt update -y
sudo apt install -y ca-certificates curl

# ---- 方式一：阿里云镜像安装 Docker CE ----
log "尝试通过阿里云镜像安装 Docker CE..."
sudo install -m 0755 -d /etc/apt/keyrings

# 阿里云 Docker 镜像源
DOCKER_MIRROR="https://mirrors.aliyun.com/docker-ce/linux/ubuntu"
DOCKER_GPG="${DOCKER_MIRROR}/gpg"

if curl -fsSL --connect-timeout 10 "$DOCKER_GPG" -o /tmp/docker.asc 2>/dev/null; then
    sudo cp /tmp/docker.asc /etc/apt/keyrings/docker.asc
    sudo chmod a+r /etc/apt/keyrings/docker.asc
    log "GPG 密钥下载成功（阿里云）"

    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] ${DOCKER_MIRROR} $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
        sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

    if sudo apt update -y 2>/dev/null; then
        log "Docker CE 源已添加，开始安装..."
        sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
    else
        warn "Docker CE 源不可用，改用系统自带 docker.io"
        USE_FALLBACK=true
    fi
else
    warn "阿里云镜像不通，改用系统自带 docker.io"
    USE_FALLBACK=true
fi

# ---- 方式二：回退到 Ubuntu 自带 docker.io ----
if [ "${USE_FALLBACK}" = "true" ]; then
    log "从 Ubuntu 默认仓库安装 docker.io..."
    sudo apt update -y
    sudo apt install -y docker.io docker-compose-v2
fi

# ---- 配置 Docker 镜像加速（阿里云）----
log "配置镜像加速器..."
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json > /dev/null << 'EOF'
{
  "registry-mirrors": [
    "https://registry.cn-hangzhou.aliyuncs.com",
    "https://dockerproxy.com"
  ]
}
EOF

# ---- 用户权限 ----
log "配置用户权限..."
sudo usermod -aG docker "$USER"

# ---- 启动 ----
sudo systemctl daemon-reload
sudo systemctl enable docker --now 2>/dev/null || sudo systemctl restart docker

log "=============================================="
log "  Docker 安装完成！"
log "=============================================="
docker --version 2>/dev/null || warn "docker 命令未找到，请检查安装"
docker compose version 2>/dev/null || docker-compose --version 2>/dev/null || warn "docker compose 未找到"
log ""
log "重要：请退出 SSH 重新登录，或执行："
log "  newgrp docker"
log "=============================================="
