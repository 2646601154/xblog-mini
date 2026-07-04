# SSL自动续期脚本部署说明

## 文件位置

将以下文件复制到服务器的 `/opt/xblog-mini/` 目录：

```
/opt/xblog-mini/
├── ssl_config.json    # 配置文件（包含API Key）
└── ssl_renew.py       # 续期脚本
```

## 安装依赖

```bash
pip3 install requests
```

## 测试运行

```bash
# 先测试一下（会实际执行续期）
python3 /opt/xblog-mini/ssl_renew.py

# 查看日志
tail -f /var/log/ssl_renew.log
```

## 配置定时任务（crontab）

```bash
# 编辑crontab
crontab -e

# 添加以下行（每月1号凌晨3点执行）
0 3 1 * * /usr/bin/python3 /opt/xblog-mini/ssl_renew.py >> /var/log/ssl_renew.log 2>&1
```

## 手动续期

```bash
python3 /opt/xblog-mini/ssl_renew.py
docker exec nginx-proxy nginx -s reload
```

## 配置说明

`ssl_config.json` 参数说明：

| 参数 | 说明 |
|------|------|
| `api_key` | 雨云API密钥 |
| `domain_name` | 主域名 |
| `cert_domains` | 申请证书的域名（泛域名+主域名） |
| `verify_method` | 验证方式：`auto`（自动）或 `dns`（手动） |
| `cert_dir` | 证书保存目录 |
| `nginx_container` | nginx容器名称 |
| `log_file` | 日志文件路径 |

## 注意事项

1. 首次运行前，确保域名 `xiaruoxin.cn` 已在雨云托管
2. 证书保存在 `/etc/letsencrypt/live/xiaruoxin.cn/`
3. 续期后会自动重载nginx配置
4. 日志记录在 `/var/log/ssl_renew.log`
