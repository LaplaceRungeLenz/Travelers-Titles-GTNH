$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$assetRoot = Join-Path $projectRoot 'examples\resourcepack\assets\travelerstitlesgtnh'
New-Item -ItemType Directory -Force -Path (Join-Path $assetRoot 'textures\titles'),(Join-Path $assetRoot 'sounds') | Out-Null
Add-Type -AssemblyName System.Drawing
$bitmap = [Drawing.Bitmap]::new(960,240)
$graphics = [Drawing.Graphics]::FromImage($bitmap)
$graphics.SmoothingMode = [Drawing.Drawing2D.SmoothingMode]::AntiAlias
$graphics.TextRenderingHint = [Drawing.Text.TextRenderingHint]::AntiAliasGridFit
$graphics.Clear([Drawing.Color]::Transparent)
$ink = [Drawing.SolidBrush]::new([Drawing.Color]::FromArgb(255,232,212,162))
$pen = [Drawing.Pen]::new($ink,2)
$font = [Drawing.Font]::new('Microsoft YaHei',64,[Drawing.FontStyle]::Regular,[Drawing.GraphicsUnit]::Pixel)
$format = [Drawing.StringFormat]::new()
$format.Alignment = [Drawing.StringAlignment]::Center
$format.LineAlignment = [Drawing.StringAlignment]::Center
$graphics.DrawString('L U N A   /   月 球',$font,$ink,[Drawing.RectangleF]::new(0,30,960,160),$format)
$graphics.DrawLine($pen,100,205,410,205)
$graphics.DrawLine($pen,550,205,860,205)
$graphics.DrawEllipse($pen,471,196,18,18)
$bitmap.Save((Join-Path $assetRoot 'textures\titles\moon.png'),[Drawing.Imaging.ImageFormat]::Png)
$format.Dispose(); $font.Dispose(); $pen.Dispose(); $ink.Dispose(); $graphics.Dispose(); $bitmap.Dispose()
# Original synthesized soft chime. No third-party recording is bundled.
& ffmpeg -hide_banner -loglevel error -y -f lavfi -i 'sine=frequency=660:duration=0.65:sample_rate=44100' -af 'afade=t=in:d=0.04,afade=t=out:st=0.08:d=0.57,volume=0.25' -c:a libvorbis (Join-Path $assetRoot 'sounds\arrival.ogg')
if ($LASTEXITCODE -ne 0) { throw 'ffmpeg failed' }
New-Item -ItemType Directory -Force -Path (Join-Path $projectRoot 'build\examples') | Out-Null
Compress-Archive -Path (Join-Path $projectRoot 'examples\resourcepack\*') -DestinationPath (Join-Path $projectRoot 'build\examples\TTGTNH-example-resourcepack.zip') -Force
