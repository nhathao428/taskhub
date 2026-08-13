"""
train.py
Buoc train: dataset/<ten_nguoi>/*.jpg -> detect+align (MTCNN) -> embedding
(InceptionResnetV1, transfer learning tu VGGFace2) -> train classifier nhe (SVM
hoac kNN) tren embedding -> luu models/classifier.pkl.

Cach chay (xem README.md de biet chi tiet tung buoc):
    python train.py --dataset-dir dataset --model-out models/classifier.pkl --classifier svm

Neu may khong co GPU hoac muon ep dung CPU:
    python train.py --cpu
"""
from __future__ import annotations

import argparse
from pathlib import Path

import joblib
import numpy as np
from sklearn.neighbors import KNeighborsClassifier
from sklearn.preprocessing import LabelEncoder
from sklearn.svm import SVC

from face_pipeline import FaceEmbedder, get_device, load_dataset_embeddings


def build_classifier(kind: str):
    """Tao classifier nhe train tren embedding 512 chieu (khong train lai mang CNN)."""
    if kind == "svm":
        # probability=True de co predict_proba - dung trong verify.py de hien do tin cay.
        return SVC(kernel="linear", probability=True, C=1.0)
    if kind == "knn":
        return KNeighborsClassifier(n_neighbors=3, metric="cosine", weights="distance")
    raise ValueError(f"Loai classifier khong ho tro: {kind}")


def main() -> None:
    parser = argparse.ArgumentParser(description="Train classifier nhan dien khuon mat tu dataset")
    parser.add_argument("--dataset-dir", default="dataset", help="Thu muc dataset/<ten_nguoi>/*.jpg")
    parser.add_argument("--model-out", default="models/classifier.pkl", help="Duong dan luu model")
    parser.add_argument("--classifier", choices=["svm", "knn"], default="svm")
    parser.add_argument("--cpu", action="store_true", help="Ep dung CPU thay vi GPU")
    args = parser.parse_args()

    device = get_device(force_cpu=args.cpu)
    print(f"[info] Dung device: {device}")

    embedder = FaceEmbedder(device=device)

    print(f"[info] Dang doc dataset tu '{args.dataset_dir}' va trich xuat embedding...")
    embeddings, labels = load_dataset_embeddings(args.dataset_dir, embedder)
    n_people = len(set(labels))
    print(f"[info] Trich xuat duoc {len(embeddings)} embedding tu {n_people} nguoi.")

    if n_people < 2:
        print(
            "[canh bao] Chi co 1 nguoi trong dataset. Classifier van train duoc nhung "
            "khong co y nghia phan biet nguoi la/quen cho toi khi co it nhat 2 nguoi "
            "(vd chup them tai khoan 'unknown' voi anh nguoi khac de test)."
        )

    encoder = LabelEncoder()
    y = encoder.fit_transform(labels)

    clf = build_classifier(args.classifier)
    clf.fit(embeddings, y)

    model_out = Path(args.model_out)
    model_out.parent.mkdir(parents=True, exist_ok=True)
    joblib.dump(
        {"classifier": clf, "label_encoder": encoder, "classifier_type": args.classifier},
        model_out,
    )
    print(f"[ok] Da luu model vao: {model_out}")

    # Luu ca embeddings + labels rieng de evaluate.py dung lai, khong phai trich xuat lai
    # tu dau (do detect+align+embed lai toan bo anh kha ton thoi gian).
    embeddings_out = model_out.parent / "embeddings.npz"
    np.savez(embeddings_out, embeddings=embeddings, labels=np.array(labels))
    print(f"[ok] Da luu embeddings de evaluate.py dung lai: {embeddings_out}")


if __name__ == "__main__":
    main()
