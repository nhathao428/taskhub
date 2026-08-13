"""
enroll.py
"Dang ky" (enroll) khuon mat cho tung nguoi: doc dataset/<ten_nguoi>/*.jpg, detect+align
(MTCNN) + trich embedding (InceptionResnetV1) tung anh, roi lay TRUNG BINH (mean) cac
embedding cua moi nguoi lam 1 "embedding dai dien" duy nhat, luu vao enrolled_embeddings.pkl.

Day la EMBEDDING-BASED VERIFICATION (so khop 1:N bang cosine similarity) - THAY THE cho
cach cu (train 1 classifier SVM/kNN tren tap embedding, xem lich su file train.py da xoa).
Ly do doi thiet ke: xem muc "Vi sao doi thiet ke" trong README.md.

Cach chay (xem README.md de biet chi tiet tung buoc):
    python enroll.py --dataset-dir dataset --output models/enrolled_embeddings.pkl

Them 1 nguoi moi vao he thong: chup anh nguoi do (capture_faces.py) roi chay lai
enroll.py - KHONG can dung lai/xoa gi cua nhung nguoi cu, khong co buoc "train lai".
"""
from __future__ import annotations

import argparse
from pathlib import Path
from typing import Dict, List

import joblib
import numpy as np

from face_pipeline import FaceEmbedder, get_device, load_dataset_embeddings


def compute_mean_embeddings(embeddings: np.ndarray, labels: List[str]) -> Dict[str, np.ndarray]:
    """Gom embedding theo tung nguoi (theo labels), tra ve dict {ten_nguoi: embedding trung binh}."""
    by_label: Dict[str, List[np.ndarray]] = {}
    for emb, lbl in zip(embeddings, labels):
        by_label.setdefault(lbl, []).append(emb)

    enrolled: Dict[str, np.ndarray] = {}
    for lbl, embs in by_label.items():
        enrolled[lbl] = np.mean(np.stack(embs), axis=0)
    return enrolled


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Enroll khuon mat tu dataset -> embedding trung binh moi nguoi (khong train classifier)"
    )
    parser.add_argument("--dataset-dir", default="dataset")
    parser.add_argument("--output", default="models/enrolled_embeddings.pkl")
    parser.add_argument("--cpu", action="store_true", help="Ep dung CPU thay vi GPU")
    args = parser.parse_args()

    device = get_device(force_cpu=args.cpu)
    print(f"[info] Dung device: {device}")

    embedder = FaceEmbedder(device=device)

    print(f"[info] Dang doc dataset tu '{args.dataset_dir}' va trich xuat embedding...")
    embeddings, labels = load_dataset_embeddings(args.dataset_dir, embedder)
    n_people = len(set(labels))
    print(f"[info] Trich xuat duoc {len(embeddings)} embedding tu {n_people} nguoi.")

    enrolled = compute_mean_embeddings(embeddings, labels)
    for name in enrolled:
        count = labels.count(name)
        print(f"  - {name}: embedding dai dien tinh tu {count} anh")

    output_path = Path(args.output)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    joblib.dump({"enrolled": enrolled}, output_path)
    print(f"[ok] Da luu embedding dai dien vao: {output_path}")
    print(
        "[info] Them nguoi moi sau nay: chup anh nguoi do bang capture_faces.py roi "
        "chay lai enroll.py - khong can dong gi den nguoi cu."
    )


if __name__ == "__main__":
    main()
