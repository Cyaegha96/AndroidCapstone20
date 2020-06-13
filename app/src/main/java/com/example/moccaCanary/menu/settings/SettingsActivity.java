package com.example.moccaCanary.menu.settings;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.BaseAdapter;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.moccaCanary.MainActivity;
import com.example.moccaCanary.R;
import com.example.moccaCanary.service.CanaryService;
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
                String summary = "카나리아 서비스가 최대 몇백미터까지 탐색을 할지 설정합니다.";
                userParameterPreference.setSummary(summary + "\n현재 탐색: "+ prefs.getString("distanceTo_parameter","500") +"m");
            }

            prefs.registerOnSharedPreferenceChangeListener(preListener);

        }

        SharedPreferences.OnSharedPreferenceChangeListener preListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equals("distanceTo_parameter")){
                    String summary = "카나리아 서비스가 최대 몇백미터까지 탐색을 할지 설정합니다.";
                    userParameterPreference.setSummary(summary + "\n현재 탐색: "+ prefs.getString("distanceTo_parameter","500") +"m");
                    if(isServiceRunning(getContext())){
                        ((CanaryService)CanaryService.mContext).restartLocationService();
                    }
                }
            }

        };
    }
    public static boolean isServiceRunning(Context context) {
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningServiceInfo rsi :
            am.getRunningServices(Integer.MAX_VALUE)) {
        if (CanaryService.class.getName().equals(rsi.service.getClassName())) //[서비스이름]에 본인 것을 넣는다.
          return true; }
    return false;
    }


}