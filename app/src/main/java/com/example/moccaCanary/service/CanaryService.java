package com.example.moccaCanary.service;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
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
import com.example.moccaCanary.MainActivity;
import com.example.moccaCanary.R;
import com.example.moccaCanary.menu.data.CwData;
import com.example.moccaCanary.menu.data.CwDataAdapter;
import com.example.moccaCanary.menu.data.Data;
import com.example.moccaCanary.menu.data.DataAdapter;
import com.example.moccaCanary.menu.data.DataBaseHelper;
import com.example.moccaCanary.menu.data.DeadAdapter;
import com.example.moccaCanary.menu.data.DeadData;
import com.example.moccaCanary.menu.data.ReportAdapter;
import com.example.moccaCanary.menu.data.RptData;
import com.example.moccaCanary.menu.data.TmacsDataAdapter;
import com.example.moccaCanary.menu.data.tmacsData;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;



import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

    SharedPreferences pref;

    private float ACCIDENT_RADIUS = 200;
    private float GEOFENCE_RADIUS = 50;
    private float DISTANCETO_PARAMETER = 500;
    protected LocationManager locationManager;

    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;

    Geocoder geocoder;

    private static final String CHANNEL_ID = "channel_01";

    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;

    private String label;
    private int icon;
    int switch_detected = 0;

    private List<Data> dList;
    private List<Data> userDataList = new ArrayList<>();
    private List<CwData> cwDataList;
    private List<CwData> userCwDataList = new ArrayList<>();
    private List<DeadData> userDeadList = new ArrayList<>();
    private List<DeadData> deadDataList;
    private List<RptData> rptDataList;
    private List<RptData> userRptDataList = new ArrayList<>();
    private List<tmacsData> tmacsDataList;
    private List<tmacsData> userTmacsDataList  = new ArrayList<>();


    private List<Geofence> userGeofenceList = new ArrayList<>();

    private int locationChangeCount =0;

    private Vibrator vibrator;
   // private AudioHelper audioHelper;
    CanaryBroadcastReceiver canaryBroadcastReceiver = new CanaryBroadcastReceiver();

    @Override
    public void onCreate() {
        super.onCreate();
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
        geocoder= new Geocoder(this);

       // audioHelper = new AudioHelper(this);
      //  audioHelper.requestaudiofocus();

        updatedLocation = getLocation();
        createNotification();
        startTracking();
        initLoadDBReturn();

        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceHelper = new GeofenceHelper(this);
        mContext = this;

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(canaryBroadcastReceiver, filter);

        Log.d(TAG, "onStartCommand ");
        setUserDataList();
        dataInputGeofence(userDataList);
        if(userGeofenceList.size() >0){
            addGeofences();
            Log.d(TAG, "geofence 개수:"+userGeofenceList.size());
        }else{
            Log.d(TAG, "주변에 geofence가 하나도 없습니다.");
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        removeGeofence();
        stopUsingGPS();
        unregisterReceiver(canaryBroadcastReceiver);
        stopTracking();
        removeNotification();


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
    public List<tmacsData> getTmacsDataList() {return userTmacsDataList;}
    public List<tmacsData> getALLTmacsDataList(){return tmacsDataList;}
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

        /*
        * for(int i=0; i<cwDataList.size();i++){

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

                //횡단보도의 좌표를 넣는다.
                LatLng latLng = new LatLng(c.getLatitude(),c.getLongitude());
                //알림에 표시할 내용은 //사고 발생건수+  (년도) 사고종류 사상자수: x명
                String geofenceId = data.getAccidentCount()+"@"+"("+data.getAccidentYear() +")" +
                        data.getAccidentType()+
                        " 사상자수: "+data.getCasualtiesCount()+"명";
               if( addGeofenceToList(geofenceId,latLng, GEOFENCE_RADIUS)){
                   userCwDataList.add(cw);
               }
            }
        }
        *
        * */
        for(int i=0;i<deadDataList.size();i++){
            DeadData deadData = deadDataList.get(i);
            Location d = new Location("d");
            d.setLatitude(deadData.getLa_crd());
            d.setLongitude(deadData.getLo_crd());

            if(location.distanceTo(d) <=DISTANCETO_PARAMETER ){

                LatLng latLng = new LatLng(d.getLatitude(),d.getLongitude());
                //알림에 표시할 내용은 // 사망자는 무조건 강 알림 주자//   (년도) 사고종류 사망자수: x명
                String geofenceId = "100"+"@"+"( "+deadData.getAcc_year() +" )" +
                        deadData.getAcc_ty_cd()+
                        " 사망자수: "+deadData.getDth_dnv_cnt()+"명";
                if(addGeofenceToList(geofenceId,latLng, GEOFENCE_RADIUS)){
                    userDeadList.add(deadData);
                }
            }
        }

        for(int i=0;i<rptDataList.size();i++){
            addRptList(rptDataList.get(i));
        }
        if (tmacsDataList != null) {
            for(int i=0;i<tmacsDataList.size();i++){
                tmacsData tmacs = tmacsDataList.get(i);
                Location t = new Location("T");
                t.setLatitude(tmacs.getLatitude());
                t.setLongitude(tmacs.getLongitude());

                if(location.distanceTo(t) <= DISTANCETO_PARAMETER){
                    LatLng latLng = new LatLng(t.getLatitude(),t.getLongitude());
                    //알림에 표시할 내용은 //사고 발생건수+ 사고 장소 +위험도
                    String geofenceId = tmacs.getAccidentCount()+"@"+"("+tmacs.getPlaceName() +")" +

                            " 위험도: "+tmacs.getTotalScore();
                    if(addGeofenceToList(geofenceId,latLng, GEOFENCE_RADIUS)){
                        userTmacsDataList.add(tmacs);
                    }
                }
            }
            Log.d(TAG,"결국 내가 갖게된 tmac 보행자 다발지 개수는?"+userTmacsDataList.size());
        }
    }

    public void addRptList(RptData rptData){

        Location r = new Location("r");

        r.setLatitude(rptData.getLatitude());
        r.setLongitude(rptData.getLongitude());
        if(location.distanceTo(r) <= DISTANCETO_PARAMETER){
            if(!userRptDataList.contains(rptData)){
                LatLng latLng = new LatLng(r.getLatitude(),r.getLongitude());
                //알림에 표시할 내용은 사용자 알림에 관한 내용
                String geofenceId = "0"+"@"+"(제보:"+rptData.getSenderName() +") [" +
                        rptData.getAccidentType()+
                        "] 알림 이유 : "+rptData.getReasonSelected();
                if(addGeofenceToList(geofenceId,latLng, GEOFENCE_RADIUS)){
                    rptData.setGeofenceid(geofenceId);
                    userRptDataList.add(rptData);
                }

            }
        }
    }

    public void removeGeofenceOnlyOne(String geofenceId){

        geofencingClient.removeGeofences(Collections.singletonList(geofenceId))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: Geofence remove...");
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

    private boolean addGeofenceToList(String geofenceId, LatLng latLng, float radius){

        Log.d(TAG,"등록된 geofenceId."+geofenceId);
        if(userGeofenceList.size() < 100){
            userGeofenceList.add(geofenceHelper.getGeofence(geofenceId, latLng, radius,
                    Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT));
            return true;
        }else{
            return false;
        }
    }

    private void addGeofences() {

        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencesRequest(userGeofenceList);


        geofencingClient.addGeofences(geofencingRequest, geofenceHelper.getPendingIntent())
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

    private void removeGeofence(){
        geofencingClient.removeGeofences(geofenceHelper.getPendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Geofence removed...");
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

        String userLocationGeocodeString = userLocationGeocode(location.getLatitude(),location.getLongitude());
        String userLocationRegion = userLocationGeocodeString.split("@")[0];

        //지원하는 지역일 경우에만 해당 DB를 열람합니다...
        if(userLocationRegion.equals("서울특별시") || userLocationRegion.equals("경기도")){

            TmacsDataAdapter tmacsDataAdapter = new TmacsDataAdapter(getApplicationContext());

            tmacsDataAdapter.createDatabase();
            tmacsDataAdapter.open();

            tmacsDataList = tmacsDataAdapter.getTableData(userLocationRegion);
            Log.d(TAG,tmacsDataList.size()+"개의 데이터가 들어왔습니다.");
            Log.d(TAG,tmacsDataList.get(1).getPlaceName()+tmacsDataList.get(1).getTotalScore());
            tmacsDataAdapter.close();
        }
    }

    private void createNotification() {

        switch_detected = 1; // 한번이라도 실행이 되었음
        //아이콘 사용을위해 비트맵설정
        Bitmap iconview = BitmapFactory.decodeResource(getResources(),icon);
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE); //진동사용

        Log.d(TAG,"createNotification()");
        //알림창 눌렀을대 나타낼 액티비티
        Intent intent= new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //클릭할 때까지 액티비티 실행을 보류하고 있는 PendingIntent 객체 생성
        PendingIntent pending= PendingIntent.getActivity(this, 0
                , intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //stop 버튼 intent
        Intent intentHide = new Intent(this, StopServiceReceiver.class);
        PendingIntent hide = PendingIntent.getBroadcast(this, (int) System.currentTimeMillis(), intentHide, PendingIntent.FLAG_CANCEL_CURRENT);

        final Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

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

        //알림을 해줄 builder 선언
        builder = new NotificationCompat.Builder(this, "default");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("카나리앱");
        builder.setContentText("알림 서비스 실행중");
        builder.setStyle(new NotificationCompat.InboxStyle()
                .addLine("현재 위치: 경도 "+ String.format("%.2f", location.getLatitude()) +" 위도 "+String.format("%.2f", location.getLongitude()))
                .addLine("반경 "+ DISTANCETO_PARAMETER+" m내의 Geofence 수: " + userGeofenceList.size()));
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
     //   audioHelper.removeFocusAudioManager();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private class backgroundLocationUpcate  extends AsyncTask<Void, Void, Void> {
        ProgressDialog asyncDialog = new ProgressDialog(getApplicationContext());

        @Override
        protected void onPreExecute() {
            ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> info;
            info = activityManager.getRunningTasks(1);
            if(info.get(0).topActivity.getClassName().equals(MainActivity.class.getClass().getName())) {
                asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                asyncDialog.setMessage("위치 변경에 따라 geofence 재설정 중입니다...");
                asyncDialog.show();
            } else {

            }

            super.onPreExecute();
        }
        @Override
        protected Void doInBackground(Void... voids) {
            try{
                removeGeofence();
                userGeofenceList.clear();
                setUserDataList();
                dataInputGeofence(userDataList);
                addGeofences();
                Thread.sleep(200);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            return null;
        }
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
        //만약 300미터 이상 이동시
        if(updatedLocation.distanceTo(location) >= 300){
            updatedLocation = location;
            locationChangeCount++;
            Log.d(TAG,"사용자가 초기 위치보다 300m 멀어지면 갱신/ locationChangeCount"+locationChangeCount);
            backgroundLocationUpcate task = new backgroundLocationUpcate();
            task.execute();
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
        builder.setContentText("알림 서비스 실행중 (위치 update) ");
        builder.setStyle(new NotificationCompat.InboxStyle()
                .addLine("현재 위치: 경도 "+ String.format("%.2f", location.getLatitude()) +" 위도 "+String.format("%.2f", location.getLongitude()))
                .addLine("반경 "+ DISTANCETO_PARAMETER+" m내의 Geofence 수: " + userGeofenceList.size()));
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

    public String userLocationGeocode(double d1, double d2){
        List<Address> list = null;
        String lo = "위치정보 없음";;
        try {
            list = geocoder.getFromLocation(
                    d1, // 위도
                    d2, // 경도
                    10); // 얻어올 값의 개수
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("test", "입출력 오류 - 서버에서 주소변환시 에러발생");
        }
        if (list != null) {
            if (list.size()==0) {
                lo = "위치정보 없음";
            } else {
                lo = list.get(1).getAdminArea()+"@"+list.get(1).getLocality();
                Log.d(TAG,"사용자 위치: " +lo);
            }
        }

        return lo;

    }
}