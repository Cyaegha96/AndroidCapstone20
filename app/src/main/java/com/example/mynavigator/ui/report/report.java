package com.example.mynavigator.ui.report;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.mynavigator.MainActivity;
import com.example.mynavigator.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class report extends Fragment
        implements OnMapReadyCallback {

    private static final String TAG = "report";
    private static final String SENDING_MAIL_TAG = "카나리아 앱 제보 메일 전송";
    private GoogleMap mGoogleMap;
    private MapView mapView;

    private double myLat;
    private double myLog;
    private LatLng sendingLatLng;

    private EditText editLatLng;
    private EditText editComment;
    private Button sendingEmailButton;
    private TextView senderName;
    private Spinner accidentSpinner;
    private String selectedAccidentType;
    private double Circle_RADOUS=30;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_report, container, false);
        mapView = (MapView) v.findViewById(R.id.map2);

        SharedPreferences userInfo = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String userName = userInfo.getString("name","이름없는 사용자");

        senderName = v.findViewById(R.id.senderNameText);
        senderName.setText(userName);
        editLatLng = v.findViewById(R.id.editLatLng);
        editComment = v.findViewById(R.id.editComment);
        sendingEmailButton = v.findViewById(R.id.sendingEmailButton);
        sendingEmailButton.setEnabled(false);

        accidentSpinner = (Spinner) v.findViewById(R.id.spinner_dAccitentType);
        ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(getActivity(),R.array.spinner_accidentType,android.R.layout.simple_spinner_dropdown_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accidentSpinner.setAdapter(arrayAdapter);

        accidentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedAccidentType = (String) accidentSpinner.getSelectedItem();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                sendingEmailButton.setEnabled(false);
            }
        });

        sendingEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailText = "["+selectedAccidentType+"]\n"+"경도:"+ sendingLatLng.latitude+ "\n 위도:" +sendingLatLng.longitude+"\n"+editComment.getText();
                Intent email = new Intent(Intent.ACTION_SEND);
                email.setType("plain/text");
                String[] address = {"zazae51@gmail.com"};
                email.putExtra(Intent.EXTRA_EMAIL, address);
                email.putExtra(Intent.EXTRA_SUBJECT, senderName.getText()+SENDING_MAIL_TAG);
                email.putExtra(Intent.EXTRA_TEXT, emailText);
                startActivity(email);
            }
        });
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this); // 비동기적 방식으로 구글 맵 실행
        // Inflate the layout for this fragment
        return v;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap = googleMap;
        myLat =  ((MainActivity)getActivity()).myLat;
        myLog = ((MainActivity)getActivity()).myLog;

        MapsInitializer.initialize(this.getActivity());

        if(accidentSpinner.isSelected()){
            googleMap.setMyLocationEnabled(true);
        }

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(myLat, myLog), 17);
        mGoogleMap.moveCamera(cameraUpdate);
        mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                sendingLatLng = latLng;
                editLatLng.setText("경도: "+ sendingLatLng.latitude+ "\n위도: "+sendingLatLng.longitude);
                mGoogleMap.clear();
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("제보할 지역"));
                mGoogleMap.addCircle(new CircleOptions()
                        .center(latLng)
                        .fillColor(0x22FF0000)
                        .radius(Circle_RADOUS)
                        .strokeColor(Color.BLACK)
                        .strokeWidth(2));

                sendingEmailButton.setEnabled(true);
            }
        });
    }
}
