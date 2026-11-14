package com.parentalcontrol.socialstopper;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AppMonitorService extends AccessibilityService {
    private static final Set<String> SOCIAL_APPS = new HashSet<>(Arrays.asList(
            "com.instagram.android", "com.facebook.katana", "com.facebook.lite",
            "com.zhiliaoapp.musically", "com.ss.android.ugc.trill", "com.twitter.android",
            "com.x.android", "com.snapchat.android", "com.reddit.frontpage",
            "com.google.android.youtube", "com.linkedin.android", "com.pinterest",
            "com.discord", "com.whatsapp"
    ));
    private String lastBlockedPackage = "";
    private long lastBlockTime = 0;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) return;
        String packageName = event.getPackageName().toString();
        if (!SOCIAL_APPS.contains(packageName) || packageName.equals(getPackageName())) return;

        var prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE);
        if (!prefs.getBoolean(MainActivity.KEY_BLOCKING, false)) return;
        if (packageName.equals(prefs.getString(MainActivity.KEY_ALLOWED_PACKAGE, ""))
                && System.currentTimeMillis() < prefs.getLong(MainActivity.KEY_ALLOW_UNTIL, 0)) return;

        long now = System.currentTimeMillis();
        if (packageName.equals(lastBlockedPackage) && now - lastBlockTime < 1500) return;
        lastBlockedPackage = packageName;
        lastBlockTime = now;
        Intent intent = new Intent(this, BlockActivity.class);
        intent.putExtra(BlockActivity.EXTRA_PACKAGE, packageName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override public void onInterrupt() { }
}
