package com.parentalcontrol.socialstopper;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.accessibility.AccessibilityEvent;

import java.util.Arrays;
import java.util.List;

public class AppMonitorService extends AccessibilityService {
    private static final List<String> DEFAULT_BLOCKED = Arrays.asList(
            "com.instagram.android",
            "com.facebook.katana",
            "com.facebook.lite",
            "com.snapchat.android",
            "com.twitter.android",
            "com.x.android",
            "com.zhiliaoapp.musically",
            "com.ss.android.ugc.trill",
            "com.google.android.youtube",
            "com.reddit.frontpage",
            "com.whatsapp",
            "com.linkedin.android"
    );

    private String lastBlockedPackage = "";
    private long lastBlockTime = 0L;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) {
            return;
        }

        String packageName = event.getPackageName().toString();
        if (!isBlockedPackage(packageName)) {
            return;
        }

        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE);
        if (!prefs.getBoolean(MainActivity.KEY_BLOCKING, false)) {
            return;
        }

        String allowedPackage = prefs.getString(MainActivity.KEY_ALLOWED_PACKAGE, "");
        long allowUntil = prefs.getLong(MainActivity.KEY_ALLOW_UNTIL, 0L);
        if (packageName.equals(allowedPackage) && System.currentTimeMillis() < allowUntil) {
            return;
        }

        long now = System.currentTimeMillis();
        if (packageName.equals(lastBlockedPackage) && (now - lastBlockTime) < 2000L) {
            return;
        }

        lastBlockedPackage = packageName;
        lastBlockTime = now;

        Intent intent = new Intent(this, BlockActivity.class);
        intent.putExtra(BlockActivity.EXTRA_PACKAGE, packageName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private boolean isBlockedPackage(String packageName) {
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE);
        String raw = prefs.getString(MainActivity.KEY_BLOCKED_PACKAGES, "");
        if (raw == null || raw.trim().isEmpty()) {
            return DEFAULT_BLOCKED.contains(packageName);
        }
        String[] packages = raw.split(",");
        for (String candidate : packages) {
            String trimmed = candidate.trim();
            if (!trimmed.isEmpty() && trimmed.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onInterrupt() {
        // Nothing to do here.
    }
}
