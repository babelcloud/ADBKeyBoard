package com.android.adbkeyboard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Immediately start SettingsActivity
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        finish();
    }
}
