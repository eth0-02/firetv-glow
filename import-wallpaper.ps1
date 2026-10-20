param(
    [Parameter(Mandatory=$true)][string]$Device,
    [Parameter(Mandatory=$true)][string]$Image
)
$ErrorActionPreference = 'Stop'
$file = Get-Item -LiteralPath $Image
if ($file.Extension -notin @('.jpg','.jpeg','.png','.webp') -or $file.Length -gt 20MB) {
    throw 'Choose a JPG, PNG, or WebP image under 20 MB.'
}
function Run-Adb([string[]]$Arguments) {
    & adb @Arguments
    if ($LASTEXITCODE -ne 0) { throw 'ADB command failed.' }
}
Run-Adb @('connect', $Device)
$name = 'photo_' + [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds() + '.jpg'
$remote = '/data/local/tmp/' + $name
Run-Adb @('-s',$Device,'push',$file.FullName,$remote)
try {
    Run-Adb @('-s',$Device,'shell','run-as','com.gathin.tvhome','mkdir','-p','files/wallpapers')
    Run-Adb @('-s',$Device,'shell','run-as','com.gathin.tvhome','cp',$remote,('files/wallpapers/' + $name))
} finally {
    Run-Adb @('-s',$Device,'shell','rm',$remote)
}
Write-Host 'Picture imported. Reopen Home settings > Wallpaper gallery to select it.'
