package grodrich7.tfg.Activities.Services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import grodrich7.tfg.Activities.ViewUserActivity;
import grodrich7.tfg.Models.Constants;
import grodrich7.tfg.Models.DrivingData;
import grodrich7.tfg.Models.User;
import grodrich7.tfg.R;

import static grodrich7.tfg.Activities.ViewUserActivity.VIEW_ACTION;
import static grodrich7.tfg.Models.Constants.DATA_REFERENCE;

public class NotificationService extends Service {

    Timer timer;
    TimerTask timerTask;
    String TAG = "Timers";
    int Your_X_SECS = 5;

    HashMap<String, DrivingData> usersData;
    FirebaseDatabase database;
    DatabaseReference dataReference;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        usersData = new HashMap<>();
        database = FirebaseDatabase.getInstance();
        dataReference = database.getReference(DATA_REFERENCE);

        dataReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try{
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()){
                        for (DataSnapshot groupSnapshot : userSnapshot.getChildren()) {
                            for (DataSnapshot drivingDataSnapshot : groupSnapshot.getChildren()){
                                if (drivingDataSnapshot.getKey().equals(FirebaseAuth.getInstance().getUid())){
                                    DrivingData drivingData = drivingDataSnapshot.getValue(DrivingData.class);
                                    if (usersData.containsKey(userSnapshot.getKey())){
                                        DrivingData old = usersData.get(userSnapshot.getKey());
                                        old.merge(drivingData);
                                        usersData.put(userSnapshot.getKey(), old);
                                    }else{
                                        usersData.put(userSnapshot.getKey(), drivingData);
                                    }
                                }
                            }
                        }
                    }
                    startTimer();
                }catch (Exception ex){
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        return START_STICKY;
    }


    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");


    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        stoptimertask();
        super.onDestroy();


    }

    //we are going to use a handler to be able to run in our TimerTask
    final Handler handler = new Handler();


    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 5000, Your_X_SECS * 1000); //
        //timer.schedule(timerTask, 5000,1000); //
    }

    private void addNotification(String key, String name){
        Intent notificationIntent = new Intent(this, ViewUserActivity.class);
        notificationIntent.putExtra("key",key);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.setAction(VIEW_ACTION);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.driving_on)
                .setContentTitle(name + " " + getString(R.string.driving_now))
                .setContentText(getString(R.string.tap_to_view))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(Color.BLUE);
        String ringtone = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getString("notifications_new_message_ringtone","");
        if (!ringtone.isEmpty()){
            Uri alarmSound = Uri.parse(ringtone);
            mBuilder.setSound(alarmSound);
        }
        boolean vibration = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean("notifications_new_message_vibrate",true);
        if (vibration){
            mBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
        }

        boolean led = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean("notifications_led",true);

        if (led){
            mBuilder.setLights(Color.GREEN, 3000, 3000);
        }

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel("001", "NOTIFICATION_USER_DRIVING", importance);
            mBuilder.setChannelId("001");
            mNotificationManager.createNotificationChannel(notificationChannel);
        }

        mNotificationManager.notify(001, mBuilder.build());
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {

                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        request();
                    }
                });
            }
        };
    }

    public void request(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dataReference = database.getReference(DATA_REFERENCE);
        dataReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try{
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()){
                        for (DataSnapshot groupSnapshot : userSnapshot.getChildren()) {
                            for (DataSnapshot drivingDataSnapshot : groupSnapshot.getChildren()){
                                if (drivingDataSnapshot.getKey().equals(FirebaseAuth.getInstance().getUid())){
                                    DrivingData drivingData = drivingDataSnapshot.getValue(DrivingData.class);
                                    DrivingData old = usersData.get(userSnapshot.getKey());
                                    if (old == null){
                                        if (drivingData.isDriving() != null && drivingData.isDriving().booleanValue()){
                                            Log.d("NOTIFICATION_DRIVING", userSnapshot.getKey() + " is now driving");
                                            usersData.put(userSnapshot.getKey(), drivingData);
                                            sendNotification(userSnapshot.getKey());
                                        }
                                    }else{
                                        if (old.isDriving() != null && !old.isDriving().booleanValue() && drivingData.isDriving() != null && drivingData.isDriving().booleanValue()){
                                            Log.d("NOTIFICATION_DRIVING", userSnapshot.getKey() + " is now driving...");
                                            usersData.put(userSnapshot.getKey(), drivingData);
                                            sendNotification(userSnapshot.getKey());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }catch (Exception ex){
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void sendNotification(final String key){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference  = database.getReference(Constants.USERS_REFERENCE);
        databaseReference.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try{
                    User user = dataSnapshot.getValue(User.class);
                    addNotification(key, user.getName());
                }catch(Exception ex){}
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
