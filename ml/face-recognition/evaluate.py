"""
evaluate.py
Danh gia model tren dataset da chup, gom 2 phan:

  1. Closed-set classification accuracy: chia dataset train/test theo tung nguoi,
     train lai 1 classifier tren phan train, danh gia accuracy tren phan test.
  2. Verification FAR / FRR / EER: sinh cap "cung nguoi" (genuine) va "khac nguoi"
     (impostor) tu toan bo embedding, quet qua danh sach nguong cosine similarity,
     tinh:
       - FAR (False Accept Rate): ty le cap KHAC nguoi nhung bi cham la giong nhau
       - FRR (False Reject Rate): ty le cap CUNG nguoi nhung bi cham la khac nhau
       - EER (Equal Error Rate): diem nguong ma FAR ~= FRR, cang thap cang tot

LUU Y QUAN TRONG (ghi ro trong bao cao do an): dataset thu tay chi co anh cua rat it
nguoi (vd 1-2 nguoi tu chup), khac xa quy mo benchmark hoc thuat (LFW co hang nghin
nguoi, hang chuc nghin anh). Ket qua o day CHI mang tinh minh hoa quy trinh danh gia
(prototype), khong dai dien do chinh xac thuc te khi trien khai voi nhieu nhan vien.

Cach chay:
    python evaluate.py --dataset-dir dataset --test-size 0.3
"""
from __future__ import annotations

import argparse
from collections import defaultdict
from typing import List, Tuple

import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder

from face_pipeline import FaceEmbedder, cosine_similarity, get_device, load_dataset_embeddings
from train import build_classifier


def split_train_test(embeddings: np.ndarray, labels: List[str], test_size: float):
    """Chia train/test theo tung nguoi (stratify thu cong) de moi nguoi co mat o ca 2 phan."""
    by_label = defaultdict(list)
    for emb, lbl in zip(embeddings, labels):
        by_label[lbl].append(emb)

    train_emb, train_lbl, test_emb, test_lbl = [], [], [], []
    for lbl, embs in by_label.items():
        if len(embs) < 2:
            # Khong du anh de chia - dua het vao train, canh bao cho nguoi dung biet.
            print(f"[canh bao] '{lbl}' chi co {len(embs)} anh, khong chia test duoc, dua het vao train.")
            train_emb.extend(embs)
            train_lbl.extend([lbl] * len(embs))
            continue
        tr, te = train_test_split(embs, test_size=test_size, random_state=42)
        train_emb.extend(tr)
        train_lbl.extend([lbl] * len(tr))
        test_emb.extend(te)
        test_lbl.extend([lbl] * len(te))

    train_emb_arr = np.array(train_emb)
    test_emb_arr = np.array(test_emb) if test_emb else np.empty((0, embeddings.shape[1]))
    return train_emb_arr, train_lbl, test_emb_arr, test_lbl


def evaluate_classification(train_emb, train_lbl, test_emb, test_lbl, classifier_kind: str):
    if len(test_lbl) == 0:
        print("[canh bao] Khong co du lieu test (moi nguoi qua it anh) - bo qua danh gia accuracy phan loai.")
        return None

    encoder = LabelEncoder()
    encoder.fit(train_lbl + test_lbl)
    y_train = encoder.transform(train_lbl)
    y_test = encoder.transform(test_lbl)

    clf = build_classifier(classifier_kind)
    clf.fit(train_emb, y_train)
    y_pred = clf.predict(test_emb)

    accuracy = float(np.mean(y_pred == y_test))
    print(f"[accuracy] Closed-set classification accuracy: {accuracy:.2%} ({len(test_lbl)} anh test)")
    return accuracy


def evaluate_verification(
    embeddings: np.ndarray, labels: List[str], thresholds: List[float]
):
    """
    Sinh cap genuine (cung nguoi) + impostor (khac nguoi) tu toan bo embedding, quet
    qua danh sach nguong cosine similarity, tinh FAR/FRR moi nguong + uoc luong EER.
    """
    n = len(embeddings)
    genuine_scores: List[float] = []
    impostor_scores: List[float] = []

    for i in range(n):
        for j in range(i + 1, n):
            sim = cosine_similarity(embeddings[i], embeddings[j])
            if labels[i] == labels[j]:
                genuine_scores.append(sim)
            else:
                impostor_scores.append(sim)

    genuine_arr = np.array(genuine_scores)
    impostor_arr = np.array(impostor_scores)

    if len(genuine_arr) == 0 or len(impostor_arr) == 0:
        print(
            "[canh bao] Khong du du lieu de tinh FAR/FRR (can it nhat 2 nguoi, moi "
            "nguoi it nhat 2 anh trong dataset). Bo qua phan nay."
        )
        return None

    print(
        f"\n[info] So cap genuine (cung nguoi): {len(genuine_arr)} | "
        f"so cap impostor (khac nguoi): {len(impostor_arr)}"
    )
    print(f"{'Nguong':>8} | {'FAR':>8} | {'FRR':>8}")
    print("-" * 32)

    results: List[Tuple[float, float, float]] = []
    for t in thresholds:
        far = float(np.mean(impostor_arr >= t))  # khac nguoi nhung bi cham giong -> chap nhan sai
        frr = float(np.mean(genuine_arr < t))     # cung nguoi nhung bi cham khac -> tu choi sai
        results.append((t, far, frr))
        print(f"{t:8.2f} | {far:8.2%} | {frr:8.2%}")

    # EER xap xi: diem nguong co |FAR - FRR| nho nhat trong danh sach da quet.
    eer_t, eer_far, eer_frr = min(results, key=lambda r: abs(r[1] - r[2]))
    print(
        f"\n[EER xap xi] nguong={eer_t:.2f} | FAR={eer_far:.2%} | FRR={eer_frr:.2%} "
        f"(EER ~ {(eer_far + eer_frr) / 2:.2%})"
    )
    return results


def main() -> None:
    parser = argparse.ArgumentParser(description="Danh gia accuracy / FAR / FRR tren dataset")
    parser.add_argument("--dataset-dir", default="dataset")
    parser.add_argument("--classifier", choices=["svm", "knn"], default="svm")
    parser.add_argument("--test-size", type=float, default=0.3)
    parser.add_argument("--cpu", action="store_true")
    args = parser.parse_args()

    device = get_device(force_cpu=args.cpu)
    print(f"[info] Dung device: {device}")
    embedder = FaceEmbedder(device=device)

    print(f"[info] Dang doc dataset tu '{args.dataset_dir}' va trich xuat embedding...")
    embeddings, labels = load_dataset_embeddings(args.dataset_dir, embedder)
    print(f"[info] Trich xuat duoc {len(embeddings)} embedding tu {len(set(labels))} nguoi.\n")

    print("=" * 60)
    print("PHAN 1: Closed-set classification accuracy (train/test split)")
    print("=" * 60)
    train_emb, train_lbl, test_emb, test_lbl = split_train_test(embeddings, labels, args.test_size)
    evaluate_classification(train_emb, train_lbl, test_emb, test_lbl, args.classifier)

    print("\n" + "=" * 60)
    print("PHAN 2: Verification FAR / FRR / EER (quet nguong cosine similarity)")
    print("=" * 60)
    thresholds = [round(t, 2) for t in np.arange(0.3, 0.95, 0.05)]
    evaluate_verification(embeddings, labels, thresholds)


if __name__ == "__main__":
    main()
