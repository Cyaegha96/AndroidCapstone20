package com.example.moccaCanary.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.moccaCanary.MainActivity;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GeofenceBroadcastReceiv";
    boolean Ntype = true;
    BroadcastReceiver broadcastReceiver;
    @Override
    public void onReceive(Context context, Intent intent) {



        //일단 간단하게, Geofence 접근에 관해서, Toast 메시지를 띄워주고, Notification 접근 표현을 하는 내용으로 구현.
        NotificationHelper notificationHelper = new NotificationHelper(context);

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        Log.d(TAG, "onReceive: geoFencing BroadCast 메시지 받음");

        if (geofencingEvent.hasError()) {
            Toast.makeText(context,"onReceive: geoFencing 이벤트 에러",Toast.LENGTH_SHORT);
            Log.d(TAG, "onReceive: geoFencing 이벤트 에러");
            return;
        }

        int accidnetCount = 0;
        String showingData = "";
        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
        for (Geofence geofence: geofenceList) {
            Log.d(TAG, "onReceive: " + geofence.getRequestId());

            accidnetCount = Integer.parseInt( geofence.getRequestId().split("@")[0]);
            showingData += geofence.getRequestId().split("@")[1];
        }
        int transitionType = geofencingEvent.getGeofenceTransition();
        //차를 타고 있으면 false 알림이 안가고 아니면 true 알림을 해주고
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("Ntype")) {
                    Ntype = intent.getBooleanExtra("Ntype", true);
                }
            }
        };

        if(Ntype) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Toast.makeText(context, "사고 다발지역 안에 들어왔습니다!", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("사고 다발지역 안에 들어왔습니다!", showingData,accidnetCount, MainActivity.class);
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Toast.makeText(context, "사고 다발지역 안을 지나고 있습니다!", Toast.LENGTH_SHORT).show();
                //notificationHelper.sendHighPriorityNotification("사고 다발지역 안을 지나고 있습니다!", showingData,accidnetCount, MainActivity.class);
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Toast.makeText(context, "사고 다발지역 안에서 나왔습니다!", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("사고 다발지역 안에서 나왔습니다!", showingData,accidnetCount, MainActivity.class);
                notificationHelper.abandonAudiofocus();
                break;
            }
        }
    }
}
