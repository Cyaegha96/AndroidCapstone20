package com.example.mynavigator.ui.home;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.mynavigator.CanaryService;
import com.example.mynavigator.MainActivity;
import com.example.mynavigator.R;
import com.example.mynavigator.ui.map.MapFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.muddzdev.styleabletoast.StyleableToast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment  {

    private HomeViewModel homeViewModel;
    private Button button;

    private LocationManager locationManager;
    private static final int REQUEST_CODE_LOCATION = 2;

    LatLng currPosition;
    Location userLocation;
    private boolean touchFlag = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
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

        //기본적으로 갖고 오는 데이터
        //이부분을 어떻게 객체화할지 고민하고 있음 흐으음...
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    this.REQUEST_CODE_LOCATION);
        }
        userLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        button = (Button) root.findViewById(R.id.button);

        //알림창을 띄우고, 서비스를 시작합니다.
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(touchFlag == false){
                    //버튼을 한번 눌렀을 때 => 켜짐
                    ((MainActivity)getActivity()).startCanaryService();
                    userLocation = getMyLocation();
                    currPosition = new LatLng(userLocation.getLatitude(),userLocation.getLongitude());

                    if (userLocation != null) {
                        StyleableToast.makeText(getActivity().getApplicationContext(),
                                "내위치 : 위도:" + currPosition.latitude + "\n경도:" + currPosition.longitude, Toast.LENGTH_LONG, R.style.mytoast).show();
                        ((MainActivity) getActivity()).setMyLatLog(currPosition); //MainActivity로 전달
                        //Service를 시작하라는 내용
                    }
                    touchFlag = true; //Flag 교체

                }else{
                    //버튼을 다시 한번 눌렀을 때->꺼짐.
                    locationManager.removeUpdates(locationListener);
                    touchFlag = false; //Flag 교체
                    ((MainActivity)getActivity()).stopCanaryService();
                }

            }
        });

        return root;
    }



    private Location getMyLocation() {

        //위험 권한 체크
        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    this.REQUEST_CODE_LOCATION);
            return getMyLocation();
        } else {

            //Criteria를 통해 정확한 위치를 얻어낸다.
            Criteria criteria = new Criteria();// 정확도
            criteria.setAccuracy(Criteria.NO_REQUIREMENT); // 전원 소비량
            criteria.setPowerRequirement(Criteria.NO_REQUIREMENT); // 고도, 높이 값을 얻어 올지를 결정
            criteria.setAltitudeRequired(true); // provider 기본 정보(방위, 방향)
            criteria.setBearingRequired(true);// 속도
            criteria.setSpeedRequired(true); // 위치 정보를 얻어 오는데 들어가는 금전적 비용
            criteria.setCostAllowed(true);

            String bestProvider = locationManager.getBestProvider(criteria, true);
            locationManager.requestLocationUpdates(bestProvider,
                    1000, 1, locationListener);

        }
        return userLocation;

    }
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            userLocation = location;
            currPosition = new LatLng(location.getLatitude(),location.getLongitude());
            StyleableToast.makeText(getActivity().getApplicationContext(),
                    "위치 업데이트\n" +
                            "위도:" + currPosition.latitude + "\n" +
                            "경도:" + currPosition.longitude,
                    Toast.LENGTH_LONG, R.style.mytoast2).show();
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
    };
}