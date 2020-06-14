package com.example.moccaCanary.menu.home;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;

import com.example.moccaCanary.MainActivity;
import com.example.moccaCanary.R;
import com.example.moccaCanary.menu.data.Data;
import com.example.moccaCanary.service.CanaryService;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;
import com.muddzdev.styleabletoast.StyleableToast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private HomeViewModel homeViewModel;
    private ImageView canaryImage;
    private Button button;
    private TextView txt_activity;
    private View root;
    private SharedPreferences prefs;
    private final int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private final int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;
    private int userAge;
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
        root = inflater.inflate(R.layout.fragment_home, container, false);
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

        LocationPermissionRequest();

        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            button.setEnabled(false);
        }else{
            button.setEnabled(true);
        }

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
                    StyleableToast.makeText(getActivity().getApplicationContext(),
                            "서비스 실행", Toast.LENGTH_SHORT, R.style.mytoast).show();

                } else {
                    //버튼을 다시 한번 눌렀을 때->꺼짐.
                    button.setText("내 위치 요청하기");
                    canaryImage.setImageResource(R.drawable.canary_wait);
                    ((MainActivity) getActivity()).stopCanaryService();
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
               Thread.sleep(200);
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

    public void LocationPermissionRequest(){
        if (Build.VERSION.SDK_INT >= 23) {
            //We need background permission
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //만약 background location 권한 거부를 한 적이 있다면
                if (shouldShowRequestPermissionRationale( Manifest.permission.ACCESS_FINE_LOCATION) || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Snackbar.make(root, "이 앱을 실행하려면 기본적인 위치 권한이 필요합니다.",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
                        }
                    }).setTextColor(Color.WHITE)
                            .show();
                } else {
                    requestPermissions( new String[] {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
                }
            }

        }
    }

    public void CanaryStartRequest(){
        if (Build.VERSION.SDK_INT >= 23) {
            //We need background permission
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                //만약 background location 권한 거부를 한 적이 있다면
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION) || shouldShowRequestPermissionRationale( Manifest.permission.ACTIVITY_RECOGNITION)) {
                    Snackbar.make(root, "추가적으로 백그라운드 위치 권한, 활동 감지 권한이 필요합니다.",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                            requestPermissions( new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.ACTIVITY_RECOGNITION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                        }
                    }).show();
                }else{
                    //만약 권한 거부를 한 적이 없다면 바로 요청합니다.
                    requestPermissions( new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.ACTIVITY_RECOGNITION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                }
            }
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case FINE_LOCATION_ACCESS_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length <=0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    button.setEnabled(false);
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        StyleableToast.makeText(getActivity().getApplicationContext(),
                                "권한이 있어야 합니다. 종료후 위치 권한을 받아주세요.", Toast.LENGTH_SHORT, R.style.mytoast).show();
                        getActivity().finish();

                    } else {
                        StyleableToast.makeText(getActivity().getApplicationContext(),
                                "권한이 있어야 합니다. 앱 설정에서 위치 권한을 설정해주세요.", Toast.LENGTH_SHORT, R.style.mytoast).show();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:"+getActivity().getPackageName()));
                        startActivity(intent);

                    }
                }else{
                    button.setEnabled(true);
                }
                return;
            }


        }
    }

}