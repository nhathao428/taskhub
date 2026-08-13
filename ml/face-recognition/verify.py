"""
verify.py
Mo webcam, detect + trich embedding khuon mat hien tai, so cosine similarity voi
TUNG embedding da enroll (models/enrolled_embeddings.pkl - xem enroll.py), chon
nguoi co similarity CAO NHAT. Neu similarity >= threshold: nhan dien la nguoi do.
Neu duoi threshold: "Unknown" (khong khop ai du tin cay).

Day la kieu 1:N VERIFICATION - KHAC voi cach classifier truoc day: khong can co
buoc "train", them nguoi moi chi can enroll them (xem enroll.py), khong anh huong
gi nguoi da co san. Nhan 'q' de thoat.

Cach chay:
    python verify.py --enrolled models/enrolled_embeddings.pkl --threshold 0.65
"""
from __future__ import annotations

import argparse
from typing import Dict, Optional, Tuple

import cv2
import joblib
import numpy as np

from face_pipeline import FaceEmbedder, cosine_similarity, get_device


def best_match(embedding: np.ndarray, enrolled: Dict[str, np.ndarray]) -> Tuple[Optional[str], float]:
    """So embedding voi tung nguoi da enroll, tra ve (ten_giong_nhat, similarity_cao_nhat)."""
    if not enrolled:
        return None, -1.0
    best_name: Optional[str] = None
    best_sim = -1.0
    for name, ref_emb in enrolled.items():
        sim = cosine_similarity(embedding, ref_emb)
        if sim > best_sim:
            best_name, best_sim = name, sim
    return best_name, best_sim


def main() -> None:
    parser = argparse.ArgumentParser(description="Xac thuc khuon mat qua webcam bang embedding da enroll (1:N)")
    parser.add_argument("--enrolled", default="models/enrolled_embeddings.pkl")
    parser.add_argument(
        "--threshold", type=float, default=0.65,
        help="Nguong cosine similarity (~0.6-0.7 voi InceptionResnetV1/VGGFace2) - "
             "duoi nguong bao 'Unknown' thay vi nhan dien nham. Xem README.md de biet cach chinh.",
    )
    parser.add_argument("--camera-index", type=int, default=0)
    parser.add_argument("--cpu", action="store_true")
    args = parser.parse_args()

    bundle = joblib.load(args.enrolled)
    enrolled: Dict[str, np.ndarray] = bundle["enrolled"]
    if not enrolled:
        raise RuntimeError(
            f"File '{args.enrolled}' khong co nguoi nao da enroll. Chay enroll.py truoc."
        )
    print(f"[info] Da nap {len(enrolled)} nguoi da enroll: {', '.join(enrolled.keys())}")

    device = get_device(force_cpu=args.cpu)
    print(f"[info] Dung device: {device}")
    embedder = FaceEmbedder(device=device)

    cap = cv2.VideoCapture(args.camera_index)
    if not cap.isOpened():
        raise RuntimeError(f"Khong mo duoc webcam index {args.camera_index}")

    print("[info] Nhan 'q' de thoat.")
    while True:
        ok, frame = cap.read()
        if not ok:
            print("[loi] Khong doc duoc frame tu webcam.")
            break

        box = embedder.detect_box(frame)
        label_text = "Khong thay khuon mat"
        color = (0, 0, 255)

        if box is not None:
            emb = embedder.embed_from_bgr_frame(frame)
            if emb is not None:
                name, sim = best_match(emb, enrolled)
                if name is not None and sim >= args.threshold:
                    label_text = f"{name} ({sim:.2f})"
                    color = (0, 200, 0)
                else:
                    label_text = f"Unknown ({sim:.2f})"
                    color = (0, 165, 255)

            x1, y1, x2, y2 = [int(v) for v in box]
            cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
            cv2.putText(
                frame, label_text, (x1, max(y1 - 10, 10)),
                cv2.FONT_HERSHEY_SIMPLEX, 0.7, color, 2,
            )
        else:
            cv2.putText(frame, label_text, (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, color, 2)

        cv2.imshow("verify.py - nhan q de thoat", frame)
        if cv2.waitKey(1) & 0xFF == ord("q"):
            break

    cap.release()
    cv2.destroyAllWindows()


if __name__ == "__main__":
    main()
