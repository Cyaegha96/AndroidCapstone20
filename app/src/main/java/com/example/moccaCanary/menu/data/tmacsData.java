package com.example.moccaCanary.menu.data;

public class tmacsData {

    private String region;
    private String district;
    private String placeName;
    private int accidentCount;
    private int deadCount;
    private int seriousCount;
    private int slightlyCount;
    private int injuredCount;
    private float blackSpotScore;
    private float severityScore;
    private float TotalScore;
    private String accidentType;
    private float latitude;
    private float longitude;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
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

    public float getBlackSpotScore() {
        return blackSpotScore;
    }

    public void setBlackSpotScore(float blackSpotScore) {
        this.blackSpotScore = blackSpotScore;
    }

    public float getSeverityScore() {
        return severityScore;
    }

    public void setSeverityScore(float severityScore) {
        this.severityScore = severityScore;
    }

    public float getTotalScore() {
        return TotalScore;
    }

    public void setTotalScore(float totalScore) {
        TotalScore = totalScore;
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
}
