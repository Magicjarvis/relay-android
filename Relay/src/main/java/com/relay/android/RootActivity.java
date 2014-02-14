package com.relay.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.Toast;

public class RootActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        handleIntent(new Intent());
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        String type = intent.getType();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (!prefs.contains("username")) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        if (Intent.ACTION_SEND.equals(action)) {
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (sharedText != null) {
                Intent i = new Intent(this, SendActivity.class);
                i.putExtra("url", sharedText);
                startActivity(i);
                finish();
                return;
            } else {
                Toast.makeText(getBaseContext(), "Error sharing link!", Toast.LENGTH_SHORT).show();
            }
        }
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }
}
