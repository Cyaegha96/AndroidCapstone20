package com.example.mynavigator.ui.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.mynavigator.MainActivity;
import com.example.mynavigator.R;
import com.example.mynavigator.service.CanaryService;
import com.example.mynavigator.ui.data.CwData;
import com.example.mynavigator.ui.data.Data;
import com.example.mynavigator.ui.data.DeadData;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MapFragment extends Fragment
        implements OnMapReadyCallback, LocationSource.OnLocationChangedListener {

    private static final String TAG = "MapFragment";
    private MapViewModel mapViewModel;
    private MapView mapView;
    private float ACCIDENT_RADIUS = 200;
    private float GEOFENCE_RADIUS = 30;

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


    private double myLat;
    private double myLog;
    private Location myLocation;
    GoogleMap mGoogleMap;
    private List<Data> userDataList;
    private List<CwData> cwdataList;
    private List<DeadData> deadDataList = new ArrayList<>();

    private List<Marker> markers = new ArrayList<Marker>();

    private ClusterManager<CanaryClusterItem> clusterManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.bicycle);
        Bitmap b = bitmapdraw.getBitmap();
        bicycleMarker = Bitmap.createScaledBitmap(b, 200, 200, false);

        BitmapDrawable bitmapdraw1 = (BitmapDrawable) getResources().getDrawable(R.drawable.child);
        Bitmap c = bitmapdraw1.getBitmap();
        childMarker = Bitmap.createScaledBitmap(c, 200, 200, false);

        BitmapDrawable bitmapdraw2 = (BitmapDrawable) getResources().getDrawable(R.drawable.crosswalk);
        Bitmap d = bitmapdraw2.getBitmap();
        crosswalkMarker = Bitmap.createScaledBitmap(d, 200, 200, false);

        BitmapDrawable bitmapdraw3 = (BitmapDrawable) getResources().getDrawable(R.drawable.old_man);
        Bitmap e = bitmapdraw3.getBitmap();
        old_manMarker = Bitmap.createScaledBitmap(e, 200, 200, false);

        BitmapDrawable bitmapdraw4 = (BitmapDrawable) getResources().getDrawable(R.drawable.school_zone);
        Bitmap f = bitmapdraw4.getBitmap();
        school_zoneMarker = Bitmap.createScaledBitmap(f, 200, 200, false);

        BitmapDrawable bitmapdraw5 = (BitmapDrawable) getResources().getDrawable(R.drawable.canary);
        Bitmap g = bitmapdraw5.getBitmap();
        null_marker = Bitmap.createScaledBitmap(g,200,200,false);

        bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.crossroad1);
        b = bitmapdraw.getBitmap();
        crossRoad1 = Bitmap.createScaledBitmap(b,150,150,false);

        bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.crossroad2);
        b = bitmapdraw.getBitmap();
        crossRoad2 = Bitmap.createScaledBitmap(b,150,150,false);

        bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.crossroad3);
        b = bitmapdraw.getBitmap();
        crossRoad3 = Bitmap.createScaledBitmap(b,150,150,false);

        bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.crossroad4);
        b = bitmapdraw.getBitmap();
        crossRoad4 = Bitmap.createScaledBitmap(b,150,150,false);

        bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.crossroadnull);
        b = bitmapdraw.getBitmap();
        crossRoadnull = Bitmap.createScaledBitmap(b,150,150,false);



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

        mapView = (MapView) root.findViewById(R.id.map);
       setMap(savedInstanceState);


       return root;
    }

    private void setMap(Bundle savedInstanceState){
        Log.d(TAG,"setMap");
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this); // 비동기적 방식으로 구글 맵 실행
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
        mGoogleMap.moveCamera(cameraUpdate);

        clusterManager = new ClusterManager<>(getContext(),googleMap);
        clusterManager.setRenderer(new MarkerRenderer(getContext(), googleMap, clusterManager));
        mGoogleMap.setOnCameraIdleListener(clusterManager);

        deadDataList = ((MainActivity)getActivity()).getDeadList();


        myLocation = new Location("내위치");
        myLocation.setLatitude(myLat);
        myLocation.setLongitude(myLog);

        MapsInitializer.initialize(this.getActivity());


        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);

        if(((MainActivity)getActivity()).isUserLocationHasResult()){ //카나리 서비스가 실행중이라면

            Log.d(TAG,"addMarker");

            markerSetting(mGoogleMap);
            cwDataSetting(mGoogleMap);
        }

    }

    public void cwDataSetting(GoogleMap googleMap){

        cwdataList = ((CanaryService)CanaryService.mContext).getCwDataList();
        Log.d(TAG,"cwdataListSize: "+cwdataList.size());

        if(cwdataList!= null){

            for(int i=0;i<cwdataList.size();i++) {

                float dLat = cwdataList.get(i).getLatitude();
                float dLog = cwdataList.get(i).getLongitude();

                int crossType = cwdataList.get(i).getCrslkKnd();
                String crossStringType;
                BitmapDescriptor mIcon = BitmapDescriptorFactory.fromBitmap(null_marker);;

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
                        mIcon = BitmapDescriptorFactory.fromBitmap(bicycleMarker);
                        break;
                    default:
                        crossStringType = "분류정보 없는 횡단보도";
                        mIcon = BitmapDescriptorFactory.fromBitmap(crossRoadnull);
                        break;
                }
                String pname = cwdataList.get(i).getRoadNm();
                if(pname == null){
                    pname = ""; //useDataList.get(j).getPlaceName();
                }

                //아이콘 설정

                clusterManager.addItem(new CanaryClusterItem(
                        new LatLng(dLat, dLog),
                        crossStringType +"\n",
                        pname +"다발지역 위험도: "+cwdataList.get(i).getAccidentCount()+"\n",
                        mIcon));

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

                float dLat =userDataList.get(i).getLatitude();
                float dLog = userDataList.get(i).getLongitude();

                String dAccidentType = userDataList.get(i).getAccidentType();
                String dName = userDataList.get(i).getPlaceName();

                String blurCount = "발생건수" + userDataList.get(i).getAccidentCount();


                googleMap.addCircle(new CircleOptions()
                        .center(new LatLng(dLat, dLog))
                        .fillColor(0x2248f7ef) //투명한 원 그리려면 색상값 앞에 0x22 가 붙어야 함!
                        .radius(ACCIDENT_RADIUS)
                        .strokeColor(Color.BLACK)
                        .strokeWidth(2));


                MarkerOptions marker = new MarkerOptions()
                        .position(new LatLng(dLat, dLog))
                        .title(dAccidentType + "\n")
                        .snippet(dName + "\n" + blurCount);

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

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // 마커 클릭시 호출되는 콜백 메서드
                Toast.makeText(getContext(),
                        marker.getTitle() + " 클릭했음"
                        , Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }


    @Override
    public void onResume(){
        super.onResume();

        if(mGoogleMap != null){ //prevent crashing if the map doesn't exist yet (eg. on starting activity)
            mGoogleMap.clear();
            markerSetting(mGoogleMap);
            cwDataSetting(mGoogleMap);
            // add markers from database to the map
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        if(myLocation.distanceTo(location) < 100){
            if(mGoogleMap != null){ //prevent crashing if the map doesn't exist yet (eg. on starting activity)
                mGoogleMap.clear();
                markerSetting(mGoogleMap);
                cwDataSetting(mGoogleMap);
                // add markers from database to the map
            }
        }
    }

    private class MarkerRenderer extends DefaultClusterRenderer<CanaryClusterItem> {
        public MarkerRenderer(Context context, GoogleMap map, ClusterManager<CanaryClusterItem> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        public void onClustersChanged(Set<? extends Cluster<CanaryClusterItem>> clusters) {
            super.onClustersChanged(clusters);
        }

        @Override
        protected void onBeforeClusterItemRendered(CanaryClusterItem item, MarkerOptions markerOptions) {

            markerOptions.icon(item.getmIcon());
            super.onBeforeClusterItemRendered(item, markerOptions);

        }

        @Override
        public void setOnClusterItemClickListener(ClusterManager.OnClusterItemClickListener<CanaryClusterItem> listener) {
            super.setOnClusterItemClickListener(listener);
        }
    }


}