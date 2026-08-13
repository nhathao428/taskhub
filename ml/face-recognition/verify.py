"""
verify.py
Mo webcam, detect + trich xuat embedding khuon mat hien tai, so voi classifier da
train (models/classifier.pkl), in ten nguoi nhan dien duoc + do tin cay len man hinh.
Nhan 'q' de thoat.

Cach chay:
    python verify.py --model models/classifier.pkl --threshold 0.6
"""
from __future__ import annotations

import argparse

import cv2
import joblib
import numpy as np

from face_pipeline import FaceEmbedder, get_device


def main() -> None:
    parser = argparse.ArgumentParser(description="Xac thuc khuon mat qua webcam bang classifier da train")
    parser.add_argument("--model", default="models/classifier.pkl")
    parser.add_argument(
        "--threshold", type=float, default=0.6,
        help="Nguong do tin cay (0-1) - duoi nguong nay bao 'Unknown' thay vi nhan dien nham",
    )
    parser.add_argument("--camera-index", type=int, default=0)
    parser.add_argument("--cpu", action="store_true")
    args = parser.parse_args()

    bundle = joblib.load(args.model)
    clf = bundle["classifier"]
    encoder = bundle["label_encoder"]
    classifier_type = bundle.get("classifier_type", "svm")

    if not hasattr(clf, "predict_proba"):
        raise RuntimeError(
            "Classifier khong ho tro predict_proba. Dung train.py voi --classifier svm "
            "(kNN mac dinh trong train.py cung ho tro predict_proba qua sklearn, "
            "nhung neu doi sang loai khac can tu kiem tra lai)."
        )

    device = get_device(force_cpu=args.cpu)
    print(f"[info] Dung device: {device} | classifier: {classifier_type}")
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
                proba = clf.predict_proba(emb.reshape(1, -1))[0]
                best_idx = int(np.argmax(proba))
                confidence = float(proba[best_idx])
                if confidence >= args.threshold:
                    name = encoder.inverse_transform([best_idx])[0]
                    label_text = f"{name} ({confidence:.2f})"
                    color = (0, 200, 0)
                else:
                    label_text = f"Unknown ({confidence:.2f})"
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
