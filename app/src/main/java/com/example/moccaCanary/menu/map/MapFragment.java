package com.example.moccaCanary.menu.map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.moccaCanary.MainActivity;
import com.example.moccaCanary.R;
import com.example.moccaCanary.service.CanaryService;
import com.example.moccaCanary.menu.data.CwData;
import com.example.moccaCanary.menu.data.Data;
import com.example.moccaCanary.menu.data.DataBaseHelper;
import com.example.moccaCanary.menu.data.DeadData;
import com.example.moccaCanary.menu.data.RptData;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
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

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment
        implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        LocationSource.OnLocationChangedListener {

    private static final String TAG = "MapFragment";
    private MapViewModel mapViewModel;
    private MapView mapView;
    private float ACCIDENT_RADIUS = 200;
    private float GEOFENCE_RADIUS = 30;
////-------------------------------
///bitmap layer

    private Bitmap bicycleMarker;
    private Bitmap childMarker;
    private Bitmap crosswalkMarker;
    private Bitmap old_manMarker;
    private Bitmap school_zoneMarker;
    private Bitmap null_marker;

    private Bitmap crossRoad1;
    private Bitmap crossRoad2;
    private Bitmap crossRoad3;
    private Bitmap crossRoad4;
    private Bitmap crossRoad99;
    private Bitmap crossRoadnull;

    private Bitmap dead;
    private Bitmap deadCrossroad;
    private Bitmap deadDriveWay;
    private Bitmap deadWalkerWay;
    private Bitmap deadWayEdge;


    private TextView InfoText;
    private double myLat;
    private double myLog;
    private Location myLocation;
    GoogleMap mGoogleMap;
    private List<Data> userDataList;
    private List<CwData> cwdataList;
    private List<DeadData> deadDataList;
    private List<Marker> markers = new ArrayList<Marker>();
    private List<Circle> circles = new ArrayList<>();
    private List<RptData> rptDataList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //bitmap settings

        bicycleMarker =  makeBitmap(R.drawable.bicycle,200,200);
        childMarker =  makeBitmap(R.drawable.child,200,200);
        crosswalkMarker = makeBitmap(R.drawable.crosswalk,200,200);
        old_manMarker = makeBitmap(R.drawable.old_man,200,200);
        school_zoneMarker = makeBitmap(R.drawable.school_zone,200,200);
        null_marker = makeBitmap(R.drawable.canary,200,200);

        crossRoad1 = makeBitmap(R.drawable.crossroad1,150,150);
        crossRoad2 = makeBitmap(R.drawable.crossroad2,150,150);
        crossRoad3 = makeBitmap(R.drawable.crossroad3,150,200);
        crossRoad4 = makeBitmap(R.drawable.crossroad4,150,200);

        crossRoadnull = makeBitmap(R.drawable.crossroadnull,150,150);

        dead = makeBitmap(R.drawable.dead,250,250);
        deadCrossroad = makeBitmap(R.drawable.dead_crossroad,250,250);
        deadDriveWay = makeBitmap(R.drawable.dead_driveway,250,250);
        deadWalkerWay = makeBitmap(R.drawable.dead_walkerway,250,250);
        deadWayEdge = makeBitmap(R.drawable.dead_wayedge,250,250);

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mapViewModel =
                ViewModelProviders.of(this).get(MapViewModel.class);
        View root = inflater.inflate(R.layout.fragment_map, container, false);
        final TextView textView = root.findViewById(R.id.text_map);
        InfoText = root.findViewById(R.id.text_map_info);
        mapViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        mapView = (MapView) root.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this); // 비동기적 방식으로 구글 맵 실행

       return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.d(TAG,"onMapReady");

        mGoogleMap = googleMap;
        myLat =  ((MainActivity)getActivity()).myLat;
        myLog = ((MainActivity)getActivity()).myLog;
        if(((MainActivity)getActivity()).isUserLocationHasResult()) { //카나리 서비스가 실행중이라면
            Location mylocation =  ((CanaryService)CanaryService.mContext).getLocationOUT();
            myLat = mylocation.getLatitude();
            myLog = mylocation.getLongitude();
        }

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(myLat, myLog), 17);
        googleMap.moveCamera(cameraUpdate);

         myLocation = new Location("내위치");
        myLocation.setLatitude(myLat);
        myLocation.setLongitude(myLog);

        MapsInitializer.initialize(this.getActivity());

        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap.getUiSettings().setMapToolbarEnabled(true);
        mGoogleMap.setOnMarkerClickListener(this);
        mGoogleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                CameraPosition cameraPosition = mGoogleMap.getCameraPosition();
                Log.d(TAG,"현재 카메라 줌레벨:"+cameraPosition.zoom);
                if(cameraPosition.zoom <16.0) {
                    //카메라 줌이 16이하로 바뀌지 않게 설정
                   mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                           new LatLng(mGoogleMap.getCameraPosition().target.latitude,mGoogleMap.getCameraPosition().target.longitude),16));
                }
            }
        });

        if(((MainActivity)getActivity()).isUserLocationHasResult()){ //카나리 서비스가 실행중이라면

            addALLMarker(mGoogleMap);
        }
    }

    private void addALLMarker(GoogleMap googleMap){
        Log.d(TAG,"addMarker");
        markerSetting(googleMap);
        cwDataSetting(googleMap);
        deadDataSetting(googleMap);
        rptDataSetting(googleMap);
    }

    public void rptDataSetting(GoogleMap googleMap){
        rptDataList = ((CanaryService)CanaryService.mContext).getRptDataList();
        if(rptDataList != null){
            for(int i=0;i<rptDataList.size();i++){

                RptData rptData = rptDataList.get(i);
                float dLat = rptData.getLatitude();
                float dLog = rptData.getLongitude();

                //순서대로 1. 데이타 타입,2 제보자 이름, 3. 제보 사유를 전달합니다.
                MarkerOptions marker = new MarkerOptions()
                        .position(new LatLng(dLat, dLog))
                        .title("제보위험지역")
                        .snippet(rptData.getAccidentType()+"@"+
                                rptData.getSenderName()+"@"+
                                rptData.getReasonSelected()+"@"+
                                rptData.getGeofenceid()+"@"+
                                rptData.getNumId())
                        .icon(BitmapDescriptorFactory.fromBitmap( makeBitmap(R.drawable.canary,200,200)))
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

    public void deadDataSetting(GoogleMap googleMap){
        deadDataList  = ((CanaryService)CanaryService.mContext).getDeadDataList();
        Log.d(TAG,"deaddataListSize: "+deadDataList.size());

        if(deadDataList != null){
            for(int i=0; i<deadDataList.size();i++){
                DeadData deadData =deadDataList.get(i);
                float dLat = deadData.getLa_crd();
                float dLog = deadData.getLo_crd();

                String deadType = deadData.getAcc_ty_cd();
                BitmapDescriptor micon = BitmapDescriptorFactory.fromBitmap(null_marker);
                switch (deadType){
                    case "횡단중":
                        micon = BitmapDescriptorFactory.fromBitmap(deadCrossroad);
                        break;
                    case "차도통행중":
                        micon = BitmapDescriptorFactory.fromBitmap(deadDriveWay);
                        break;
                    case "보도통행중":
                        micon = BitmapDescriptorFactory.fromBitmap(deadWalkerWay);
                        break;
                    case "길가장자리구역통행중":
                        micon = BitmapDescriptorFactory.fromBitmap(deadWayEdge);
                        break;
                    case "기타":
                        micon = BitmapDescriptorFactory.fromBitmap(dead);
                        break;
                }

                //사망자 정보로 넣을 것은 1. 사망자사고 종류 / 2 장소정보 / 3.도로형태 대분류 4.사망자 수/ 5.가해차량종류 6. 세부종류
                MarkerOptions marker = new MarkerOptions()
                        .position(new LatLng(dLat, dLog))
                        .title("사망자")
                        .snippet(deadData.getAcc_ty_cd()+"@"+
                                deadData.getOccrrnc_lc_sgg_cd()+"@"+
                                deadData.getRoad_frm_cd()+"@"+
                                deadData.getDth_dnv_cnt()+"@"+
                                deadData.getWrngdo_isrty_vhcty_lclas_cd()+"@"+
                                deadData.getWrngdo_isrty_vhcty_cd())
                        .icon(micon);
                //아이콘 설정
                googleMap.addMarker(marker);

                CircleOptions circleOptions = new CircleOptions()
                        .center(new LatLng(dLat,dLog))
                        .fillColor(0x22eb4034)
                        .strokeWidth(0)
                        .radius(GEOFENCE_RADIUS);
                googleMap.addCircle(circleOptions);

            }

        }

    }

    public void cwDataSetting(GoogleMap googleMap){

        cwdataList = ((CanaryService)CanaryService.mContext).getCwDataList();
        Log.d(TAG,"cwdataListSize: "+cwdataList.size());

        if(cwdataList!= null){

            for(int i=0;i<cwdataList.size();i++) {

                CwData cwData = cwdataList.get(i);
                float dLat = cwData.getLatitude();
                float dLog = cwData.getLongitude();

                int crossType = cwData.getCrslkKnd();
                String crossStringType;
                BitmapDescriptor mIcon = BitmapDescriptorFactory.fromBitmap(null_marker);

                switch (crossType) {
                    case 1:
                        crossStringType = "일반형";
                        mIcon = BitmapDescriptorFactory.fromBitmap(crossRoad1);
                        break;
                    case 2:
                        crossStringType = "대각선";
                        mIcon = BitmapDescriptorFactory.fromBitmap(crossRoad2);
                        break;
                    case 3:
                        crossStringType = "스테거드";
                        mIcon = BitmapDescriptorFactory.fromBitmap(crossRoad3);
                        break;
                    case 4:
                        crossStringType = "도류화";
                        mIcon = BitmapDescriptorFactory.fromBitmap(crossRoad4);
                        break;
                    case 99:
                        crossStringType = "기타";
                        mIcon = BitmapDescriptorFactory.fromBitmap(crossRoadnull);
                        break;
                    default:
                        crossStringType = "분류정보 없는";
                        mIcon = BitmapDescriptorFactory.fromBitmap(crossRoadnull);
                        break;
                }
                String pname = cwdataList.get(i).getRoadNm();
                String lnmadr = cwdataList.get(i).getLnmadr();
                if(lnmadr == null){
                    double d1 =cwdataList.get(i).getLatitude();
                    double d2 = cwdataList.get(i).getLongitude();
                    lnmadr = ((MainActivity)getActivity()).geocoderLocation(d1,d2,cwdataList.get(i).getIndex());
                }
                if(pname == null){
                    pname = "도로정보 없음";
                }
                //marker 추가사항: 횡단보도 타입, 장소+도로 위치() 다발지역 얼마나 겹치는지+ 인덱스
                MarkerOptions marker = new MarkerOptions()
                        .position(new LatLng(dLat, dLog))
                        .title("횡단보도")
                        .snippet(crossStringType+ "@"+lnmadr+"@"+pname+"@"+cwdataList.get(i).getAccidentCount()+"@"+cwdataList.get(i).getIndex())
                        .icon(mIcon);

                //아이콘 설정
                googleMap.addMarker(marker);

                googleMap.addCircle(new CircleOptions()
                        .center(new LatLng(dLat,dLog))
                        .fillColor(0x22eb4034)
                        .strokeWidth(0)
                        .radius(GEOFENCE_RADIUS));

            }
        }

    }

    public void markerSetting(GoogleMap googleMap){
        userDataList = ((CanaryService)CanaryService.mContext).getUserDataList();

        if(userDataList != null) {
            for (int i = 0; i < userDataList.size(); i++) {
                Data data = userDataList.get(i);
                float dLat = data.getLatitude();
                float dLog = data.getLongitude();

                String dAccidentType = data.getAccidentType();


                googleMap.addCircle(new CircleOptions()
                        .center(new LatLng(dLat, dLog))
                        .fillColor(0x2248f7ef) //투명한 원 그리려면 색상값 앞에 0x22 가 붙어야 함!
                        .radius(ACCIDENT_RADIUS)
                        .strokeColor(Color.BLACK)
                        .strokeWidth(2));

                //sniffer에 담길 내용은 순서대로
                //1. 사고년도 , 2. 사건장소, 3. 발생건수 , 4. 사상자수,5.사망자수,6,중상자수,7,경상자수,8,부상자수 9.사고타입
                MarkerOptions marker = new MarkerOptions()
                        .position(new LatLng(dLat, dLog))
                        .title("보행자사고다발지역")
                        .snippet( data.getAccidentYear()+"@"
                        +data.getPlaceName()+"@" + data.getAccidentCount()+"@"
                                +data.getCasualtiesCount()+"@"
                                +data.getDeadCount() + "@" + data.getSeriousCount()+"@"
                                +data.getSlightlyCount()+"@"+data.getInjuredCount()+"@"
                                +data.getAccidentType()
                        );

                if (dAccidentType.equals("자전거")) {
                    marker.icon(BitmapDescriptorFactory.fromBitmap(bicycleMarker));

                } else if (dAccidentType.equals("보행어린이")) {
                   marker.icon(BitmapDescriptorFactory.fromBitmap(childMarker));
                } else if (dAccidentType.equals("보행노인")) {
                    marker.icon(BitmapDescriptorFactory.fromBitmap(old_manMarker));
                } else if (dAccidentType.equals("스쿨존어린이")) {
                    marker.icon(BitmapDescriptorFactory.fromBitmap(school_zoneMarker));
                } else if (dAccidentType.equals("무단횡단")) {
                    marker.icon(BitmapDescriptorFactory.fromBitmap(crosswalkMarker));
                }

                //지정된 마커를 추가.
                googleMap.addMarker(marker);


            }

        }

        String s = InfoText.getText().toString();
        InfoText.setText(s+"현재 사용자 근처 보행자 다발구역 갯수는 "+userDataList.size()+"개");
    }


    @Override
    public void onLocationChanged(Location location) {

        if(myLocation.distanceTo(location) >= 300){
            if(mGoogleMap != null){ //prevent crashing if the map doesn't exist yet (eg. on starting activity)
                Log.d(TAG,"사용자가 초기 위치보다 500m 멀어지면 갱신 갱신");
                myLocation = location;
                mGoogleMap.clear();
                addALLMarker(mGoogleMap);
                // add markers from database to the map
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
        else if(marker.getTitle().equals("횡단보도")){
            bottomSheetDialog.setContentView(CrossroadDataView(marker));
        }else if(marker.getTitle().equals("사망자")){
            bottomSheetDialog.setContentView(DeadDataView(marker));
        }else if(marker.getTitle().equals("제보위험지역")){
            bottomSheetDialog.setContentView(ReportDataView(marker));
        }

        bottomSheetDialog.show();

        return true;
    }

    //만약 보행자 다발지역 마커를 클릭했을 시
    private View AccidentDataView(Marker marker){

        View  root= getLayoutInflater().inflate(R.layout.bottom_sheet_data, null);

        TextView bottomSheetAccidentType = root.findViewById(R.id.bottomSheet_accident_type);
        ImageView bottomSheetImageView = root.findViewById(R.id.accident_data_imageView);
        TextView bottomSheetYear  = root.findViewById(R.id.bottomSheet_accident_year);
        TextView bottomSheetPlacename = root.findViewById(R.id.bottomSheet_place_name);
        TextView bottomSheetAccidentCount = root.findViewById(R.id.bottomSheet_accident_count);
        TextView bottomSheetCasualtiesCount = root.findViewById(R.id.bottomSheet_casualties_count);
        TextView bottomSheetDeadCount = root.findViewById(R.id.bottomSheet_dead_count);
        TextView bottomSheetSlightCount = root.findViewById(R.id.bottomSheet_slight_count);
        TextView bottomSheetInjuredCount = root.findViewById(R.id.bottomSheet_injured_count);
        TextView bottomSheetSeriousCount = root.findViewById(R.id.bottomSheet_serious_count);

        //sniffer에 담길 내용은 순서대로
        //1. 사고년도 , 2. 사건장소, 3. 발생건수 , 4. 사상자수,5.사망자수,6,중상자수,7,경상자수,8,부상자수 9.사고 타입.

        String accidentType = marker.getSnippet().split("@")[8];

        if(accidentType.equals("자전거")){
            bottomSheetImageView.setImageResource(R.drawable.bicycle);
        }else if(accidentType.equals("보행어린이")){
            bottomSheetImageView.setImageResource(R.drawable.child);
        }else if(accidentType.equals("스쿨존어린이")){
            bottomSheetImageView.setImageResource(R.drawable.school_zone);
        }else if(accidentType.equals("보행노인")){
            Log.d(TAG,"보행노인");
            bottomSheetImageView.setImageResource(R.drawable.old_man);
        }else if(accidentType.equals("무단횡단")){
            bottomSheetImageView.setImageResource(R.drawable.crosswalk);
        }else {bottomSheetImageView.setImageResource(R.drawable.carlary_app_logo3);}

        bottomSheetAccidentType.setText(accidentType+"사고 다발지역");
        bottomSheetYear.setText(marker.getSnippet().split("@")[0]);
        bottomSheetPlacename.setText(marker.getSnippet().split("@")[1]);
        bottomSheetAccidentCount.setText(marker.getSnippet().split("@")[2]);
        bottomSheetCasualtiesCount.setText(marker.getSnippet().split("@")[3]);
        bottomSheetDeadCount.setText(marker.getSnippet().split("@")[4]);
        bottomSheetSeriousCount.setText(marker.getSnippet().split("@")[5]);
        bottomSheetSlightCount.setText(marker.getSnippet().split("@")[6]);
        bottomSheetInjuredCount.setText(marker.getSnippet().split("@")[7]);

        return root;
    }
    private View CrossroadDataView(final Marker marker){
        final View root= getLayoutInflater().inflate(R.layout.bottom_sheet_crossroad, null);
        //marker 추가사항: 횡단보도 타입, 장소 위치() 다발지역 얼마나 겹치는지

        final String crossRoadType = marker.getSnippet().split("@")[0];
        final TextView bottomSheetcrossroadType = root.findViewById(R.id.bottomSheet_crossroad_type);
        final ImageView bottomSheetCrossroadImageView = root.findViewById(R.id.crossroad_data_imageView);
        TextView bottomSheetLnmadr = root.findViewById(R.id.bottomSheet_lnmadr);
        final TextView bottomSheetPlacename = root.findViewById(R.id.bottomSheet_pname);
        TextView bottomSheetCrossroadAccidentCount = root.findViewById(R.id.bottomSheet_crossroad_accident_count);
        Button updateButton = root.findViewById(R.id.bottomSheetreport_update_button);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 커스텀 다이얼로그를 생성한다. 사용자가 만든 클래스이다.
                UpdateCrossRoad customDialog = new UpdateCrossRoad (getActivity());

                // 커스텀 다이얼로그를 호출한다.
                customDialog.callFunction(marker,
                        bottomSheetCrossroadImageView,bottomSheetcrossroadType,
                        bottomSheetPlacename,
                        cwdataList);
            }
        });

        if(crossRoadType.equals("일반형")){
            bottomSheetCrossroadImageView.setImageResource(R.drawable.crossroad1);
        }else if(crossRoadType.equals("대각선")){
            bottomSheetCrossroadImageView.setImageResource(R.drawable.crossroad2);
        }else if(crossRoadType.equals("스테거드")){
            bottomSheetCrossroadImageView.setImageResource(R.drawable.crossroad3);
        }else if(crossRoadType.equals("도류화")){
            bottomSheetCrossroadImageView.setImageResource(R.drawable.crossroad4);
        }else if(crossRoadType.equals("기타")){
            bottomSheetCrossroadImageView.setImageResource(R.drawable.crossroadnull);
        }else{
            bottomSheetCrossroadImageView.setImageResource(R.drawable.crossroadnull);
        }

        bottomSheetcrossroadType.setText(crossRoadType+" 횡단보도");
        bottomSheetLnmadr.setText(marker.getSnippet().split("@")[1]);
        bottomSheetPlacename.setText(marker.getSnippet().split("@")[2]);
        bottomSheetCrossroadAccidentCount.setText("다발지역 위험도: "+marker.getSnippet().split("@")[3]);
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
                deadImage.setImageResource(R.drawable.dead_crossroad);
                break;
            case "차도통행중":
                deadImage.setImageResource(R.drawable.dead_driveway);
                break;
            case "보도통행중":
                deadImage.setImageResource(R.drawable.dead_walkerway);
                break;
            case "길가장자리구역통행중":
                deadImage.setImageResource(R.drawable.dead_wayedge);
                break;
            case "기타":
                deadImage.setImageResource(R.drawable.dead);
                break;

        }
        deadType.setText(marker.getSnippet().split("@")[0] +"사망자 발생지역");
        deadPlace.setText(marker.getSnippet().split("@")[1]);
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
                        db.execSQL("DELETE FROM report_table WHERE numId = " +marker.getSnippet().split("@")[5]+";");

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

}