package com.example.moccaCanary.menu.report;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
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
import android.widget.Toast;

import com.example.moccaCanary.MainActivity;
import com.example.moccaCanary.R;
import com.example.moccaCanary.service.CanaryService;
import com.example.moccaCanary.menu.data.DataBaseHelper;
import com.example.moccaCanary.menu.data.RptData;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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
    private Button sendingEmailButton;
    private TextView senderName;
    private Spinner accidentSpinner;
    private EditText reasonSelected;
    private String selectedAccidentType;
    private double GEOFENCE_RADIUS=30;

    public static final String TABLE_NAME    = "report_table";
    private DataBaseHelper dataBaseHelper;
    private SQLiteDatabase db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_report, container, false);
        mapView = (MapView) v.findViewById(R.id.map2);

        SharedPreferences userInfo = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        final String userName = userInfo.getString("name","이름없는 사용자");

        senderName = v.findViewById(R.id.senderNameText);
        senderName.setText(userName);
        editLatLng = v.findViewById(R.id.editLatLng);
        sendingEmailButton = v.findViewById(R.id.sendingEmailButton);
        sendingEmailButton.setEnabled(false);

        reasonSelected = v.findViewById(R.id.editComment);

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

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("제보");
                builder.setMessage("제보한 내용을 바로 자신의 DB에 적용하겠습니까?");
                builder.setPositiveButton("네",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dataBaseHelper = new DataBaseHelper(getContext(),"data_all.db", 1);
                                // db에 추가하는 코드
                                int numId = dbinsert(senderName.getText().toString(), selectedAccidentType, sendingLatLng.latitude, sendingLatLng.longitude, reasonSelected.getText().toString());
                                //이제 이걸 카나리 서비스에 추가하자고.
                                if(((MainActivity)getActivity()).isLaunchingService(getContext())){
                                    RptData rptData = new RptData();
                                    rptData.setAccidentType(selectedAccidentType);
                                    rptData.setSenderName(senderName.getText().toString());
                                    rptData.setReasonSelected(reasonSelected.getText().toString());
                                    rptData.setLatitude((float) sendingLatLng.latitude);
                                    rptData.setLongitude((float) sendingLatLng.longitude);
                                    rptData.setNumId(numId);
                                    ((CanaryService)CanaryService.mContext).addRptList(rptData);

                                }
                                Log.d(TAG,"DB에 해당 내용이 저장되었습니다!");
                                String emailText = "["+selectedAccidentType+"]\n"+"경도:"+ sendingLatLng.latitude+ "\n 위도:" +sendingLatLng.longitude;
                                Intent email = new Intent(Intent.ACTION_SEND);
                                email.setType("plain/text");
                                String[] address = {"wldnd1102@naver.com"};
                                email.putExtra(Intent.EXTRA_EMAIL, address);
                                email.putExtra(Intent.EXTRA_SUBJECT, senderName.getText()+SENDING_MAIL_TAG);
                                email.putExtra(Intent.EXTRA_TEXT, emailText);
                                startActivity(email);
                            }
                        });
                builder.setNegativeButton("아뇨", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity().getApplicationContext(),"DB의 적용은 바로 이어지지 않습니다. 이메일 제보를 뒤이어 해주세요!",Toast.LENGTH_SHORT);

                        String emailText = "["+selectedAccidentType+"]\n"+"경도:"+ sendingLatLng.latitude+ "\n 위도:" +sendingLatLng.longitude;
                        Intent email = new Intent(Intent.ACTION_SEND);
                        email.setType("plain/text");
                        String[] address = {"wldnd1102@naver.com"};
                        email.putExtra(Intent.EXTRA_EMAIL, address);
                        email.putExtra(Intent.EXTRA_SUBJECT, senderName.getText()+SENDING_MAIL_TAG);
                        email.putExtra(Intent.EXTRA_TEXT, emailText);
                        startActivity(email);
                    }
                });
                builder.show();


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
        mGoogleMap.setMyLocationEnabled(true);
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
                        .radius(GEOFENCE_RADIUS)
                        .strokeColor(Color.BLACK)
                        .strokeWidth(2));

                sendingEmailButton.setEnabled(true);
            }
        });
    }

    private int dbinsert (String sendername, String accidentType, double latitude, double longitude, String reasonSelected){

        int numid=0;
        db = dataBaseHelper.getWritableDatabase();

        db.execSQL("INSERT INTO " + TABLE_NAME + "(userName,accidentType,latitude,longitude,reasonSelected ) "+ " VALUES(\"" + sendername + "\", \"" + accidentType + "\", \"" + latitude +"\", " + longitude +", \"" + reasonSelected + "\");");
        Cursor cursor = db.rawQuery("SELECT * FROM sqlite_sequence WHERE name = '" + TABLE_NAME+ "';",null);
        while(cursor.moveToNext()){
             numid = cursor.getInt(1);
             Log.d(TAG, "insertID"+numid);
             return numid;
        }
        return numid;
    }
    public void dbdelete(double column_latitude, double column_longitude){

        db = dataBaseHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE latitude = " + column_latitude +" AND longitude = " + column_longitude + " ;");
    }

}
