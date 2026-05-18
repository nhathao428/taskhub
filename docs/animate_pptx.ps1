# ============================================================
# animate_pptx.ps1
# Thêm hiệu ứng xuất hiện (entrance animation) cho từng phần tử
# trong THUYET_TRINH_DO_AN.pptx bằng PowerPoint COM.
# Chạy SAU build_slides_pptx.py:
#   python build_slides_pptx.py
#   powershell -ExecutionPolicy Bypass -File animate_pptx.ps1
# ============================================================
$ErrorActionPreference = 'Stop'

$pptx = Join-Path $PSScriptRoot 'THUYET_TRINH_DO_AN.pptx'

# Hằng số PowerPoint
$EFFECT_FADE       = 10   # msoAnimEffectFade
$TRIGGER_AFTERPREV = 3    # msoAnimTriggerAfterPrevious
$TRIGGER_WITHPREV  = 2    # msoAnimTriggerWithPrevious

$app = New-Object -ComObject PowerPoint.Application
$pres = $app.Presentations.Open($pptx, $false, $false, $false)
$total = $pres.Slides.Count

for ($i = 1; $i -le $total; $i++) {
    $sl = $pres.Slides.Item($i)
    $shapes = $sl.Shapes
    $count = $shapes.Count

    # Slide nội dung (2..total-1) dùng header() = 7 hình đầu (giữ tĩnh).
    # Slide bìa (1) và slide cảm ơn (cuối) thì cho cả slide chạy hiệu ứng.
    if ($i -eq 1 -or $i -eq $total) { $start = 1 } else { $start = 8 }
    if ($start -gt $count) { continue }

    $seq = $sl.TimeLine.MainSequence
    $k = 0
    for ($j = $start; $j -le $count; $j++) {
        $shp = $shapes.Item($j)
        try {
            if ($k -eq 0) { $trig = $TRIGGER_AFTERPREV } else { $trig = $TRIGGER_WITHPREV }
            $eff = $seq.AddEffect($shp, $EFFECT_FADE, 0, $trig)
            $eff.Timing.Duration = 0.4
            $eff.Timing.TriggerDelayTime = [Math]::Round($k * 0.08, 2)
            $k++
        } catch { }
    }
    Write-Output ("slide {0,2}: {1} hieu ung" -f $i, $seq.Count)
}

$pres.Save()
$pres.Close()
$app.Quit()
Write-Output 'DONE'
