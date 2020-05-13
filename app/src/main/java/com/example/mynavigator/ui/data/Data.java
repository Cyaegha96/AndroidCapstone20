package com.example.mynavigator.ui.data;


import java.io.Serializable;

public class Data {

    private int accidentCode;
    private int accidentYear;
    private String accidentType;
    private int placeCode;
    private String cityName;
    private String placeName;
    private int accidentCount;
    private int casualtiesCount;
    private int deadCount;
    private int seriousCount;
    private int slightlyCount;
    private int injuredCount;
    private float latitude;
    private float longitude;
    private String dataDate;


    public int getAccidentCode() {
        return accidentCode;
    }

    public void setAccidentCode(int accidentCode) {
        this.accidentCode = accidentCode;
    }

    public int getAccidentYear() {
        return accidentYear;
    }

    public void setAccidentYear(int accidentYear) {
        this.accidentYear = accidentYear;
    }

    public String getAccidentType() {
        return accidentType;
    }

    public void setAccidentType(String accidentType) {
        this.accidentType = accidentType;
    }

    public int getPlaceCode() {
        return placeCode;
    }

    public void setPlaceCode(int placeCode) {
        this.placeCode = placeCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public int getAccidentCount() {
        return accidentCount;
    }

    public void setAccidentCount(int accidentCount) {
        this.accidentCount = accidentCount;
    }

    public int getCasualtiesCount() {
        return casualtiesCount;
    }

    public void setCasualtiesCount(int casualtiesCount) {
        this.casualtiesCount = casualtiesCount;
    }

    public int getDeadCount() {
        return deadCount;
    }

    public void setDeadCount(int deadCount) {
        this.deadCount = deadCount;
    }

    public int getSeriousCount() {
        return seriousCount;
    }

    public void setSeriousCount(int seriousCount) {
        this.seriousCount = seriousCount;
    }

    public int getSlightlyCount() {
        return slightlyCount;
    }

    public void setSlightlyCount(int slightlyCount) {
        this.slightlyCount = slightlyCount;
    }

    public int getInjuredCount() {
        return injuredCount;
    }

    public void setInjuredCount(int injuredCount) {
        this.injuredCount = injuredCount;
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

    public String getDataDate() {
        return dataDate;
    }

    public void setDataDate(String dataDate) {
        this.dataDate = dataDate;
    }

}

