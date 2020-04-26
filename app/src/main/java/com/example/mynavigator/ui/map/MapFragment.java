package com.example.mynavigator.ui.map;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.cocoahero.android.geojson.GeoJSON;
import com.cocoahero.android.geojson.GeoJSONObject;
import com.cocoahero.android.geojson.Polygon;
import com.cocoahero.android.geojson.Position;
import com.cocoahero.android.geojson.PositionList;
import com.cocoahero.android.geojson.Ring;
import com.example.mynavigator.MainActivity;
import com.example.mynavigator.R;
import com.example.mynavigator.ui.data.Data;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment
        implements OnMapReadyCallback{

    private static final String TAG = "MapFragment";
    private MapViewModel mapViewModel;
    private MapView mapView;
    private int focusingDistanceLevel =1000 ;

    private double myLat;
    private double myLog;
    private Location myLocation;
    private List<Data> dList;
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

        mapView = (MapView)root.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this); // 비동기적 방식으로 구글 맵 실행

        return root;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        myLat =  ((MainActivity)getActivity()).myLat;
        myLog = ((MainActivity)getActivity()).myLog;

        dList = ((MainActivity)getActivity()).getDataList();

        myLocation = new Location("내위치");
        myLocation.setLatitude(myLat);
        myLocation.setLongitude(myLog);

        MapsInitializer.initialize(this.getActivity());

        googleMap.setMyLocationEnabled(true);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(myLat, myLog), 17);

        googleMap.animateCamera(cameraUpdate);
        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {

                CameraPosition cameraPosition = googleMap.getCameraPosition();
                Log.d(TAG,"현재 카메라 줌레벨:"+cameraPosition.zoom);
                if(cameraPosition.zoom <15.0){
                    focusingDistanceLevel = 5000;
                }else{
                    focusingDistanceLevel = 1000;
                }
            }
        });
        markerSetting(googleMap);
    }

    public void markerSetting(GoogleMap googleMap){
        if(dList != null){
            for(int i=0;i<dList.size();i++){

                float dLat = dList.get(i).getLatitude();
                float dLog = dList.get(i).getLongitude();

                Location l = new Location("d");
                l.setLatitude(dLat);
                l.setLongitude(dLog);

                if(myLocation.distanceTo(l) <= focusingDistanceLevel){
                    //거리 비교해서 1km 안에 있는거만 표시하자

                    String dAccidentType = dList.get(i).getAccidentType();
                    String dName = dList.get(i).getPlaceName();

                    BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.marker);
                    Bitmap b=bitmapdraw.getBitmap();
                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, 200, 200, false);

                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(dLat, dLog ))
                            .title(dAccidentType+"\n")
                            .snippet(dName)
                            .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));

                    googleMap.addCircle(new CircleOptions()
                            .center(new LatLng(dLat, dLog ))
                            .fillColor(0x22FF0000) //투명한 원 그리려면 색상값 앞에 0x22 가 붙어야 함!
                            .radius(30)
                            .strokeColor(Color.BLACK)
                            .strokeWidth(2));

                }
            }
        }
    }

}