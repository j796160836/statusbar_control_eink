# Status Bar Control (E-Ink)

語言：[English](README.md) | [繁體中文](README-zh-tw.md)

一款可從狀態列調整**媒體音量**、**鈴聲音量**、**鈴聲模式**（一般／震動）與**螢幕鎖定**（自動螢幕鎖定／永久不螢幕鎖定）的 Android App，專為**電子紙（E-Ink）Android 裝置**設計 —— 介面全採白底高對比，不依賴顏色或動畫來表示狀態。

## 功能

- 媒體音量、鈴聲音量滑桿
- 鈴聲模式切換：一般 / 震動
- 螢幕鎖定切換：自動螢幕鎖定 / 永久不鎖定（讓螢幕保持常亮）
- 三種同步的操作方式：
  1. **App 主畫面** — 所有控制項的滑桿與切換開關，以及 App 設定
  2. **快速設定磚（Quick Settings Tiles）** — 點擊「媒體音量」「鈴聲音量」磚會開啟滑桿對話框；「鈴聲/震動」磚直接原地切換
  3. **常駐通知** — 可在主畫面中選擇三種版面之一（按鈕分割版 / 滑桿分割版 / 滑桿堆疊版）
- 對電子紙友善設計系統：純白／純黑、方框線條控制項，用「反色填滿」而非顏色或漣漪動畫來表示當前選取狀態

## 系統需求

- Android 7.0（API 24）以上

部分功能需要非一般跳窗式的系統權限 —— App 偵測到尚未授權時，會直接帶你前往對應的系統設定頁面：

| 功能 | 所需權限 |
|---|---|
| 常駐通知（Android 13+） | 通知權限 |
| 鈴聲模式切換 | 零打擾權限（Do Not Disturb access / Notification Policy Access） |
| 螢幕鎖定切換 | 修改系統設定（`WRITE_SETTINGS`） |

## 建置方式

需要 JDK（17 以上）與 Android SDK（`compileSdk 37`）。

```sh
./gradlew assembleDebug      # 建置 debug APK
./gradlew installDebug       # 建置並安裝到已連接的裝置
```

> 在部分基於 GraalVM 的 JDK 上，完整建置可能會出現 `jlink`／`JdkImageTransform` 錯誤。若遇到此問題，請將 `JAVA_HOME` 指向標準 OpenJDK（例如 Temurin）。

## 專案結構

```
app/src/main/java/com/johnny/statusbar_control_eink/
├── audio/           # AudioManager 包裝：音量、鈴聲模式
├── screen/          # 螢幕逾時（Settings.System）包裝
├── prefs/           # App 設定值
├── tiles/           # 快速設定 TileService
├── tiledetail/       # 由音量磚開啟的滑桿對話框
├── notification/     # 常駐通知：前景服務、RemoteViews 建構器、
│                      # 開機自動啟動接收器、按鈕動作接收器
├── ui/               # Compose 主畫面（MainActivity）與電子紙風格元件
└── MainActivity.kt
```

## 授權條款

MIT © Johnny Sung —— 詳見 [LICENSE](LICENSE)
