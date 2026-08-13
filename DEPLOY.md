# Production Deployment Guide

Triển khai TaskHub lên VPS (Ubuntu/Debian) bằng Docker Compose + Caddy (auto HTTPS).

## Yêu cầu

- VPS Linux có ít nhất **2 GB RAM**, 20 GB ổ cứng (DigitalOcean / Vultr / Hetzner ~$5-10/tháng)
- Domain trỏ DNS A record về IP của VPS
- SSH access vào VPS

## Bước 1 — Cài Docker trên VPS

```bash
# SSH vào VPS, chạy:
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER
newgrp docker  # hoặc logout & login lại
```

## Bước 2 — Mở firewall

```bash
sudo ufw allow OpenSSH
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
```

## Bước 3 — Trỏ DNS

Tại nhà cung cấp domain (Cloudflare/Namecheap/...), tạo A record:

| Type | Name              | Value           |
|------|-------------------|-----------------|
| A    | task (hoặc @)     | <IP của VPS>    |

Đợi 1-5 phút cho DNS propagate. Verify: `dig +short task.example.com` phải trả về đúng IP.

## Bước 4 — Clone code lên VPS

```bash
cd /opt
sudo git clone https://github.com/nhathao428/taskhub.git
sudo chown -R $USER:$USER taskhub
cd taskhub
```

## Bước 5 — Cấu hình env

```bash
cp .env.production.example .env
nano .env
```

Điền các giá trị bắt buộc:

```bash
DOMAIN=task.example.com         # đúng domain bạn đã trỏ DNS
DB_PASSWORD=$(openssl rand -base64 24)
JWT_SECRET=$(openssl rand -base64 48)
ADMIN_PASSWORD=<mật khẩu mạnh>
GEMINI_API_KEY=AIza...          # tùy chọn, lấy free tại aistudio.google.com/apikey
```

Tip — tự gen secrets nhanh:
```bash
echo "DB_PASSWORD=$(openssl rand -base64 24)" >> .env
echo "JWT_SECRET=$(openssl rand -base64 48)" >> .env
```

## Bước 6 — Khởi động stack

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

Lần đầu sẽ build image (~5-10 phút). Sau đó:
- Caddy tự xin Let's Encrypt cert qua HTTP-01 challenge (cần port 80 mở)
- Backend boot ~30s rồi seed admin account

## Bước 7 — Verify

```bash
# Xem log từng service
docker compose logs -f caddy
docker compose logs -f backend
docker compose logs -f postgres

# Health check
curl https://task.example.com/api/auth/login -X POST \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@example.com","password":"<ADMIN_PASSWORD>"}'
```

Nếu trả về JWT token → deploy thành công.

Mở browser tại `https://task.example.com` → thấy trang Login.

## Cập nhật khi có code mới

```bash
cd /opt/taskhub
git pull
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
```

## Backup database

```bash
# Dump
docker compose exec postgres pg_dump -U postgres task_management_db > backup_$(date +%F).sql

# Restore
cat backup.sql | docker compose exec -T postgres psql -U postgres task_management_db
```

## Troubleshoot

| Triệu chứng | Nguyên nhân thường gặp |
|---|---|
| Caddy không có cert | DNS chưa trỏ đúng / port 80 bị chặn |
| Backend crash on boot | Thiếu env var `JWT_SECRET` hoặc `ADMIN_PASSWORD` |
| 502 Bad Gateway | Backend chưa boot xong (chờ ~30s) hoặc crash — `docker compose logs backend` |
| CORS error trong browser | `DOMAIN` trong `.env` không khớp với URL truy cập |
| AI suggestion 422 | `GEMINI_API_KEY` chưa set |

## Tắt stack

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml down

# Xóa luôn volume (cẩn thận — mất data!)
docker compose -f docker-compose.yml -f docker-compose.prod.yml down -v
```
