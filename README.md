# VitaVault Mobile (Android Companion)

VitaVault Mobile is an Android companion app for the VitaVault web platform. It is designed to authenticate against VitaVault's mobile API, request Health Connect permissions, read selected health data, and sync supported readings into the VitaVault backend.

## Current Scope

This Android app is built as a GitHub-ready companion project and includes:
- secure login against VitaVault mobile auth endpoints
- persisted session with logout support
- configurable backend base URL for local/LAN testing
- Health Connect availability check
- Health Connect permission request flow
- sync actions for last 24 hours and last 7 days
- reading support for:
  - steps
  - heart rate
  - weight
  - blood pressure
  - oxygen saturation
- backend connection list view from VitaVault
- sync summary feedback inside the app

## Expected VitaVault Backend Endpoints

- `POST /api/mobile/auth/login`
- `POST /api/mobile/auth/logout`
- `GET /api/mobile/me`
- `GET /api/mobile/connections`
- `POST /api/mobile/device-readings`

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- DataStore
- Retrofit + Gson
- OkHttp logging interceptor
- Android Health Connect `1.1.0`

## Setup Notes

### Local backend from Android emulator
Use:

```text
http://10.0.2.2:3000/
```

### Local backend from a real phone on the same Wi‑Fi
Use your computer's LAN IP, for example:

```text
http://192.168.1.10:3000/
```
