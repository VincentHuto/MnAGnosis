Add-Type -AssemblyName System.Drawing

$textureDirectory = Join-Path $PSScriptRoot `
    "..\src\main\resources\assets\mnagnosis\textures\mna"
$sourcePath = Join-Path $textureDirectory `
    "ineffable_hud_concept_base.png"
$backingPath = Join-Path $textureDirectory `
    "ineffable_hud_concept_backing.png"
$framePath = Join-Path $textureDirectory `
    "ineffable_hud_concept_frame.png"

$source = [System.Drawing.Bitmap]::FromFile(
    (Resolve-Path $sourcePath)
)
$backing = New-Object System.Drawing.Bitmap(
    $source.Width,
    $source.Height,
    [System.Drawing.Imaging.PixelFormat]::Format32bppArgb
)
$frame = New-Object System.Drawing.Bitmap(
    $source.Width,
    $source.Height,
    [System.Drawing.Imaging.PixelFormat]::Format32bppArgb
)

try {
    for ($y = 0; $y -lt $source.Height; $y++) {
        for ($x = 0; $x -lt $source.Width; $x++) {
            $color = $source.GetPixel($x, $y)
            $insideChannel = $x -ge 80 -and $x -lt 870 `
                -and $y -ge 52 -and $y -lt 106
            $isDark = $color.A -eq 255 -and $color.R -eq 5 `
                -and $color.G -eq 5 -and $color.B -eq 5

            if ($insideChannel -and $isDark) {
                $backing.SetPixel($x, $y, $color)
                $frame.SetPixel(
                    $x,
                    $y,
                    [System.Drawing.Color]::Transparent
                )
            } else {
                $backing.SetPixel(
                    $x,
                    $y,
                    [System.Drawing.Color]::Transparent
                )
                $frame.SetPixel($x, $y, $color)
            }
        }
    }

    $backing.Save(
        $backingPath,
        [System.Drawing.Imaging.ImageFormat]::Png
    )
    $frame.Save(
        $framePath,
        [System.Drawing.Imaging.ImageFormat]::Png
    )
} finally {
    $source.Dispose()
    $backing.Dispose()
    $frame.Dispose()
}
