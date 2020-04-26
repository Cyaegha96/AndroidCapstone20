package com.example.mynavigator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.widget.Toast;

public class AlertReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        boolean isEntering = intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, false);
        // boolean getBooleanExtra(String name, boolean defaultValue)
        if(isEntering)
            Toast.makeText(context, "목표 지점에 접근중입니다..", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(context, "목표 지점에서 벗어납니다..", Toast.LENGTH_LONG).show();
    }
}
