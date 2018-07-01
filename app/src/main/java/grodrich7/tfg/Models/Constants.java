package grodrich7.tfg.Models;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

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
    public static final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather?";
    public static final String GOOGLE_MAPS_URL = "https://maps.googleapis.com/maps/api/distancematrix/json?";
    public static final ArrayList<String> LANGUAGES_AVAILABLES = new ArrayList<String>() {
        {
            add("en");
            add("es");
            add("ca");
        }
    };
}
