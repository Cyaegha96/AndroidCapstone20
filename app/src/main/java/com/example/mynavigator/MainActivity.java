package com.example.mynavigator;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.mynavigator.ui.data.CwData;
import com.example.mynavigator.ui.data.CwDataAdapter;
import com.example.mynavigator.ui.data.Data;
import com.example.mynavigator.ui.data.DataAdapter;
import com.example.mynavigator.user.UserActivity;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.muddzdev.styleabletoast.StyleableToast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private AppBarConfiguration mAppBarConfiguration;
    public double myLat =37.56;
    public double myLog = 126.97;
    private Location userLocation;

    List<Data> dataList;
    List<CwData> cwdataList;
    private GeofencingClient mGeofencingClient;
    private ArrayList<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;
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

            Intent intent = new Intent(MainActivity.this, UserActivity.class);
            startActivity(intent);
            finish();

        }

        Intent i = getIntent();
        int title = i.getIntExtra("sign", -1);
        if(title == 2){
            finish();
        }
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initLoadDBReturn();

        mGeofenceList = new ArrayList<>();


        mGeofencePendingIntent = null;
        mGeofencingClient = LocationServices.getGeofencingClient(this);


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);


        mAppBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_home, R.id.nav_map, R.id.nav_data)
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
            subtitle= choice_do+"시 "+choice_city;
        }
        if(year != -1){
            subtitle = year+" 년생 " + subtitle+" 거주";
        }
        nav_sub_view.setText(subtitle);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        return super.onOptionsItemSelected(item);
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
                setMyLatLog(new LatLng(myLat,myLog));
                userLocation.setSpeed(speed);
                StyleableToast.makeText(getApplicationContext(),
                        text,
                        Toast.LENGTH_LONG, R.style.mytoast2).show();
            }

        }
    };

    public List<Data> getDataList(){
        return dataList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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

    public void startCanaryService(){
        Intent intent = new Intent(this, CanaryService.class);
        startService(intent);

    }

    public void stopCanaryService() {
        stopService(new Intent(this, CanaryService.class));

    }

    public Location getUserLocation() {
        return userLocation;
    }

    public boolean isUserLocationHasResult(){
        if(userLocation != null) return true;
        else return false;
    }



    private void showSnackbar(final String text) {
        View container = findViewById(android.R.id.content);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    //t
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