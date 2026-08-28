# Office Ledger

A personal office attendance tracker, packaged as native Android and iOS apps
via [Capacitor](https://capacitorjs.com) — same HTML/CSS/JS running inside a
native shell on each platform, with a real app icon, launch screen, native
share sheet for backups, and native notifications.

## What it does

Office Ledger tracks attendance against the company's formula:

```
attendance % = days present ÷ eligible working days × 100
eligible working days = working days − leave − public holidays − comp off
```

The financial year runs **1 October – 30 September**. Day to day, it covers:

- **Daily log** — mark each day as Present, Leave, Worked Weekend, Comp Off
  Taken, or leave it as a weekend/holiday, from a calendar view.
- **Leave, as two separate pools** — Annual leave and Sick/Carer leave are
  tracked independently (Sick and Carer still share one balance between
  themselves; Annual is its own). Both are self-reported each month, since
  the real balance lives in the company's HR system, not in this app.
- **Public holidays** — add the dates and names your company releases each
  year; they're excluded from eligible working days automatically.
- **Comp off** — earned automatically when you log a worked weekend, spent
  when you mark a day as comp off taken.
- **Reminders** — an in-app banner, and a native notification on the phone's
  notification centre, when a leave pool hasn't been updated this month or
  the holiday list is missing for the current financial year.
- **Today / Calendar / trend views** — current percentage against your
  target, days needed to reach it this month, and a month-by-month table for
  the financial year to date.
- **About & update check** — Settings → About Office Ledger shows the
  installed version and can check GitHub for a newer tagged release.

Everything is stored only on the device it's installed on (Capacitor's
Preferences plugin — native SharedPreferences on Android, UserDefaults on
iOS) — there's no login, backend, or sync between devices. Export a backup
before switching phones or reinstalling.

## Project layout

- `www/index.html` — the actual app. One self-contained file. Edit this for
  any app change, on either platform.
- `android/` — the Android Studio project (buildable on Windows, macOS, or
  Linux).
- `ios/` — the Xcode project (buildable on macOS only — that's an Apple
  requirement, not a project limitation).

## Android — build on this Windows laptop

**Requirements:** [Android Studio](https://developer.android.com/studio) (free).
On first project open it downloads the Android SDK and Gradle itself — no manual
setup needed.

1. Open Android Studio → **Open** → select the `android` folder in this repo
   (not the repo root — Android Studio needs `android/build.gradle` at the
   project root it opens).
2. Let it sync (first sync downloads dependencies — can take a few minutes).
3. Plug in your Android phone via USB with **USB debugging** enabled (Settings →
   About phone → tap **Build number** 7 times to unlock Developer options, then
   Settings → Developer options → **USB debugging** → on), or use the built-in
   emulator.
4. Pick your device from the toolbar dropdown and hit **Run** (▶). That's it — no
   signing, no Apple-style device trust step, no expiry. The first time a
   reminder notification fires, Android will prompt for notification
   permission.

**To get a standalone APK file** (to install without a cable, e.g. via email or a
USB drive): **Build → Build App Bundle(s) / APK(s) → Build APK(s)**. Android
Studio drops it at `android/app/build/outputs/apk/debug/app-debug.apk`. Copy that
file to the phone and open it — Android will ask you to allow installs from that
source once, then it installs like any app. This debug build doesn't expire and
doesn't need a Play Store listing.

Command-line alternative, no Android Studio UI needed once the SDK is installed:
```
cd android
gradlew.bat assembleDebug
```
The APK lands at the same `outputs/apk/debug/app-debug.apk` path.

## iOS — build on a Mac

**Requirements:** a Mac with Xcode, and your Apple ID signed into it (Xcode →
Settings → Accounts). A free Apple ID installs the app on your own iPhone via
cable, re-signing every 7 days; the $99/year Apple Developer Program removes
that limit and is what App Store submission requires.

1. Open `ios/App/App.xcodeproj` in Xcode.
2. Select the **App** target → **Signing & Capabilities** → set **Team** to your
   Apple ID.
3. Plug in your iPhone (or pick it from the device menu), hit **Run** (▶).
4. First run only: on the iPhone, **Settings → General → VPN & Device Management**
   → trust the developer certificate. iOS will also prompt for notification
   permission the first time a reminder fires.

This project uses Swift Package Manager, not CocoaPods, so there's nothing extra
to install.

## Updating the app itself

Edit `www/index.html`, then sync it into both native projects:
```
npm install
npx cap sync android
npx cap sync ios
```
(`sync` picks up both app code and any Capacitor *plugin* changes; `cap copy`
alone is only enough for a plain code edit that doesn't touch dependencies.)
Then re-run from Android Studio / Xcode as above.

The app version shown on the About page comes from the `APP_VERSION` constant
near the top of `www/index.html` — bump it alongside `package.json`'s
`"version"` when you cut a release, and tag the GitHub release `vX.Y.Z` to
match, so the in-app update check has something newer to find.

## What's different from the plain web version

- **Backups**: Settings → Export backup opens the native share sheet (Android:
  send to Drive, email, Bluetooth, etc.; iOS: AirDrop, Files, Mail) instead of a
  browser download — via the `@capacitor/filesystem` and `@capacitor/share`
  plugins.
- **Notifications**: reminder banners also push a real notification to the
  phone's notification centre (once per reminder period) via
  `@capacitor/local-notifications`.
- **Update check**: About → Check for updates opens release links in the
  system browser via `@capacitor/browser`, instead of navigating away from
  the app's own webview.
- **Storage**: still local to the device only — no backend, no login, same as
  the web version. Export a backup before uninstalling or switching phones.
- **Icon & splash**: a generated teal "ledger" mark, consistent across both
  platforms and the web favicon.

## Publishing to app stores (optional, later)

Not required to use the app yourself.
- **Play Store**: enroll in the [Google Play Console](https://play.google.com/console)
  ($25 one-time), build a signed **release** APK/AAB (Build → Generate Signed
  Bundle/APK in Android Studio — you'll create a signing key the first time),
  and upload it. Review is usually a few hours to a couple of days.
- **App Store**: enroll in the Apple Developer Program ($99/year), then in Xcode:
  Product → Archive → upload via the Organizer to App Store Connect. Review is
  typically 1–3 days.

App/bundle ID is currently `com.debarun.officeledger` on both platforms — change
it in `capacitor.config.json`, and in Android Studio / Xcode's respective
project settings, if you want a different one (e.g. under a company account).
