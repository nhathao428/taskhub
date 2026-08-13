# Face Recognition Prototype — TaskHub (đồ án chuyên ngành)

Prototype nhận diện khuôn mặt để nâng cấp module chấm công TaskHub. Dùng transfer
learning (không tự train mạng CNN từ đầu) + self-collected data, theo đúng hướng đã
chốt trong đề tài chuyên ngành.

**Quan trọng:** code này **chưa chạy thử được bằng torch thật** trong sandbox dựng
code (mạng sandbox chặn cả PyPI lẫn `download.pytorch.org` khi tải gói CUDA đầy đủ).
Cú pháp Python đã kiểm tra bằng `py_compile` (không lỗi cú pháp), và toàn bộ phần
logic thuần Python (cosine similarity, tính embedding trung bình, chia enroll/test,
tính FAR/FRR...) đã test bằng dữ liệu giả lập — nhưng **Hào cần tự chạy trên máy có
GPU thật (laptop GTX 1650) để xác nhận với ảnh mặt thật và lấy số liệu cho báo cáo.**
Làm theo đúng thứ tự các bước dưới đây.

---

## Vì sao đổi thiết kế (embedding-based verification, không dùng classifier)

Bản đầu tiên của prototype này dùng cách **train 1 classifier (SVM/kNN)** trên tập
embedding — giống bài toán phân loại ảnh thông thường. Cách đó có 2 vấn đề thực tế:

- Cần khá nhiều ảnh mỗi người (20-30) để classifier học được ranh giới phân biệt.
- **Thêm 1 nhân viên mới = phải train lại toàn bộ model** từ đầu (baseline model
  không "nhớ" được người mới nếu không train lại) — không hợp lý cho 1 hệ thống
  chấm công thực tế, nơi nhân viên vào/ra liên tục.

Bản hiện tại đổi sang **embedding-based verification (so khớp 1:N bằng cosine
similarity)** — đúng cách Face ID và phần lớn hệ thống chấm công/xác thực khuôn mặt
thực tế làm:

1. Mỗi người chỉ cần **3-5 ảnh** để tính ra 1 "embedding đại diện" (trung bình cộng
   các embedding từ vài ảnh — xem `enroll.py`).
2. Lúc xác thực (`verify.py`): trích embedding của ảnh/khung hình mới, so cosine
   similarity với **từng người đã enroll**, chọn người giống nhất — nếu độ giống đủ
   cao (vượt ngưỡng) thì nhận diện, không thì báo "Unknown".
3. **Thêm người mới = chụp ảnh + chạy `enroll.py` lại** (chỉ tính thêm embedding cho
   người mới, không đụng đến người cũ, không có khái niệm "train lại mô hình").

Đánh đổi: cách này không "học" được ranh giới phân biệt tinh vi như classifier, độ
chính xác phụ thuộc hoàn toàn vào chất lượng embedding của mạng pretrained
(`InceptionResnetV1`/VGGFace2) — nhưng đây cũng chính là lý do dùng transfer learning
từ 1 mạng đã pretrain tốt, và là cách tiếp cận chuẩn công nghiệp cho bài toán "nhận
diện danh tính từ tập người dùng thay đổi liên tục" như chấm công.

---

## Cấu trúc thư mục

```
ml/face-recognition/
├── requirements.txt      # Danh sách thư viện (trừ torch — cài riêng, xem Bước 2)
├── face_pipeline.py       # Module dùng chung: load MTCNN + InceptionResnetV1, đọc dataset
├── capture_faces.py       # Chụp ảnh khuôn mặt từ webcam -> dataset/<tên>/*.jpg
├── enroll.py                # Tính embedding trung bình mỗi người -> models/enrolled_embeddings.pkl
├── verify.py                 # Test nhận diện qua webcam bằng so khớp cosine similarity (1:N)
├── evaluate.py                 # Tính Identification Accuracy / FAR / FRR / EER
├── liveness.py                   # Test chống giả mạo bằng phát hiện chớp mắt (EAR)
├── dataset/                       # Ảnh khuôn mặt tự chụp (KHÔNG commit lên git — xem .gitignore)
└── models/                         # enrolled_embeddings.pkl (KHÔNG commit lên git)
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

**QUAN TRỌNG — phải ghim đúng version:** `facenet-pytorch==2.6.0` yêu cầu chặt
`torch>=2.2.0,<2.3.0` và `torchvision>=0.17.0,<0.18.0`. Nếu cài torch **không ghim
version** (chỉ gõ `pip install torch torchvision`), pip sẽ tự lấy bản mới nhất
(vd 2.13.x) — báo lỗi xung đột dependency ngay khi cài `facenet-pytorch` ở Bước 3.
Các lệnh dưới đây đã ghim đúng `torch==2.2.2` + `torchvision==0.17.2` (cặp version
tương thích nhau, nằm trong khoảng facenet-pytorch cho phép) — copy nguyên văn,
không tự ý bỏ phần `==...`.

Laptop của Hào có **GTX 1650** (hỗ trợ CUDA) — nên cài bản CUDA để enroll/verify
nhanh hơn CPU nhiều lần (đặc biệt lúc test webcam realtime).

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
   pip install torch==2.2.2 torchvision==0.17.2 --index-url https://download.pytorch.org/whl/cu121
   ```
   Nếu lệnh trên báo lỗi không tìm thấy bản phù hợp, thử bản CUDA 11.8 (cũ hơn,
   tương thích rộng hơn):
   ```bash
   pip install torch==2.2.2 torchvision==0.17.2 --index-url https://download.pytorch.org/whl/cu118
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
pip install torch==2.2.2 torchvision==0.17.2 --index-url https://download.pytorch.org/whl/cpu
```
Toàn bộ script (`enroll.py`, `verify.py`, `evaluate.py`) đều có cờ `--cpu` để ép
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
python capture_faces.py --name hao --num-images 5
```

- Cửa sổ webcam hiện lên, khung xanh là vị trí đang thấy mặt.
- Nhấn **SPACE** để chụp 1 ảnh, nhấn **q** để dừng sớm.
- **3-5 ảnh là đủ** (khác cách classifier cũ cần 20-30 ảnh — xem lý do ở mục "Vì
  sao đổi thiết kế" phía trên). Nếu có thể, đổi nhẹ giữa các lần chụp:
  - Góc mặt: chính diện, nghiêng trái/phải nhẹ
  - Ánh sáng: 1-2 ảnh sáng, 1-2 ảnh hơi tối hơn (mô phỏng điều kiện check-in thực
    tế — sáng sớm, đèn phòng)

Ảnh lưu vào `dataset/hao/000.jpg`, `001.jpg`, ...

**Muốn test phân biệt người lạ/quen:** chụp thêm 1 người khác (bạn cùng phòng,
người thân — có xin phép) với tên khác, ví dụ:
```bash
python capture_faces.py --name ban_a --num-images 5
```
Không bắt buộc cho tính năng verify chính (verify chỉ cần 1 người enroll là chạy
được), nhưng nếu chỉ có 1 người trong dataset thì `evaluate.py` (Bước 7) sẽ không
tính được FAR (cần ít nhất 2 người để có ảnh "khác người" so sánh).

---

## Bước 5: Enroll (đăng ký) khuôn mặt (`enroll.py`)

```bash
python enroll.py --dataset-dir dataset --output models/enrolled_embeddings.pkl
```

Script sẽ:
1. Đọc từng ảnh trong `dataset/<tên>/`, detect + align khuôn mặt bằng MTCNN.
2. Trích embedding 512 chiều bằng `InceptionResnetV1` (pretrained VGGFace2 —
   transfer learning, không train lại mạng này).
3. **Lấy trung bình (mean)** các embedding của mỗi người thành 1 embedding đại diện
   duy nhất (KHÔNG train classifier).
4. Lưu tất cả vào `models/enrolled_embeddings.pkl` — 1 dict `{tên_người: embedding}`.

Nếu máy không có GPU hoặc muốn ép CPU: thêm `--cpu`.

Nếu có ảnh không detect được mặt (ảnh mờ, quá tối, không rõ mặt), script sẽ in
cảnh báo và bỏ qua ảnh đó — không dừng chương trình.

**Thêm người mới sau này:** chụp ảnh người đó bằng `capture_faces.py` rồi chạy lại
đúng lệnh `enroll.py` ở trên — script tự đọc lại toàn bộ `dataset/` và ghi đè
`enrolled_embeddings.pkl` mới, không cần thao tác gì thêm với người cũ.

---

## Bước 6: Test nhận diện qua webcam (`verify.py`)

```bash
python verify.py --enrolled models/enrolled_embeddings.pkl --threshold 0.65
```

- Đứng trước webcam, khung màu quanh mặt hiện tên người giống nhất + độ tin cậy
  (cosine similarity, 0-1).
- Xanh lá = nhận diện được (similarity ≥ threshold), cam = "Unknown" (dưới
  threshold — coi như người lạ, tránh nhận nhầm).
- Nhấn **q** để thoát.

**Cách chọn `--threshold`:** với embedding của `InceptionResnetV1` pretrained trên
VGGFace2, cosine similarity giữa 2 ảnh **cùng 1 người** thường rơi vào khoảng
0.7-1.0, còn giữa **2 người khác nhau** thường dưới 0.4-0.5 — vùng 0.6-0.7 là mốc
khởi điểm hợp lý để cân bằng. Ngưỡng càng cao → càng khó bị nhận nhầm người khác
(FAR thấp) nhưng càng dễ từ chối nhầm chính chủ (FRR cao), và ngược lại. Dùng
`evaluate.py` ở Bước 7 để xem bảng FAR/FRR thực tế trên dữ liệu của Hào rồi tinh
chỉnh lại threshold cho phù hợp (chấm công nên ưu tiên threshold hơi cao để giảm
rủi ro chấm công hộ/giả mạo).

---

## Bước 7: Đánh giá Identification Accuracy / FAR / FRR (`evaluate.py`)

```bash
python evaluate.py --dataset-dir dataset --test-ratio 0.3 --threshold 0.65
```

Script tự chia ảnh mỗi người thành 2 phần: một phần dùng "enroll" (tính embedding
đại diện, giống hệt `enroll.py` thật làm), phần còn lại dùng để "test" (không liên
quan gì đến enroll) — mô phỏng đúng tình huống thực tế: enroll trước, sau đó có ảnh
mới đưa vào xác thực. In ra 2 phần:

1. **Identification Accuracy (1:N)** — với mỗi ảnh test, so với TẤT CẢ người đã
   enroll, chọn người giống nhất; tính đúng nếu chọn đúng người VÀ similarity vượt
   threshold.
2. **Verification FAR / FRR / EER** — với mỗi ảnh test (người thật sự là L): so với
   embedding đại diện của chính L (genuine — dưới threshold là **FRR**, từ chối
   nhầm chính chủ), và so với embedding đại diện của từng người KHÁC (impostor — từ
   threshold trở lên là **FAR**, nhận nhầm người khác). Quét nhiều ngưỡng, in bảng
   FAR/FRR mỗi ngưỡng + ước lượng điểm EER (nơi FAR ≈ FRR).

**Lưu ý viết vào báo cáo:** dataset tự chụp chỉ có 1-2 người, 3-5 ảnh/người — số
liệu ở đây mang tính minh hoạ quy trình đánh giá (prototype), không đại diện độ
chính xác thực tế khi triển khai với hàng chục/hàng trăm nhân viên thật — đây là
hạn chế cần nêu rõ, không nên trình bày như kết quả cuối cùng.

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

pip install torch==2.2.2 torchvision==0.17.2 --index-url https://download.pytorch.org/whl/cu121
pip install -r requirements.txt

python capture_faces.py --name hao --num-images 5
python enroll.py --dataset-dir dataset --output models/enrolled_embeddings.pkl
python verify.py --enrolled models/enrolled_embeddings.pkl
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
| `enroll.py` báo "Khong phat hien duoc khuon mat" nhiều ảnh | Ảnh quá mờ/tối/xa — chụp lại gần hơn, đủ sáng hơn ở Bước 4 |
| `verify.py` luôn báo "Unknown" dù đúng người | Threshold đang quá cao, hoặc `enrolled_embeddings.pkl` enroll từ ảnh chất lượng kém — thử giảm `--threshold` xuống 0.55-0.6, hoặc chụp/enroll lại ảnh rõ hơn |
| `evaluate.py` không tính được FAR | Dataset chỉ có 1 người — cần ít nhất 2 người (xem gợi ý ở Bước 4) |
| Enroll/verify rất chậm dù có GPU | Kiểm tra lại log `[info] Dung device: ...` khi chạy — nếu in ra `cpu` dù có GPU, xem lại Bước 2 |

---

## Ghi chú cho báo cáo đồ án

- **Kỹ thuật AI dùng:** transfer learning — MTCNN (detect+align) + InceptionResnetV1
  pretrained trên VGGFace2 (trích embedding 512 chiều), **không** train lại mạng
  CNN từ đầu.
- **Phương pháp nhận diện:** embedding-based verification (so khớp 1:N bằng cosine
  similarity với embedding trung bình mỗi người) — **không** dùng classifier
  SVM/kNN. Đây là cách tiếp cận chuẩn công nghiệp (tương tự Face ID) cho bài toán
  danh sách người dùng thay đổi liên tục, ưu điểm chính: chỉ cần vài ảnh/người, thêm
  người mới không cần huấn luyện lại. Xem mục "Vì sao đổi thiết kế" đầu file để có
  nội dung so sánh đưa vào báo cáo.
- **Phần tự làm/tự thu thập:** dataset tự thu thập (`capture_faces.py`), quy trình
  enroll + tính embedding đại diện tự viết (`enroll.py`), tự đánh giá
  Identification Accuracy/FAR/FRR (`evaluate.py`), khung liveness detection tự viết
  (`liveness.py`).
- **Bảo mật:** dataset ảnh thật và embedding đã enroll **không được commit lên git**
  (xem `.gitignore` trong thư mục này) — đây là dữ liệu sinh trắc học cá nhân.
- Prototype này **chưa tích hợp** vào backend Spring Boot của TaskHub — đây là bước
  chứng minh ý tưởng (proof of concept) độc lập, dùng cho báo cáo/demo đồ án. Bước
  tích hợp thật (API endpoint nhận ảnh từ frontend, lưu embedding mã hoá vào DB,
  kết hợp với geofence hiện có) là phần mở rộng sau nếu đồ án yêu cầu.
