# Social Media Stopper (Android)

A small, parent-controlled Android app that detects launches of a configurable list of social-media apps and displays a block screen. Parents create a PIN, and the PIN is required to turn blocking on/off, change the PIN, or allow a selected app for 15 minutes.

## Build and install

1. Open this repository in Android Studio and allow Gradle sync (Android Gradle Plugin 8.7.3; JDK 17; Android SDK 35).
2. Build and install the `app` configuration on the child's Android phone.
3. A parent/guardian opens the app and creates a 6–12 digit PIN.
4. A parent enables blocking, then enables **Social Media Stopper app monitor** in Android **Settings → Accessibility**. Android will show its standard accessibility disclosure/confirmation; review and accept only if you agree.

## Important limitations

- This app uses Android Accessibility events. Android must keep the service enabled; the user/device administrator can disable it, and some manufacturers may stop background services. It is not tamper-proof and does not block websites, other devices, or apps not listed in `AppMonitorService.java`.
- The package list is an example starter list. Add package IDs for the specific apps you want to block. Different regional or lightweight versions may have different IDs.
- A parent PIN is a local app control, not an Android system lock. Protect the device and PIN, and use Android/Google Family Link or your device manufacturer's parental-control features for stronger protections. Discuss the rules with your children and use monitoring transparently.
- Reinstalling/clearing app data resets the locally stored PIN and settings.

## Included controls

- Parent PIN is stored as a salted PBKDF2 hash (not plain text).
- PIN required for turning the block on/off and changing the PIN.
- A parent can grant one detected app a 15-minute exception with the PIN.
- The app does not collect or transmit usage data.
