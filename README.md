# Marriott Bonvoy demo app

Kotlin / Jetpack Compose look-alike of the Marriott Bonvoy Android app. Sign-in is stubbed;
stays, points balance and hotel search are local demo data. **Redeem points** is the only
screen that talks to a backend: `POST {BACKEND_BASE_URL}/api/bonvoy/points/redeem` (the `bonvoy` vertical in `event-driven-devin`).

## Build

```sh
./gradlew assembleDebug            # app/build/outputs/apk/debug/app-debug.apk
./gradlew test
./gradlew lint
```

Requires JDK 17 and an Android SDK (`ANDROID_HOME` or `local.properties` → `sdk.dir`).

## Backend URL

Baked in at build time as `BuildConfig.BACKEND_BASE_URL`. Default is `http://10.0.2.2:3000`
(the emulator's alias for the host machine). Override with either:

```sh
./gradlew assembleDebug -PbonvoyBaseUrl=https://demo.example.com
BONVOY_BASE_URL=https://demo.example.com ./gradlew assembleDebug
```

The active URL is shown on the **Account** tab.

## Presenter token

The backend only raises a Slack alert and a Devin session for requests carrying
`X-Bonvoy-Demo-Token`, which the app sends only when built with the token:

```sh
./gradlew assembleDebug -PbonvoyBaseUrl=https://devindemos.com -PbonvoyDemoToken=<token>
```

Leave it off for development and for verifying a fix — the redemption still
fails the same way, it just does not page anyone.

## Run on the emulator

```sh
emulator -avd devin -no-window -no-audio -gpu swiftshader_indirect &
adb wait-for-device && adb shell getprop sys.boot_completed
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.marriott.bonvoy.demo/com.marriott.bonvoy.MainActivity
```

Flow: Sign in → Redeem points (or Find & Reserve) → pick a hotel → Redeem N points.
