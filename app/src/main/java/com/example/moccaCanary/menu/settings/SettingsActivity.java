package com.example.moccaCanary.menu.settings;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

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
        ListPreference criteriaSelector;
        SwitchPreference gpsSwitch;
        SwitchPreference networkSwitch;
        SwitchPreference criteriaSwitch;
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

            criteriaSelector = (ListPreference) findPreference("criteria_selector");
            if(!prefs.getString("criteria_selector"," ").equals(" ")){
                String summary = "criteria의 세부설정을 진행합니다.";
                criteriaSelector.setSummary(summary + "\n현재 설정: "+ prefs.getString("criteria_selector","high"));
            }
            gpsSwitch = (SwitchPreference) findPreference("useGPS");
            networkSwitch = (SwitchPreference) findPreference("useNetwork");
            criteriaSwitch = (SwitchPreference) findPreference("useCriteria");

            prefs.registerOnSharedPreferenceChangeListener(preListener);

        }

        SharedPreferences.OnSharedPreferenceChangeListener preListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equals("distanceTo_parameter")){
                    String summary = "카나리아 서비스가 최대 몇백미터까지 탐색을 할지 설정합니다.";
                    userParameterPreference.setSummary(summary + "\n현재 탐색: "+ prefs.getString("distanceTo_parameter","500") +"m");
                    if(isServiceRunning(getContext())){
                        ((CanaryService)CanaryService.mContext).restartGeofenceService();
                    }
                }

                if(key.equals("useGPS")){
                    if(prefs.getBoolean("useGPS",true) == true){
                        Toast.makeText(getActivity().getApplicationContext(),"GPS Provider 사용 설정이 완료되었습니다.",Toast.LENGTH_SHORT).show();
                        restartLocationService();
                    }
                    else if(prefs.getBoolean("useGPS",true) == false){
                        Toast.makeText(getActivity().getApplicationContext(),"GPS Provider 사용이 중지되었습니다.",Toast.LENGTH_SHORT).show();
                        restartLocationService();
                    }
                }

                if(key.equals("useNetwork")){
                    if(prefs.getBoolean("useNetwork",true) == true){
                        Toast.makeText(getActivity().getApplicationContext(),"Network Provider 사용 설정이 완료되었습니다.",Toast.LENGTH_SHORT).show();
                        restartLocationService();
                    }
                    else if(prefs.getBoolean("useNetwork",true) == false){
                        Toast.makeText(getActivity().getApplicationContext(),"Network Provider 사용이 중지되었습니다.",Toast.LENGTH_SHORT).show();
                        restartLocationService();
                    }
                }
                if(key.equals("useCriteria")){
                    if(prefs.getBoolean("useCriteria",true) == true){
                        Toast.makeText(getActivity().getApplicationContext(),"Criteria 사용 설정이 완료되었습니다.",Toast.LENGTH_SHORT).show();
                        restartLocationService();
                    }
                    else if(prefs.getBoolean("useCriteria",true) == false){
                        Toast.makeText(getActivity().getApplicationContext(),"Criteria 사용이 중지되었습니다.",Toast.LENGTH_SHORT).show();
                        restartLocationService();
                    }
                }

                if(key.equals("criteria_selector")){
                    String summary = "criteria의 세부설정을 진행합니다.";
                    criteriaSelector.setSummary(summary + "\n현재 설정: "+ prefs.getString("criteria_selector","high"));
                    restartLocationService();
                }

                //만약 gps스위치와 criteria 스위치가 false인 경우
                if(!gpsSwitch.isChecked() && !criteriaSwitch.isChecked()){
                    networkSwitch.setEnabled(false);
                }
                //만약 network 스위치와 criteria 스위치가 false 인경우
                else if(!networkSwitch.isChecked() && !criteriaSwitch.isChecked()){
                    gpsSwitch.setEnabled(false);

                }
                //만약 gps 스위치와 network 스위치가 false인 경우
                else if(!gpsSwitch.isChecked() && !networkSwitch.isChecked()){
                    criteriaSwitch.setEnabled(false);
                }
                else{
                    gpsSwitch.setEnabled(true);
                    networkSwitch.setEnabled(true);
                    criteriaSwitch.setEnabled(true);
                }
            }

        };
        public void restartLocationService(){
            if(isServiceRunning(getContext())) {
                ((CanaryService)CanaryService.mContext).restartLocationService();
            }
        }
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