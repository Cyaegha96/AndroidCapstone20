package com.example.moccaCanary.menu.home;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
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
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.moccaCanary.MainActivity;
import com.example.moccaCanary.R;
import com.example.moccaCanary.menu.data.Data;
import com.google.android.gms.maps.model.LatLng;
import com.muddzdev.styleabletoast.StyleableToast;

import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private HomeViewModel homeViewModel;
    private ImageView canaryImage;
    private Button button;
    private TextView txt_activity;

    private LocationManager locationManager;
    private int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;
    private int ACTIVITY_RECOGNITION_CODE = 303;

    List<Data> dataList;
    LatLng currPosition;
    Location userLocation;

    ProgressDialog progressDialog;

    private Handler handler = new Handler();
    final private int PROGRESS_DIALOG = 0;

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
        txt_activity = (TextView) root.findViewById(R.id.txt_activity);
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

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                if (!((MainActivity) getActivity()).isLaunchingService(getContext()) ) {

                    CheckTypeTask task = new CheckTypeTask();
                    task.execute();
                    //버튼을 한번 눌렀을 때 => 켜짐
                    CanaryStartRequest();
                    button.setText("카나리아 서비스 실행중~~");
                    canaryImage.setImageResource(R.drawable.canary);

                    if(((MainActivity)getActivity()).isUserLocationHasResult()){
                        Log.d(TAG,"Activity의 userLocation 값을 가져옴");
                        userLocation = ((MainActivity)getActivity()).getUserLocation();
                    }
                    else{
                        Log.d(TAG,"사전에 받아둔 로케이션 값을 사용함.");
                        if(userLocation != null){
                            Log.d(TAG,"경도: "+ userLocation.getLatitude()+ "위도"+userLocation.getLongitude());
                            currPosition = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
                        }else{
                            StyleableToast.makeText(getActivity().getApplicationContext(),
                                    "아직 서비스 전에 받아놓은 위치 정보가 없습니다. 서비스 시작 후 업데이트 됩니다.", Toast.LENGTH_SHORT, R.style.mytoast).show();
                        }

                    }
                    if (userLocation != null) {
                        StyleableToast.makeText(getActivity().getApplicationContext(),
                                "내위치 : 위도:" + String.format("%.2f", userLocation.getLatitude()) + "\n경도:" + String.format("%.2f", userLocation.getLongitude()), Toast.LENGTH_SHORT, R.style.mytoast).show();
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

    private class CheckTypeTask extends AsyncTask<Void ,Void ,Void >{

        ProgressDialog asyncDialog = new ProgressDialog(getContext());

        @Override
        protected void onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("로딩중입니다...");

            asyncDialog.show();
            super.onPreExecute();
        }


        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected Void doInBackground(Void... voids) {
           try{
               ((MainActivity)getActivity()).startCanaryService();
               Thread.sleep(300);
           }catch(InterruptedException e){
               e.printStackTrace();
           }
           return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            asyncDialog.dismiss();
            super.onPostExecute(aVoid);
        }
    }




    public Location getFirstLocation(){
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                //GPS 설정화면으로 이동
                StyleableToast.makeText(getActivity().getApplicationContext(),
                        "위치설정을 켜주시고 카나리아를 시작해주세요", Toast.LENGTH_SHORT, R.style.mytoast).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                startActivity(intent);
            }
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
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    //We show a dialog and ask for permission
                    ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                }
            }
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACTIVITY_RECOGNITION)) {
                    ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.ACTIVITY_RECOGNITION},ACTIVITY_RECOGNITION_CODE);
                }else{
                    ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.ACTIVITY_RECOGNITION},ACTIVITY_RECOGNITION_CODE);
                }

            }

        } else {

        }
    }

    public void chaneTextViewByDetectiveService(String text){
        if(text != null){
            Log.d(TAG,"text Change: "+text);
            txt_activity.setText(text);
        }
        else{
            Log.d(TAG,"Detected activity : text is Null");
        }
    }

}