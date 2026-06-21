#!/bin/bash
# ============================================================
# Ubuntu 22.04 环境安装脚本
# 安装：OpenJDK 17、Maven 3.9、MySQL 8.0、Redis 7
# 所有软件安装到 /usr 目录下（apt 默认行为）
# ============================================================

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log() { echo -e "${GREEN}[INFO]${NC} $1"; }
warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }

# ---- 检查是否为 root 或具备 sudo ----
if [ "$EUID" -ne 0 ]; then
    warn "建议以 root 权限运行此脚本，或确保当前用户有 sudo 权限"
    SUDO="sudo"
else
    SUDO=""
fi

# ============================================================
# 1. 更新系统包索引
# ============================================================
log "更新 apt 包索引..."
$SUDO apt update -y

# ============================================================
# 2. 安装 OpenJDK 17
#    安装路径：/usr/lib/jvm/java-17-openjdk-amd64
#    可执行文件通过 alternatives 链接到 /usr/bin/
# ============================================================
log "安装 OpenJDK 17..."
$SUDO apt install -y openjdk-17-jdk

JAVA_HOME_PATH="/usr/lib/jvm/java-17-openjdk-amd64"
if [ -d "$JAVA_HOME_PATH" ]; then
    log "JDK 17 安装成功: $JAVA_HOME_PATH"
else
    warn "未找到 JDK 17 默认路径，尝试查找..."
    JAVA_HOME_PATH=$(dirname $(dirname $(readlink -f $(which java))))
    log "JAVA_HOME 路径: $JAVA_HOME_PATH"
fi

# ============================================================
# 3. 安装 Maven 3.9（使用 Apache 官方包，解压到 /usr/local）
# ============================================================
log "安装 Maven 3.9..."
MAVEN_VERSION="3.9.9"
MAVEN_DIR="/usr/local/apache-maven-${MAVEN_VERSION}"

if [ ! -d "$MAVEN_DIR" ]; then
    cd /tmp
    # 使用清华镜像下载，国内更快
    wget -q "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz"
    $SUDO tar -xzf "apache-maven-${MAVEN_VERSION}-bin.tar.gz" -C /usr/local/
    rm -f "apache-maven-${MAVEN_VERSION}-bin.tar.gz"
    log "Maven 解压到 ${MAVEN_DIR}"
else
    log "Maven 已存在，跳过下载"
fi

# ============================================================
# 4. 安装 MySQL 8.0
#    数据目录：/var/lib/mysql
#    配置文件：/etc/mysql/
#    可执行文件：/usr/bin/
# ============================================================
log "安装 MySQL 8.0..."
if ! command -v mysql &> /dev/null; then
    $SUDO apt install -y mysql-server
    log "MySQL 安装完成"
else
    log "MySQL 已安装，跳过"
fi

# ============================================================
# 5. 安装 Redis 7
#    配置文件：/etc/redis/redis.conf
#    可执行文件：/usr/bin/
# ============================================================
log "安装 Redis..."
if ! command -v redis-server &> /dev/null; then
    $SUDO apt install -y redis-server
    log "Redis 安装完成"
else
    log "Redis 已安装，跳过"
fi

# ============================================================
# 6. 配置环境变量（写入 /etc/profile.d/）
# ============================================================
log "配置环境变量..."
ENV_FILE="/etc/profile.d/devenv.sh"
$SUDO tee "$ENV_FILE" > /dev/null << EOF
# Java & Maven 环境变量
export JAVA_HOME=${JAVA_HOME_PATH}
export MAVEN_HOME=${MAVEN_DIR}
export PATH=\$JAVA_HOME/bin:\$MAVEN_HOME/bin:\$PATH
EOF

$SUDO chmod +x "$ENV_FILE"
log "环境变量已写入 ${ENV_FILE}"

# ============================================================
# 7. 启动 MySQL 和 Redis
# ============================================================
log "启动 MySQL..."
$SUDO systemctl start mysql
$SUDO systemctl enable mysql &> /dev/null

log "启动 Redis..."
$SUDO systemctl start redis-server
$SUDO systemctl enable redis-server &> /dev/null

# ============================================================
# 8. MySQL 初始安全配置（设置 root 密码）
# ============================================================
log "配置 MySQL root 密码..."
MYSQL_ROOT_PASS="root123"

# 尝试两种常见的 auth_socket 方式连接
$SUDO mysql -u root <<SQL 2>/dev/null || true
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '${MYSQL_ROOT_PASS}';
FLUSH PRIVILEGES;
SQL

log "MySQL root 密码设置为: ${MYSQL_ROOT_PASS}"

# ============================================================
# 9. Redis 绑定地址配置（允许外部连接）
# ============================================================
log "配置 Redis 允许远程连接..."
$SUDO sed -i 's/^bind 127.0.0.1 ::1/bind 0.0.0.0/' /etc/redis/redis.conf
$SUDO sed -i 's/^protected-mode yes/protected-mode no/' /etc/redis/redis.conf
$SUDO systemctl restart redis-server

# ============================================================
# 10. 检查防火墙（如果启用则放行端口）
# ============================================================
if command -v ufw &> /dev/null && $SUDO ufw status | grep -q "Status: active"; then
    log "配置防火墙规则..."
    $SUDO ufw allow 3306/tcp comment 'MySQL'
    $SUDO ufw allow 6379/tcp comment 'Redis'
    $SUDO ufw allow 8080/tcp comment 'Spring Boot App'
fi

# ============================================================
# 验证安装
# ============================================================
echo ""
echo "=============================================="
echo "  安装完成！各软件版本："
echo "=============================================="
echo "Java:    $(java -version 2>&1 | head -1)"
echo "Maven:   $(mvn -version 2>&1 | head -1)"
echo "MySQL:   $(mysql --version 2>&1)"
echo "Redis:   $(redis-cli --version 2>&1)"
echo ""
echo "JAVA_HOME:  ${JAVA_HOME_PATH}"
echo "MAVEN_HOME: ${MAVEN_DIR}"
echo ""
echo "MySQL root 密码: ${MYSQL_ROOT_PASS}"
echo "=============================================="
echo ""
log "请执行 source /etc/profile.d/devenv.sh 或重新登录以使环境变量生效"
