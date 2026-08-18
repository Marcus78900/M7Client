# M7 Client — Bedrock Companion

M7 Client is an independent Android companion/launcher for Minecraft Bedrock/MCPE.
It **does not bundle Minecraft** and uses its own package: `com.m7.client`.

## v0.1 Alpha

- Professional mobile-first M7 interface.
- Detects and opens the official Minecraft Bedrock app.
- Also detects the old M7/Apollon laboratory build if installed.
- Imports `.mcpack`, `.mcaddon`, `.mcworld`, `.mctemplate`, and `.mcskin` files into Minecraft using Android content URIs.
- Profiles: Survival, PvP, Creator and Battery.
- Persistent module settings.
- HUD/Visual/Performance module center.
- M7 Labs section for sandbox/training concepts.
- Config export/share.
- GitHub Actions workflow that builds an APK automatically.

## M7 Labs policy

`X-Ray`, `Scaffold`, `AutoClick` and `FastDrop` are represented in the UI/config model as **sandbox/training modules**. This repository does not implement bypasses, anti-cheat evasion, or automatic unfair multiplayer advantages. Their toggles are stored for future local/test integrations.

## Build

The GitHub workflow builds on every push to `main` and uploads `M7-Client-v0.1-alpha` as an Actions artifact.

Local build requirements:

- JDK 17
- Gradle 8.13
- Android SDK 36

Run:

```bash
gradle :app:assembleDebug
```

APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Project identity

Created for **M7 / Marcus**.
