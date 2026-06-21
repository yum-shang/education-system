#!/bin/bash
# ============================================================
# VM 端部署脚本
# 用法：将本项目上传到 VM 后，在项目根目录执行
#   bash deploy/deploy-on-vm.sh
# ============================================================

set -e
GREEN='\033[0;32m'; YELLOW='\033[1;33m'; NC='\033[0m'
log() { echo -e "${GREEN}[DEPLOY]${NC} $1"; }
warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }

APP_NAME="education-system"
APP_VERSION="0.0.1-SNAPSHOT"
APP_JAR="target/${APP_NAME}-${APP_VERSION}.jar"
DB_USER="root"
DB_PASS="${DB_PASSWORD:-root123}"
DB_NAME="education_system"
DEPLOY_DIR="/opt/${APP_NAME}"

# ---- 1. 检查环境 ----
log "检查环境..."
source /etc/profile.d/devenv.sh 2>/dev/null || true

for cmd in java mvn mysql redis-cli; do
    if ! command -v $cmd &> /dev/null; then
        warn "$cmd 未安装，请先运行 install-env.sh"
        exit 1
    fi
done
log "环境检查通过"

# ---- 2. 初始化数据库 ----
log "初始化数据库..."
mysql -u"${DB_USER}" -p"${DB_PASS}" <<SQL
CREATE DATABASE IF NOT EXISTS ${DB_NAME} DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
SQL
log "数据库 ${DB_NAME} 已就绪"

# 导入 DDL
log "导入表结构..."
mysql -u"${DB_USER}" -p"${DB_PASS}" "${DB_NAME}" < src/main/resources/db/mysql.sql 2>/dev/null && log "mysql.sql 导入完成" || warn "mysql.sql 可能已导入过"
mysql -u"${DB_USER}" -p"${DB_PASS}" "${DB_NAME}" < src/main/resources/db/ai_session_summaries.sql 2>/dev/null && log "ai_session_summaries.sql 导入完成" || warn "ai_session_summaries.sql 可能已导入过"

# ---- 3. 打包 ----
log "Maven 打包（跳过测试）..."
mvn clean package -DskipTests -q
log "打包完成: ${APP_JAR}"

# ---- 4. 部署到 /opt ----
log "部署到 ${DEPLOY_DIR}..."
sudo mkdir -p "${DEPLOY_DIR}"
sudo cp "${APP_JAR}" "${DEPLOY_DIR}/"
sudo cp deploy/application-prod.yml "${DEPLOY_DIR}/"

# 创建上传目录
sudo mkdir -p "${DEPLOY_DIR}/uploads/images" "${DEPLOY_DIR}/uploads/reports" "${DEPLOY_DIR}/logs"
sudo chown -R "$(whoami)":"$(whoami)" "${DEPLOY_DIR}"

# ---- 5. 创建 systemd 服务（开机自启） ----
log "创建 systemd 服务..."
SERVICE_FILE="/etc/systemd/system/${APP_NAME}.service"
sudo tee "${SERVICE_FILE}" > /dev/null << UNIT
[Unit]
Description=Education System
After=network.target mysql.service redis-server.service
Wants=mysql.service redis-server.service

[Service]
Type=simple
User=$(whoami)
WorkingDirectory=${DEPLOY_DIR}
ExecStart=/usr/bin/java -jar ${DEPLOY_DIR}/${APP_NAME}-${APP_VERSION}.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
UNIT

sudo systemctl daemon-reload
sudo systemctl enable "${APP_NAME}" &>/dev/null

log "=============================================="
log "  部署完成！"
log "=============================================="
log "启动：sudo systemctl start ${APP_NAME}"
log "状态：sudo systemctl status ${APP_NAME}"
log "日志：journalctl -u ${APP_NAME} -f"
log "地址：http://$(hostname -I | awk '{print $1}'):8080/api/"
log "=============================================="

# 询问是否立即启动
read -rp "是否现在启动？[Y/n] " answer
if [[ "$answer" =~ ^[Nn] ]]; then
    log "跳过启动，稍后手动执行：sudo systemctl start ${APP_NAME}"
else
    log "启动服务..."
    sudo systemctl start "${APP_NAME}"
    sleep 3
    sudo systemctl status "${APP_NAME}" --no-pager
fi
