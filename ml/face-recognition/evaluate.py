"""
evaluate.py
Danh gia he thong theo dung kieu VERIFICATION (1:N matching bang cosine similarity),
khop voi thiet ke moi cua enroll.py + verify.py (KHONG con classifier SVM/kNN):

  1. Chia dataset moi nguoi thanh 2 phan: "enroll" (dung tinh embedding dai dien -
     giong het enroll.py that lam) va "test" (dung de danh gia, khong dinh lieu gi
     den enroll).
  2. Identification accuracy (1:N): voi moi anh test, so voi TAT CA nguoi da enroll,
     chon nguoi giong nhat (best match) - dung khi best match la dung nguoi VA
     similarity >= threshold.
  3. Verification FAR / FRR / EER: voi moi anh test (nguoi that su la L) -
       - So voi embedding dai dien cua CHINH L (genuine) -> duoi threshold la
         FALSE REJECT (FRR) - dang la L nhung bi tu choi.
       - So voi embedding dai dien cua TUNG nguoi KHAC (impostor) -> tu threshold
         tro len la FALSE ACCEPT (FAR) - khac L nhung bi nhan nham la nguoi do.
     Quet nhieu nguong, in bang FAR/FRR + uoc luong EER (diem FAR ~= FRR).

LUU Y QUAN TRONG (ghi ro trong bao cao do an): dataset tu chup chi co rat it nguoi
(vd 1-2 nguoi, 3-5 anh/nguoi) - so lieu o day CHI mang tinh minh hoa quy trinh danh
gia (prototype), khong dai dien do chinh xac thuc te khi trien khai voi nhieu nhan
vien that. Can it nhat 2 nguoi trong dataset de FAR co y nghia (can nguoi "khac" de
so sanh) - neu dataset chi co 1 nguoi, phan FAR se bao "khong tinh duoc", nhung
phan FRR + identification accuracy van chay binh thuong.

Cach chay:
    python evaluate.py --dataset-dir dataset --test-ratio 0.3 --threshold 0.65
"""
from __future__ import annotations

import argparse
from collections import defaultdict
from typing import Dict, List, Tuple

import numpy as np

from enroll import compute_mean_embeddings
from face_pipeline import FaceEmbedder, cosine_similarity, get_device, load_dataset_embeddings


def split_enroll_test(
    embeddings: np.ndarray, labels: List[str], test_ratio: float, seed: int = 42
):
    """
    Chia embedding cua tung nguoi thanh 2 phan: enroll (tinh embedding dai dien) va
    test (danh gia). Moi nguoi luon giu lai IT NHAT 1 anh cho enroll; neu nguoi do
    chi co 1 anh, dua het vao enroll (canh bao, khong co anh test cho nguoi nay).
    """
    rng = np.random.default_rng(seed)
    by_label: Dict[str, List[np.ndarray]] = defaultdict(list)
    for emb, lbl in zip(embeddings, labels):
        by_label[lbl].append(emb)

    enroll_emb: List[np.ndarray] = []
    enroll_lbl: List[str] = []
    test_emb: List[np.ndarray] = []
    test_lbl: List[str] = []

    for lbl, embs in by_label.items():
        n = len(embs)
        if n < 2:
            print(f"[canh bao] '{lbl}' chi co {n} anh - dua het vao enroll, khong co anh test cho nguoi nay.")
            enroll_emb.extend(embs)
            enroll_lbl.extend([lbl] * n)
            continue

        idx = rng.permutation(n)
        n_test = max(1, round(n * test_ratio))
        n_test = min(n_test, n - 1)  # luon giu it nhat 1 anh lai cho enroll
        test_idx = set(idx[:n_test].tolist())

        for i, emb in enumerate(embs):
            if i in test_idx:
                test_emb.append(emb)
                test_lbl.append(lbl)
            else:
                enroll_emb.append(emb)
                enroll_lbl.append(lbl)

    enroll_emb_arr = np.array(enroll_emb) if enroll_emb else np.empty((0, embeddings.shape[1]))
    test_emb_arr = np.array(test_emb) if test_emb else np.empty((0, embeddings.shape[1]))
    return enroll_emb_arr, enroll_lbl, test_emb_arr, test_lbl


def evaluate_identification(
    test_emb: np.ndarray, test_lbl: List[str], enrolled: Dict[str, np.ndarray], threshold: float
):
    """Identification accuracy (1:N): voi moi anh test, chon nguoi enroll giong nhat."""
    if len(test_lbl) == 0 or not enrolled:
        print("[canh bao] Khong du du lieu de tinh identification accuracy.")
        return None

    correct = 0
    for emb, true_lbl in zip(test_emb, test_lbl):
        best_name, best_sim = None, -1.0
        for name, ref_emb in enrolled.items():
            sim = cosine_similarity(emb, ref_emb)
            if sim > best_sim:
                best_name, best_sim = name, sim
        if best_name == true_lbl and best_sim >= threshold:
            correct += 1

    accuracy = correct / len(test_lbl)
    print(f"[identification accuracy] {accuracy:.2%} ({correct}/{len(test_lbl)} anh test, nguong={threshold:.2f})")
    return accuracy


def evaluate_far_frr(
    test_emb: np.ndarray, test_lbl: List[str], enrolled: Dict[str, np.ndarray], thresholds: List[float]
):
    """
    Voi moi anh test (nguoi that su = L): so voi embedding dai dien CUA CHINH L ->
    genuine score (dung cho FRR); so voi embedding dai dien CUA TUNG nguoi KHAC ->
    impostor score (dung cho FAR).
    """
    genuine_scores: List[float] = []
    impostor_scores: List[float] = []

    for emb, true_lbl in zip(test_emb, test_lbl):
        for name, ref_emb in enrolled.items():
            sim = cosine_similarity(emb, ref_emb)
            if name == true_lbl:
                genuine_scores.append(sim)
            else:
                impostor_scores.append(sim)

    genuine_arr = np.array(genuine_scores)
    impostor_arr = np.array(impostor_scores)

    if len(genuine_arr) == 0:
        print("[canh bao] Khong co sample genuine nao (nguoi test khong nam trong enrolled) - bo qua FRR.")
    if len(impostor_arr) == 0:
        print("[canh bao] Khong co sample impostor nao (chi co 1 nguoi trong enrolled) - bo qua FAR.")
    if len(genuine_arr) == 0 and len(impostor_arr) == 0:
        return None

    print(f"\n[info] So sample genuine: {len(genuine_arr)} | so sample impostor: {len(impostor_arr)}")
    print(f"{'Nguong':>8} | {'FAR':>8} | {'FRR':>8}")
    print("-" * 32)

    results: List[Tuple[float, float, float]] = []
    for t in thresholds:
        far = float(np.mean(impostor_arr >= t)) if len(impostor_arr) else float("nan")
        frr = float(np.mean(genuine_arr < t)) if len(genuine_arr) else float("nan")
        results.append((t, far, frr))
        far_s = f"{far:8.2%}" if not np.isnan(far) else "     N/A"
        frr_s = f"{frr:8.2%}" if not np.isnan(frr) else "     N/A"
        print(f"{t:8.2f} | {far_s} | {frr_s}")

    valid_results = [r for r in results if not (np.isnan(r[1]) or np.isnan(r[2]))]
    if valid_results:
        eer_t, eer_far, eer_frr = min(valid_results, key=lambda r: abs(r[1] - r[2]))
        print(
            f"\n[EER xap xi] nguong={eer_t:.2f} | FAR={eer_far:.2%} | FRR={eer_frr:.2%} "
            f"(EER ~ {(eer_far + eer_frr) / 2:.2%})"
        )
    return results


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Danh gia verification: identification accuracy + FAR/FRR/EER"
    )
    parser.add_argument("--dataset-dir", default="dataset")
    parser.add_argument("--test-ratio", type=float, default=0.3)
    parser.add_argument("--threshold", type=float, default=0.65, help="Nguong dung cho identification accuracy")
    parser.add_argument("--cpu", action="store_true")
    args = parser.parse_args()

    device = get_device(force_cpu=args.cpu)
    print(f"[info] Dung device: {device}")
    embedder = FaceEmbedder(device=device)

    print(f"[info] Dang doc dataset tu '{args.dataset_dir}' va trich xuat embedding...")
    embeddings, labels = load_dataset_embeddings(args.dataset_dir, embedder)
    print(f"[info] Trich xuat duoc {len(embeddings)} embedding tu {len(set(labels))} nguoi.\n")

    enroll_emb, enroll_lbl, test_emb, test_lbl = split_enroll_test(embeddings, labels, args.test_ratio)
    enrolled = compute_mean_embeddings(enroll_emb, enroll_lbl)
    print(f"[info] Enroll tu {len(enroll_lbl)} anh -> {len(enrolled)} nguoi. Test tren {len(test_lbl)} anh.\n")

    print("=" * 60)
    print("PHAN 1: Identification accuracy (1:N, chon nguoi giong nhat)")
    print("=" * 60)
    evaluate_identification(test_emb, test_lbl, enrolled, args.threshold)

    print("\n" + "=" * 60)
    print("PHAN 2: Verification FAR / FRR / EER (quet nguong cosine similarity)")
    print("=" * 60)
    thresholds = [round(t, 2) for t in np.arange(0.3, 0.95, 0.05)]
    evaluate_far_frr(test_emb, test_lbl, enrolled, thresholds)


if __name__ == "__main__":
    main()
