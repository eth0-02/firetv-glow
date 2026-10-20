param([string]$Device)
$ErrorActionPreference = 'Stop'
Push-Location "$PSScriptRoot/launcher"
try {
    & ./gradlew.bat --no-daemon assembleDebug testDebugUnitTest
    if ($LASTEXITCODE -ne 0) { throw 'Build or tests failed.' }
} finally { Pop-Location }
if ($Device) {
    & adb connect $Device
    if ($LASTEXITCODE -ne 0) { throw 'ADB connection failed.' }
    & adb -s $Device install -r "$PSScriptRoot/launcher/app/build/outputs/apk/debug/app-debug.apk"
    if ($LASTEXITCODE -ne 0) { throw 'Installation failed.' }
    & adb -s $Device shell am start -n com.gathin.tvhome/.MainActivity
    if ($LASTEXITCODE -ne 0) { throw 'Could not open launcher.' }
}
