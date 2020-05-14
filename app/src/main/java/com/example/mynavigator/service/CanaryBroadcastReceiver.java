package com.example.mynavigator.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;


public class CanaryBroadcastReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent){
        if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
            int type = intent.getIntExtra("type", -1);
            int confidence = intent.getIntExtra("confidence", 0);
            ((CanaryService) CanaryService.mContext).handleUserActivity(type, confidence);
        }
    }
}
