package grodrich7.tfg.Models;


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
        this.driving = this.driving != null ? this.driving : object.driving;
        this.acceptCalls = this.acceptCalls != null ? this.acceptCalls : object.acceptCalls;
        this.startTimeHour = this.startTimeHour != null ? this.startTimeHour : object.startTimeHour;
        this.startTimeMin = this.startTimeMin != null ? this.startTimeMin : object.startTimeMin;
        this.searchingParking = this.searchingParking != null ? this.searchingParking : object.searchingParking;
        this.destination = this.destination != null ? this.destination : object.destination;
        this.locationInfo = this.locationInfo != null ? this.locationInfo : object.locationInfo;
    }

}
