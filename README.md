# FireTV Glow

**A quieter Fire TV. A better home.**

A customized native Android TV launcher with a compact five-app dock, a photographic backdrop, smooth remote focus, and a small, reversible Fire TV debloat script.

Built on [Gathin23/tv-vibehack](https://github.com/Gathin23/tv-vibehack). The original package ID, `com.gathin.tvhome`, is retained for compatibility.

## The home screen

- Five customizable favorites in a compact dock.
- Compact tiles, translucent charcoal dock, and a clear focus highlight.
- Porsche wallpaper positioned above the dock; no app-colored background switching.
- Clock, date, Wi-Fi, settings, and input shortcuts.
- More installed apps available below the favorites using the remote.
- Remote-editable favorites: add, remove, and reorder 1–7 apps.
- Local wallpaper gallery with previews, picture import, ten-minute rotation, and pinning.
- Appearance controls for icon size, clock size, wallpaper position, and dimming.
- Clean home mode hides the lower row until Down is pressed.
- Connection diagnostics distinguish disconnected Wi-Fi, DNS failure, and failed secure web access.
- Local layout backup/restore plus JSON export/import.

The streaming apps are separate installations; this repository does not bundle them.

## Build and install

Requires JDK 17, Android SDK Platform 34, Build Tools 34, and Android platform-tools (`adb`) on PATH. Set `JAVA_HOME` and `ANDROID_HOME`, or configure the SDK in `launcher/local.properties`.

```powershell
.\build.ps1
.\build.ps1 -Device 'YOUR_TV_IP:5555'
```

The first command builds the debug APK and runs the included JVM tests. The second also installs and opens it. Enable ADB debugging on your TV and approve its connection prompt first.

APK output: `launcher/app/build/outputs/apk/debug/app-debug.apk`.

A newly generated debug signing key cannot update an existing installation signed with a different key. Keep your signing key private and reuse the original key for future updates.

### Home button on Fire TV

Keep Amazon's launcher enabled: it hosts Fire TV Settings. On the tested device, Home on Fire (`io.github.toolicious.homeonfire`) redirects the physical Home button. In that app, choose **Home (com.gathin.tvhome)** and enable its Home replacement/accessibility service. This helper is not bundled or automatically configured here.

Behavior varies with Fire OS and device restrictions. Verify physical Home and video playback after installation.

## Optional, reversible debloat

```powershell
.\debloat.ps1 -Device 'YOUR_TV_IP:5555'
.\debloat.ps1 -Device 'YOUR_TV_IP:5555' -Restore
```

The script disables only these four packages, when installed and currently enabled:

| Package | Purpose |
| --- | --- |
| `com.amazon.hybridadidservice` | Advertising identifier service |
| `com.amazon.kso.blackbird` | Advertising component |
| `com.amazon.device.telemetry.emitter` | Telemetry emitter |
| `com.amazon.wirelessmetrics.service` | Wireless metrics |

It records successful changes locally under ignored `device-state/`. Restore re-enables only those recorded packages. Nothing is uninstalled, and packages already disabled are left alone. Keep that directory for recovery.

The script does not change DNS, accessibility settings, Amazon's launcher, Appstore, updates, voice, Bluetooth, or streaming apps.

## Customize

Edit `launcher/app/src/main/res/values/config.xml` to change the dock's package IDs and column count, then rebuild. The bundled wallpaper is `launcher/app/src/main/res/drawable-nodpi/porsche_wallpaper.png`; its upward offset is in `MainActivity.java`. A scenic alternative is included as `scenic_wallpaper.png`.

Open **Settings** on the home screen for these controls. Fire TV system settings remain available inside Home settings. Select the Wi-Fi pill for a connection test; hold OK on it to open Wi-Fi settings.

The Porsche picture stays selected by default. Enable **Wallpaper gallery → Rotate every 10 minutes** to cycle through bundled and imported photos. **Keep current wallpaper** turns rotation off. Rotation is calculated while away from Home, updates on return, and does not run a background service over video playback. Reboot starts a fresh interval. Reddit fetching is not included.

Photo and layout import/export use Android's document picker when available. Some Fire TVs have no compatible picker. For those devices, use these PowerShell helpers with `adb` on PATH:

```powershell
.\import-wallpaper.ps1 -Device 'YOUR_TV_IP:5555' -Image 'C:\Pictures\wallpaper.jpg'
# First select "Save layout on this TV" in Home settings:
.\layout-backup.ps1 -Device 'YOUR_TV_IP:5555' -File '.\layout.json'
.\layout-backup.ps1 -Device 'YOUR_TV_IP:5555' -File '.\layout.json' -Import
```

After importing a layout with the helper, choose **Restore saved layout** on the TV. Backups include favorites and appearance settings, not image files or installed apps. A missing imported wallpaper falls back to the built-in picture. A malformed backup is rejected before any setting is changed. The companion scripts require the debug build's `run-as` access.

The optional connection test performs DNS resolution and an HTTPS request to `example.com`. No analytics or recurring network requests are added.

## Validation and limits

Built and tested on an Amazon AFTKRT running Fire OS 8 / Android 11. Version 3.0 passes 55 JVM tests. On-device checks cover backup round trips, rejecting malformed backups without partial changes, favorite ordering/count, gallery rotation and pinning. The remote editor, image preview/import helper, appearance changes, local backup controls, and successful DNS/HTTPS diagnostic were also exercised on the TV. Physical Home redirection and video playback were verified during initial setup. This is not a guarantee of compatibility with every Fire TV.

To run the device checks (they restore the original preferences afterward):

```powershell
cd launcher
.\gradlew.bat assembleDebugAndroidTest
adb -s YOUR_TV_IP:5555 install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s YOUR_TV_IP:5555 shell am instrument -w com.gathin.tvhome.test/com.gathin.tvhome.GlowIntegration
```

After setup, disable ADB debugging when it is no longer needed. Disconnecting the client alone does not turn off the TV's ADB listener.

## Credits and licensing

- Launcher source: Gathin's tv-vibehack, MIT; see [LICENSE](LICENSE).
- Outfit typeface: SIL Open Font License; see [font license](launcher/FONT-LICENSE-OFL.txt).
- Porsche wallpaper: user-supplied image associated with [this Reddit post](https://www.reddit.com/r/wallpaper/comments/1sb3p3d/rate_my_wallpaper_2560x1600_2023_porsche_911_gt3/). It is not covered by the software's MIT license; redistribution rights have not been established. This repository is initially private.
- Scenic alternative: generated during customization.

Independent customization project, not affiliated with Amazon, Apple, Porsche, or the listed apps.
