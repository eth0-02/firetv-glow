param(
    [Parameter(Mandatory=$true)][ValidatePattern('^[a-zA-Z0-9.:-]+$')][string]$Device,
    [switch]$Restore
)
$ErrorActionPreference = 'Stop'
function Invoke-Adb([string[]]$Arguments) {
    $result = & adb -s $Device @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) { throw "ADB failed: $result" }
    return ($result -join "`n")
}
& adb connect $Device
if ($LASTEXITCODE -ne 0) { throw 'ADB connection failed.' }
$stateDir = Join-Path $PSScriptRoot ('device-state/' + ($Device -replace ':','_'))
New-Item -ItemType Directory -Force $stateDir | Out-Null
$ledger = Join-Path $stateDir 'disabled-by-us.txt'
$candidates = @(
    'com.amazon.hybridadidservice',
    'com.amazon.kso.blackbird',
    'com.amazon.device.telemetry.emitter',
    'com.amazon.wirelessmetrics.service'
)
$recorded = @()
if (Test-Path $ledger) { $recorded = @(Get-Content $ledger | Where-Object { $_ }) }
if ($Restore) {
    foreach ($package in @($recorded)) {
        if ($package -notin $candidates) { throw "Unexpected package in ledger: $package" }
        $result = Invoke-Adb @('shell','pm','enable','--user','0',$package)
        if ($result -notmatch 'new state: enabled') { throw "Restore not confirmed: $result" }
        $recorded = @($recorded | Where-Object { $_ -ne $package })
        Set-Content -LiteralPath $ledger -Value $recorded
        Write-Host "Restored $package"
    }
} else {
    $enabled = (Invoke-Adb @('shell','pm','list','packages','-e','--user','0')) -split '\r?\n'
    foreach ($package in $candidates) {
        if ("package:$package" -notin $enabled) { continue }
        $result = Invoke-Adb @('shell','pm','disable-user','--user','0',$package)
        if ($result -notmatch 'new state: disabled-user') { throw "Disable not confirmed: $result" }
        if ($package -notin $recorded) {
            Add-Content -LiteralPath $ledger -Value $package
            $recorded += $package
        }
        Write-Host "Disabled $package"
    }
}
