param([string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot))
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
# Analyze alpha only. The approved PNG artwork is never resampled or rewritten.
Add-Type -TypeDefinition @'
using System;
public static class TitleAtlasBounds {
    public static int[][] Read(byte[] pixels, int w, int h, int stride, string path) {
            if (w != 1536 || h != 1024) throw new Exception("Unexpected atlas size: " + path);
            var alpha = new byte[w,h];
            int transparent = 0;
            for (int y=0;y<h;y++) for (int x=0;x<w;x++) {
                alpha[x,y] = pixels[y*stride+x*4+3];
                if (alpha[x,y] == 0) transparent++;
            }
            if (transparent < w*h/2) throw new Exception("Atlas lacks transparent background: " + path);
            // Generated layouts are not exact thirds. Split at empty horizontal gutters
            // so a title never includes the top edge of the following row.
            var rows = new int[]{0,0,0,h};
            for (int boundary=1;boundary<3;boundary++) {
                int bestStart=0, bestLength=0, runStart=0, length=0;
                for (int y=boundary*h/3-h/8;y<boundary*h/3+h/8;y++) {
                    bool empty=true;
                    for (int x=0;x<w;x++) if (alpha[x,y]>8) { empty=false; break; }
                    if (empty) { if (length==0) runStart=y; length++; }
                    else length=0;
                    if (length>bestLength) { bestLength=length; bestStart=runStart; }
                }
                if (bestLength<4) throw new Exception("No clear gap between rows: " + path);
                rows[boundary]=bestStart+bestLength/2;
            }
            var result = new int[6][];
            for (int row=0;row<3;row++) {
                int top=rows[row], bottom=rows[row+1];
                int bestStart=0, bestLength=0, runStart=0, length=0;
                for (int x=w*2/5;x<w*3/5;x++) {
                    bool empty=true;
                    for (int y=top;y<bottom;y++) if (alpha[x,y]>8) { empty=false; break; }
                    if (empty) { if (length==0) runStart=x; length++; }
                    else length=0;
                    if (length>bestLength) { bestLength=length; bestStart=runStart; }
                }
                if (bestLength<4) throw new Exception("No clear gap between titles: " + path);
                int split=bestStart+bestLength/2;
                for (int col=0;col<2;col++) {
                    int left=col==0?0:split, right=col==0?split:w;
                    int x0=right, y0=bottom, x1=left, y1=top;
                    for (int y=top;y<bottom;y++) for (int x=left;x<right;x++) if (alpha[x,y]>8) {
                        x0=Math.Min(x0,x); y0=Math.Min(y0,y); x1=Math.Max(x1,x+1); y1=Math.Max(y1,y+1);
                    }
                    if (x1<=x0 || y1<=y0) throw new Exception("Empty title: " + path);
                    x0=Math.Max(left,x0-2); y0=Math.Max(top,y0-2);
                    x1=Math.Min(right,x1+2); y1=Math.Min(bottom,y1+2);
                    result[row*2+col]=new int[]{x0,y0,x1,y1};
                }
            }
            return result;
    }
}
'@
$assets = Join-Path $ProjectRoot 'src/main/resources/assets/travelerstitlesgtnh'
$catalog = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'title-artwork.json') -Raw | ConvertFrom-Json
$bounds = @{}
$rules = foreach ($entry in $catalog.titles) {
    if (-not $bounds.ContainsKey($entry.atlas)) {
        $path = Join-Path $assets "textures/titles/$($entry.atlas)"
        $image = [Drawing.Bitmap]::new($path)
        try {
            $rect = [Drawing.Rectangle]::new(0, 0, $image.Width, $image.Height)
            $data = $image.LockBits($rect, [Drawing.Imaging.ImageLockMode]::ReadOnly, [Drawing.Imaging.PixelFormat]::Format32bppArgb)
            try {
                $pixels = [byte[]]::new($data.Stride * $image.Height)
                [Runtime.InteropServices.Marshal]::Copy($data.Scan0, $pixels, 0, $pixels.Length)
                $bounds[$entry.atlas] = [TitleAtlasBounds]::Read($pixels, $image.Width, $image.Height, $data.Stride, $path)
            } finally { $image.UnlockBits($data) }
        } finally { $image.Dispose() }
    }
    $b = $bounds[$entry.atlas][$entry.cell]
    [ordered]@{
        id = "builtin:$($entry.id)"
        priority = $entry.priority
        match = $entry.match
        style = [ordered]@{
            title = $entry.title
            texture = "travelerstitlesgtnh:textures/titles/$($entry.atlas)"
            textureU0 = $b[0] / 1536.0
            textureV0 = $b[1] / 1024.0
            textureU1 = $b[2] / 1536.0
            textureV1 = $b[3] / 1024.0
            imageWidth = [int][Math]::Ceiling(($b[2] - $b[0]) / 4.0)
            imageHeight = [int][Math]::Ceiling(($b[3] - $b[1]) / 4.0)
            decoration = $false
            showSubtitle = $true
            biomeSubtitleColor = $true
        }
    }
}
$index = [ordered]@{schemaVersion=1; files=@('biome-colors.json'); defaults=@{}; rules=@($rules)}
$json = $index | ConvertTo-Json -Depth 15
[IO.File]::WriteAllText((Join-Path $assets 'titles/index.json'), $json + "`n", [Text.UTF8Encoding]::new($false))
Write-Output "Generated $($rules.Count) title rules from $($bounds.Count) unmodified transparent atlases."
