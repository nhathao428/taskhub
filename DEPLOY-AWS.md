# Triển khai lên AWS EC2 (all-in-one)

Hướng dẫn chạy Task Management System lâu dài trên **một** máy ảo AWS EC2:
một server chạy tất cả qua Docker — PostgreSQL + Redis + Spring Boot + React + Caddy.

| Mục | Giá trị |
|---|---|
| Loại máy | EC2 `t3.small` (2 vCPU, 2 GB RAM) |
| Hệ điều hành | Ubuntu Server 24.04 LTS |
| Ổ cứng | 20 GB gp3 |
| Chi phí ước tính | Miễn phí 12 tháng đầu (nếu trong free-tier), sau đó ~15 USD/tháng |
| Truy cập | `http://<Elastic-IP>` (HTTP, chưa có domain) |

> Khi có domain riêng: trỏ DNS về Elastic IP rồi đổi sang `docker-compose.prod.yml`
> + `Caddyfile` để Caddy tự cấp HTTPS (xem mục cuối).

---

## Bước 1 — Tạo EC2 instance

AWS Console → **EC2** → **Launch instance**:

1. **Name**: `task-management`
2. **AMI**: Ubuntu Server 24.04 LTS (64-bit x86)
3. **Instance type**: `t3.small`
4. **Key pair**: tạo mới (`task-mgmt-key`) → tải file `.pem` về máy, **giữ kỹ**
5. **Network settings** → Edit → tạo Security Group với 2 inbound rule:
   | Type | Port | Source |
   |------|------|--------|
   | SSH | 22 | My IP |
   | HTTP | 80 | Anywhere (0.0.0.0/0) |
6. **Storage**: 20 GB gp3
7. **Launch instance**

## Bước 2 — Gán Elastic IP (IP cố định)

EC2 → **Elastic IPs** → **Allocate Elastic IP address** → Allocate →
chọn IP vừa tạo → **Actions → Associate** → chọn instance `task-management`.

> Elastic IP giúp IP không đổi khi restart máy. Miễn phí khi đang gắn vào instance đang chạy.
Ghi lại IP này — ví dụ `3.27.10.55`.

## Bước 3 — SSH vào server

```bash
chmod 400 task-mgmt-key.pem
ssh -i task-mgmt-key.pem ubuntu@<Elastic-IP>
```

## Bước 4 — Cài Docker

```bash
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER
newgrp docker
```

## Bước 5 — Tạo swap 2 GB (quan trọng)

`t3.small` chỉ có 2 GB RAM — build image dễ thiếu bộ nhớ. Thêm swap:

```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

## Bước 6 — Tải mã nguồn

```bash
cd ~
git clone https://github.com/nhathao428/taskhub.git
cd taskhub
```

## Bước 7 — Cấu hình biến môi trường

```bash
cp .env.aws.example .env
nano .env
```

Điền các giá trị:

```bash
APP_URL=http://<Elastic-IP>             # IP đã ghi ở Bước 2
DB_PASSWORD=<dán kết quả: openssl rand -base64 24>
JWT_SECRET=<dán kết quả: openssl rand -base64 48>
ADMIN_PASSWORD=<mật khẩu admin mạnh>
GEMINI_API_KEY=AIza...                  # key Gemini (bật tính năng AI gợi ý)
```

Tạo nhanh 2 khóa bí mật:

```bash
openssl rand -base64 24    # → DB_PASSWORD
openssl rand -base64 48    # → JWT_SECRET
```

## Bước 8 — Khởi động

```bash
docker compose -f docker-compose.yml -f docker-compose.aws.yml up -d --build
```

Lần đầu build image mất ~8–12 phút. Theo dõi:

```bash
docker compose logs -f backend
```

## Bước 9 — Kiểm tra

Mở trình duyệt: `http://<Elastic-IP>` → thấy trang đăng nhập.

```bash
curl http://<Elastic-IP>/api/auth/login -X POST \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@example.com","password":"<ADMIN_PASSWORD>"}'
```

Trả về JWT token → thành công.

---

## Cập nhật khi có code mới

```bash
cd ~/taskhub
git pull
docker compose -f docker-compose.yml -f docker-compose.aws.yml up -d --build
```

## Sao lưu database

Chạy thủ công:

```bash
docker compose exec postgres pg_dump -U postgres task_management_db > ~/backup_$(date +%F).sql
```

Tự động hằng ngày lúc 2h sáng — `crontab -e`, thêm dòng:

```
0 2 * * * cd ~/taskhub && docker compose exec -T postgres pg_dump -U postgres task_management_db > ~/backup_$(date +\%F).sql
```

Khôi phục:

```bash
cat backup_2026-05-17.sql | docker compose exec -T postgres psql -U postgres task_management_db
```

## Nâng cấp lên domain + HTTPS (sau này)

1. Mua domain, tạo DNS A record trỏ về Elastic IP.
2. Mở thêm port **443** trong Security Group.
3. Sửa `.env`: thêm `DOMAIN=task.example.com`.
4. Đổi lệnh chạy sang bản prod (Caddy tự xin chứng chỉ Let's Encrypt):

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

## Xử lý sự cố

| Triệu chứng | Nguyên nhân thường gặp |
|---|---|
| Build bị kill / hết RAM | Chưa tạo swap (Bước 5) |
| 502 Bad Gateway | Backend chưa boot xong (~30s) hoặc crash — `docker compose logs backend` |
| Backend crash khi boot | Thiếu `JWT_SECRET` / `ADMIN_PASSWORD` trong `.env` |
| Không mở được trang | Security Group chưa mở port 80 |
| AI gợi ý lỗi 422 | `GEMINI_API_KEY` chưa điền |
