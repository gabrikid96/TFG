package grodrich7.tfg.Models;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by gabri on 14/01/2018.
 */

public class Constants {
    public enum Data {
        DRIVING,
        LOCATION,
        ACCEPT_CALLS,
        TRIP_TIME_START,
        DESTINATION,
        SEARCHING_PARKING,
        IMAGES
    }

    public static final String USERS_REFERENCE = "users";
    public static final String GROUPS_REFERENCE = "groups";
    public static final String FRIENDS_REFERENCE = "friends";
    public static final String DATA_REFERENCE = "driving_data";
    public static final int DEFAULT_TIME_LOCATION = 5000;//5 segundos
    public static final long DEFAULT_TIME_CAMERA = 60000; //60 segundos
    public static final LatLng DEFAULT_LOCATION = new LatLng(41.386377, 2.164178);
    public static final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather?";
}
