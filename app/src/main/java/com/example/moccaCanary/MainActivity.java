package com.example.moccaCanary;


import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.example.moccaCanary.service.BackgroundDetectedActivitiesService;
import com.example.moccaCanary.service.CanaryService;
import com.example.moccaCanary.menu.data.Data;
import com.example.moccaCanary.menu.data.DataAdapter;
import com.example.moccaCanary.menu.data.DataBaseHelper;
import com.example.moccaCanary.menu.data.DeadData;
import com.example.moccaCanary.menu.settings.SettingsActivity;
import com.example.moccaCanary.user.IntroActivity;
import com.example.moccaCanary.user.UserActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.navigation.NavigationView;


import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private AppBarConfiguration mAppBarConfiguration;

    //초기위치 (경기대 한복판)
    public double myLat =37.300513;
    public double myLog = 127.035848;
    private Location userLocation;

    List<Data> dataList;
    List<DeadData> deadList;

   Geocoder geocoder;

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mAlertReceiver, new IntentFilter("LocationSenderReciever"));

        SharedPreferences firstStartCheck = getSharedPreferences("checkFirst",MODE_PRIVATE);
        boolean checkFirst = firstStartCheck.getBoolean("checkFirst",false);
        if(!checkFirst){



            SharedPreferences.Editor editor = firstStartCheck.edit();
            editor.putBoolean("checkFirst", true);
            editor.commit();

            Intent intent = new Intent(MainActivity.this, IntroActivity.class);
            startActivity(intent);
            finish();

        }


        geocoder= new Geocoder(this);
        Intent i = getIntent();
        int title = i.getIntExtra("sign", -1);
        if(title == 2){
            finish();
        }
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        initLoadDBReturn();


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);


        mAppBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_home, R.id.nav_map)
                    .setDrawerLayout(drawer)
                    .build();
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

            NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
            NavigationUI.setupWithNavController(navigationView, navController);

        SharedPreferences userInfo = getSharedPreferences("userInfo",MODE_PRIVATE);
        String userName = userInfo.getString("name","이름 없는 사용자");
        String choice_do = userInfo.getString("choice_do","지역 정보 없음");
        String choice_city= userInfo.getString("choice_city","도시 정보 없음");
        int year = userInfo.getInt("year",-1);

        String subtitle;
        View navigation_header = navigationView.getHeaderView(0);
        TextView nav_header_view = navigation_header.findViewById(R.id.navHeaderIdText);
        TextView nav_sub_view = navigation_header.findViewById(R.id.navHeaderSubText);
        nav_header_view.setText(userName);

        if(choice_city.equals("없음")){
            subtitle=choice_do;
        }else{
            subtitle= choice_do+ " " + choice_city;
        }
        if(year != -1){
            subtitle = year+" 년생 " + subtitle+" 거주";
        }
        nav_sub_view.setText(subtitle);

    }


    public String geocoderLocation(double d1, double d2, int cwindex){
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
                lo = list.get(0).getAddressLine(0);
            }
        }
        SQLiteDatabase db;
        DataBaseHelper dataBaseHelper =  new DataBaseHelper(this,"data_all.db", 1);
        db = dataBaseHelper.getWritableDatabase();
        Log.d(TAG,"UPDATE cw_table SET lnmadr = '"+ lo +
                "' WHERE cwIndex = " + cwindex);

        String sql = "UPDATE cw_table SET lnmadr = '"+ lo +
                "' WHERE cwIndex = " + cwindex+ ";";
        db.execSQL(sql);
        db.close();

        return lo;

    }

    private void initLoadDBReturn() {

        DataAdapter mDbHelper = new DataAdapter(getApplicationContext());

        mDbHelper.createDatabase();
        mDbHelper.open();

        // db에 있는 값들을 model을 적용해서 넣는다.
        dataList = mDbHelper.getTableData();

        // db 닫기
        mDbHelper.close();

    }

    private BroadcastReceiver mAlertReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"onReceive : Data 가 Activity에 도착" );

            myLat = intent.getDoubleExtra("lat",0);
            myLog = intent.getDoubleExtra("lng",0);
            String locationProvider =  intent.getStringExtra("provider");
            float speed = intent.getFloatExtra("speed",0);
            String text = "위치 업데이트\n" +
                    "위도:" + String.format("%.2f", myLat) + "\n" +
                    "경도:" + String.format("%.2f", myLog)+"\n"+
                    "스피드: " + String.format("%.2f",speed);
            Log.d(TAG,"onReceive : "+text );
            if(myLat != 0 && myLog !=0){
                userLocation = new Location(locationProvider);
                userLocation.setLatitude(myLat);
                userLocation.setLongitude(myLog);
                setMyLatLog(new LatLng(myLat,myLog));
                userLocation.setSpeed(speed);
              //  StyleableToast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG, R.style.mytoast2).show();
            }

        }
    };

    public List<Data> getDataList(){
        return dataList;
    }

    public List<DeadData> getDeadList() {
        return deadList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_explanation:

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    public void setMyLatLog(LatLng latLng){
        myLat = latLng.latitude;
        myLog = latLng.longitude;
    }

    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
        setMyLatLog(new LatLng(userLocation.getLatitude(),userLocation.getLongitude()));
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void startCanaryService(){
        Intent canaryIntent = new Intent(this, CanaryService.class);

        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(canaryIntent);
        }
        else {
           startService(canaryIntent);
        }

        Intent detectedIntent = new Intent(MainActivity.this, BackgroundDetectedActivitiesService.class);
        startService(detectedIntent);


    }

    public void stopCanaryService() {
        stopService(new Intent(this, CanaryService.class));
        stopService(new Intent(this, BackgroundDetectedActivitiesService.class));

    }

    public Location getUserLocation() {
        return userLocation;
    }

    public boolean isUserLocationHasResult(){
        if(userLocation != null) return true;
        else return false;
    }



    public Boolean isLaunchingService(Context mContext){

        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (CanaryService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return  false;
    }

}