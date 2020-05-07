package com.example.mynavigator.ui.map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class CanaryClusterItem implements ClusterItem {

    private final LatLng mPosition;
    private final String mTitle;
    private final String mSniffet;

    public CanaryClusterItem(LatLng mPosition, String title, String sniffet) {
        this.mPosition = mPosition;
        this.mTitle = title;
        this.mSniffet = sniffet;
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Nullable
    @Override
    public String getTitle() {
        return mTitle;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return mSniffet;
    }


}
