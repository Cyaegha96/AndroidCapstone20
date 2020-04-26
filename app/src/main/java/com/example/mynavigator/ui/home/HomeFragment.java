package com.example.mynavigator.ui.home;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.mynavigator.AlertReceiver;
import com.example.mynavigator.MainActivity;
import com.example.mynavigator.R;
import com.example.mynavigator.ui.data.Data;
import com.google.android.gms.maps.model.LatLng;
import com.muddzdev.styleabletoast.StyleableToast;

import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private HomeViewModel homeViewModel;
    private Button button;

    private LocationManager locationManager;
    private static final int REQUEST_CODE_LOCATION = 2;

    List<Data> dataList;
    LatLng currPosition;
    Location userLocation;
    AlertReceiver receiver;
    PendingIntent proximityIntent;
    private boolean touchFlag;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Log.i(TAG, " onCreateView() ");
        if (savedInstanceState != null) {
            String data = savedInstanceState.getString("touchFlagKey");
            if (data.equals("true")) {
                touchFlag = true;
            } else if (data.equals("false")) {
                touchFlag = false;
            }
        }
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);

        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        button = (Button) root.findViewById(R.id.button);

        return root;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i(TAG, " onCreate() ");
        super.onCreate(savedInstanceState);
        touchFlag = false;
        if (savedInstanceState != null) {
            String data = savedInstanceState.getString("touchFlagKey");
            if (data.equals("true")) {
                touchFlag = true;
            } else if (data.equals("false")) {
                touchFlag = false;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, " onStart() ");
        //기본적으로 갖고 오는 데이터

        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        userLocation = getFirstLocation();

        //알림창을 띄우고, 서비스를 시작합니다.
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (touchFlag == false) {
                    //버튼을 한번 눌렀을 때 => 켜짐
                    ((MainActivity) getActivity()).startCanaryService();
                    dataList =  ((MainActivity) getActivity()).getDataList();
                    Log.d(TAG,"dataList 잘 가져 왔냐??: "+dataList.get(0).accidentType+" "+dataList.get(0).getAccidentYear());
                    receiver = new AlertReceiver();
                    IntentFilter filter = new IntentFilter("kr.ac.koreatech.msp.locationAlert");
                    getActivity().getApplicationContext().registerReceiver(receiver, filter);

                    // ProximityAlert 등록을 위한 PendingIntent 객체 얻기
                    /*
                    Intent intent = new Intent("kr.ac.koreatech.msp.locationAlert");
                    proximityIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 0, intent, 0);
                    Log.d("TAG","Fragment:Call MainActivity, startCanaryService");

                    try {
                        // 근접 경보 등록 메소드
                        // void addProximityAlert(double latitude, double longitude, float radius, long expiration, PendingIntent intent)
                        // 아래 위도, 경도 값의 위치는 2공학관 420호 창가 부근
                        for(int i = 0; i< dataList.size() ; i++){
                            LatLng d = new LatLng(dataList.get(i).getLatitude(),dataList.get(i).getLongitude());
                            Location l = new Location("l");
                            l.setLatitude(d.latitude);
                            l.setLongitude(d.longitude);
                            if(userLocation.distanceTo(l) < 1000)
                                locationManager.addProximityAlert(d.latitude, d.longitude, 20, -1, proximityIntent);
                        }

                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                     */

                    if(((MainActivity)getActivity()).isUserLocationHasResult()){
                        Log.d(TAG,"Activity의 userLocation 값을 가져옴");
                        userLocation = ((MainActivity)getActivity()).getUserLocation();
                    }
                    else{
                        Log.d(TAG,"사전에 받아둔 로케이션 값을 사용함.");
                        Log.d(TAG,"경도: "+ userLocation.getLatitude()+ "위도"+userLocation.getLongitude());
                    }
                    currPosition = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());

                    if (userLocation != null) {
                        StyleableToast.makeText(getActivity().getApplicationContext(),
                                "내위치 : 위도:" + String.format("%.2f", currPosition.latitude) + "\n경도:" + String.format("%.2f", currPosition.longitude), Toast.LENGTH_LONG, R.style.mytoast).show();
                        ((MainActivity) getActivity()).setUserLocation(userLocation); //MainActivity로 전달
                        //Service를 시작하라는 내용
                    }
                    touchFlag = true; //Flag 교체

                } else {
                    //버튼을 다시 한번 눌렀을 때->꺼짐.
                    touchFlag = false; //Flag 교체
                    ((MainActivity) getActivity()).stopCanaryService();
                    locationManager.removeProximityAlert(proximityIntent);
                    getActivity().getApplicationContext().unregisterReceiver(receiver);
                    StyleableToast.makeText(getActivity().getApplicationContext(),
                            "서비스 종료", Toast.LENGTH_LONG, R.style.mytoast).show();

                }

            }
        });
    }

    public Location getFirstLocation(){
        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    this.REQUEST_CODE_LOCATION);
            return getFirstLocation();
        }else{
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location == null){
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            return location;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (touchFlag == false)
            outState.putString("touchFlagKey", "false");
        else if (touchFlag == true)
            outState.putString("touchFlagKey", "true");
        Log.d("HomeFragment/onsave", "touchFlagKey" + touchFlag);
    }

}