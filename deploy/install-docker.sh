#!/bin/bash
# ============================================================
# Ubuntu 22.04 Docker + Docker Compose 安装脚本
# ============================================================
set -e

GREEN='\033[0;32m'; NC='\033[0m'
log() { echo -e "${GREEN}[INFO]${NC} $1"; }

# 卸载旧版本
log "清理旧版本..."
for pkg in docker.io docker-doc docker-compose docker-compose-v2 podman-docker containerd runc; do
    sudo apt remove -y $pkg 2>/dev/null || true
done

# 安装依赖
log "安装依赖..."
sudo apt update -y
sudo apt install -y ca-certificates curl

# 添加 Docker GPG 密钥
log "添加 Docker GPG 密钥..."
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc

# 添加 Docker 仓库
log "添加 Docker 仓库..."
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
    sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 安装 Docker
log "安装 Docker..."
sudo apt update -y
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 将当前用户加入 docker 组（无需 sudo 运行 docker）
log "配置用户权限..."
sudo usermod -aG docker "$USER"

# 启动 Docker
sudo systemctl enable docker --now

log "=============================================="
log "  Docker 安装完成！"
log "=============================================="
docker --version
docker compose version
log ""
log "重要：请退出 SSH 重新登录，或执行以下命令使 docker 组生效："
log "  newgrp docker"
log "=============================================="
