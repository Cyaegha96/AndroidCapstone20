package com.example.moccaCanary.service;

import android.content.Context;
import android.content.ContextWrapper;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;

public class AudioHelper extends ContextWrapper {
    private static final String TAG = "AudioHelper";
    private AudioManager audioManager;

    public AudioHelper(Context base) {
        super(base);
    }

    //**오디오포커스 요청
    public void requestaudiofocus(){
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        audioManager.requestAudioFocus(mOnAudioFocusChangeListener, AudioManager.STREAM_NOTIFICATION, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);

    }
    //**오디오포커스 리스너
    public AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange){
                case AudioManager.AUDIOFOCUS_GAIN:

                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:

                case AudioManager.AUDIOFOCUS_LOSS:

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:

                default:
                    break;
            }
        }
    };

    public boolean headsetCheck() {
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

    public void removeFocusAudioManager(){
        //**오디오포커스 반납
        audioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
    }
}
