# ============================================================
# animate_pptx.ps1
# Thêm hiệu ứng xuất hiện cho slide THUYET_TRINH_DO_AN.pptx (PowerPoint COM).
# Nội dung gom thành "nhóm" — mỗi lần CLICK hiện một nhóm, dễ thuyết trình.
# Chạy SAU build_slides_pptx.py:
#   python build_slides_pptx.py
#   powershell -ExecutionPolicy Bypass -File animate_pptx.ps1
# ============================================================
$ErrorActionPreference = 'Stop'

$pptx = Join-Path $PSScriptRoot 'THUYET_TRINH_DO_AN.pptx'

$FADE       = 10   # msoAnimEffectFade
$ON_CLICK   = 1    # msoAnimTriggerOnClick
$WITH_PREV  = 2    # msoAnimTriggerWithPrevious
$AFTER_PREV = 3    # msoAnimTriggerAfterPrevious
$TOL        = 6.0  # dung sai (point) khi xét hình ở "hàng dưới"

$app = New-Object -ComObject PowerPoint.Application
$pres = $app.Presentations.Open($pptx, $false, $false, $false)
$total = $pres.Slides.Count

for ($i = 1; $i -le $total; $i++) {
    $sl = $pres.Slides.Item($i)
    $shapes = $sl.Shapes
    $count = $shapes.Count
    $isCover = ($i -eq 1 -or $i -eq $total)
    $start = if ($isCover) { 1 } else { 8 }   # slide nội dung: bỏ 7 hình header
    if ($start -gt $count) { continue }

    # Thu thập hình nội dung
    $items = @()
    for ($j = $start; $j -le $count; $j++) {
        $shp = $shapes.Item($j)
        $isPic = ($shp.Type -eq 13 -or $shp.Type -eq 11 -or $shp.Type -eq 16)
        $items += [pscustomobject]@{
            Shp = $shp
            Top = [double]$shp.Top
            Left = [double]$shp.Left
            Bottom = [double]$shp.Top + [double]$shp.Height
            IsPic = $isPic
        }
    }
    $items = @($items | Sort-Object Top, Left)

    $seq = $sl.TimeLine.MainSequence
    $groupNo = 0
    $posInGroup = 0
    $groupBottom = -1000000.0
    $idx = 0
    foreach ($it in $items) {
        # hình mới sang nhóm mới nếu là ảnh, hoặc nằm hẳn dưới nhóm hiện tại
        $newGroup = $it.IsPic -or ($it.Top -ge ($groupBottom - $TOL))
        if ($newGroup) {
            $groupNo++
            $posInGroup = 0
            $groupBottom = if ($it.IsPic) { $it.Top } else { $it.Bottom }
        } else {
            $posInGroup++
            if ($it.Bottom -gt $groupBottom) { $groupBottom = $it.Bottom }
        }

        if ($isCover) {
            # Slide bìa / cảm ơn: tự chạy khi vào slide
            $trig = if ($idx -eq 0) { $AFTER_PREV } else { $WITH_PREV }
            $delay = [Math]::Round($idx * 0.05, 2)
        } elseif ($posInGroup -gt 0) {
            $trig = $WITH_PREV
            $delay = [Math]::Round($posInGroup * 0.04, 2)
        } elseif ($groupNo -eq 1) {
            $trig = $AFTER_PREV   # nhóm đầu: hiện sẵn khi vào slide
            $delay = 0
        } else {
            $trig = $ON_CLICK     # các nhóm sau: click mới hiện
            $delay = 0
        }

        try {
            $eff = $seq.AddEffect($it.Shp, $FADE, 0, $trig)
            $eff.Timing.Duration = 0.35
            $eff.Timing.TriggerDelayTime = $delay
        } catch { }
        $idx++
    }
    Write-Output ("slide {0,2}: {1} nhom click" -f $i, $groupNo)
}

$pres.Save()
$pres.Close()
$app.Quit()
Write-Output 'DONE'
