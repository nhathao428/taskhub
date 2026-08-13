# Face Recognition Prototype — TaskHub (đồ án chuyên ngành)

Prototype nhận diện khuôn mặt để nâng cấp module chấm công TaskHub. Đây là **bước
đầu tiên** (dựng khung/pipeline), dùng transfer learning (không tự train mạng CNN
từ đầu) + self-collected data + self-trained classifier nhẹ, theo đúng hướng đã
chốt trong đề tài chuyên ngành.

**Quan trọng:** code này **chưa chạy thử được** trong sandbox (không cài được
`torch` vì mạng sandbox chặn cả PyPI lẫn `download.pytorch.org` khi tải gói CUDA
đầy đủ). Cú pháp Python đã được kiểm tra bằng `py_compile` (không lỗi cú pháp), nhưng
**Hào cần tự chạy trên máy có GPU thật (laptop GTX 1650) để xác nhận logic đúng và
lấy số liệu thật cho báo cáo.** Làm theo đúng thứ tự các bước dưới đây.

---

## Cấu trúc thư mục

```
ml/face-recognition/
├── requirements.txt      # Danh sách thư viện (trừ torch — cài riêng, xem Bước 2)
├── face_pipeline.py       # Module dùng chung: load MTCNN + InceptionResnetV1, đọc dataset
├── capture_faces.py       # Chụp ảnh khuôn mặt từ webcam -> dataset/<tên>/*.jpg
├── train.py                # Train classifier (SVM/kNN) trên embedding -> models/classifier.pkl
├── verify.py                # Test nhận diện qua webcam bằng model đã train
├── evaluate.py              # Tính Accuracy / FAR / FRR / EER
├── liveness.py               # Test chống giả mạo bằng phát hiện chớp mắt (EAR)
├── dataset/                   # Ảnh khuôn mặt tự chụp (KHÔNG commit lên git — xem .gitignore)
└── models/                     # Model đã train (KHÔNG commit lên git)
```

---

## Bước 1: Tạo virtualenv

Mở terminal, `cd` vào thư mục `ml/face-recognition/` trong repo, rồi:

**Windows (PowerShell):**
```powershell
python -m venv venv
venv\Scripts\activate
```

**macOS/Linux:**
```bash
python3 -m venv venv
source venv/bin/activate
```

Sau khi activate, prompt terminal sẽ có `(venv)` ở đầu dòng. Kiểm tra Python đang
dùng đúng bản trong venv:
```bash
python --version
```
Khuyến nghị dùng **Python 3.10 hoặc 3.11** — `mediapipe` và `torch` tương thích tốt
nhất với 2 bản này (Python 3.12+ có thể thiếu wheel sẵn cho vài thư viện).

---

## Bước 2: Cài torch

**Đây là bước quan trọng nhất và khác nhau tuỳ máy — làm đúng thứ tự: cài torch
TRƯỚC, rồi mới cài `requirements.txt` ở Bước 3 (facenet-pytorch phụ thuộc torch có
sẵn).**

Laptop của Hào có **GTX 1650** (hỗ trợ CUDA) — nên cài bản CUDA để train/verify
nhanh hơn CPU nhiều lần (đặc biệt lúc train và test webcam realtime).

### Cách A — Bản CUDA (khuyến nghị cho GTX 1650)

1. Kiểm tra driver NVIDIA đã cài chưa và version CUDA driver hỗ trợ:
   ```bash
   nvidia-smi
   ```
   Nhìn dòng `CUDA Version: xx.x` ở góc trên phải — đó là version CUDA **tối đa**
   driver hỗ trợ (không phải version phải cài).

2. Cài torch bản CUDA 12.1 (tương thích tốt với hầu hết driver hiện tại, GTX 1650
   dùng kiến trúc Turing — chạy tốt với CUDA 12.x):
   ```bash
   pip install torch torchvision --index-url https://download.pytorch.org/whl/cu121
   ```
   Nếu lệnh trên báo lỗi không tìm thấy bản phù hợp, thử bản CUDA 11.8 (cũ hơn,
   tương thích rộng hơn):
   ```bash
   pip install torch torchvision --index-url https://download.pytorch.org/whl/cu118
   ```

3. Kiểm tra torch nhận đúng GPU:
   ```bash
   python -c "import torch; print(torch.cuda.is_available(), torch.cuda.get_device_name(0))"
   ```
   Kết quả mong đợi: `True NVIDIA GeForce GTX 1650`. Nếu ra `False`, torch cài bản
   CPU nhầm hoặc driver chưa đúng — kiểm tra lại `nvidia-smi` trước.

### Cách B — Bản CPU (fallback, chậm hơn nhưng chắc chắn chạy được)

Nếu Cách A lỗi (driver cũ, không có GPU lúc test, hoặc chỉ muốn chạy nhanh không
cần cài driver CUDA):
```bash
pip install torch torchvision --index-url https://download.pytorch.org/whl/cpu
```
Toàn bộ script (`train.py`, `verify.py`, `evaluate.py`) đều có cờ `--cpu` để ép
chạy CPU dù máy có GPU — dùng khi cần so sánh tốc độ hoặc debug.

---

## Bước 3: Cài các thư viện còn lại

Sau khi Bước 2 xong (torch đã cài và `import torch` chạy được):
```bash
pip install -r requirements.txt
```

Nếu `mediapipe` báo lỗi không tìm thấy bản phù hợp với Python đang dùng, đó thường
là do Python quá mới (3.12+) — quay lại Bước 1, tạo venv mới với Python 3.10/3.11.

---

## Bước 4: Chụp ảnh khuôn mặt (`capture_faces.py`)

Chụp ảnh của chính Hào để làm dataset:
```bash
python capture_faces.py --name hao --num-images 30
```

- Cửa sổ webcam hiện lên, khung xanh là vị trí đang thấy mặt.
- Nhấn **SPACE** để chụp 1 ảnh, nhấn **q** để dừng sớm.
- **Chụp ít nhất 20-30 ảnh**, và giữa các lần chụp nên đổi:
  - Góc mặt: chính diện, nghiêng trái/phải nhẹ, ngửa/cúi nhẹ
  - Biểu cảm: bình thường, cười, nghiêm túc
  - Ánh sáng: chỗ sáng, chỗ hơi tối hơn (mô phỏng điều kiện check-in thực tế —
    sáng sớm, đèn phòng, có/không đeo kính)

Ảnh lưu vào `dataset/hao/000.jpg`, `001.jpg`, ...

**Muốn test phân biệt người lạ/quen:** chụp thêm 1 người khác (bạn cùng phòng,
người thân — có xin phép) với tên khác, ví dụ:
```bash
python capture_faces.py --name ban_a --num-images 20
```
Không bắt buộc, nhưng nếu chỉ có 1 người trong dataset thì `evaluate.py` (Bước 6)
sẽ không tính được FAR/FRR (cần ít nhất 2 người để có cặp "khác người").

---

## Bước 5: Train model (`train.py`)

```bash
python train.py --dataset-dir dataset --model-out models/classifier.pkl --classifier svm
```

Script sẽ:
1. Đọc từng ảnh trong `dataset/<tên>/`, detect + align khuôn mặt bằng MTCNN.
2. Trích embedding 512 chiều bằng `InceptionResnetV1` (pretrained VGGFace2 —
   transfer learning, không train lại mạng này).
3. Train 1 classifier nhẹ (SVM mặc định, có thể đổi `--classifier knn`) trên các
   embedding đó.
4. Lưu model vào `models/classifier.pkl` và embeddings vào `models/embeddings.npz`
   (dùng lại cho `evaluate.py`).

Nếu máy không có GPU hoặc muốn ép CPU: thêm `--cpu`.

Nếu có ảnh không detect được mặt (ảnh mờ, quá tối, không rõ mặt), script sẽ in
cảnh báo và bỏ qua ảnh đó — không dừng chương trình.

---

## Bước 6: Test nhận diện qua webcam (`verify.py`)

```bash
python verify.py --model models/classifier.pkl --threshold 0.6
```

- Đứng trước webcam, khung màu quanh mặt hiện tên + độ tin cậy (0-1).
- Xanh lá = nhận diện được (độ tin cậy ≥ threshold), cam = "Unknown" (dưới
  threshold — coi như người lạ, tránh nhận nhầm).
- Nhấn **q** để thoát.
- Thử tăng/giảm `--threshold` (ví dụ 0.5 hoặc 0.7) để xem ảnh hưởng tới việc nhận
  nhầm — ghi lại quan sát này vào báo cáo (đây chính là đánh đổi giữa FAR và FRR).

---

## Bước 7: Đánh giá Accuracy / FAR / FRR (`evaluate.py`)

```bash
python evaluate.py --dataset-dir dataset --test-size 0.3
```

In ra 2 phần:
1. **Accuracy phân loại (closed-set)** — chia dataset thành train/test theo từng
   người, train lại classifier trên phần train, đo accuracy trên phần test.
2. **FAR / FRR / EER kiểu xác thực (verification)** — so từng cặp ảnh với nhau
   bằng cosine similarity, quét qua nhiều ngưỡng, in bảng FAR/FRR mỗi ngưỡng và
   ước lượng điểm EER (nơi FAR ≈ FRR).

**Lưu ý viết vào báo cáo:** dataset tự chụp chỉ có 1-2 người, số liệu ở đây mang
tính minh hoạ quy trình đánh giá (prototype), không đại diện độ chính xác thực tế
khi triển khai với hàng chục/hàng trăm nhân viên thật — đây là hạn chế cần nêu rõ,
không nên trình bày như kết quả cuối cùng.

---

## Bước 8: Test chống giả mạo (`liveness.py`)

```bash
python liveness.py --timeout 10
```

- Nhìn thẳng vào camera trong 10 giây và chớp mắt bình thường.
- Script tính Eye Aspect Ratio (EAR) qua từng frame bằng MediaPipe Face Mesh —
  EAR giảm đột ngột rồi tăng lại = 1 lần chớp mắt = dấu hiệu người thật.
- Kết quả: "PHÁT HIỆN CHỚP MẮT" (live) hoặc "KHÔNG phát hiện" (nghi ngờ giả mạo —
  ví dụ đưa ảnh in/ảnh trên điện thoại ra trước camera sẽ không chớp mắt được).

**Thử test giả mạo:** cầm 1 tấm ảnh mặt người (in ra hoặc mở trên điện thoại) đưa
trước webcam thay vì mặt thật — script phải KHÔNG phát hiện chớp mắt (đúng như kỳ
vọng). Ghi lại kết quả này vào báo cáo làm bằng chứng phần liveness detection.

**Hạn chế cần ghi rõ trong báo cáo:** đây là baseline đơn giản (chỉ dựa vào chớp
mắt), chưa chống được video replay (quay sẵn video có chớp mắt) hoặc mặt nạ 3D.
Hướng mở rộng: phân tích texture/tần số ảnh, phát hiện phản xạ ánh sáng màn hình,
hoặc dùng model chuyên dụng.

---

## Tổng hợp lệnh (copy chạy tuần tự)

```bash
cd ml/face-recognition
python -m venv venv
# Windows: venv\Scripts\activate | macOS/Linux: source venv/bin/activate

pip install torch torchvision --index-url https://download.pytorch.org/whl/cu121
pip install -r requirements.txt

python capture_faces.py --name hao --num-images 30
python train.py --dataset-dir dataset --model-out models/classifier.pkl
python verify.py --model models/classifier.pkl
python evaluate.py --dataset-dir dataset
python liveness.py --timeout 10
```

---

## Troubleshoot

| Triệu chứng | Nguyên nhân thường gặp |
|---|---|
| `torch.cuda.is_available()` trả `False` dù có GPU | Cài nhầm bản CPU ở Bước 2, hoặc driver NVIDIA chưa đúng — chạy lại `nvidia-smi` kiểm tra |
| `pip install mediapipe` lỗi không tìm thấy bản phù hợp | Python quá mới (3.12+) — tạo lại venv với Python 3.10/3.11 |
| `Khong mo duoc webcam index 0` | Webcam đang bị app khác dùng (Zoom, Camera app...), hoặc thử `--camera-index 1` |
| `train.py` báo "Khong phat hien duoc khuon mat" nhiều ảnh | Ảnh quá mờ/tối/xa — chụp lại gần hơn, đủ sáng hơn ở Bước 4 |
| `evaluate.py` không tính được FAR/FRR | Dataset chỉ có 1 người — cần ít nhất 2 người (xem gợi ý ở Bước 4) |
| Train/verify rất chậm dù có GPU | Kiểm tra lại log `[info] Dung device: ...` khi chạy — nếu in ra `cpu` dù có GPU, xem lại Bước 2 |

---

## Ghi chú cho báo cáo đồ án

- **Kỹ thuật AI dùng:** transfer learning — MTCNN (detect+align) + InceptionResnetV1
  pretrained trên VGGFace2 (trích embedding), **không** train lại mạng CNN từ đầu.
- **Phần tự làm/tự train:** dataset tự thu thập (`capture_faces.py`), classifier
  nhẹ tự train trên embedding (`train.py`), tự đánh giá Accuracy/FAR/FRR
  (`evaluate.py`).
- **Bảo mật:** dataset ảnh thật và model đã train **không được commit lên git**
  (xem `.gitignore` trong thư mục này) — đây là dữ liệu sinh trắc học cá nhân.
- Prototype này **chưa tích hợp** vào backend Spring Boot của TaskHub — đây là bước
  chứng minh ý tưởng (proof of concept) độc lập, dùng cho báo cáo/demo đồ án. Bước
  tích hợp thật (API endpoint nhận ảnh từ frontend, lưu embedding mã hoá vào DB,
  kết hợp với geofence hiện có) là phần mở rộng sau nếu đồ án yêu cầu.
