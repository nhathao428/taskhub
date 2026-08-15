"""
api_service.py
Service HTTP nho boc pipeline nhan dien khuon mat, de backend Java (Spring Boot) goi sang.

VI SAO CAN SERVICE RIENG: Spring Boot chay tren JVM, khong chay truc tiep PyTorch duoc.
Nen tach thanh 2 tien trinh: Java giu nghiep vu + database, Python chi lam phan AI.

THIET KE: service nay KHONG LUU GI CA (stateless). No chi nhan anh -> tra ve vector 512
chieu. Toan bo embedding cua nhan vien do Java luu trong PostgreSQL (da ma hoa). Lam vay
de du lieu sinh trac hoc chi nam o MOT noi duy nhat, de kiem soat va sao luu.

Chay:
    pip install -r requirements.txt
    uvicorn api_service:app --host 127.0.0.1 --port 8000

Kiem tra nhanh:
    curl http://127.0.0.1:8000/health
"""
from __future__ import annotations

import base64
import binascii
import io
from typing import List, Optional

import numpy as np
from fastapi import FastAPI, HTTPException
from PIL import Image, UnidentifiedImageError
from pydantic import BaseModel, Field

from face_pipeline import FaceEmbedder, get_device
from liveness import EAR_BLINK_THRESHOLD, LEFT_EYE_IDX, RIGHT_EYE_IDX, eye_aspect_ratio

app = FastAPI(
    title="TaskHub Face Recognition Service",
    description="Tra ve embedding khuon mat + kiem tra chop mat. Khong luu du lieu.",
    version="1.0.0",
)

# Nap model MOT LAN luc khoi dong (nap moi request se rat cham).
_embedder: Optional[FaceEmbedder] = None


def get_embedder() -> FaceEmbedder:
    global _embedder
    if _embedder is None:
        _embedder = FaceEmbedder(device=get_device())
    return _embedder


class ImageRequest(BaseModel):
    image_base64: str = Field(..., description="Anh JPEG/PNG ma hoa base64 (co the kem tien to data:image)")


class FramesRequest(BaseModel):
    frames_base64: List[str] = Field(..., description="Nhieu frame lien tiep de kiem tra chop mat")


class EmbeddingResponse(BaseModel):
    face_detected: bool
    embedding: Optional[List[float]] = None
    message: str


class LivenessResponse(BaseModel):
    live: bool
    frames_with_face: int
    min_ear: Optional[float] = None
    max_ear: Optional[float] = None
    message: str


def decode_image(image_base64: str) -> Image.Image:
    """Giai ma base64 -> anh RGB. Chap nhan ca dang 'data:image/jpeg;base64,....'."""
    raw = image_base64.strip()
    if raw.startswith("data:"):
        parts = raw.split(",", 1)
        if len(parts) != 2:
            raise HTTPException(status_code=400, detail="Chuoi data URI khong hop le")
        raw = parts[1]
    try:
        data = base64.b64decode(raw, validate=True)
    except (binascii.Error, ValueError) as exc:
        raise HTTPException(status_code=400, detail="Khong giai ma duoc base64") from exc
    if not data:
        raise HTTPException(status_code=400, detail="Anh rong")
    try:
        return Image.open(io.BytesIO(data)).convert("RGB")
    except UnidentifiedImageError as exc:
        raise HTTPException(status_code=400, detail="Du lieu khong phai anh hop le") from exc


@app.get("/health")
def health() -> dict:
    """Java goi endpoint nay luc khoi dong de biet service AI da san sang chua."""
    try:
        embedder = get_embedder()
        return {"status": "ok", "device": str(embedder.device)}
    except Exception as exc:  # pragma: no cover - chi xay ra khi thieu model/thu vien
        raise HTTPException(status_code=503, detail=f"Model chua san sang: {exc}") from exc


@app.post("/embed", response_model=EmbeddingResponse)
def embed(req: ImageRequest) -> EmbeddingResponse:
    """
    Nhan 1 anh -> tra ve embedding 512 chieu.
    KHONG so sanh voi ai o day: viec so khop do Java lam, vi Java moi giu danh sach
    nhan vien da dang ky.
    """
    img = decode_image(req.image_base64)
    embedder = get_embedder()
    vector = embedder.embed_from_pil(img)
    if vector is None:
        return EmbeddingResponse(
            face_detected=False,
            embedding=None,
            message="Khong phat hien khuon mat trong anh",
        )
    return EmbeddingResponse(
        face_detected=True,
        embedding=[float(x) for x in vector],
        message="OK",
    )


@app.post("/liveness", response_model=LivenessResponse)
def liveness(req: FramesRequest) -> LivenessResponse:
    """
    Nhan nhieu frame lien tiep, tra ve co phat hien chop mat khong.

    Cach hoat dong: tinh Eye Aspect Ratio (EAR) tung frame. Nguoi that chop mat se lam
    EAR tut xuong duoi nguong roi tang lai. Anh tinh in ra hoac mo tren dien thoai thi
    EAR gan nhu khong doi -> bi tu choi.

    HAN CHE (ghi ro trong bao cao): chua chan duoc video quay san co canh chop mat.
    """
    if len(req.frames_base64) < 3:
        raise HTTPException(status_code=400, detail="Can it nhat 3 frame de kiem tra chop mat")

    import mediapipe as mp

    ear_values: List[float] = []
    frames_with_face = 0

    with mp.solutions.face_mesh.FaceMesh(
        max_num_faces=1,
        refine_landmarks=True,
        min_detection_confidence=0.5,
        min_tracking_confidence=0.5,
    ) as face_mesh:
        for frame_b64 in req.frames_base64:
            img = decode_image(frame_b64)
            arr = np.asarray(img)
            h, w = arr.shape[:2]
            result = face_mesh.process(arr)
            if not result.multi_face_landmarks:
                continue
            frames_with_face += 1
            landmarks = result.multi_face_landmarks[0].landmark
            left = eye_aspect_ratio(landmarks, LEFT_EYE_IDX, w, h)
            right = eye_aspect_ratio(landmarks, RIGHT_EYE_IDX, w, h)
            ear_values.append((left + right) / 2.0)

    if not ear_values:
        return LivenessResponse(
            live=False,
            frames_with_face=0,
            message="Khong phat hien khuon mat trong bat ky frame nao",
        )

    min_ear = min(ear_values)
    max_ear = max(ear_values)
    # Coi la nguoi that neu co frame mat nham (EAR thap) VA co frame mat mo (EAR cao).
    blinked = min_ear < EAR_BLINK_THRESHOLD and max_ear >= EAR_BLINK_THRESHOLD
    return LivenessResponse(
        live=blinked,
        frames_with_face=frames_with_face,
        min_ear=min_ear,
        max_ear=max_ear,
        message="Phat hien chop mat" if blinked else "Khong thay chop mat - nghi ngo gia mao",
    )
