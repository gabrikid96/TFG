package grodrich7.tfg.Models;


import android.location.Location;

import java.lang.reflect.Field;
import java.util.Date;

import grodrich7.tfg.Activities.DrivingActivity;

/**
 * Created by gabri on 21/04/2018.
 */

public class DrivingData {
    private Boolean driving;
    private Boolean acceptCalls;

    private Integer startTimeHour;
    private Integer startTimeMin;
    private Boolean searchingParking;
    private String destination;
    private String lat;
    private String lon;

    public DrivingData() {
    }


    public Boolean isDriving() {
        return driving;
    }

    public void setDriving(Boolean driving) {
        this.driving = driving;
    }

    public Boolean isAcceptCalls() {
        return acceptCalls;
    }

    public void setAcceptCalls(Boolean acceptCalls) {
        this.acceptCalls = acceptCalls;
    }

    public Integer getStartTimeHour() {
        return startTimeHour;
    }

    public void setStartTimeHour(Integer startTimeHour) {
        this.startTimeHour = startTimeHour;
    }

    public Integer getStartTimeMin() {
        return startTimeMin;
    }

    public void setStartTimeMin(Integer startTimeMin) {
        this.startTimeMin = startTimeMin;
    }

    public Boolean isSearchingParking() {
        return searchingParking;
    }

    public void setSearchingParking(Boolean searchingParking) {
        this.searchingParking = searchingParking;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public void merge(DrivingData object) {
        this.driving = this.driving != null ? this.driving : object.driving;
        this.acceptCalls = this.acceptCalls != null ? this.acceptCalls : object.acceptCalls;
        this.startTimeHour = this.startTimeHour != null ? this.startTimeHour : object.startTimeHour;
        this.startTimeMin = this.startTimeMin != null ? this.startTimeMin : object.startTimeMin;
        this.searchingParking = this.searchingParking != null ? this.searchingParking : object.searchingParking;
        this.destination = this.destination != null ? this.destination : object.destination;
        this.lat = this.lat != null ? this.lat : object.lat;
        this.lon = this.lon != null ? this.lon : object.lon;
    }

}
