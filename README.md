# Social Media Stopper

A small Android guardian-control tool for families.

What it does:
- lets a parent/guardian create a PIN
- turns blocking ON or OFF
- keeps a list of social-media apps to block
- watches for selected app launches through Android Accessibility
- shows a block screen when a blocked app opens
- allows the guardian to unlock a single app for 15 minutes using the PIN

Important notes:
- This is a practical app-level blocker, not a full OS-level lockdown.
- Android Accessibility permission must be enabled by the parent/guardian.
- It cannot block every browser or cloned app, and it only targets apps listed in the app's package list.
- Real protection can be improved by combining this with Android Family Link or device-level parental controls.

How to use:
1. Open this project in Android Studio.
2. Sync Gradle and install on the child device.
3. Open the app and create a guardian PIN.
4. Turn blocking ON.
5. Go to Settings -> Accessibility -> Social Media Stopper and enable it.
6. Add package names for apps you want blocked.

Example package names:
- com.instagram.android
- com.facebook.katana
- com.snapchat.android
- com.twitter.android
- com.x.android
- com.zhiliaoapp.musically
- com.ss.android.ugc.trill
- com.google.android.youtube
- com.reddit.frontpage

This project is intentionally small and family-focused, and it does not send any usage data to a server.
