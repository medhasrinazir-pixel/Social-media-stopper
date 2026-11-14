package com.parentalcontrol.socialstopper;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class MainActivity extends Activity {
    static final String PREFS = "parental_settings";
    static final String KEY_BLOCKING = "blocking_enabled";
    static final String KEY_PIN_HASH = "pin_hash";
    static final String KEY_SALT = "pin_salt";
    static final String KEY_ALLOWED_PACKAGE = "allowed_package";
    static final String KEY_ALLOW_UNTIL = "allow_until";
    private LinearLayout layout;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        showScreen();
    }

    @Override protected void onResume() {
        super.onResume();
        if (layout != null) showScreen();
    }

    private void showScreen() {
        var prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);
        TextView title = new TextView(this);
        title.setText("Social Media Stopper\nParent / guardian controls");
        title.setTextSize(23);
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        if (prefs.getString(KEY_PIN_HASH, null) == null) {
            TextView setup = new TextView(this);
            setup.setText("Create a parent PIN. Keep it private and do not share it with children.");
            setup.setPadding(0, pad, 0, 0);
            layout.addView(setup);
            EditText pin = pinField("Create PIN (at least 6 digits)");
            layout.addView(pin);
            Button save = new Button(this);
            save.setText("Set parent PIN");
            save.setOnClickListener(v -> {
                String value = pin.getText().toString();
                if (!value.matches("\\d{6,12}")) {
                    Toast.makeText(this, "Use 6 to 12 digits", Toast.LENGTH_SHORT).show();
                    return;
                }
                byte[] saltBytes = new byte[16];
                new SecureRandom().nextBytes(saltBytes);
                String salt = android.util.Base64.encodeToString(saltBytes, android.util.Base64.NO_WRAP);
                prefs.edit().putString(KEY_SALT, salt).putString(KEY_PIN_HASH, hashPin(value, salt)).apply();
                showScreen();
            });
            layout.addView(save);
        } else {
            TextView status = new TextView(this);
            boolean active = prefs.getBoolean(KEY_BLOCKING, false);
            status.setText(active ? "Blocking is ON" : "Blocking is OFF");
            status.setTextSize(19);
            status.setPadding(0, pad, 0, 0);
            layout.addView(status);

            Button toggle = new Button(this);
            toggle.setText(active ? "Turn blocking OFF (parent PIN)" : "Turn blocking ON (parent PIN)");
            toggle.setOnClickListener(v -> changeBlocking(!active));
            layout.addView(toggle);

            Button permissions = new Button(this);
            permissions.setText("Enable / check app monitoring permission");
            permissions.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
            layout.addView(permissions);

            Button changePin = new Button(this);
            changePin.setText("Change parent PIN");
            changePin.setOnClickListener(v -> changePin());
            layout.addView(changePin);

            TextView note = new TextView(this);
            note.setText("Monitoring works only while Android Accessibility access is enabled. Add the apps your family wants blocked in the app's package list.");
            note.setPadding(0, pad, 0, 0);
            layout.addView(note);
        }
        setContentView(layout);
    }

    private EditText pinField(String hint) {
        EditText field = new EditText(this);
        field.setHint(hint);
        field.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        field.setSingleLine(true);
        return field;
    }

    private void changeBlocking(boolean enable) {
        EditText pin = pinField("Parent PIN");
        layout.addView(pin, 1);
        Button confirm = new Button(this);
        confirm.setText(enable ? "Confirm: turn ON" : "Confirm: turn OFF");
        confirm.setOnClickListener(v -> {
            var prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
            String salt = prefs.getString(KEY_SALT, "");
            String expected = prefs.getString(KEY_PIN_HASH, "");
            if (!expected.equals(hashPin(pin.getText().toString(), salt))) {
                Toast.makeText(this, "Incorrect parent PIN", Toast.LENGTH_SHORT).show();
                return;
            }
            prefs.edit().putBoolean(KEY_BLOCKING, enable).remove(KEY_ALLOWED_PACKAGE).putLong(KEY_ALLOW_UNTIL, 0).apply();
            showScreen();
            if (enable && !isAccessibilityEnabled()) {
                Toast.makeText(this, "Now enable Social Media Stopper in Android Accessibility settings", Toast.LENGTH_LONG).show();
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }
        });
        layout.addView(confirm, 2);
    }

    private boolean isAccessibilityEnabled() {
        String enabled = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return enabled != null && enabled.contains(new ComponentName(this, AppMonitorService.class).flattenToString());
    }

    private void changePin() {
        EditText oldPin = pinField("Current parent PIN");
        EditText newPin = pinField("New PIN (6 to 12 digits)");
        layout.addView(oldPin, 1);
        layout.addView(newPin, 2);
        Button save = new Button(this);
        save.setText("Save new PIN");
        save.setOnClickListener(v -> {
            var prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
            String salt = prefs.getString(KEY_SALT, "");
            if (!prefs.getString(KEY_PIN_HASH, "").equals(hashPin(oldPin.getText().toString(), salt))) {
                Toast.makeText(this, "Incorrect current PIN", Toast.LENGTH_SHORT).show();
                return;
            }
            String value = newPin.getText().toString();
            if (!value.matches("\\d{6,12}")) {
                Toast.makeText(this, "Use 6 to 12 digits", Toast.LENGTH_SHORT).show();
                return;
            }
            byte[] random = new byte[16]; new SecureRandom().nextBytes(random);
            String nextSalt = android.util.Base64.encodeToString(random, android.util.Base64.NO_WRAP);
            prefs.edit().putString(KEY_SALT, nextSalt).putString(KEY_PIN_HASH, hashPin(value, nextSalt)).apply();
            showScreen();
        });
        layout.addView(save, 3);
    }

    static String hashPin(String pin, String salt) {
        try {
            byte[] saltBytes = android.util.Base64.decode(salt, android.util.Base64.DEFAULT);
            PBEKeySpec spec = new PBEKeySpec(pin.toCharArray(), saltBytes, 120000, 256);
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return android.util.Base64.encodeToString(hash, android.util.Base64.NO_WRAP);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to verify PIN", e);
        }
    }
}
