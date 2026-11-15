package com.parentalcontrol.socialstopper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class BlockActivity extends Activity {
    public static final String EXTRA_PACKAGE = "target_package";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String targetPackage = getIntent().getStringExtra(EXTRA_PACKAGE);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(32, 32, 32, 32);

        TextView title = new TextView(this);
        title.setText("This app is blocked by your parent or guardian.");
        title.setGravity(Gravity.CENTER);
        title.setTextSize(22);
        layout.addView(title);

        TextView packageText = new TextView(this);
        packageText.setText("App: " + (targetPackage == null ? "unknown" : targetPackage));
        packageText.setPadding(0, 12, 0, 12);
        layout.addView(packageText);

        final EditText pinInput = new EditText(this);
        pinInput.setHint("Guardian PIN");
        pinInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        pinInput.setSingleLine(true);
        layout.addView(pinInput);

        Button allowButton = new Button(this);
        allowButton.setText("Allow this app for 15 minutes");
        allowButton.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE);
            String salt = prefs.getString(MainActivity.KEY_SALT, null);
            String expectedHash = prefs.getString(MainActivity.KEY_PIN_HASH, null);
            if (salt == null || expectedHash == null) {
                Toast.makeText(this, "Guardian PIN not set", Toast.LENGTH_SHORT).show();
                return;
            }

            String enteredPin = pinInput.getText().toString().trim();
            String hash = MainActivity.hashPin(enteredPin, salt);
            if (!expectedHash.equals(hash)) {
                Toast.makeText(this, "Incorrect guardian PIN", Toast.LENGTH_SHORT).show();
                pinInput.setText("");
                return;
            }

            prefs.edit()
                    .putString(MainActivity.KEY_ALLOWED_PACKAGE, targetPackage)
                    .putLong(MainActivity.KEY_ALLOW_UNTIL, System.currentTimeMillis() + 15 * 60 * 1000L)
                    .apply();

            Toast.makeText(this, "App allowed for 15 minutes", Toast.LENGTH_SHORT).show();
            finish();
        });
        layout.addView(allowButton);

        Button backButton = new Button(this);
        backButton.setText("Go back home");
        backButton.setOnClickListener(v -> {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.addCategory(Intent.CATEGORY_HOME);
            home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(home);
            finish();
        });
        layout.addView(backButton);

        setContentView(layout);
    }
}
