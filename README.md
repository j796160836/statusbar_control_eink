# Status Bar Control (E-Ink)

Language: [English](README.md) | [繁體中文](README-zh-tw.md)

An Android app for adjusting **media volume**, **ring volume**, **ringer mode** (Normal/Vibrate), and **screen lock** (Auto Lock/Never Lock) from the status bar — built for **e-ink Android devices**, where every screen is white-background, high-contrast, and avoids color-only or animated state cues.

## Features

- Media volume and ring volume sliders
- Ringer mode toggle: Normal / Vibrate
- Screen lock toggle: Auto Lock / Never Lock (keep the screen on indefinitely)
- Three synced ways to control everything:
  1. **In-app dashboard** — sliders and toggles for all controls, plus app settings
  2. **Quick Settings tiles** — Media Volume and Ring Volume tiles open a slider dialog; the Ring/Vibrate tile toggles in place
  3. **Persistent notification** — pick one of three layouts (Buttons split / Slider split / Slider stacked) from the dashboard
- E-ink-friendly design system: pure white/black, boxy outlined controls, inverted-fill (not color or ripple) to show active state

## Requirements

- Android 7.0 (API 24) or higher

Some features need a permission that isn't a normal runtime prompt — the app detects when it's missing and takes you straight to the right settings screen:

| Feature | Permission needed |
|---|---|
| Persistent notification (Android 13+) | Notifications |
| Ringer mode toggle | Do Not Disturb access (Notification Policy Access) |
| Never Lock screen toggle | Modify system settings (`WRITE_SETTINGS`) |

## Building

Requires a JDK (17+) and the Android SDK (`compileSdk 37`).

```sh
./gradlew assembleDebug      # build a debug APK
./gradlew installDebug       # build and install on a connected device
```

> On some GraalVM-based JDKs, a full build can fail with a `jlink`/`JdkImageTransform` error. If that happens, point `JAVA_HOME` at a standard OpenJDK build (e.g. Temurin) instead.

## Project structure

```
app/src/main/java/com/johnny/statusbar_control_eink/
├── audio/           # AudioManager wrappers: volume, ringer mode
├── screen/          # Screen-timeout (Settings.System) wrapper
├── prefs/           # Persisted app settings
├── tiles/           # Quick Settings TileServices
├── tiledetail/       # Slider dialog opened from volume tiles
├── notification/     # Persistent notification: foreground service, RemoteViews
│                      # builder, boot receiver, action receiver
├── ui/               # Compose dashboard (MainActivity) and e-ink themed components
└── MainActivity.kt
```

## License

MIT © Johnny Sung — see [LICENSE](LICENSE)
