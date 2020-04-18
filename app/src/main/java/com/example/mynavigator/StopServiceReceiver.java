package com.example.mynavigator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class StopServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent service = new Intent(context, CanaryService.class);
        context.stopService(service);

        Intent activity = new Intent(context, MainActivity.class);
        activity.putExtra("sign", 2);
        context.startActivity(activity);


    }
}
