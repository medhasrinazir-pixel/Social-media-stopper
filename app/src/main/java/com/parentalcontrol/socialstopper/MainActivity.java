package com.parentalcontrol.socialstopper;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class MainActivity extends Activity {
    public static final String PREFS = "parental_settings";
    public static final String KEY_BLOCKING = "blocking_enabled";
    public static final String KEY_PIN_HASH = "pin_hash";
    public static final String KEY_SALT = "pin_salt";
    public static final String KEY_ALLOWED_PACKAGE = "allowed_package";
    public static final String KEY_ALLOW_UNTIL = "allow_until";
    public static final String KEY_BLOCKED_PACKAGES = "blocked_packages";

    private static final String DEFAULT_BLOCKED_PACKAGES =
            "com.instagram.android,com.facebook.katana,com.snapchat.android,com.twitter.android,com.x.android," +
                    "com.zhiliaoapp.musically,com.ss.android.ugc.trill,com.google.android.youtube,com.reddit.frontpage," +
                    "com.whatsapp,com.facebook.lite,com.linkedin.android";

    private LinearLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showMainScreen();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showMainScreen();
    }

    private void showMainScreen() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setGravity(Gravity.CENTER);
        rootLayout.setPadding(32, 32, 32, 32);

        ScrollView scrollView = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);

        TextView title = new TextView(this);
        title.setText("Social Media Stopper\nGuardian Control");
        title.setGravity(Gravity.CENTER);
        title.setTextSize(24);
        content.addView(title);

        if (prefs.getString(KEY_PIN_HASH, null) == null) {
            renderPinSetup(content, prefs);
        } else {
            renderGuardianPanel(content, prefs);
        }

        scrollView.addView(content);
        setContentView(scrollView);
    }

    private void renderPinSetup(LinearLayout content, SharedPreferences prefs) {
        TextView label = new TextView(this);
        label.setText("Create the parent/guardian PIN.");
        label.setPadding(0, 24, 0, 12);
        content.addView(label);

        final EditText pinInput = new EditText(this);
        pinInput.setHint("Enter 6 to 12 digit PIN");
        pinInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        pinInput.setSingleLine(true);
        content.addView(pinInput);

        Button saveButton = new Button(this);
        saveButton.setText("Save guardian PIN");
        saveButton.setOnClickListener(v -> {
            String pin = pinInput.getText().toString().trim();
            if (!pin.matches("\\d{6,12}")) {
                Toast.makeText(this, "Use 6 to 12 digits", Toast.LENGTH_SHORT).show();
                return;
            }

            byte[] saltBytes = new byte[16];
            new SecureRandom().nextBytes(saltBytes);
            String salt = Base64.encodeToString(saltBytes, Base64.NO_WRAP);
            prefs.edit()
                    .putString(KEY_SALT, salt)
                    .putString(KEY_PIN_HASH, hashPin(pin, salt))
                    .putString(KEY_BLOCKED_PACKAGES, DEFAULT_BLOCKED_PACKAGES)
                    .putBoolean(KEY_BLOCKING, false)
                    .apply();
            showMainScreen();
        });
        content.addView(saveButton);
    }

    private void renderGuardianPanel(LinearLayout content, SharedPreferences prefs) {
        boolean active = prefs.getBoolean(KEY_BLOCKING, false);

        TextView status = new TextView(this);
        status.setText(active ? "Blocking is ON" : "Blocking is OFF");
        status.setPadding(0, 20, 0, 8);
        status.setGravity(Gravity.CENTER);
        status.setTextSize(20);
        content.addView(status);

        Button toggleButton = new Button(this);
        toggleButton.setText(active ? "Turn blocking OFF" : "Turn blocking ON");
        toggleButton.setOnClickListener(v -> promptForPinAndToggle(!active));
        content.addView(toggleButton);

        TextView packagesLabel = new TextView(this);
        packagesLabel.setText("Blocked apps");
        packagesLabel.setPadding(0, 24, 0, 8);
        content.addView(packagesLabel);

        TextView blockedList = new TextView(this);
        blockedList.setText(buildBlockedPackageSummary(prefs));
        blockedList.setPadding(0, 0, 0, 12);
        content.addView(blockedList);

        final EditText packageInput = new EditText(this);
        packageInput.setHint("Add package name, e.g. com.instagram.android");
        packageInput.setSingleLine(true);
        content.addView(packageInput);

        Button addPackageButton = new Button(this);
        addPackageButton.setText("Add app to block list");
        addPackageButton.setOnClickListener(v -> {
            String pkg = packageInput.getText().toString().trim();
            if (pkg.isEmpty()) {
                Toast.makeText(this, "Enter package name", Toast.LENGTH_SHORT).show();
                return;
            }
            Set<String> packages = new LinkedHashSet<>(getBlockedPackages(prefs));
            packages.add(pkg);
            prefs.edit().putString(KEY_BLOCKED_PACKAGES, joinPackages(packages)).apply();
            packageInput.setText("");
            showMainScreen();
        });
        content.addView(addPackageButton);

        Button removePackageButton = new Button(this);
        removePackageButton.setText("Clear/Reset blocked app list");
        removePackageButton.setOnClickListener(v -> {
            prefs.edit().putString(KEY_BLOCKED_PACKAGES, DEFAULT_BLOCKED_PACKAGES).apply();
            showMainScreen();
        });
        content.addView(removePackageButton);

        Button accessibilityButton = new Button(this);
        accessibilityButton.setText("Open Android Accessibility settings");
        accessibilityButton.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        content.addView(accessibilityButton);

        Button changePinButton = new Button(this);
        changePinButton.setText("Change guardian PIN");
        changePinButton.setOnClickListener(v -> showChangePinPrompt());
        content.addView(changePinButton);

        TextView note = new TextView(this);
        note.setText("This app blocks only known app packages listed here. It needs Android Accessibility permission to work.");
        note.setPadding(0, 20, 0, 0);
        content.addView(note);
    }

    private void promptForPinAndToggle(boolean enable) {
        LinearLayout popup = new LinearLayout(this);
        popup.setOrientation(LinearLayout.VERTICAL);
        popup.setPadding(32, 32, 32, 32);

        TextView title = new TextView(this);
        title.setText("Parent/guardian confirmation");
        popup.addView(title);

        final EditText pinInput = new EditText(this);
        pinInput.setHint("Enter guardian PIN");
        pinInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        pinInput.setSingleLine(true);
        popup.addView(pinInput);

        Button confirmButton = new Button(this);
        confirmButton.setText(enable ? "Turn blocking ON" : "Turn blocking OFF");
        confirmButton.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
            String salt = prefs.getString(KEY_SALT, "");
            String expectedHash = prefs.getString(KEY_PIN_HASH, "");
            String enteredHash = hashPin(pinInput.getText().toString().trim(), salt);

            if (!expectedHash.equals(enteredHash)) {
                Toast.makeText(this, "Incorrect guardian PIN", Toast.LENGTH_SHORT).show();
                return;
            }

            prefs.edit()
                    .putBoolean(KEY_BLOCKING, enable)
                    .remove(KEY_ALLOWED_PACKAGE)
                    .putLong(KEY_ALLOW_UNTIL, 0)
                    .apply();

            if (enable && !isAccessibilityEnabled()) {
                Toast.makeText(this, "Open Accessibility settings and enable Social Media Stopper", Toast.LENGTH_LONG).show();
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }

            showMainScreen();
        });

        popup.addView(confirmButton);

        setContentView(popup);
    }

    private void showChangePinPrompt() {
        LinearLayout popup = new LinearLayout(this);
        popup.setOrientation(LinearLayout.VERTICAL);
        popup.setPadding(32, 32, 32, 32);

        final EditText currentPin = new EditText(this);
        currentPin.setHint("Current guardian PIN");
        currentPin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        currentPin.setSingleLine(true);

        final EditText newPin = new EditText(this);
        newPin.setHint("New guardian PIN (6 to 12 digits)");
        newPin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        newPin.setSingleLine(true);

        Button save = new Button(this);
        save.setText("Save new PIN");
        save.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
            String salt = prefs.getString(KEY_SALT, "");
            String currentHash = prefs.getString(KEY_PIN_HASH, "");

            if (!currentHash.equals(hashPin(currentPin.getText().toString().trim(), salt))) {
                Toast.makeText(this, "Incorrect current guardian PIN", Toast.LENGTH_SHORT).show();
                return;
            }

            String newValue = newPin.getText().toString().trim();
            if (!newValue.matches("\\d{6,12}")) {
                Toast.makeText(this, "Use 6 to 12 digits", Toast.LENGTH_SHORT).show();
                return;
            }

            byte[] nextSaltBytes = new byte[16];
            new SecureRandom().nextBytes(nextSaltBytes);
            String nextSalt = Base64.encodeToString(nextSaltBytes, Base64.NO_WRAP);
            prefs.edit()
                    .putString(KEY_SALT, nextSalt)
                    .putString(KEY_PIN_HASH, hashPin(newValue, nextSalt))
                    .apply();

            Toast.makeText(this, "Guardian PIN updated", Toast.LENGTH_SHORT).show();
            showMainScreen();
        });

        popup.addView(currentPin);
        popup.addView(newPin);
        popup.addView(save);
        setContentView(popup);
    }

    private boolean isAccessibilityEnabled() {
        String enabledServices = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabledServices == null) {
            return false;
        }
        return enabledServices.contains(new ComponentName(this, AppMonitorService.class).flattenToString());
    }

    private String buildBlockedPackageSummary(SharedPreferences prefs) {
        List<String> packages = getBlockedPackages(prefs);
        if (packages.isEmpty()) {
            return "No apps are currently blocked.";
        }
        return joinPackages(packages);
    }

    private List<String> getBlockedPackages(SharedPreferences prefs) {
        String raw = prefs.getString(KEY_BLOCKED_PACKAGES, DEFAULT_BLOCKED_PACKAGES);
        if (raw == null || raw.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<String> list = new ArrayList<>();
        for (String value : raw.split(",")) {
            String trimmed = value.trim();
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }
        return list;
    }

    public static List<String> getBlockedPackagesForService(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, MODE_PRIVATE);
        return new MainActivity().getBlockedPackages(prefs);
    }

    private String joinPackages(Iterable<String> packages) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (String item : packages) {
            if (item == null || item.trim().isEmpty()) continue;
            if (!first) {
                builder.append(", ");
            }
            builder.append(item.trim());
            first = false;
        }
        return builder.toString();
    }

    public static String hashPin(String pin, String salt) {
        if (pin == null || salt == null) {
            return "";
        }
        try {
            byte[] saltBytes = Base64.decode(salt, Base64.DEFAULT);
            PBEKeySpec spec = new PBEKeySpec(pin.toCharArray(), saltBytes, 120000, 256);
            byte[] bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to hash guardian PIN", e);
        }
    }
}
