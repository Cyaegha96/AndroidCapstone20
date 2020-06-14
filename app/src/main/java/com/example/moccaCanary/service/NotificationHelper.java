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
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.moccaCanary.R;

import java.util.Random;

public class NotificationHelper extends ContextWrapper {
    AudioManager audioManager;//**
    public static Context mContext;
    final Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
    int currentVolume;

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

        int notifyId = new Random().nextInt();
        requestaudiofocus();
        int currentVolumeCheck = 0;
        NotificationManagerCompat.from(this).notify(notifyId, notification);
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Log.e("MyApp","Silent mode" + currentVolume);

        if(currentVolume == 0) {
            currentVolumeCheck = 1;
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_RING) / 3, AudioManager.FLAG_PLAY_SOUND);
        }

        switch (audioManager.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                if(headsetCheck()){
                    vibrator.vibrate(pattern, // 진동 패턴을 배열로
                            -1);
                    MediaPlayer player1 = MediaPlayer.create(this, R.raw.pling);
                    player1.start();
                }
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                Log.i("MyApp","Vibrate mode");

                MediaPlayer player1 = MediaPlayer.create(this, R.raw.pling);
                player1.start();

                break;
            case AudioManager.RINGER_MODE_NORMAL:
                Log.i("MyApp","Normal mode");
                MediaPlayer player11 = MediaPlayer.create(this, R.raw.pling);
                player11.start();
                break;
        }
        if(currentVolumeCheck == 1){
            currentVolumeCheck =0;
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_PLAY_SOUND);
        }
        audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_NOTIFICATION, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK);

    }
    //**오디오포커스 요청
    private void requestaudiofocus(){
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_NOTIFICATION, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);

    }
    public void abandonAudiofocus(){
        audioManager.abandonAudioFocus(audioFocusChangeListener);
    }
    //**오디오포커스 리스너
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {
                    switch (focusChange){
                        case AudioManager.AUDIOFOCUS_GAIN:

                        case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:

                        case AudioManager.AUDIOFOCUS_LOSS:

                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:

                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume ,AudioManager.FLAG_PLAY_SOUND);
                        default:
                            break;

                    }
                }
            };
    private boolean headsetCheck() {
        boolean chkFlag = false; // 연결 x로 초기화

        AudioManager mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        AudioDeviceInfo[] devices;

        // API 레벨이 23 미만인 경우(isWiredHeadsetOn 메소드 사용)
        // isWiredHeadsetOn : 현재 헤드셋이 연결되었는가?
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (mAudioManager.isWiredHeadsetOn()) {
                chkFlag = true;
            }
            // API 레벨이 23 이상인 경우(getDevices 메소드 사용)
            // getDevices : 현재 연결된 오디오 기기 목록을 가져오는 메소드
        } else {
            devices = mAudioManager.getDevices(AudioManager.GET_DEVICES_ALL);
            // 연결된 기기 목록 중, 헤드셋이 있는가?
            for (AudioDeviceInfo device : devices) {
                if (device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET
                        || device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADPHONES) {
                    chkFlag = true;
                }
            }
        }

        return chkFlag;
    }

}
