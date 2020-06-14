package com.example.moccaCanary.service;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;

public class CanaryLocationListener implements LocationListener {

    private static CanaryLocationListener canaryLocationListener;

    public static CanaryLocationListener getInstance(){
        if(canaryLocationListener == null){
            canaryLocationListener = new CanaryLocationListener();
        }
        return canaryLocationListener;
    }

    Location location;

    @Override
    public void onLocationChanged(Location location) {
        ((CanaryService)CanaryService.mContext).CanaryLocationUpdate(location);
        this.location = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
