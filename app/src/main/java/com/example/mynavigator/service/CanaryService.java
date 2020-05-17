package com.example.mynavigator.service;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.mynavigator.MainActivity;
import com.example.mynavigator.R;
import com.example.mynavigator.ui.data.CwData;
import com.example.mynavigator.ui.data.CwDataAdapter;
import com.example.mynavigator.ui.data.Data;
import com.example.mynavigator.ui.data.DataAdapter;
import com.example.mynavigator.ui.data.DeadAdapter;
import com.example.mynavigator.ui.data.DeadData;
import com.example.mynavigator.ui.data.ReportAdapter;
import com.example.mynavigator.ui.data.RptData;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;


public class CanaryService extends Service implements LocationListener {

    private static final String TAG = "CanaryService";
    public static Context mContext;

    Location location; // Location
    Location updatedLocation;
    double latitude; // Latitude
    double longitude; // Longitude

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 1 meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 1; // 1 second

    private float ACCIDENT_RADIUS = 200;
    private float GEOFENCE_RADIUS = 30;
    private float DISTANCETO_PARAMETER = 700;
    protected LocationManager locationManager;

    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;
    private static final String CHANNEL_ID = "channel_01";

    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;

    private String label;
    private int icon;
    int switch_detected = 0;

    PendingIntent pendingIntent;
    private List<Data> dList;
    private List<Data> userDataList = new ArrayList<>();
    private List<CwData> cwDataList;
    private List<CwData> userCwDataList = new ArrayList<>();
    private List<DeadData> userDeadList = new ArrayList<>();
    private List<DeadData> deadDataList;
    private List<RptData> rptDataList;
    private List<RptData> userRptDataList = new ArrayList<>();

    private int locationChangeCount =0;

    private Vibrator vibrator;
    CanaryBroadcastReceiver canaryBroadcastReceiver = new CanaryBroadcastReceiver();

    @Override
    public void onCreate() {
        super.onCreate();
        updatedLocation = getLocation();
        createNotification();
        startTracking();
        initLoadDBReturn();
        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceHelper = new GeofenceHelper(this);
        mContext = this;

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(canaryBroadcastReceiver, filter);

        Log.d(TAG, "onCreate ");
    }

    public void handleUserActivity(int type, int confidence) {
        label = getString(R.string.activity_unknown);
        icon = R.drawable.ic_still;

        switch (type) {
            case DetectedActivity.IN_VEHICLE: {
                label = getString(R.string.activity_in_vehicle);
                icon = R.drawable.ic_driving;
                break;
            }
            case DetectedActivity.ON_BICYCLE: {
                label = getString(R.string.activity_on_bicycle);
                icon = R.drawable.ic_on_bicycle;
                break;
            }
            case DetectedActivity.ON_FOOT: {
                label = getString(R.string.activity_on_foot);
                icon = R.drawable.ic_walking;
                break;
            }
            case DetectedActivity.RUNNING: {
                label = getString(R.string.activity_running);
                icon = R.drawable.ic_running;
                break;
            }
            case DetectedActivity.STILL: {
                label = getString(R.string.activity_still);
                break;
            }
            case DetectedActivity.TILTING: {
                label = getString(R.string.activity_tilting);
                icon = R.drawable.ic_tilting;
                break;
            }
            case DetectedActivity.WALKING: {
                label = getString(R.string.activity_walking);
                icon = R.drawable.ic_walking;
                break;
            }
            case DetectedActivity.UNKNOWN: {
                label = getString(R.string.activity_unknown);
                break;
            }
        }

        Log.e(TAG, "User activity: " + label + ", Confidence: " + confidence);
        if (switch_detected == 1) {
            createNotification();
        }

    }

    private void startTracking() {
        Intent intent = new Intent(this, BackgroundDetectedActivitiesService.class);
        startService(intent);
    }

    private void stopTracking() {
        Intent intent = new Intent(this, BackgroundDetectedActivitiesService.class);
        stopService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand ");
        setUserDataList();
        dataInputGeofence(userDataList);
        Log.d(TAG, "userDataList dataInput");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        stopUsingGPS();
        stopTracking();
        removeNotification();
        removeGeofence();
    }

    private Location getLocation() {
        Log.d(TAG, "getLocation Service");
        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled && !isNetworkEnabled) {
                Log.d(TAG, "GPS와 Network 공급자 둘다 작동할 수 없는 상황입니다.");
                //GPS 설정화면으로 이동
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                startActivity(intent);
                stopSelf();

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
            Log.d("@@@", "" + e.toString());
        }
        return location;
    }

    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(CanaryService.this);
        }
    }

    public Location getLocationOUT() {
        if (location != null) {
            return location;
        } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

        }return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    public List<Data> getUserDataList(){
        return userDataList;
    }
    public List<DeadData> getDeadDataList() { return userDeadList; }
    public List<CwData> getCwDataList() {
        return userCwDataList;
    }
    public List<RptData> getRptDataList() {return userRptDataList;}

    //보행자 다발지역 리스트 중에서 user로부터 1km 이내의 것들만 골라냅니다.
    private void setUserDataList(){
        for(int i=0;i<dList.size();i++){
            Location l = new Location("p");
            l.setLatitude(dList.get(i).getLatitude());
            l.setLongitude(dList.get(i).getLongitude());
            if(location.distanceTo(l) <= DISTANCETO_PARAMETER){
               userDataList.add(dList.get(i));
            }

        }
    }

    //데이터 리스트를, Geofenc에 적용시켜 넣습니다! 물론, DISTANCETO_PARAMETER 내의 데이터만 골라서 말이죠!
    private void dataInputGeofence(List<Data> dList){

        for(int i=0; i<cwDataList.size();i++){

            CwData cw = cwDataList.get(i);
                Location c = new Location("p");
                c.setLatitude(cw.getLatitude());
                c.setLongitude(cw.getLongitude());

            int dataCount = 0;
            int accidentMagnitude = 0;

            Data data = null;

            for(int j=0;j<dList.size();j++){
                Location l = new Location("p");
                l.setLatitude(dList.get(j).getLatitude());
                l.setLongitude(dList.get(j).getLongitude());

                //만약 보행자 다발 구역 내에 있는 횡단보도라면 표시
                if(l.distanceTo(c) <= ACCIDENT_RADIUS){

                    if(data != null){
                        //일단 다발지 데이터를 받아옵니다.
                        //사고 규모는 발생횟수+사상자수로 판단합니다.
                        data = dList.get(j);
                        dataCount = dataCount+1;
                        accidentMagnitude = dList.get(j).getAccidentCount() + dList.get(j).getCasualtiesCount();
                    }
                    else{
                       //만약 데이터를 이미 받아왔다면
                        dataCount = dataCount+1;

                        //사고 규모가 더 큰게 들어왓다면  그데이터를 씁니다.
                        if(accidentMagnitude < (dList.get(j).getAccidentCount() + dList.get(j).getCasualtiesCount()) ){
                            data = dList.get(j);
                            accidentMagnitude = (dList.get(j).getAccidentCount() + dList.get(j).getCasualtiesCount());
                        }
                    }

                }
            }
            //최종적으로 결정된 데이터를 이용해 geofence를 추가합니다.
            if(data !=null){
                cw.setAccidentType(data.getAccidentType());
                cw.setAccidentCount(dataCount);
                userCwDataList.add(cw);
                //횡단보도의 좌표를 넣는다.
                LatLng latLng = new LatLng(c.getLatitude(),c.getLongitude());
                //알림에 표시할 내용은 //사고 발생건수+  (년도) 사고종류 사상자수: x명
                String geofenceId = data.getAccidentCount()+"@"+"("+data.getAccidentYear() +")" +
                        data.getAccidentType()+
                        " 사상자수: "+data.getCasualtiesCount()+"명";
                addGeofence(geofenceId,latLng, GEOFENCE_RADIUS);
            }
        }
        for(int i=0;i<deadDataList.size();i++){
            DeadData deadData = deadDataList.get(i);
            Location d = new Location("d");
            d.setLatitude(deadData.getLa_crd());
            d.setLongitude(deadData.getLo_crd());

            if(location.distanceTo(d) <=DISTANCETO_PARAMETER ){
                userDeadList.add(deadData);
                LatLng latLng = new LatLng(d.getLatitude(),d.getLongitude());
                //알림에 표시할 내용은 // 사망자는 무조건 강 알림 주자//   (년도) 사고종류 사망자수: x명
                String geofenceId = "15"+"@"+"("+deadData.getAcc_year() +")" +
                        deadData.getAcc_ty_cd()+
                        " 사망자수: "+deadData.getDth_dnv_cnt()+"명";
                addGeofence(geofenceId,latLng, GEOFENCE_RADIUS);
            }
        }

        for(int i=0;i<rptDataList.size();i++){
            addRptList(rptDataList.get(i));
        }
    }

    public void addRptList(RptData rptData){
        Log.d(TAG,"아니 이거 제대로 되는지 확인좀");

        Location r = new Location("r");

        r.setLatitude(rptData.getLatitude());
        r.setLongitude(rptData.getLongitude());
        if(location.distanceTo(r) <= DISTANCETO_PARAMETER){
            if(!userRptDataList.contains(rptData)){
                userRptDataList.add(rptData);
                LatLng latLng = new LatLng(r.getLatitude(),r.getLongitude());
                //알림에 표시할 내용은 사용자 알림에 관한 내용
                String geofenceId = "1"+"@"+"(제보:"+rptData.getSenderName() +")" +
                        rptData.getAccidentType()+
                        " 알림 이유"+rptData.getReasonSelected();
                addGeofence(geofenceId,latLng, GEOFENCE_RADIUS);
            }
        }
    }

    private void addGeofence(String geofenceId, LatLng latLng, float radius) {

        Geofence geofence = geofenceHelper.getGeofence(geofenceId, latLng, radius, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        pendingIntent = geofenceHelper.getPendingIntent();

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Log.d(TAG, "onSuccess: Geofence Added...");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMessage = geofenceHelper.getErrorString(e);
                        Log.d(TAG, "onFailure: " + errorMessage);
                    }
                });
    }

    private void removeGeofence(){
        geofencingClient.removeGeofences(pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Geofence Added...");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMessage = geofenceHelper.getErrorString(e);
                        Log.d(TAG, "onFailure: " + errorMessage);
                    }
                });

    }

    private void initLoadDBReturn() {

        DataAdapter mDbHelper = new DataAdapter(getApplicationContext());
        mDbHelper.createDatabase();
        mDbHelper.open();

        // db에 있는 값들을 model을 적용해서 넣는다.
        dList = mDbHelper.getTableData();

        // db 닫기
        mDbHelper.close();

        CwDataAdapter cwDbHelper = new CwDataAdapter(getApplicationContext());
        cwDbHelper.createDatabase();
        cwDbHelper.open();
        cwDataList = cwDbHelper.getTableData();
        cwDbHelper.close();

        DeadAdapter mDeadDbHelper = new DeadAdapter(getApplicationContext());

        mDeadDbHelper.createDatabase();
        mDeadDbHelper.open();

        deadDataList = mDeadDbHelper.getTableData();

        mDeadDbHelper.close();

        ReportAdapter mReportAdapter = new ReportAdapter(getApplicationContext());

        mReportAdapter.createDatabase();
        mReportAdapter.open();

        rptDataList = mReportAdapter.getTableData();

        mReportAdapter.close();

    }

    private void createNotification() {

        switch_detected = 1; // 한번이라도 실행이 되었음
        //아이콘 사용을위해 비트맵설정
        Bitmap iconview = BitmapFactory.decodeResource(getResources(),icon);
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE); //진동사용

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
        builder = new NotificationCompat.Builder(this, "default");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("카나리앱");
        builder.setContentText("알림 서비스 실행중 / 현재 위치: 경도 "+ String.format("%.2f", location.getLatitude()) +" 위도 "+String.format("%.2f", location.getLongitude()));
        builder.setColor(Color.RED);
        builder.setOngoing(true);
        builder.setContentIntent(pending);
        builder.setSmallIcon(R.drawable.carlary_app_logo3);
        builder.setLargeIcon(iconview);
        builder.addAction(R.drawable.ic_launcher_foreground, "stop", hide);


        // 알림 표시
        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel("default", "기본 채널", NotificationManager.IMPORTANCE_LOW));
        }

        // id값은
        // 정의해야하는 각 알림의 고유한 int값
       // notificationManager.notify(1, builder.build());
        startForeground(1,builder.build());
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
        if(location.getAccuracy() < 0.5 ){ //정확도가 0일 경우 --> 무시해야함!
            return;
        }
        this.location = location;
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        sendLocation(location);
        Log.d(TAG,"Location changed: 위도:" +location.getLatitude()+ " 경도: "+location.getLongitude() );

        //500미터 단위로 geofence 주기적 갱신
        if(updatedLocation.distanceTo(location) <= 500){
            locationChangeCount++;
            if(locationChangeCount < 4){
                return;
            }
            Log.d(TAG,"사용자가 초기 위치보다 500m 멀어지면 갱신 갱신/ locationChangeCount"+locationChangeCount);
            Toast.makeText(getApplicationContext(),"사용자가 초기 위치보다 500m 멀어지면 갱신 갱신",Toast.LENGTH_SHORT);
            removeGeofence();
            setUserDataList();
            dataInputGeofence(userDataList);
            updatedLocation = location;
        }

    }

    private void sendLocation(Location location) {
        //intent를 활용해 Location 정보 전송
        Intent intent = new Intent("LocationSenderReciever");
        intent.putExtra("provider",location.getProvider()+"");
        intent.putExtra("lat", location.getLatitude());
        intent.putExtra("lng",location.getLongitude());
        intent.putExtra("speed",location.getSpeed());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        Log.d(TAG,"sendLocation(Location location) ");

        builder.setContentText("알림 서비스 실행중 / update: 현재 위치: 경도 "
                + String.format("%.2f", location.getLatitude()) +" 위도 "+String.format("%.2f", location.getLongitude()));
        notificationManager.notify(1, builder.build());
        Log.d(TAG,"notification 갱신");
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