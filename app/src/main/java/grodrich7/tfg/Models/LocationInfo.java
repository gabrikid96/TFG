package grodrich7.tfg.Models;

import com.google.firebase.database.Exclude;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LocationInfo {
    private String lat;
    private String lon;
    private String lastLocationTime;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy");

    public LocationInfo(){}

    public LocationInfo(String lat, String lon, Date lastLocationTime) {
        this.lat = lat;
        this.lon = lon;
        this.lastLocationTime = sdf.format(lastLocationTime);
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

    @Exclude
    public void setLastLocationTime(Date date){
        this.lastLocationTime = sdf.format(date);
    }

    public void setLastLocationTime(String date){
        this.lastLocationTime = date;
    }

    public String getLastLocationTime() {
        return lastLocationTime;
    }
}
