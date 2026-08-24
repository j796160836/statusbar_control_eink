# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project purpose

An Android app that lets the user adjust **media volume**, **ring volume**, **ringer mode** (Normal/Vibrate), and **screen auto-lock** (Auto Lock/Never Lock) from the status bar area on an **e-ink Android device**. Every UI surface — the in-app dashboard, Quick Settings tiles, and the persistent notification — follows an e-ink design system: pure white background, pure black content, boxy shapes with a bit of corner rounding, no ripple/color-only state cues (active states use inverted black-fill instead), since the target display is low-color and high-latency.

There is no README; this file plus the code is the source of truth.

## Build system

Gradle project (Kotlin DSL) using the version catalog at `gradle/libs.versions.toml` for all dependency/plugin versions — add new dependencies there rather than hardcoding versions in `app/build.gradle.kts`.

- Single module: `:app`, namespace/applicationId `com.johnny.statusbar_control_eink`
- `minSdk 24`, `targetSdk`/`compileSdk 37`
- Kotlin `2.2.10`, AGP `9.3.1`, Compose BOM `2026.02.01`
- UI toolkit: Jetpack Compose with Material3 (no XML layouts for app screens — XML layouts only exist for the notification's `RemoteViews`, see below)

### Common commands

Run from the project root using the wrapper (`./gradlew`):

- Build debug APK: `./gradlew assembleDebug`
- Install on connected device/emulator: `./gradlew installDebug`
- Run unit tests (JVM, `app/src/test`): `./gradlew test`
- Run a single unit test class: `./gradlew test --tests "com.johnny.statusbar_control_eink.ExampleUnitTest"`
- Run instrumented tests (`app/src/androidTest`, requires a device/emulator): `./gradlew connectedAndroidTest`
- Lint: `./gradlew lintDebug` (always run this after touching notification `RemoteViews` layouts — see gotchas below; it does not catch the RemoteViews class-allowlist issue, but does catch API-level and manifest issues)
- Clean build: `./gradlew clean`

### Known environment quirk: GraalVM breaks the build

This machine's default JDK (GraalVM, matching `gradle/gradle-daemon-jvm.properties`' `toolchainVersion=25`) fails AGP's `androidJdkImage` transform with a `jlink` error on `compileDebugJavaWithJavac` / any full `assembleDebug`. A plain OpenJDK (e.g. Temurin 25, already installed at `/Library/Java/JavaVirtualMachines/temurin-25.jdk`) does not have this problem. If a build fails with `JdkImageTransform` / `jlink` errors, stop the daemon and rerun with the other JDK:

```
./gradlew --stop
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home ./gradlew assembleDebug
```

`compileDebugKotlin` alone works fine under either JDK — it's specifically the full `assembleDebug`/`lintDebug` pipeline that needs the working JDK.

## Architecture

Three independent UI surfaces all drive the **same** underlying state through **shared controller classes** — never touch `AudioManager`/`Settings.System` directly from UI code:

| Concern | Controller |
|---|---|
| Media/ring stream volume | `audio/VolumeController.kt` |
| Ringer mode (Normal/Vibrate) | `audio/RingerModeController.kt` (sealed `RingerToggleResult`: `Success` / `NeedsNotificationPolicyAccess`) |
| Screen timeout (Auto Lock/Never Lock) | `screen/ScreenTimeoutController.kt` (sealed `ScreenTimeoutResult`: `Success` / `NeedsWriteSettingsPermission`) |

Both `RingerModeController` and `ScreenTimeoutController` require a special (non-runtime-prompt) permission — Notification Policy Access and `WRITE_SETTINGS` respectively — checked before acting, with the same fallback pattern everywhere it's used: show the user why, then launch the relevant system settings screen (`ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS` / `ACTION_MANAGE_WRITE_SETTINGS`).

### The three UI surfaces

1. **Dashboard** (`MainActivity.kt`) — a Compose screen with sliders/toggles for everything, plus settings: enable persistent notification, resume-on-boot, notification layout style picker, screen lock. State syncs live via a dynamically-registered `BroadcastReceiver` for `AudioManager.RINGER_MODE_CHANGED_ACTION` and the (non-public-API) `"android.media.VOLUME_CHANGED_ACTION"` — see `audio/AudioBroadcastActions.kt`'s `SystemAudioBroadcasts` for why that string is hand-declared instead of referencing an SDK constant. All dynamic receivers in this codebase use `ContextCompat.registerReceiver(..., RECEIVER_NOT_EXPORTED)`.

2. **Quick Settings Tiles** (`tiles/`) — `MediaVolumeTileService` and `RingVolumeTileService` extend `BaseVolumeTileService` (shared listening/update logic) and open `tiledetail/TileDetailActivity` (a translucent dialog Activity with a real Compose `Slider`) via `startActivityAndCollapse` on tap, since a QS tile itself can't host a drag gesture. `VibrateToggleTileService` toggles in place with no detail screen. **Important**: `startActivityAndCollapse(Intent)` is deprecated and throws on API 34+; both tile services branch on `Build.VERSION.SDK_INT >= UPSIDE_DOWN_CAKE` to use the `PendingIntent` overload instead.

3. **Persistent notification** (`notification/`) — a foreground service (`StatusBarControlService`, `foregroundServiceType="specialUse"`) whose `RemoteViews` content is entirely built by `EinkNotificationBuilder` (the single source of truth other code must funnel through — both the service's own broadcast-triggered refresh and button-tap-triggered refresh call `EinkNotificationBuilder.refresh()`, so the two paths can never diverge). Button taps go to the manifest-registered (not dynamic) `NotificationActionReceiver`, which works even if the app process/service has died. `BootReceiver` restarts the service after reboot, gated by **two independent** `SettingsPrefs` flags (`notificationEnabled` AND `resumeOnBoot` must both be true).

   The notification has **3 user-selectable layouts** (`NotificationLayoutStyle` enum → `notification_expanded_{buttons_split,slider_split,slider_stacked}.xml`), picked from the dashboard and persisted in `SettingsPrefs`. All three share the same view IDs (`tv_media_value`, `btn_media_minus`/`plus`, `pb_media` for the slider variants, `btn_ringer_normal`/`vibrate`, `btn_screen_auto_lock`/`never_lock`) so `EinkNotificationBuilder` populates them with one code path regardless of which layout is inflated — `RemoteViews.setX()` calls targeting a view ID absent from the current layout are silently no-ops, which is exactly what makes this safe.

### RemoteViews gotchas (learned the hard way — verify on-device, lint won't catch these)

Notification `RemoteViews` only allow a curated set of Android widget classes to be inflated by SystemUI. Two classes that seem reasonable but are **rejected at inflate time** (`InflateException: Class not allowed to be inflated`), only visible in `adb logcat` filtered for `StatusBar`/`InflateException`, never as a build or lint error:
- **`SeekBar`** — not allowed. The "slider" notification layouts use a plain `ProgressBar` (`style="?android:attr/progressBarStyleHorizontal"`) as a **read-only** level indicator instead, with real `[-]`/`[+]` buttons doing the actual adjustment (there is no supported way to get a live drag gesture out of a notification back to the app anyway).
- **A bare `<View>`** (e.g. used as a spacer) — also not allowed. Use a zero-content `TextView`, or a margin on the adjacent view, instead.

If you add a 4th notification layout variant or edit an existing one, install and open it on a real device/emulator and check logcat before assuming it works — a broken layout fails silently from the Kotlin side (the service keeps running, `notify()` returns normally) and the notification just doesn't render.

### E-ink design components

`ui/components/EinkComponents.kt` — reusable Compose widgets: `EinkOutlinedButton`, `EinkStepper`, `EinkToggleSwitch` (bracket-style `[Label]`), `EinkRadioOption` (inverted-fill selected state), `EinkOutlinedSlider` (custom bordered thumb/track, discrete integer steps). `ui/theme/` forces the light color scheme unconditionally (no dark theme, no dynamic color) and disables ripple. When adding a new active/inactive state anywhere (dashboard, tile, or notification), prefer the inverted-fill convention (`bg_eink_button_active.xml`) over bracket text — bracket text was the original pattern but was replaced for the ringer-mode and screen-lock notification rows after user feedback that it read as ambiguous.
