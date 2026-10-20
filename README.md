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

The current wallpaper is static. Reddit wallpaper retrieval was blocked during setup; automatic ten-minute subreddit rotation is **not implemented**.

## Validation and limits

Built and tested on an Amazon AFTKRT running Fire OS 8 / Android 11. The customized launcher passed 51 included unit tests; its five-app layout was checked on-device. Physical Home redirection and video playback were also verified during setup. This is not a guarantee of compatibility with every Fire TV.

After setup, disable ADB debugging when it is no longer needed. Disconnecting the client alone does not turn off the TV's ADB listener.

## Credits and licensing

- Launcher source: Gathin's tv-vibehack, MIT; see [LICENSE](LICENSE).
- Outfit typeface: SIL Open Font License; see [font license](launcher/FONT-LICENSE-OFL.txt).
- Porsche wallpaper: user-supplied image associated with [this Reddit post](https://www.reddit.com/r/wallpaper/comments/1sb3p3d/rate_my_wallpaper_2560x1600_2023_porsche_911_gt3/). It is not covered by the software's MIT license; redistribution rights have not been established. This repository is initially private.
- Scenic alternative: generated during customization.

Independent customization project, not affiliated with Amazon, Apple, Porsche, or the listed apps.
