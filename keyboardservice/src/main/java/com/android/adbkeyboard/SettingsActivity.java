package com.android.adbkeyboard;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.provider.Settings;

public class SettingsActivity extends Activity {
    private static final String GLOBAL_SETTING_SHOW_INPUT_VIEW = "adbkeyboard_show_input_view";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setup ActionBar with back button
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.settings_title));
        }
        
        // Display the fragment as the main content
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new SettingsFragment())
                    .commit();
        }
    }
    
    @Override
    public boolean onNavigateUp() {
        finish();
        return true;
    }
    
    public static class SettingsFragment extends android.preference.PreferenceFragment {
        private static final String GLOBAL_SETTING_SHOW_INPUT_VIEW = "adbkeyboard_show_input_view";
        private SwitchPreference showInputViewPref;
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            
            showInputViewPref = (SwitchPreference) findPreference("show_input_view");
            
            // Load current value from Global Settings
            updatePreferenceFromGlobalSettings();
            
            // Set change listener
            showInputViewPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean enabled = (Boolean) newValue;
                    boolean success = false;
                    try {
                        // Try to write to Global Settings (requires system permission)
                        Settings.Global.putInt(getActivity().getContentResolver(), 
                                GLOBAL_SETTING_SHOW_INPUT_VIEW, enabled ? 1 : 0);
                        success = true;
                    } catch (SecurityException e) {
                        // Fallback to SharedPreferences if no permission
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        prefs.edit().putBoolean("show_input_view", enabled).apply();
                        success = true;
                    }
                    // Always update SharedPreferences as backup
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    prefs.edit().putBoolean("show_input_view", enabled).apply();
                    return success;
                }
            });
        }
        
        @Override
        public void onResume() {
            super.onResume();
            updatePreferenceFromGlobalSettings();
        }
        
        private void updatePreferenceFromGlobalSettings() {
            if (showInputViewPref != null) {
                try {
                    // Read from Global Settings
                    int value = Settings.Global.getInt(getActivity().getContentResolver(), 
                            GLOBAL_SETTING_SHOW_INPUT_VIEW, 0);
                    showInputViewPref.setChecked(value == 1);
                } catch (Exception e) {
                    // Fallback to SharedPreferences
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    showInputViewPref.setChecked(prefs.getBoolean("show_input_view", false));
                }
            }
        }
    }
}
