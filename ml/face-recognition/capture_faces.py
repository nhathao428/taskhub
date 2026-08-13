"""
capture_faces.py
Chup anh khuon mat tu webcam, luu vao dataset/<ten_nguoi>/*.jpg de train.py dung.

Cach chay:
    python capture_faces.py --name hao --num-images 30

Huong dan chup (quan trong, anh huong truc tiep den accuracy sau nay):
    - Chup it nhat 20-30 anh moi nguoi.
    - Doi goc mat giua cac lan chup: chinh dien, nghieng trai/phai nhe, ngua/cui nhe.
    - Doi bieu cam: binh thuong, cuoi, nghiem tuc.
    - Doi anh sang neu duoc: chup ca cho sang va cho hoi toi hon (nhung van thay ro
      mat) - de model chiu duoc dieu kien check-in thuc te (sang som, den, den flash).
    - Nhan SPACE de chup 1 anh, nhan 'q' de thoat som.
"""
from __future__ import annotations

import argparse
from pathlib import Path

import cv2


def main() -> None:
    parser = argparse.ArgumentParser(description="Chup anh khuon mat tu webcam luu vao dataset/")
    parser.add_argument(
        "--name", required=True,
        help="Ten nguoi (dung lam ten thu muc - nen viet khong dau, khong dau cach, vd 'hao')",
    )
    parser.add_argument("--dataset-dir", default="dataset")
    parser.add_argument("--num-images", type=int, default=30, help="So anh muon chup them")
    parser.add_argument("--camera-index", type=int, default=0)
    args = parser.parse_args()

    out_dir = Path(args.dataset_dir) / args.name
    out_dir.mkdir(parents=True, exist_ok=True)

    existing = list(out_dir.glob("*.jpg"))
    start_idx = len(existing)
    if start_idx:
        print(f"[info] Da co {start_idx} anh cua '{args.name}' truoc do, chup them tiep tu #{start_idx}.")

    cap = cv2.VideoCapture(args.camera_index)
    if not cap.isOpened():
        raise RuntimeError(
            f"Khong mo duoc webcam index {args.camera_index}. "
            "Kiem tra webcam co dang bi ung dung khac dung khong, hoac thu --camera-index 1."
        )

    # Haar cascade co san trong goi opencv-python, chi dung de VE KHUNG XEM TRUOC cho de
    # canh mat khi chup - KHONG dung cho train (train.py dung MTCNN chinh xac hon nhieu).
    cascade_path = cv2.data.haarcascades + "haarcascade_frontalface_default.xml"
    face_cascade = cv2.CascadeClassifier(cascade_path)

    captured = 0
    print(f"[info] San sang chup cho '{args.name}'. Nhan SPACE de chup, 'q' de thoat.")
    print("[info] Nho doi goc mat / bieu cam / anh sang giua cac lan chup (xem huong dan dau file).")

    while captured < args.num_images:
        ok, frame = cap.read()
        if not ok:
            print("[loi] Khong doc duoc frame tu webcam.")
            break

        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        faces = face_cascade.detectMultiScale(gray, scaleFactor=1.1, minNeighbors=5, minSize=(80, 80))

        preview = frame.copy()
        for (x, y, w, h) in faces:
            cv2.rectangle(preview, (x, y), (x + w, y + h), (0, 200, 0), 2)

        cv2.putText(
            preview, f"{args.name}: {captured}/{args.num_images} - SPACE=chup, q=thoat",
            (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 200, 0), 2,
        )
        cv2.imshow("capture_faces.py", preview)

        key = cv2.waitKey(1) & 0xFF
        if key == ord("q"):
            print("[info] Dung som theo yeu cau.")
            break
        if key == 32:  # phim SPACE
            img_path = out_dir / f"{start_idx + captured:03d}.jpg"
            cv2.imwrite(str(img_path), frame)
            captured += 1
            print(f"[ok] Da luu {img_path} ({captured}/{args.num_images})")

    cap.release()
    cv2.destroyAllWindows()
    print(f"[xong] Da chup {captured} anh moi cho '{args.name}' vao {out_dir}")


if __name__ == "__main__":
    main()
