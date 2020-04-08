package com.example.mynavigator.ui.map;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

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
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.List;

public class MapFragment extends Fragment
        implements OnMapReadyCallback {

    private MapViewModel mapViewModel;
    private MapView mapView;

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
    public void onMapReady(GoogleMap googleMap) {

        double myLat =  ((MainActivity)getActivity()).myLat;
        double myLog = ((MainActivity)getActivity()).myLog;
        Location myLocation = new Location("내위치");
        myLocation.setLatitude(myLat);
        myLocation.setLongitude(myLog);

        MapsInitializer.initialize(this.getActivity());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(myLat, myLog), 17);

        googleMap.animateCamera(cameraUpdate);

        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(myLat, myLog))
                .title("내 현위치\n")
                .snippet("GPS로 확인"));

        List<Data> dList = ((MainActivity)getActivity()).getDataList();
        if(dList != null){
            for(int i=0;i<dList.size();i++){
                float dLat = dList.get(i).getLatitude();
                float dLog = dList.get(i).getLongitude();
                String dAccidentType = dList.get(i).getAccidentType();
                String dName = dList.get(i).getPlaceName();

                Location l = new Location("d");
                l.setLatitude(dLat);
                l.setLongitude(dLog);

                BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.marker);
                Bitmap b=bitmapdraw.getBitmap();
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 200, 200, false);


                if(myLocation.distanceTo(l) <= 1000){
                    //거리 비교해서 1km 안에 있는거만 표시하자

                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(dLat, dLog ))
                            .title(dAccidentType+"\n")
                            .snippet(dName)
                            .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));


                    googleMap.addCircle(new CircleOptions()
                            .center(new LatLng(dLat, dLog ))
                            .fillColor(Color.RED)
                            .radius(30)
                            .strokeColor(Color.BLACK));
                }
            }
        }
    }


}
