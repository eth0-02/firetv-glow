param(
    [Parameter(Mandatory=$true)][string]$Device,
    [Parameter(Mandatory=$true)][string]$File,
    [switch]$Import
)
$ErrorActionPreference = 'Stop'
& adb connect $Device
if ($LASTEXITCODE -ne 0) { throw 'Connection failed' }
if ($Import) {
    $content = Get-Content -LiteralPath $File -Raw
    $data = $content | ConvertFrom-Json
    if ($data.version -ne 1) { throw 'Unsupported layout backup' }
    & adb -s $Device push $File /data/local/tmp/glow-layout.json
    if ($LASTEXITCODE -ne 0) { throw 'Upload failed' }
    & adb -s $Device shell run-as com.gathin.tvhome cp /data/local/tmp/glow-layout.json files/glow-backup.json
    if ($LASTEXITCODE -ne 0) { throw 'Copy failed' }
    & adb -s $Device shell rm /data/local/tmp/glow-layout.json
    Write-Host 'Open Home settings > Backup and restore > Restore saved layout to apply and validate it.'
} else {
    $content = & adb -s $Device shell run-as com.gathin.tvhome cat files/glow-backup.json
    if ($LASTEXITCODE -ne 0) { throw 'First use Save layout on this TV in Home settings.' }
    $content | Set-Content -LiteralPath $File -Encoding utf8
    Write-Host 'Saved layout exported.'
}
