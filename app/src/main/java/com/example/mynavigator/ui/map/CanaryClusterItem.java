package com.example.mynavigator.ui.map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class CanaryClusterItem implements ClusterItem {

    private final LatLng mPosition;
    private final String mTitle;
    private final String mSniffet;
    private final BitmapDescriptor mIcon;

    @Override
    public boolean equals(@Nullable Object o) {
        CanaryClusterItem objectItem = (CanaryClusterItem) o;
        return (this.mPosition.latitude == objectItem.mPosition.latitude) &&
                (this.mPosition.longitude == this.mPosition.longitude);
    }


    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public CanaryClusterItem(LatLng mPosition, String title, String sniffet, BitmapDescriptor mIcon) {
        this.mPosition = mPosition;
        this.mTitle = title;
        this.mSniffet = sniffet;
        this.mIcon = mIcon;
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

    public BitmapDescriptor getmIcon() { return mIcon;};

}
