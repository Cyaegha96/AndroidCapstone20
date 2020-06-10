package com.example.moccaCanary.menu.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.BaseAdapter;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.moccaCanary.R;
import com.example.moccaCanary.user.UserActivity;

import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        SharedPreferences prefs;
        Preference userSettingsPreference;
        ListPreference userParameterPreference;
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            userSettingsPreference = (Preference) findPreference("userSettings");
            userSettingsPreference.setIntent(new Intent(getContext(),UserActivity.class));



            prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            userParameterPreference = (ListPreference) findPreference("distanceTo_parameter");
            if(!prefs.getString("distanceTo_parameter"," ").equals(" ")){
                userParameterPreference.setSummary(prefs.getString("distanceTo_parameter","500"));
            }

            prefs.registerOnSharedPreferenceChangeListener(preListener);

        }

        SharedPreferences.OnSharedPreferenceChangeListener preListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equals("distanceTo_parameter")){
                    userParameterPreference.setSummary(prefs.getString("distanceTo_parameter","500"));
                }
            }

        };
    }


}