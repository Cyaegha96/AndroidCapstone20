package com.example.moccaCanary.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.moccaCanary.R;

import java.util.Random;

public class NotificationHelper extends ContextWrapper {

    private static final String TAG = "NotificationHelper";
    private  NotificationManager notificationManager;
    public NotificationHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannels();
        }
    }

    private String CHANNEL_NAME = "High priority channel";
    private String CHANNEL_ID = "com.example.notifications" + CHANNEL_NAME;

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannels() {

        //원래 알림음 설정으로 쓸 애였는데 이걸로 하면 무음모드시에 소리가 안남
       AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();

        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setDescription("this is the description of the channel.");
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
       notificationChannel.setSound(null, null);

        notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);

    }

    public void sendHighPriorityNotification(String title, String body, int accidnetCount, Class activityName ) {


        long[] pattern;

        if(accidnetCount >= 0 && accidnetCount <=4){
            pattern = new long[]{0, 10, 50, 10};
        }else if(accidnetCount > 5 && accidnetCount <= 10){
             pattern = new long[]{0, 100, 50, 100};

        }else{
            pattern = new long[]{10, 100, 50, 200};
        }


        Intent intent = new Intent(this, activityName);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 267, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String summaryText;
        if(accidnetCount == 0){
            summaryText = "제보 받은 알림";
        }else if (accidnetCount <100){
            summaryText =  "발생건수 :"+accidnetCount;
        }else if(accidnetCount==100){
            summaryText = "사망사고 발생지역";
        }else{
            summaryText="";
        }


        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
             .setContentTitle(title)
              .setContentText(body)
                .setSmallIcon(R.drawable.carlary_app_logo3)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle().setSummaryText(summaryText).setBigContentTitle(title).bigText(body))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults( Notification.DEFAULT_LIGHTS)  //default로 사용하는 설정입니다.
                .setVibrate(pattern)    //위에서 설정된 패턴으로 진동합니다.
                .setTimeoutAfter(5000)   //5초후 자동으로 닫힙니다.
                .build();

        AudioHelper audioHelper = new AudioHelper(getApplicationContext());
        audioHelper.requestaudiofocus();
        int notifyId = new Random().nextInt();
        //알림 보내주기
        NotificationManagerCompat.from(this).notify(notifyId, notification);

        //음악 실행
        MediaPlayer player1 = MediaPlayer.create(this, R.raw.pling);
        player1.start();
        audioHelper.removeFocusAudioManager();

    }

}
