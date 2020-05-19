package com.example.moccaCanary.menu.data;

public class RptData {


    private String senderName;
    private String accidentType;
    private float latitude;
    private float longitude;
    private String reasonSelected;
    private int numId;
    private String Geofenceid;

    public int getNumId() {
        return numId;
    }

    public void setNumId(int numId) {
        this.numId = numId;
    }

    public String getGeofenceid() {
        return Geofenceid;
    }

    public void setGeofenceid(String geofenceid) {
        this.Geofenceid = geofenceid;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getAccidentType() {
        return accidentType;
    }

    public void setAccidentType(String accidentType) {
        this.accidentType = accidentType;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public String getReasonSelected() {
        return reasonSelected;
    }

    public void setReasonSelected(String reasonSelected) {
        this.reasonSelected = reasonSelected;
    }



}
