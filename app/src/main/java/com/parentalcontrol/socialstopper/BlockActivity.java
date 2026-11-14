package com.parentalcontrol.socialstopper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class BlockActivity extends Activity {
    static final String EXTRA_PACKAGE = "target_package";
    private EditText pinInput;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        String target = getIntent().getStringExtra(EXTRA_PACKAGE);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);

        TextView message = new TextView(this);
        message.setText("This app is blocked by your parent or guardian.");
        message.setTextSize(22);
        message.setGravity(Gravity.CENTER);
        layout.addView(message);

        pinInput = new EditText(this);
        pinInput.setHint("Parent PIN");
        pinInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        pinInput.setSingleLine(true);
        layout.addView(pinInput);

        Button unlock = new Button(this);
        unlock.setText("Parent: allow this app for 15 minutes");
        unlock.setOnClickListener(v -> {
            String salt = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE)
                    .getString(MainActivity.KEY_SALT, null);
            String expected = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE)
                    .getString(MainActivity.KEY_PIN_HASH, null);
            if (salt != null && expected != null
                    && expected.equals(MainActivity.hashPin(pinInput.getText().toString(), salt))) {
                getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE).edit()
                        .putString(MainActivity.KEY_ALLOWED_PACKAGE, target)
                        .putLong(MainActivity.KEY_ALLOW_UNTIL, System.currentTimeMillis() + 15 * 60 * 1000L)
                        .apply();
                finish();
            } else {
                pinInput.setText("");
                Toast.makeText(this, "Incorrect parent PIN", Toast.LENGTH_SHORT).show();
            }
        });
        layout.addView(unlock);

        Button leave = new Button(this);
        leave.setText("Go back");
        leave.setOnClickListener(v -> {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.addCategory(Intent.CATEGORY_HOME);
            home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(home);
            finish();
        });
        layout.addView(leave);
        setContentView(layout);
    }
}
