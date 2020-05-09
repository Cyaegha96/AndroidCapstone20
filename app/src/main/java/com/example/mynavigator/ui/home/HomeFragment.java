package com.example.mynavigator.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.mynavigator.MainActivity;
import com.example.mynavigator.R;
import com.example.mynavigator.ui.data.Data;
import com.google.android.gms.maps.model.LatLng;
import com.muddzdev.styleabletoast.StyleableToast;

import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private HomeViewModel homeViewModel;
    private ImageView canaryImage;
    private Button button;

    private LocationManager locationManager;
    private int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;

    List<Data> dataList;
    LatLng currPosition;
    Location userLocation;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i(TAG, " onCreate() ");
        super.onCreate(savedInstanceState);

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Log.i(TAG, " onCreateView() ");
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
        canaryImage = (ImageView) root.findViewById(R.id.CanaryBird);
        button = (Button) root.findViewById(R.id.button);
        if (!((MainActivity) getActivity()).isLaunchingService(getContext()) ) {
            button.setText("내 위치 요청하기");
            canaryImage.setImageResource(R.drawable.canary_wait);
        }else{
            button.setText("카나리아 서비스 실행중~~");
            canaryImage.setImageResource(R.drawable.canary);
        }

        return root;
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

                if (!((MainActivity) getActivity()).isLaunchingService(getContext()) ) {
                    //버튼을 한번 눌렀을 때 => 켜짐
                    CanaryStartRequest();
                    button.setText("카나리아 서비스 실행중~~");
                    canaryImage.setImageResource(R.drawable.canary);

                    ((MainActivity) getActivity()).startCanaryService();
                    dataList =  ((MainActivity) getActivity()).getDataList();
                    Log.d(TAG,"dataList 잘 가져 왔냐??: "+dataList.get(0).accidentType+" "+dataList.get(0).getAccidentYear());

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
                                "내위치 : 위도:" + String.format("%.2f", currPosition.latitude) + "\n경도:" + String.format("%.2f", currPosition.longitude), Toast.LENGTH_SHORT, R.style.mytoast).show();
                        ((MainActivity) getActivity()).setUserLocation(userLocation); //MainActivity로 전달
                        //Service를 시작하라는 내용
                    }

                } else {
                    //버튼을 다시 한번 눌렀을 때->꺼짐.
                    button.setText("내 위치 요청하기");
                    canaryImage.setImageResource(R.drawable.canary_wait);
                    ((MainActivity) getActivity()).stopCanaryService();
                   // getActivity().getApplicationContext().unregisterReceiver(receiver);
                    StyleableToast.makeText(getActivity().getApplicationContext(),
                            "서비스 종료", Toast.LENGTH_SHORT, R.style.mytoast).show();

                }

            }
        });
    }

    public Location getFirstLocation(){
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location == null){
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            return location;
        } else {
            //Ask for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            }
            return getFirstLocation();
        }
    }


    public void CanaryStartRequest(){
        if (Build.VERSION.SDK_INT >= 29) {
            //We need background permission
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    //We show a dialog and ask for permission
                    ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                }
            }

        } else {

        }
    }

}