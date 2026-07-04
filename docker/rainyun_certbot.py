#!/usr/bin/env python3
"""
雨云DNS API + Certbot 泛域名证书自动续期脚本
使用 Let's Encrypt DNS 验证方式申请泛域名证书
"""

import json
import time
import logging
import subprocess
import sys
import os
from pathlib import Path
from typing import Optional, List

# 配置文件路径
CONFIG_FILE = Path(__file__).parent / "ssl_config.json"


class RainyunDNS:
    """雨云DNS管理类"""

    def __init__(self, config_path: str = CONFIG_FILE):
        self.config = self.load_config(config_path)
        self.api_key = self.config["api_key"]
        self.api_base = self.config["api_base_url"]
        self.headers = {
            "X-Api-Key": self.api_key,
            "Content-Type": "application/json"
        }
        self.domain_id: Optional[int] = None

        # 配置日志
        log_file = self.config.get("log_file", "/var/log/certbot_rainyun.log")
        logging.basicConfig(
            level=logging.INFO,
            format="%(asctime)s [%(levelname)s] %(message)s",
            handlers=[
                logging.StreamHandler(),
                logging.FileHandler(log_file)
            ]
        )
        self.logger = logging.getLogger(__name__)

    @staticmethod
    def load_config(path: str) -> dict:
        """加载配置文件"""
        with open(path, "r", encoding="utf-8") as f:
            return json.load(f)

    def api_request(self, method: str, endpoint: str, **kwargs) -> dict:
        """发送API请求"""
        import requests
        url = f"{self.api_base}{endpoint}"
        response = requests.request(method, url, headers=self.headers, **kwargs)
        response.raise_for_status()
        return response.json()

    def get_domain_id(self) -> int:
        """获取域名ID"""
        if self.domain_id:
            return self.domain_id

        result = self.api_request("GET", "/product/domain/", params={"options": "{}"})
        domains = result.get("data", [])

        for domain in domains:
            if domain.get("domain") == self.config["domain_name"]:
                self.domain_id = domain["id"]
                self.logger.info(f"找到域名: {self.config['domain_name']} (ID: {self.domain_id})")
                return self.domain_id

        raise Exception(f"未找到域名: {self.config['domain_name']}")

    def get_dns_records(self) -> List[dict]:
        """获取DNS解析记录列表"""
        domain_id = self.get_domain_id()
        result = self.api_request(
            "GET",
            f"/product/domain/{domain_id}/dns/",
            params={"limit": 100, "page_no": 1}
        )
        return result.get("data", [])

    def add_txt_record(self, host: str, value: str) -> int:
        """添加TXT记录，返回记录ID"""
        domain_id = self.get_domain_id()
        data = {
            "host": host,
            "value": value,
            "type": "TXT",
            "line": "DEFAULT",
            "ttl": 600
        }
        result = self.api_request("POST", f"/product/domain/{domain_id}/dns", json=data)
        record_id = result.get("data", {}).get("record_id")
        self.logger.info(f"添加TXT记录: {host} -> {value} (ID: {record_id})")
        return record_id

    def delete_txt_record(self, record_id: int) -> None:
        """删除DNS记录"""
        domain_id = self.get_domain_id()
        result = self.api_request(
            "DELETE",
            f"/product/domain/{domain_id}/dns/",
            json={"record_id": record_id}
        )
        self.logger.info(f"删除DNS记录: ID={record_id}")


class CertbotHook:
    """Certbot 钩子脚本"""

    def __init__(self, dns_manager: RainyunDNS):
        self.dns = dns_manager
        self.record_id: Optional[int] = None
        domain = os.environ.get("CERTBOT_DOMAIN", "").replace("*.", "")
        self.record_file = Path(f"/tmp/_acme-challenge.{domain}.txt")

    def auth_hook(self) -> None:
        """Certbot auth hook - 添加DNS TXT记录"""
        domain = os.environ.get("CERTBOT_DOMAIN", "")
        validation = os.environ.get("CERTBOT_VALIDATION", "")

        if not domain or not validation:
            self.dns.logger.error("环境变量 CERTBOT_DOMAIN 或 CERTBOT_VALIDATION 未设置")
            sys.exit(1)

        # 构建主机名
        domain_root = domain.replace("*.", "")
        host = "_acme-challenge." + domain_root

        try:
            self.record_id = self.dns.add_txt_record(host, validation)
            # 保存record_id用于cleanup
            self.record_file.write_text(str(self.record_id))
            self.dns.logger.info("已添加DNS验证记录，等待DNS传播...")
            time.sleep(60)  # 等待DNS记录生效
        except Exception as e:
            self.dns.logger.error(f"添加DNS记录失败: {e}")
            sys.exit(1)

    def cleanup_hook(self) -> None:
        """Certbot cleanup hook - 删除DNS TXT记录"""
        try:
            if self.record_file.exists():
                record_id = int(self.record_file.read_text().strip())
                self.dns.delete_txt_record(record_id)
                self.record_file.unlink()
                self.dns.logger.info("已清理DNS验证记录")
            else:
                # 尝试通过域名查找并删除
                domain = os.environ.get("CERTBOT_DOMAIN", "").replace("*.", "")
                host = "_acme-challenge." + domain
                records = self.dns.get_dns_records()
                for record in records:
                    if record.get("host") == host and record.get("type") == "TXT":
                        self.dns.delete_txt_record(record["id"])
                        break
        except Exception as e:
            self.dns.logger.warning(f"清理DNS记录时出错: {e}")

    def deploy_hook(self) -> None:
        """Certbot deploy hook - 重新加载nginx"""
        try:
            container = self.dns.config.get("nginx_container", "nginx-proxy")
            subprocess.run(
                ["docker", "exec", container, "nginx", "-s", "reload"],
                check=True,
                capture_output=True,
                text=True
            )
            self.dns.logger.info("nginx配置已重新加载")
        except subprocess.CalledProcessError as e:
            self.dns.logger.error(f"nginx重载失败: {e}")


def main():
    """主函数"""
    if len(sys.argv) > 1:
        config_path = sys.argv[1]
    else:
        config_path = CONFIG_FILE

    dns_manager = RainyunDNS(config_path)
    hook = CertbotHook(dns_manager)

    # 根据命令行参数决定执行哪个hook
    if len(sys.argv) > 2:
        action = sys.argv[2]
        if action == "auth":
            hook.auth_hook()
        elif action == "cleanup":
            hook.cleanup_hook()
        elif action == "deploy":
            hook.deploy_hook()
        else:
            dns_manager.logger.error(f"未知操作: {action}")
            sys.exit(1)
    else:
        dns_manager.logger.error("用法: python3 rainyun_certbot.py <配置文件路径> <auth|cleanup|deploy>")
        sys.exit(1)


if __name__ == "__main__":
    main()