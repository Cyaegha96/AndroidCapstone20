package com.example.mynavigator;
import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class CanaryService extends Service implements LocationListener {

    private static final String TAG = "CanaryService";
    private static Context mContext;

    boolean canGetLocation = false;

    Location location; // Location
    double latitude; // Latitude
    double longitude; // Longitude

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 1 meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 1; // 1 second

    protected LocationManager locationManager;

    private static final String CHANNEL_ID = "channel_01";

    private Location getLocation() {
        Log.d(TAG,"getLocation Service");
        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled && !isNetworkEnabled) {

            } else {
                int hasFineLocationPermission = ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
                int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION);
                if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                        hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

                } else return null;
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.
                            NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(LocationManager.
                                GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.d("@@@", ""+e.toString());
        }
        return location;
    }

    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(CanaryService.this);
        }
    }

    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }


    /**
     * Function to get longitude
     * */
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        getLocation();
        createNotification();
        Log.d(TAG,"onCreate ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand ");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy");
        super.onDestroy();
        stopUsingGPS();
        removeNotification();
    }

    private void createNotification() {
        Log.d(TAG,"createNotification()");
        //알림창 눌렀을대 나타낼 액티비티
        Intent intent= new Intent(this, MainActivity.class);
        //클릭할 때까지 액티비티 실행을 보류하고 있는 PendingIntent 객체 생성
        PendingIntent pending= PendingIntent.getActivity(this, 0
                , intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //stop 버튼 intent
        Intent intentHide = new Intent(this, StopServiceReceiver.class);
        PendingIntent hide = PendingIntent.getBroadcast(this, (int) System.currentTimeMillis(), intentHide, PendingIntent.FLAG_CANCEL_CURRENT);

        //알림을 해줄 builder 선언
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("카나리앱");
        builder.setContentText("테스트용 실행");
        builder.setColor(Color.RED);
        builder.setOngoing(true);
        builder.setContentIntent(pending);
        builder.setSmallIcon(R.drawable.carlary_app_logo3);
        builder.addAction(R.drawable.ic_launcher_foreground, "stop", hide);


        // 알림 표시
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel("default", "기본 채널", NotificationManager.IMPORTANCE_DEFAULT));
        }

        // id값은
        // 정의해야하는 각 알림의 고유한 int값
        notificationManager.notify(1, builder.build());
    }

    private void removeNotification() {
        Log.d(TAG,"removeNotification()");
        // Notification 제거
        NotificationManagerCompat.from(this).cancel(1);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        sendLocation(location);
        Log.d(TAG,"Location changed: ");

    }

    private void sendLocation(Location location) {
        Intent intent = new Intent("LocationSenderReciever");
        intent.putExtra("lat", location.getLatitude());
        intent.putExtra("lng",location.getLongitude());
        intent.putExtra("speed",location.getSpeed());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d(TAG,"sendLocation(Location location) ");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}