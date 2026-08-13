"""
face_pipeline.py
Module dung chung cho train.py / verify.py / evaluate.py: load model MTCNN
(detect + align khuon mat) + InceptionResnetV1 (trich xuat embedding 512 chieu),
va cac ham doc dataset theo cau truc thu muc dataset/<ten_nguoi>/*.jpg.

Tach rieng module nay de khong lap code load model o 3 file kia.
"""
from __future__ import annotations

from pathlib import Path
from typing import List, Optional, Tuple

import numpy as np

try:
    import torch
    from facenet_pytorch import MTCNN, InceptionResnetV1
except ImportError as exc:  # pragma: no cover - chi xay ra khi chua cai torch/facenet-pytorch
    raise ImportError(
        "Thieu torch/facenet-pytorch. Cai theo huong dan trong README.md "
        "(muc 'Buoc 2: Cai torch' va 'Buoc 3: Cai requirements.txt') truoc khi chay script nay."
    ) from exc

import cv2
from PIL import Image

VALID_IMAGE_EXT = {".jpg", ".jpeg", ".png"}


def get_device(force_cpu: bool = False) -> "torch.device":
    """Tra ve GPU (cuda) neu co va khong bi ep dung CPU, nguoc lai tra ve CPU."""
    if not force_cpu and torch.cuda.is_available():
        return torch.device("cuda")
    return torch.device("cpu")


class FaceEmbedder:
    """Boc goi MTCNN (detect + align) + InceptionResnetV1 (embedding 512 chieu, pretrained VGGFace2)."""

    def __init__(self, device: Optional["torch.device"] = None, min_face_size: int = 40):
        self.device = device or get_device()
        # MTCNN: phat hien + align khuon mat, tra ve tensor 160x160 da chuan hoa.
        # select_largest=True: neu 1 anh co nhieu mat, chi lay mat lon nhat (gan camera nhat)
        # - phu hop tinh huong check-in (1 nguoi dung truoc cam moi lan).
        self.mtcnn = MTCNN(
            image_size=160,
            margin=20,
            min_face_size=min_face_size,
            select_largest=True,
            post_process=True,
            device=self.device,
        )
        # InceptionResnetV1 pretrained tren VGGFace2 -> dung transfer learning (khong train
        # lai mang nay), chi dung no de trich embedding roi train classifier nhe o tren.
        self.resnet = InceptionResnetV1(pretrained="vggface2").eval().to(self.device)

    def embed_from_path(self, image_path: str) -> Optional[np.ndarray]:
        """Doc anh tu file, detect + align + embed. Tra None neu khong tim thay khuon mat."""
        img = Image.open(image_path).convert("RGB")
        return self.embed_from_pil(img)

    def embed_from_pil(self, img: "Image.Image") -> Optional[np.ndarray]:
        face_tensor = self.mtcnn(img)
        if face_tensor is None:
            return None
        return self._embed_tensor(face_tensor)

    def embed_from_bgr_frame(self, frame_bgr: np.ndarray) -> Optional[np.ndarray]:
        """Dung cho frame lay truc tiep tu OpenCV VideoCapture (dinh dang BGR)."""
        rgb = cv2.cvtColor(frame_bgr, cv2.COLOR_BGR2RGB)
        img = Image.fromarray(rgb)
        return self.embed_from_pil(img)

    def _embed_tensor(self, face_tensor: "torch.Tensor") -> np.ndarray:
        with torch.no_grad():
            emb = self.resnet(face_tensor.unsqueeze(0).to(self.device))
        return emb.squeeze(0).cpu().numpy()

    def detect_box(self, frame_bgr: np.ndarray) -> Optional[np.ndarray]:
        """Tra ve bounding box [x1, y1, x2, y2] cua khuon mat lon nhat trong frame, hoac None."""
        rgb = cv2.cvtColor(frame_bgr, cv2.COLOR_BGR2RGB)
        img = Image.fromarray(rgb)
        boxes, _ = self.mtcnn.detect(img)
        if boxes is None or len(boxes) == 0:
            return None
        return boxes[0]


def load_dataset_embeddings(
    dataset_dir: str, embedder: FaceEmbedder
) -> Tuple[np.ndarray, List[str]]:
    """
    Duyet dataset_dir/<ten_nguoi>/*.jpg (hoac .jpeg/.png), tra ve (embeddings, labels).
    embeddings: np.ndarray shape (N, 512). labels: list ten nguoi tuong ung tung dong.
    Bo qua anh khong detect duoc khuon mat (in canh bao ra console).
    """
    dataset_path = Path(dataset_dir)
    if not dataset_path.is_dir():
        raise FileNotFoundError(f"Khong tim thay thu muc dataset: {dataset_dir}")

    embeddings: List[np.ndarray] = []
    labels: List[str] = []

    for person_dir in sorted(dataset_path.iterdir()):
        if not person_dir.is_dir():
            continue
        person_name = person_dir.name
        image_paths = [p for p in sorted(person_dir.iterdir()) if p.suffix.lower() in VALID_IMAGE_EXT]
        if not image_paths:
            print(f"[canh bao] Khong co anh nao trong {person_dir}, bo qua.")
            continue

        for img_path in image_paths:
            emb = embedder.embed_from_path(str(img_path))
            if emb is None:
                print(f"[canh bao] Khong phat hien duoc khuon mat trong {img_path}, bo qua.")
                continue
            embeddings.append(emb)
            labels.append(person_name)

    if not embeddings:
        raise RuntimeError(
            "Khong trich xuat duoc embedding nao tu dataset. Kiem tra lai anh trong dataset/ "
            "(dung cau truc dataset/<ten_nguoi>/anh.jpg, va anh phai thay ro khuon mat)."
        )

    return np.stack(embeddings), labels


def cosine_similarity(a: np.ndarray, b: np.ndarray) -> float:
    """Cosine similarity giua 2 vector embedding. Tra ve gia tri trong khoang [-1, 1]."""
    a_norm = a / (np.linalg.norm(a) + 1e-10)
    b_norm = b / (np.linalg.norm(b) + 1e-10)
    return float(np.dot(a_norm, b_norm))
