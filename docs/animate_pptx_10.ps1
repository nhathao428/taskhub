# ============================================================
# animate_pptx_10.ps1 — Animation cho deck 10 slide (build_slides_10.py)
# Tiêu đề tự hiện khi vào slide; nội dung fade/zoom theo từng click (gom nhóm theo hàng).
# Chữ = Fade, sơ đồ/ảnh = Zoom. Chạy SAU build_slides_10.py.
#   powershell -ExecutionPolicy Bypass -File animate_pptx_10.ps1
# ============================================================
$ErrorActionPreference = 'Stop'
$pptx = Join-Path $PSScriptRoot 'THUYET_TRINH_DO_AN.pptx'

$FADE = 10; $ZOOM = 23
$ON_CLICK = 1; $WITH_PREV = 2; $AFTER_PREV = 3
$TOL = 8.0

$app = New-Object -ComObject PowerPoint.Application
$pres = $app.Presentations.Open($pptx, $false, $false, $false)
$total = $pres.Slides.Count

function Add-Eff($seq, $shp, $effId, $trig, $dur, $delay) {
    try {
        $eff = $seq.AddEffect($shp, $effId, 0, $trig)
        $eff.Timing.Duration = $dur
        $eff.Timing.TriggerDelayTime = $delay
    } catch { }
}

for ($i = 1; $i -le $total; $i++) {
    $sl = $pres.Slides.Item($i)
    $shapes = $sl.Shapes
    $count = $shapes.Count
    $seq = $sl.TimeLine.MainSequence

    if ($i -eq 1) {
        # BÌA: mọi thứ tự hiện, cascade
        $idx = 0
        for ($j = 3; $j -le $count; $j++) {
            $shp = $shapes.Item($j)
            $isPic = ($shp.Type -eq 13 -or $shp.Type -eq 11)
            $eff = if ($isPic) { $ZOOM } else { $FADE }
            $trig = if ($idx -eq 0) { $AFTER_PREV } else { $WITH_PREV }
            Add-Eff $seq $shp $eff $trig 0.5 ([Math]::Round($idx * 0.12, 2))
            $idx++
        }
        Write-Output ("slide {0}: bia (auto cascade)" -f $i)
        continue
    }

    # SLIDE NỘI DUNG: tiêu đề (shape 3 = textbox header, shape 4 = gạch chân) tự hiện
    if ($count -ge 3) { Add-Eff $seq $shapes.Item(3) $FADE $AFTER_PREV 0.4 0 }
    if ($count -ge 4) { Add-Eff $seq $shapes.Item(4) $FADE $WITH_PREV 0.3 0.1 }

    # Nội dung 5..count: gom nhóm theo hàng (Top), mỗi nhóm 1 click
    $items = @()
    for ($j = 5; $j -le $count; $j++) {
        $shp = $shapes.Item($j)
        $items += [pscustomobject]@{
            Shp = $shp; Top = [double]$shp.Top; Left = [double]$shp.Left
            Bottom = [double]$shp.Top + [double]$shp.Height
            IsPic = ($shp.Type -eq 13 -or $shp.Type -eq 11)
            IsMedia = ($shp.Type -eq 16)
        }
    }
    $items = @($items | Sort-Object Top, Left)

    # Slide demo (video) => mọi thứ tự hiện cho gọn
    $autoAll = ($i -eq 8)
    $groupBottom = -1000000.0; $groups = 0
    foreach ($it in $items) {
        $newGroup = $it.IsPic -or ($it.Top -ge ($groupBottom - $TOL))
        if ($newGroup) {
            $groups++
            $trig = if ($autoAll) { $AFTER_PREV } else { $ON_CLICK }
            $groupBottom = if ($it.IsPic) { $it.Top } else { $it.Bottom }
        } else {
            $trig = $WITH_PREV
            if ($it.Bottom -gt $groupBottom) { $groupBottom = $it.Bottom }
        }
        $eff = if ($it.IsPic) { $ZOOM } else { $FADE }
        $dur = if ($it.IsPic) { 0.6 } else { 0.4 }
        Add-Eff $seq $it.Shp $eff $trig $dur 0.0
    }
    Write-Output ("slide {0,2}: {1} nhom" -f $i, $groups)
}

$pres.Save()
$pres.Close()
$app.Quit()
Write-Output 'DONE'
