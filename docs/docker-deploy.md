# Docker 部署说明

本项目后端按 Docker 路线部署，不要求服务器直接安装 Java/Maven/MySQL。

## 启动

在项目根目录运行：

```bash
cd ~/barber-booking
docker compose up -d --build
```

## 查看状态

```bash
docker compose ps
docker compose logs -f api
docker compose logs -f mysql
```

## 健康检查

```bash
curl http://127.0.0.1:8080/api/health
```

预期返回类似：

```json
{"status":"ok","service":"barber-booking-api","time":"..."}
```

## 数据库初始化脚本

首次创建 MySQL Docker volume 时，`docker/mysql/init/*.sql` 会自动执行。

如果容器已经初始化过，新增 SQL 不会自动重复执行；可以手动应用：

```bash
docker exec -i barber-booking-mysql \
  mysql -ubarber_booking -pbarber_booking_password barber_booking \
  < docker/mysql/init/002-mvp-schema.sql
```

检查表：

```bash
docker exec barber-booking-mysql \
  mysql -ubarber_booking -pbarber_booking_password barber_booking \
  -e 'SHOW TABLES;'
```

## 停止

```bash
docker compose down
```

如需连同数据库数据一起删除：

```bash
docker compose down -v
```

## 环境变量

可复制 `.env.example` 为 `.env` 后修改密码和端口：

```bash
cp .env.example .env
```

生产环境一定要修改默认密码。
