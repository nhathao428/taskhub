# -*- coding: utf-8 -*-
"""Sinh bìa giới thiệu sản phẩm TaskHub (HTML) -> render JPEG riêng.

Bìa kiểu product-intro: brand wordmark + tagline + tính năng lõi + tech stack,
kèm thông tin đồ án (trường / GVHD / SVTH) như một trang bìa báo cáo.
"""
import base64
import os

HERE = os.path.dirname(os.path.abspath(__file__))
DOCS = os.path.dirname(HERE)
LOGO = os.path.join(DOCS, "hutech_logo.png")

logo_b64 = base64.b64encode(open(LOGO, "rb").read()).decode()
logo_uri = f"data:image/png;base64,{logo_b64}"

HTML = f"""<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="utf-8">
<style>
  @import url('https://fonts.googleapis.com/css2?family=Sora:wght@400;600;700;800&family=Inter:wght@400;500;600&family=JetBrains+Mono:wght@500&display=swap');

  :root {{
    --bg-0: #0a0e1a;
    --bg-1: #0f1730;
    --ink: #eef2ff;
    --muted: #9aa6c7;
    --accent: #5b8cff;
    --accent-2: #22d3ee;
    --accent-3: #a78bfa;
    --line: rgba(148,163,210,0.16);
    --card: rgba(255,255,255,0.045);
  }}
  * {{ margin:0; padding:0; box-sizing:border-box; }}
  html,body {{ width:1920px; height:1080px; }}
  body {{
    font-family:'Inter',system-ui,sans-serif;
    color:var(--ink);
    background:
      radial-gradient(1100px 700px at 78% -8%, rgba(91,140,255,0.28), transparent 60%),
      radial-gradient(900px 700px at 8% 112%, rgba(167,139,250,0.20), transparent 55%),
      radial-gradient(700px 600px at 100% 100%, rgba(34,211,238,0.16), transparent 55%),
      linear-gradient(160deg, var(--bg-1), var(--bg-0) 70%);
    overflow:hidden;
    position:relative;
  }}
  /* subtle grid texture */
  body::before {{
    content:""; position:absolute; inset:0;
    background-image:
      linear-gradient(rgba(148,163,210,0.045) 1px, transparent 1px),
      linear-gradient(90deg, rgba(148,163,210,0.045) 1px, transparent 1px);
    background-size:56px 56px;
    mask-image: radial-gradient(1200px 800px at 60% 40%, #000 30%, transparent 78%);
  }}
  .stage {{ position:relative; width:1920px; height:1080px; padding:64px 84px; display:flex; flex-direction:column; }}

  /* top bar */
  .top {{ display:flex; align-items:center; justify-content:space-between; }}
  .school {{ display:flex; align-items:center; gap:20px; }}
  .school img {{ height:74px; width:auto; filter:drop-shadow(0 6px 20px rgba(0,0,0,.5)); }}
  .school .lines {{ line-height:1.35; }}
  .school .l1 {{ font-weight:700; font-size:21px; letter-spacing:.5px; }}
  .school .l2 {{ font-size:16px; color:var(--muted); letter-spacing:2px; text-transform:uppercase; }}
  .tag {{
    font-family:'JetBrains Mono',monospace; font-size:15px; letter-spacing:3px;
    color:var(--accent-2); border:1px solid var(--line); border-radius:999px;
    padding:11px 22px; background:var(--card); text-transform:uppercase;
  }}

  /* main grid */
  .main {{ flex:1; display:grid; grid-template-columns: 1.05fr 0.95fr; gap:56px; align-items:center; margin-top:18px; }}

  .eyebrow {{
    display:inline-flex; align-items:center; gap:12px; color:var(--muted);
    font-family:'JetBrains Mono',monospace; font-size:16px; letter-spacing:4px; text-transform:uppercase;
    margin-bottom:26px;
  }}
  .eyebrow .dot {{ width:9px;height:9px;border-radius:50%;background:var(--accent-2); box-shadow:0 0 16px var(--accent-2); }}

  .brand {{ display:flex; align-items:center; gap:26px; margin-bottom:22px; }}
  .glyph {{
    width:96px; height:96px; border-radius:26px; flex:none;
    background:linear-gradient(145deg, var(--accent), var(--accent-3));
    box-shadow:0 18px 50px rgba(91,140,255,.45), inset 0 1px 0 rgba(255,255,255,.4);
    position:relative;
  }}
  .glyph::before {{
    content:""; position:absolute; inset:0; border-radius:26px;
    background:
      linear-gradient(#fff,#fff) 26px 32px/30px 9px no-repeat,
      linear-gradient(#fff,#fff) 26px 50px/44px 9px no-repeat,
      linear-gradient(#fff,#fff) 26px 68px/36px 9px no-repeat;
    opacity:.95;
  }}
  .glyph::after {{
    content:"✓"; position:absolute; right:12px; bottom:8px; font-size:30px; font-weight:800;
    color:#fff; text-shadow:0 2px 8px rgba(0,0,0,.35);
  }}
  .wordmark {{ font-family:'Sora',sans-serif; font-weight:800; font-size:104px; letter-spacing:-2px; line-height:.95; }}
  .wordmark b {{ background:linear-gradient(120deg,#fff,#c9d6ff 45%, var(--accent-2)); -webkit-background-clip:text; background-clip:text; color:transparent; }}

  .headline {{ font-family:'Sora',sans-serif; font-weight:700; font-size:38px; line-height:1.22; margin-bottom:18px; }}
  .headline .hl {{ color:var(--accent-2); }}
  .sub {{ font-size:21px; color:var(--muted); line-height:1.6; max-width:760px; margin-bottom:34px; }}

  .features {{ display:flex; flex-wrap:wrap; gap:14px; margin-bottom:40px; }}
  .pill {{
    display:flex; align-items:center; gap:11px; font-size:17px; font-weight:500;
    background:var(--card); border:1px solid var(--line); border-radius:14px; padding:13px 18px;
  }}
  .pill .i {{ font-size:19px; }}

  .stack {{ display:flex; align-items:center; gap:14px; flex-wrap:wrap; }}
  .stack .label {{ font-family:'JetBrains Mono',monospace; font-size:14px; letter-spacing:2px; color:var(--muted); text-transform:uppercase; }}
  .chip {{
    font-family:'JetBrains Mono',monospace; font-size:15px; color:#dfe7ff;
    border:1px solid var(--line); border-radius:9px; padding:8px 13px; background:rgba(91,140,255,.08);
  }}

  /* right: product mock card */
  .mock {{
    position:relative; height:560px; border-radius:24px; overflow:hidden;
    background:linear-gradient(160deg, rgba(20,28,54,.95), rgba(12,18,38,.95));
    border:1px solid var(--line);
    box-shadow:0 40px 90px rgba(0,0,0,.55), inset 0 1px 0 rgba(255,255,255,.06);
  }}
  .mock .bar {{ display:flex; align-items:center; gap:9px; padding:18px 22px; border-bottom:1px solid var(--line); }}
  .mock .bar i {{ width:13px;height:13px;border-radius:50%; display:inline-block; }}
  .mock .bar .t {{ margin-left:14px; font-size:15px; color:var(--muted); font-family:'JetBrains Mono',monospace; }}
  .mock .body {{ padding:26px; display:grid; grid-template-columns:repeat(2,1fr); gap:18px; }}
  .stat {{ background:var(--card); border:1px solid var(--line); border-radius:16px; padding:20px 22px; }}
  .stat .k {{ font-size:14px; color:var(--muted); letter-spacing:1px; text-transform:uppercase; }}
  .stat .v {{ font-family:'Sora',sans-serif; font-weight:800; font-size:40px; margin-top:8px; }}
  .stat .v.a {{ color:var(--accent-2); }} .stat .v.b {{ color:var(--accent-3); }} .stat .v.c {{ color:#7df2c0; }}
  .ai {{
    grid-column:1/3; background:linear-gradient(120deg, rgba(91,140,255,.16), rgba(167,139,250,.12));
    border:1px solid rgba(123,150,255,.32); border-radius:16px; padding:22px 24px;
  }}
  .ai .h {{ display:flex; align-items:center; gap:10px; font-weight:600; font-size:18px; margin-bottom:12px; }}
  .ai .h .b {{ font-family:'JetBrains Mono',monospace; font-size:12px; color:#0a0e1a; background:var(--accent-2); padding:4px 9px; border-radius:6px; letter-spacing:1px;}}
  .row {{ display:flex; align-items:center; justify-content:space-between; padding:11px 0; border-top:1px dashed var(--line); font-size:16px; }}
  .row:first-of-type {{ border-top:none; }}
  .row .name {{ color:#e7ecff; }}
  .row .score {{ font-family:'JetBrains Mono',monospace; color:var(--accent-2); }}
  .row .who {{ color:var(--muted); font-size:14px; }}

  /* bottom info bar */
  .foot {{ display:flex; align-items:center; justify-content:space-between; padding-top:30px; margin-top:26px; border-top:1px solid var(--line); }}
  .who-block {{ display:flex; gap:54px; }}
  .who-block .it .k {{ font-family:'JetBrains Mono',monospace; font-size:13px; letter-spacing:2px; color:var(--muted); text-transform:uppercase; }}
  .who-block .it .v {{ font-size:19px; font-weight:600; margin-top:5px; }}
  .place {{ text-align:right; color:var(--muted); font-size:16px; line-height:1.6; }}
</style>
</head>
<body>
  <div class="stage">
    <div class="top">
      <div class="school">
        <img src="{logo_uri}" alt="HUTECH">
        <div class="lines">
          <div class="l1">TRƯỜNG ĐẠI HỌC CÔNG NGHỆ TP.HCM</div>
          <div class="l2">Khoa Công nghệ Thông tin</div>
        </div>
      </div>
      <div class="tag">Đồ án cơ sở · 2026</div>
    </div>

    <div class="main">
      <div class="left">
        <div class="eyebrow"><span class="dot"></span> Giới thiệu sản phẩm</div>
        <div class="brand">
          <div class="glyph"></div>
          <div class="wordmark">Task<b>Hub</b></div>
        </div>
        <div class="headline">Hệ thống <span class="hl">Quản lý Công việc</span><br>cho Doanh nghiệp Nhỏ Đa ngành · Tích hợp AI</div>
        <div class="sub">Nền tảng hợp nhất quản lý nhân sự, chấm công, dự án và tiến độ công việc — kèm trợ lý AI tự động gợi ý nhân viên phù hợp cho từng nhiệm vụ.</div>

        <div class="features">
          <div class="pill"><span class="i">👥</span> Nhân sự &amp; chấm công</div>
          <div class="pill"><span class="i">📋</span> Dự án &amp; tiến độ</div>
          <div class="pill"><span class="i">🤖</span> AI gợi ý phân công</div>
          <div class="pill"><span class="i">🔐</span> Phân quyền JWT 3 vai trò</div>
        </div>

        <div class="stack">
          <span class="label">Tech stack</span>
          <span class="chip">Spring Boot</span>
          <span class="chip">React</span>
          <span class="chip">Flutter</span>
          <span class="chip">PostgreSQL</span>
          <span class="chip">Redis</span>
          <span class="chip">Docker</span>
          <span class="chip">Gemini AI</span>
        </div>
      </div>

      <div class="right">
        <div class="mock">
          <div class="bar">
            <i style="background:#ff5f57"></i><i style="background:#febc2e"></i><i style="background:#28c840"></i>
            <span class="t">taskhub · dashboard</span>
          </div>
          <div class="body">
            <div class="stat"><div class="k">Dự án</div><div class="v a">24</div></div>
            <div class="stat"><div class="k">Công việc</div><div class="v b">187</div></div>
            <div class="stat"><div class="k">Nhân viên</div><div class="v">42</div></div>
            <div class="stat"><div class="k">Đúng hạn</div><div class="v c">93%</div></div>
            <div class="ai">
              <div class="h">✨ AI gợi ý nhân viên <span class="b">GEMINI</span></div>
              <div class="row"><span class="name">Thiết kế UI màn hình chấm công</span><span class="who">Phù hợp</span><span class="score">96%</span></div>
              <div class="row"><span class="name">Nguyễn Văn A · Frontend</span><span class="who">đang rảnh</span><span class="score">↑</span></div>
              <div class="row"><span class="name">Trần Thị B · Mobile</span><span class="who">2 task</span><span class="score">88%</span></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="foot">
      <div class="who-block">
        <div class="it"><div class="k">GVHD</div><div class="v">ThS. Dương Thành Phết</div></div>
        <div class="it"><div class="k">SVTH</div><div class="v">Nguyễn Nhật Hào</div></div>
        <div class="it"><div class="k">MSSV</div><div class="v">2380612688</div></div>
        <div class="it"><div class="k">Lớp</div><div class="v">23DTHC1</div></div>
      </div>
      <div class="place">TP. Hồ Chí Minh<br>Tháng 5 năm 2026</div>
    </div>
  </div>
</body>
</html>"""

out = os.path.join(HERE, "cover.html")
open(out, "w", encoding="utf-8").write(HTML)
print("wrote", out)
