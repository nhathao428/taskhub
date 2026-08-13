"""
liveness.py
Khung chong gia mao co ban (baseline liveness detection): dung MediaPipe Face Mesh
de lay landmark mat, tinh Eye Aspect Ratio (EAR) qua nhieu frame lien tiep de phat
hien chop mat - phan biet nguoi that dung truoc cam vs anh tinh/anh in (khong the
tu chop mat).

LUU Y QUAN TRONG (ghi ro trong bao cao do an): day CHI la khung co ban, khong phai
giai phap chong gia mao hoan chinh. Cac kieu gia mao tinh vi hon (video replay tren
man hinh dien thoai/may tinh, mat na 3D in...) can them ky thuat khac: phan tich
texture/tan so anh, phat hien mo hinh phan xa anh sang man hinh, hoac dung model
chuyen dung (vd Silent-Face-Anti-Spoofing). Prototype nay dung lam buoc dau chung
minh y tuong tich hop liveness vao luong check-in.

Cach chay doc lap (test thu chop mat):
    python liveness.py --timeout 10
"""
from __future__ import annotations

import argparse
import time
from collections import deque

import cv2
import mediapipe as mp
import numpy as np

# Landmark index cua MediaPipe Face Mesh (468 diem) cho 6 diem quanh moi mat, theo
# thu tu chuan de tinh EAR kieu dlib: [goc trong, tren-1, tren-2, goc ngoai, duoi-2, duoi-1]
LEFT_EYE_IDX = [362, 385, 387, 263, 373, 380]
RIGHT_EYE_IDX = [33, 160, 158, 133, 153, 144]

EAR_BLINK_THRESHOLD = 0.21  # EAR duoi nguong nay coi nhu dang nham mat
EAR_CONSEC_FRAMES = 2  # so frame lien tiep phai duoi nguong moi tinh la 1 lan chop that (chong nhieu)


def _euclid(p1, p2) -> float:
    return float(np.linalg.norm(np.array(p1) - np.array(p2)))


def eye_aspect_ratio(landmarks, eye_idx, frame_w: int, frame_h: int) -> float:
    """Tinh EAR cho 1 mat tu 6 landmark. landmarks: face_landmarks.landmark cua MediaPipe."""
    pts = [(landmarks[i].x * frame_w, landmarks[i].y * frame_h) for i in eye_idx]
    p1, p2, p3, p4, p5, p6 = pts
    vertical_1 = _euclid(p2, p6)
    vertical_2 = _euclid(p3, p5)
    horizontal = _euclid(p1, p4)
    if horizontal == 0:
        return 0.0
    return (vertical_1 + vertical_2) / (2.0 * horizontal)


def check_liveness(timeout_sec: float = 10.0, camera_index: int = 0, show_window: bool = True) -> bool:
    """
    Mo webcam trong toi da timeout_sec giay, tra ve True neu phat hien duoc it nhat
    1 lan chop mat that (dau hieu nguoi that dang dung truoc camera), False neu het
    thoi gian ma khong thay chop mat nao (nghi ngo la anh tinh / gia mao).
    """
    mp_face_mesh = mp.solutions.face_mesh
    cap = cv2.VideoCapture(camera_index)
    if not cap.isOpened():
        raise RuntimeError(f"Khong mo duoc webcam index {camera_index}")

    blink_detected = False
    consec_below = 0
    ear_history: deque = deque(maxlen=5)
    start_time = time.time()

    with mp_face_mesh.FaceMesh(
        max_num_faces=1,
        refine_landmarks=True,
        min_detection_confidence=0.5,
        min_tracking_confidence=0.5,
    ) as face_mesh:
        while time.time() - start_time < timeout_sec:
            ok, frame = cap.read()
            if not ok:
                break

            h, w = frame.shape[:2]
            rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
            results = face_mesh.process(rgb)

            status_text = "Dang tim khuon mat..."
            color = (0, 165, 255)

            if results.multi_face_landmarks:
                landmarks = results.multi_face_landmarks[0].landmark
                left_ear = eye_aspect_ratio(landmarks, LEFT_EYE_IDX, w, h)
                right_ear = eye_aspect_ratio(landmarks, RIGHT_EYE_IDX, w, h)
                ear = (left_ear + right_ear) / 2.0
                ear_history.append(ear)

                if ear < EAR_BLINK_THRESHOLD:
                    consec_below += 1
                else:
                    if consec_below >= EAR_CONSEC_FRAMES:
                        blink_detected = True
                    consec_below = 0

                status_text = f"EAR={ear:.3f} | Da chop mat: {'CO' if blink_detected else 'CHUA'}"
                color = (0, 200, 0) if blink_detected else (0, 165, 255)

            if show_window:
                cv2.putText(frame, status_text, (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.6, color, 2)
                remaining = max(0.0, timeout_sec - (time.time() - start_time))
                cv2.putText(
                    frame, f"Con lai: {remaining:.1f}s (q=thoat som)", (10, 60),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.6, color, 2,
                )
                cv2.imshow("liveness.py", frame)
                if cv2.waitKey(1) & 0xFF == ord("q"):
                    break

            if blink_detected:
                break

    cap.release()
    if show_window:
        cv2.destroyAllWindows()
    return blink_detected


def main() -> None:
    parser = argparse.ArgumentParser(description="Test chop mat (liveness) qua webcam")
    parser.add_argument("--timeout", type=float, default=10.0, help="Thoi gian toi da cho (giay)")
    parser.add_argument("--camera-index", type=int, default=0)
    args = parser.parse_args()

    print(f"[info] Nhin thang vao camera va chop mat binh thuong trong {args.timeout:.0f}s...")
    is_live = check_liveness(timeout_sec=args.timeout, camera_index=args.camera_index)
    if is_live:
        print("[ket qua] PHAT HIEN CHOP MAT -> coi la nguoi that (live).")
    else:
        print("[ket qua] KHONG phat hien chop mat trong thoi gian cho -> nghi ngo gia mao (anh tinh?).")


if __name__ == "__main__":
    main()
