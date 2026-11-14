# Parent setup guide: keep essentials available

The app blocks only the social-media package IDs explicitly listed in `AppMonitorService.java`. The list intentionally does **not** include WhatsApp, phone/calling apps, SMS/texting apps, Google Pay, PhonePe, maps, or location services. Those apps remain available because they are not in the block list. YouTube is included as a social/video app; remove its package ID from the list if your children need it for school.

## Websites

This app's current accessibility-based monitor only detects Android app launches. It does **not** filter websites inside Chrome, another browser, or in-app web views. Blocking browsers would also block useful sites and is not an appropriate substitute for website filtering.

For website restrictions, configure Google Family Link's web/content controls for the child's supervised Google account and Chrome, and review the controls on the child's device. For coverage across browsers and devices, consider a family-safe DNS/filtering service configured by a parent on the home router and devices. No filter catches every site, and off-network coverage depends on the service and device settings.

## Keep essential features working

- Do not add the phone, messaging, payment, maps, or system packages to `SOCIAL_APPS`.
- Keep Android's emergency calling available. Do not rely on this app to restrict calls or emergency features.
- Test calls, SMS, WhatsApp, payments, and location sharing after installing/configuring any separate parental-control or DNS service.
- The app is a basic helper, not tamper-proof device management. Children or device users may disable its Accessibility service; for stronger supervision, use Android's supported parental controls and explain the rules to your children.
