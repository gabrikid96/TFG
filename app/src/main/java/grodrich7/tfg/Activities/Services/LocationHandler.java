package grodrich7.tfg.Activities.Services;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import grodrich7.tfg.Activities.DrivingActivity;
import grodrich7.tfg.Controller.Controller;
import grodrich7.tfg.Models.Constants;
import grodrich7.tfg.R;

import static grodrich7.tfg.Activities.DrivingActivity.FINISH_ACTION;

public class LocationHandler {

    private Service service;
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    public LocationManager locationManager;
    public MyLocationListener listener;
    public Location previousBestLocation = null;

    public LocationHandler(Service service){
        this.service = service;
        addNotification();
    }

    @SuppressLint("MissingPermission")
    public void start(){
        locationManager = (LocationManager) service.getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();

        String time = PreferenceManager.getDefaultSharedPreferences(service.getApplicationContext())
                .getString("location_sync", "");
        int interval;
        try{
            interval = Integer.parseInt(time) * 1000;//seconds
        }catch (NumberFormatException ex){
            interval = Constants.DEFAULT_TIME_LOCATION;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, interval, 0, listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, interval, 0, listener);
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    private void addNotification(){
        Intent notificationIntent = new Intent(service, DrivingActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.setAction(FINISH_ACTION);
        PendingIntent pendingIntent = PendingIntent.getActivity(service, 0,
                notificationIntent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(service)
                .setSmallIcon(R.drawable.car)
                .setContentTitle(service.getString(R.string.sharingData))
                .setContentText(service.getString(R.string.sharingData))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(Color.BLUE)
                .addAction(new NotificationCompat.Action(
                        R.mipmap.driving_on,
                        service.getString(R.string.finish),
                        PendingIntent.getActivity(service,0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                ));
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId("1337");
        }
        service.startForeground(1337,notificationBuilder.build());
    }

    public void stop(){
        locationManager.removeUpdates(listener);
    }

    public class MyLocationListener implements LocationListener
    {


        public void onLocationChanged(final Location loc)
        {
            Log.i("LOCATION", "Location changed");
            if(isBetterLocation(loc, previousBestLocation)) {
                loc.getLatitude();
                loc.getLongitude();
                Controller.getInstance().updateLocation(loc);
            }
        }

        public void onProviderDisabled(String provider)
        {
        }


        public void onProviderEnabled(String provider)
        {
        }


        public void onStatusChanged(String provider, int status, Bundle extras)
        {
        }

    }
}
