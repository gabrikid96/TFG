package grodrich7.tfg.Activities.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import grodrich7.tfg.Models.Constants;

public class AppService extends Service {

    private LocationHandler locationHandler;
    private CameraHandler cameraHandler;
    private Handler handler;
    private Runnable runnableCode;


    @Override
    public void onCreate() {
        super.onCreate();
        locationHandler = new LocationHandler(this);
        cameraHandler = new CameraHandler(this);
        handler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationHandler.start();

        startCameraService();
        return START_STICKY;
    }

    private void startCameraService(){
        String time = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getString("image_sync", "");
        long interval = Constants.DEFAULT_TIME_LOCATION;
        try{
            interval = Long.parseLong(time) * 60 * 1000;//seconds
        }catch (NumberFormatException ex){
        }

        final long finalInterval = interval;
        runnableCode = new Runnable() {
            @Override
            public void run() {
                try{
                    Log.v("HANDLER", "PING");
                    cameraHandler.start();
                    handler.postDelayed(this, finalInterval);
                }catch (Exception ex){
                    Toast.makeText(AppService.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        };
        // Start the initial runnable task by posting through the handler
        handler.post(runnableCode);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("STOP_SERVICE", "DONE");
        try{
            handler.removeCallbacks(runnableCode);
            locationHandler.stop();
            cameraHandler.stop();
        }catch (Exception ex){
            Log.e("STOP_SERVICE", ex.getMessage());
        }
    }

    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {

                }
            }
        };
        t.start();
        return t;
    }
}