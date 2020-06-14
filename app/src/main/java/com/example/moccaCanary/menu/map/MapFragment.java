package com.example.moccaCanary.menu.map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;

import com.example.moccaCanary.MainActivity;
import com.example.moccaCanary.R;
import com.example.moccaCanary.menu.data.CommentData;
import com.example.moccaCanary.menu.data.CommentDataAdapter;
import com.example.moccaCanary.menu.data.DeadAdapter;
import com.example.moccaCanary.menu.data.ReportAdapter;
import com.example.moccaCanary.menu.data.TmacsDataAdapter;
import com.example.moccaCanary.menu.data.tmacsData;
import com.example.moccaCanary.service.CanaryService;
import com.example.moccaCanary.menu.data.CwData;
import com.example.moccaCanary.menu.data.Data;
import com.example.moccaCanary.menu.data.DataBaseHelper;
import com.example.moccaCanary.menu.data.DeadData;
import com.example.moccaCanary.menu.data.RptData;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment
        implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraIdleListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationChangeListener
       {

    private static final String TAG = "MapFragment";
    private MapViewModel mapViewModel;
    private MapView mapView;

    private float GEOFENCE_RADIUS = 50;
    private float DISTANCETO_PARAMETER = 500;

    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
////-------------------------------
///bitmap layer


    private Bitmap deadPing;
    private Bitmap deadCrossroadPing;
    private Bitmap deadDriveWayPing;
    private Bitmap deadWalkerWayPing;
    private Bitmap deadWayEdgePing;


    private float ZOOM_LEVEL = 17;
    private double myLat;
    private double myLog;
    private Location myLocation;
    private Location cameraLocation;
    GoogleMap mGoogleMap;

    private boolean FREE_DRAG = true;
    private Geocoder geocoder;

    //HAVE
    private List<Data> userDataList;
    private List<CwData> cwdataList;
    private List<DeadData> deadDataList;
    private List<Marker> markers = new ArrayList<Marker>();
    private List<Circle> circles = new ArrayList<>();
    private List<RptData> rptDataList;
    private List<tmacsData> tmacsList;

    private SharedPreferences prefs;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null){
            myLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraLocation= myLocation;
            myLat = myLocation.getLatitude();
            myLog = myLocation.getLongitude();
        }

        deadPing = makeBitmap(R.drawable.dead,250,250);
        deadCrossroadPing = makeBitmap(R.drawable.dead_crossroad,250,250);
        deadDriveWayPing = makeBitmap(R.drawable.dead_driveway,250,250);
        deadWalkerWayPing = makeBitmap(R.drawable.dead_walkerway,250,250);
        deadWayEdgePing = makeBitmap(R.drawable.dead_wayedge,250,250);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mapViewModel =
                ViewModelProviders.of(this).get(MapViewModel.class);
        View root = inflater.inflate(R.layout.fragment_map, container, false);
        final TextView textView = root.findViewById(R.id.text_map);

        mapViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);

            }
        });
        myLat =  ((MainActivity)getActivity()).myLat;
        myLog = ((MainActivity)getActivity()).myLog;
        if(((MainActivity) getActivity()).isLaunchingService(getContext()) ) { //카나리 서비스가 실행중이라면
            myLocation =  ((CanaryService)CanaryService.mContext).getLocationOUT();
            cameraLocation = myLocation;
            myLat = myLocation.getLatitude();
            myLog = myLocation.getLongitude();
        }else{
            myLocation = new Location("p");
            myLocation.setLatitude(myLat);
            myLocation.setLongitude(myLog);
            cameraLocation= myLocation;

        }
        geocoder= new Geocoder(getContext());
        initDB();
        mapView = (MapView) root.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this); // 비동기적 방식으로 구글 맵 실행

       return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if(!prefs.getString("distanceTo_parameter"," ").equals(" ")){
            DISTANCETO_PARAMETER = Integer.parseInt(prefs.getString("distanceTo_parameter","500"));
        }
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {

        if(mGoogleMap != null){
            outState.putParcelable(KEY_LOCATION,myLocation);
            super.onSaveInstanceState(outState);
        }
    }


    public void initDB(){
        DeadAdapter mDeadDbHelper = new DeadAdapter(getContext());

        mDeadDbHelper.createDatabase();
        mDeadDbHelper.open();

        deadDataList = mDeadDbHelper.getTableData();

        mDeadDbHelper.close();

        ReportAdapter mReportAdapter = new ReportAdapter(getContext());

        mReportAdapter.createDatabase();
        mReportAdapter.open();

        rptDataList = mReportAdapter.getTableData();

        mReportAdapter.close();


        String userLocationGeocodeString = geoCoderLocation(myLocation.getLatitude(),myLocation.getLongitude());
        String userLocationRegion = userLocationGeocodeString.split(" ")[1];
        Log.d(TAG, "geocoding" + userLocationRegion);


        if(userLocationRegion.equals("서울특별시") || userLocationRegion.equals("경기도")){

            TmacsDataAdapter tmacsDataAdapter = new TmacsDataAdapter(getContext());

            tmacsDataAdapter.createDatabase();
            tmacsDataAdapter.open();

            tmacsList  = tmacsDataAdapter.getTableData(userLocationRegion);
            tmacsDataAdapter.close();
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.d(TAG,"onMapReady");

        mGoogleMap = googleMap;


        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(myLat, myLog), ZOOM_LEVEL);
        googleMap.moveCamera(cameraUpdate);

        MapsInitializer.initialize(this.getActivity());

        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.setOnMyLocationButtonClickListener(this);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap.getUiSettings().setMapToolbarEnabled(true);
        mGoogleMap.setOnMarkerClickListener(this);
        mGoogleMap.setOnCameraMoveListener(this);
        mGoogleMap.setOnCameraIdleListener(this);
        mGoogleMap.setOnMyLocationChangeListener(this);

        if(((MainActivity)getActivity()).isUserLocationHasResult()){ //카나리 서비스가 실행중이라면
            addALLMarker(mGoogleMap);
        }
    }

    private void addALLMarker(GoogleMap googleMap){
        Log.d(TAG,"addMarker");

        deadDataSetting(googleMap);
        rptDataSetting(googleMap);
        tmacsDataSetting(googleMap);
    }

    public void tmacsDataSetting(GoogleMap googleMap){


        if(tmacsList != null){
            Log.d(TAG,tmacsList.size()+"개의 다발지 데이터 보유중");
            for(int i=0;i<tmacsList.size();i++){
                tmacsData tData = tmacsList.get(i);
                float dLat = tData.getLatitude();
                float dLog = tData.getLongitude();
                Location t = new Location("T");

                t.setLatitude(dLat);
                t.setLongitude(dLog);

                //자신의 반경 이내 마커만 표시하세용
                if(myLocation.distanceTo(t) <  DISTANCETO_PARAMETER){

                    //1. 사고년도 , 2. 사건장소, 3. 발생건수 , 4. 사상자수,5.사망자수,6,중상자수,7,경상자수,8,부상자수 9.사고 타입.
                    BitmapDescriptor micon;
                    switch (tData.getAccidentType()) {
                        case "횡단중":
                            micon = BitmapDescriptorFactory.fromBitmap(deadCrossroadPing);
                            break;
                        case "차도통행중":
                            micon = BitmapDescriptorFactory.fromBitmap(deadDriveWayPing);
                            break;
                        case "보도통행중":
                            micon = BitmapDescriptorFactory.fromBitmap(deadWalkerWayPing);
                            break;
                        case "길가장자리구역통행중":
                            micon = BitmapDescriptorFactory.fromBitmap(deadWayEdgePing);
                            break;
                        default:
                            micon = BitmapDescriptorFactory.fromBitmap(deadPing);
                            break;
                    }

                    MarkerOptions marker = new MarkerOptions()
                            .position(new LatLng(dLat, dLog))
                            .title("보행자사고다발지역")
                            .snippet("2018"+"@"
                                    +tData.getPlaceName()+"@" + tData.getAccidentCount()+"@"
                                    +tData.getRegion()+"@"
                                    +tData.getDeadCount() + "@" + tData.getSeriousCount()+"@"
                                    +tData.getSlightlyCount()+"@"+tData.getInjuredCount()+"@"
                                    +tData.getAccidentType()+"@"+tData.getIndex()
                            )
                            .icon(micon)
                            ;
                    //아이콘 설정
                    markers.add( googleMap.addMarker(marker));

                    circles.add(googleMap.addCircle(new CircleOptions()
                                    .center(new LatLng(dLat,dLog))
                                    .fillColor(0x22eb4034)
                                    .strokeWidth(0)
                                    .radius(GEOFENCE_RADIUS)
                            )
                    );
                }

            }
        }

    }

    public void rptDataSetting(GoogleMap googleMap){

        if(rptDataList != null){
            for(int i=0;i<rptDataList.size();i++){

                RptData rptData = rptDataList.get(i);
                float dLat = rptData.getLatitude();
                float dLog = rptData.getLongitude();

                Location r = new Location("r");
                r.setLatitude(dLat);
                r.setLongitude(dLog);

                if(cameraLocation.distanceTo(r) <= DISTANCETO_PARAMETER){
                    //순서대로 1. 데이타 타입,2 제보자 이름, 3. 제보 사유를 전달합니다.
                    MarkerOptions marker = new MarkerOptions()
                            .position(new LatLng(dLat, dLog))
                            .title("제보위험지역")
                            .snippet(rptData.getAccidentType()+"@"+
                                    rptData.getSenderName()+"@"+
                                    rptData.getReasonSelected()+"@"+
                                    rptData.getGeofenceid()+"@"+
                                    rptData.getNumId())
                            .icon(BitmapDescriptorFactory.fromBitmap( makeBitmap(R.drawable.canary_shadow,200,200)))
                            ;
                    //아이콘 설정
                    markers.add( googleMap.addMarker(marker));

                    circles.add(googleMap.addCircle(new CircleOptions()
                                    .center(new LatLng(dLat,dLog))
                                    .fillColor(0x22eb4034)
                                    .strokeWidth(0)
                                    .radius(GEOFENCE_RADIUS)
                            )
                    );
                }

            }
        }

    }

    public void deadDataSetting(GoogleMap googleMap){

        Log.d(TAG,"deaddataListSize: "+deadDataList.size());

        if(deadDataList != null){
            for(int i=0; i<deadDataList.size();i++) {
                DeadData deadData = deadDataList.get(i);
                float dLat = deadData.getLa_crd();
                float dLog = deadData.getLo_crd();

                Location r = new Location("r");
                r.setLatitude(dLat);
                r.setLongitude(dLog);

                if (cameraLocation.distanceTo(r) <= DISTANCETO_PARAMETER) {

                    String deadType = deadData.getAcc_ty_cd();
                    BitmapDescriptor micon = BitmapDescriptorFactory.fromBitmap(deadPing);
                    switch (deadType) {
                        case "횡단중":
                            micon = BitmapDescriptorFactory.fromBitmap(deadCrossroadPing);
                            break;
                        case "차도통행중":
                            micon = BitmapDescriptorFactory.fromBitmap(deadDriveWayPing);
                            break;
                        case "보도통행중":
                            micon = BitmapDescriptorFactory.fromBitmap(deadWalkerWayPing);
                            break;
                        case "길가장자리구역통행중":
                            micon = BitmapDescriptorFactory.fromBitmap(deadWayEdgePing);
                            break;
                        case "기타":
                            micon = BitmapDescriptorFactory.fromBitmap(deadPing);
                            break;
                    }

                    //사망자 정보로 넣을 것은 1. 사망자사고 종류 / 2 장소정보 / 3.도로형태 대분류 4.사망자 수/ 5.가해차량종류 6. 세부종류
                    MarkerOptions marker = new MarkerOptions()
                            .position(new LatLng(dLat, dLog))
                            .title("사망자")
                            .snippet(deadData.getAcc_ty_cd() + "@" +
                                    deadData.getOccrrnc_lc_sgg_cd() + "@" +
                                    deadData.getRoad_frm_cd() + "@" +
                                    deadData.getDth_dnv_cnt() + "@" +
                                    deadData.getWrngdo_isrty_vhcty_lclas_cd() + "@" +
                                    deadData.getWrngdo_isrty_vhcty_cd())
                            .icon(micon);
                    //아이콘 설정
                    googleMap.addMarker(marker);

                    CircleOptions circleOptions = new CircleOptions()
                            .center(new LatLng(dLat, dLog))
                            .fillColor(0x22eb4034)
                            .strokeWidth(0)
                            .radius(GEOFENCE_RADIUS);
                    googleMap.addCircle(circleOptions);

                }
            }
        }

    }

    private Bitmap makeBitmap(int drawable,int width, int height){
        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(drawable);
        Bitmap b = bitmapdraw.getBitmap();
        return  Bitmap.createScaledBitmap(b, width, height, false);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());

        if(marker.getTitle().equals("보행자사고다발지역")){
            bottomSheetDialog.setContentView(AccidentDataView(marker));
        }
        else if(marker.getTitle().equals("사망자")){
            bottomSheetDialog.setContentView(DeadDataView(marker));
        }else if(marker.getTitle().equals("제보위험지역")){
            bottomSheetDialog.setContentView(ReportDataView(marker));
        }

        bottomSheetDialog.show();

        return true;
    }

    //만약 보행자 다발지역 마커를 클릭했을 시
    private View AccidentDataView(Marker marker){

        View  root;
        if(marker.getSnippet().split("@")[3].equals("서울특별시") && Integer.parseInt(marker.getSnippet().split("@")[9]) < 213){
            root= getLayoutInflater().inflate(R.layout.bottom_sheet_data_addcomment, null);
            CommentDataAdapter commentDataAdapter = new CommentDataAdapter(getContext());

            commentDataAdapter.createDatabase();
            commentDataAdapter.open();

            CommentData commentData = commentDataAdapter.getTableData("서울특별시",Integer.parseInt(marker.getSnippet().split("@")[9]));
            commentDataAdapter.close();

            TextView bottomSheetAddText = root.findViewById(R.id.addText);
            bottomSheetAddText.setText(commentData.getComment());
        }
       else{
            root= getLayoutInflater().inflate(R.layout.bottom_sheet_data, null);


        }

        TextView bottomSheetAccidentType = root.findViewById(R.id.bottomSheet_accident_type);
        ImageView bottomSheetImageView = root.findViewById(R.id.accident_data_imageView);
        TextView bottomSheetYear  = root.findViewById(R.id.bottomSheet_accident_year);
        TextView bottomSheetPlacename = root.findViewById(R.id.bottomSheet_place_name);
        TextView bottomSheetAccidentCount = root.findViewById(R.id.bottomSheet_accident_count);
        TextView bottomSheetDeadCount = root.findViewById(R.id.bottomSheet_dead_count);
        TextView bottomSheetSlightCount = root.findViewById(R.id.bottomSheet_slight_count);
        TextView bottomSheetInjuredCount = root.findViewById(R.id.bottomSheet_injured_count);
        TextView bottomSheetSeriousCount = root.findViewById(R.id.bottomSheet_serious_count);


        //sniffer에 담길 내용은 순서대로
        //1. 사고년도 , 2. 사건장소, 3. 발생건수 , 4. 사상자수,5.사망자수,6,중상자수,7,경상자수,8,부상자수 9.사고 타입.

        String accidentType = marker.getSnippet().split("@")[8];

        switch (accidentType){
            case "횡단중":
                bottomSheetImageView.setImageResource(R.drawable.crossroad);
                break;
            case "차도통행중":
                bottomSheetImageView.setImageResource(R.drawable.driveway);
                break;
            case "보도통행중":
                bottomSheetImageView.setImageResource(R.drawable.walkerway);
                break;
            case "길가장자리구역통행중":
                bottomSheetImageView.setImageResource(R.drawable.wayedge);
                break;
            default:
                bottomSheetImageView.setImageResource(R.drawable.others);
                break;
        }

        bottomSheetAccidentType.setText(accidentType+" 사고 다발지역");
        bottomSheetYear.setText(marker.getSnippet().split("@")[0]);
        bottomSheetPlacename.setText(marker.getSnippet().split("@")[1]);
        bottomSheetAccidentCount.setText(marker.getSnippet().split("@")[2]);
        bottomSheetDeadCount.setText(marker.getSnippet().split("@")[4]);
        bottomSheetSeriousCount.setText(marker.getSnippet().split("@")[5]);
        bottomSheetSlightCount.setText(marker.getSnippet().split("@")[6]);
        bottomSheetInjuredCount.setText(marker.getSnippet().split("@")[7]);

        return root;
    }



    private View DeadDataView(Marker marker){
        //만약 사망자 정보 마커를 클릭했을 시
        View  root= getLayoutInflater().inflate(R.layout.bottom_sheet_dead, null);
        //사망자 정보로 넣을 것은 1. 사망자사고 종류 / 2 장소정보 / 3.도로형태 대분류 4.사망자 수/ 5.가해차량종류 6. 세부종류
        ImageView deadImage = root.findViewById(R.id.dead_data_imageView);
        TextView deadType = root.findViewById(R.id.bottomSheet_dead_accident_type);
        TextView deadPlace = root.findViewById(R.id.bottomSheet_dead_place_name);
        TextView deadCount = root.findViewById(R.id.bottomSheet_dead_pcount);
        TextView deadRoad = root.findViewById(R.id.bottomSheet_roadType);
        TextView deadCar= root.findViewById(R.id.bottomSheet_carType);

        switch (marker.getSnippet().split("@")[0]) {
            case "횡단중":
                deadImage.setImageResource(R.drawable.crossroad);
                break;
            case "차도통행중":
                deadImage.setImageResource(R.drawable.driveway);
                break;
            case "보도통행중":
                deadImage.setImageResource(R.drawable.walkerway);
                break;
            case "길가장자리구역통행중":
                deadImage.setImageResource(R.drawable.wayedge);
                break;
            case "기타":
                deadImage.setImageResource(R.drawable.others);
                break;

        }
        deadType.setText(marker.getSnippet().split("@")[0] +"사망자 발생지역");
        String Occrrnc_lc_sgg_cd = "위치정보없음";
        if(marker.getSnippet().split("@")[1].equals("위치정보없음")) {
            Occrrnc_lc_sgg_cd = geoCoderLocation(marker.getPosition().latitude,marker.getPosition().longitude);
        }

        deadPlace.setText(Occrrnc_lc_sgg_cd.split(" ",2)[1] );
        deadRoad.setText(marker.getSnippet().split("@")[2]);
        deadCount.setText("사망자수:" + marker.getSnippet().split("@")[3]);
        deadCar.setText("가해차량:" +marker.getSnippet().split("@")[4] + "["+ marker.getSnippet().split("@")[5]+"]");
        return root;
    }
    //유저 커스텀 마커 클릭시
    private View ReportDataView(final Marker marker){
        View  root= getLayoutInflater().inflate(R.layout.bottom_sheet_report, null);

        TextView reportType = root.findViewById(R.id.bottomSheet_report_accident_type);
        TextView reporterName = root.findViewById(R.id.bottomSheet_report_name);
        TextView reportReason = root.findViewById(R.id.bottomSheet_report_description);
        Button reportDelete = root.findViewById(R.id.bottomSheetreport_deleteButton);
        reportType.setText(marker.getSnippet().split("@")[0]);
        reporterName.setText("제보자:" + marker.getSnippet().split("@")[1]);
        reportReason.setText(marker.getSnippet().split("@")[2]);
        final String geofenceid = marker.getSnippet().split("@")[3]+"@"+marker.getSnippet().split("@")[4];


        reportDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setIcon(R.drawable.eye_icon);
                builder.setTitle("삭제");
                builder.setMessage("정말로 해당 내용을 삭제하시겠습니까?");
                builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        float latitude = (float) marker.getPosition().latitude;
                        float longitude = (float) marker.getPosition().longitude;

                       //reportList에서도 빼고
                        for(int i=0;i<rptDataList.size();i++){
                            if((rptDataList.get(i).getLatitude() == latitude)
                            && (rptDataList.get(i).getLongitude() == longitude)){
                                rptDataList.remove(i);
                            }
                        }
                        //카나리아 서비스에서도 제거하고
                        ((CanaryService)CanaryService.mContext).removeGeofenceOnlyOne(geofenceid);

                        //일단 DB에서 지우고
                        SQLiteDatabase db;
                        DataBaseHelper dataBaseHelper =  new DataBaseHelper(getContext(),"data_all.db", 1);
                        db = dataBaseHelper.getWritableDatabase();
                        if((marker.getSnippet().split("@")[3]+"").equals("null")){
                            db.execSQL("DELETE FROM report_table WHERE numId = " +marker.getSnippet().split("@")[4]+";");
                        }else{
                            db.execSQL("DELETE FROM report_table WHERE numId = " +marker.getSnippet().split("@")[5]+";");
                        }


                        db.close();

                        //원도 지우고
                        for(int i=0;i<circles.size();i++){
                            LatLng latLng = circles.get(i).getCenter();
                            if((latLng.longitude == marker.getPosition().longitude) && (latLng.latitude ==marker.getPosition().latitude)){
                                circles.get(i).remove();
                            }
                        }
                        //마커도 지워야지
                        marker.remove();

                    }
                });
                builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            }
        });
        return root;
    }

           @Override
           public void onCameraMove() {
               CameraPosition cameraPosition = mGoogleMap.getCameraPosition();
               //Log.d(TAG,"현재 카메라 줌레벨:"+cameraPosition.zoom);
               ZOOM_LEVEL = cameraPosition.zoom;
               if(cameraPosition.zoom <15.0) {
                   //카메라 줌이 16이하로 바뀌지 않게 설정
                   mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                           new LatLng(mGoogleMap.getCameraPosition().target.latitude,mGoogleMap.getCameraPosition().target.longitude),15));
               }
           }

           @Override
           public void onCameraIdle() {


                   Location cameraLocationReal = new Location("googleMap");
                   cameraLocationReal.setLatitude(mGoogleMap.getCameraPosition().target.latitude);
                   cameraLocationReal.setLongitude(mGoogleMap.getCameraPosition().target.longitude);
                   if(cameraLocation.distanceTo(cameraLocationReal) > 200){
                       Log.d(TAG,"카메라 옮겨짐:");
                       if(mGoogleMap != null){
                           mGoogleMap.clear();
                           markers.clear();
                       }

                       cameraLocation.setLatitude(mGoogleMap.getCameraPosition().target.latitude);
                       cameraLocation.setLongitude(mGoogleMap.getCameraPosition().target.longitude);

                       final Handler handler = new Handler();
                       handler.postDelayed(new Runnable() {
                           @Override
                           public void run() {
                               addALLMarker(mGoogleMap);
                           }
                       },200);
                   }

           }
           public String geoCoderLocation(double d1, double d2){
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
                       lo = "위치정보없음";
                   } else {
                       lo = list.get(0).getAddressLine(0);
                   }
               }
               return lo;
           }

           public GoogleMap getmGoogleMap(){
                return mGoogleMap;
           }

           @Override
           public boolean onMyLocationButtonClick() {
               FREE_DRAG = !FREE_DRAG;
               if(FREE_DRAG){
                   Toast.makeText(getContext(),"자유탐방모드",Toast.LENGTH_SHORT).show();
                   CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(myLat, myLog), ZOOM_LEVEL);
                   mGoogleMap.moveCamera(cameraUpdate);
                   return false;
               }else{
                   Toast.makeText(getContext(),"사용자 추적모드",Toast.LENGTH_SHORT).show();
                   CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(myLat, myLog), ZOOM_LEVEL);
                   mGoogleMap.moveCamera(cameraUpdate);
                   return false;
               }
           }

           @Override
           public void onMyLocationChange(Location location) {
            if(!FREE_DRAG){
                myLat = location.getLatitude();
                myLog = location.getLongitude();
                myLocation = location;

                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(myLat, myLog), ZOOM_LEVEL);
                mGoogleMap.moveCamera(cameraUpdate);
            }

           }



           /*
            if(!FREE_DRAG){
                   myLat = location.getLatitude();


                   myLog = location.getLongitude();
                   myLocation = location;

                   CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(myLat, myLog), ZOOM_LEVEL);
                   mGoogleMap.moveCamera(cameraUpdate);
               }
            */


       }