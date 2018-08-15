package grodrich7.tfg.Models;


import com.google.firebase.database.Exclude;

import java.util.ArrayList;

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
    private LocationInfo locationInfo;
    private ArrayList<String> images;


    public DrivingData() {
    }


    public Boolean isDriving() {
        return driving == null ? false : driving;
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

    public LocationInfo getLocationInfo() {
        return locationInfo;
    }

    public void setLocationInfo(LocationInfo locationInfo) {
        this.locationInfo = locationInfo;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void merge(DrivingData object) {
        this.driving = getNew(this.driving, object.driving);
        this.acceptCalls = getNew(this.acceptCalls, object.acceptCalls);
        this.startTimeHour = getNew(this.startTimeHour, object.startTimeHour);
        this.startTimeMin = getNew(this.startTimeMin, object.startTimeMin);
        this.searchingParking = getNew(this.searchingParking, object.searchingParking);
        this.destination = getNew(this.destination, object.destination);
        this.locationInfo = getNew(this.locationInfo, object.locationInfo);
        this.images = getNew(this.images, object.images);
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }

    @Exclude
    private <T extends Object> T getNew(T old, T newO){
       return (old != null && newO != null) || (old == null && newO != null) ? newO : old;
    }

}
