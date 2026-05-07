# barber_service

理发预约系统后端服务。

技术栈：Spring Boot 3 + Java 17 + MySQL 8。

## 开发分支

当前开发分支：`test_0.0.1`。

除非明确要求，不在 `main` 分支继续开发；需要合并时按约定合并到 `master` 分支。

## 本地 Docker 运行

```bash
docker compose up -d --build
```

健康检查：

```bash
curl http://127.0.0.1:8080/api/health
```

公网入口由云服务器 + frp + 本机 gateway 提供：

```text
https://www.ringsora.com/barber/health
```
