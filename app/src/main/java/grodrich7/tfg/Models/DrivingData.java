package grodrich7.tfg.Models;


import android.location.Location;

import java.lang.reflect.Field;
import java.util.Date;

/**
 * Created by gabri on 21/04/2018.
 */

public class DrivingData {
    private boolean driving;
    private boolean acceptCalls;

    private int startTimeHour;
    private int startTimeMin;
    private boolean searchingParking;
    private String destination;
    private String lat;
    private String lon;

    public DrivingData() {
    }


    public boolean isDriving() {
        return driving;
    }

    public void setDriving(boolean driving) {
        this.driving = driving;
    }

    public boolean isAcceptCalls() {
        return acceptCalls;
    }

    public void setAcceptCalls(boolean acceptCalls) {
        this.acceptCalls = acceptCalls;
    }

    public int getStartTimeHour() {
        return startTimeHour;
    }

    public void setStartTimeHour(int startTimeHour) {
        this.startTimeHour = startTimeHour;
    }

    public int getStartTimeMin() {
        return startTimeMin;
    }

    public void setStartTimeMin(int startTimeMin) {
        this.startTimeMin = startTimeMin;
    }

    public boolean isSearchingParking() {
        return searchingParking;
    }

    public void setSearchingParking(boolean searchingParking) {
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

}
