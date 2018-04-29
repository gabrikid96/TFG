package grodrich7.tfg.Activities;

import android.Manifest;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import grodrich7.tfg.Models.Services.CameraService;
import grodrich7.tfg.Models.Services.LocationService;
import grodrich7.tfg.R;

public class DrivingActivity extends HelperActivity {

    private ImageButton drivingToggle;
    private ImageButton helpBtn;

    private TextView destination;
    private TextView timeEstimated;
    private TextView startTimeData;

    private ImageView parking_icon;
    private ImageView lastImage;

    private RelativeLayout call_layout;

    private static final int MY_PERMISSION_REQUEST_CODE = 7171;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationSettings();
    }

    private void locationSettings(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
        }else{
            if (checkPlayServices()){
            }
        }

    }

    protected void getViewsByXML() {
        setContentView(R.layout.activity_driving);
        enableToolbar(R.string.driving_title);
        drivingToggle = findViewById(R.id.drivingToggle);
        helpBtn = findViewById(R.id.helpBtn);
        drivingToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDrivingDialog();
            }
        });
        helpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(DrivingActivity.this, R.style.FullHeightDialog);
                dialog.setContentView(R.layout.help_actions);
                dialog.setTitle("");
                dialog.setCancelable(true);
                dialog.show();
            }
        });

        destination = findViewById(R.id.destination);
        timeEstimated = findViewById(R.id.time_estimated);
        startTimeData = findViewById(R.id.startTimeData);
        parking_icon = findViewById(R.id.parking_icon);
        call_layout = findViewById(R.id.call_layout);
        lastImage = findViewById(R.id.image);
        this.destination.setText(controller.getDrivingData().getDestination() == null || controller.getDrivingData().getDestination().isEmpty() ? getString(R.string.unknownInformation) : controller.getDrivingData().getDestination());
        toggleDrivingIcon();
        toggleParkingIcon();
        toggleCallIcon();
        changeStartTimeText();
    }

    private void showDrivingDialog(){
        int message = controller.getDrivingData().isDriving() == null || !controller.getDrivingData().isDriving() ? R.string.driving_mode_on_attempt : R.string.driving_mode_off_attempt;

        new AlertDialog.Builder(DrivingActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.driving_title)
                .setMessage(message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        controller.getDrivingData().setDriving(controller.getDrivingData().isDriving() != null ? !controller.getDrivingData().isDriving() : true);
                        toggleDrivingIcon();

                        if (controller.getDrivingData().isDriving()){
                            shareData();
                        }else{
                            stopService(new Intent(DrivingActivity.this, LocationService.class));
                            controller.endDriving();
                            //turnGPSOff();
                        }
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void shareData() {
//        turnGPSOn();
        controller.getDrivingData().setDriving(true);
        Date now = Calendar.getInstance().getTime();
        controller.getDrivingData().setStartTimeHour(now.getHours());
        controller.getDrivingData().setStartTimeMin(now.getMinutes());
        changeStartTimeText();

        //createNotification();
        controller.saveDrivingData();
        startService(new Intent(this, LocationService.class));
    }

    private void createNotification(){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this,getString(R.string.driving_title))
                .setSmallIcon(R.mipmap.driving_on)
                .setContentTitle(getString(R.string.driving_title))
                .setContentText(getString(R.string.driving_notification))
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        mBuilder.build();
        initChannels();
    }

    public void initChannels() {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel( getString(R.string.driving_title),
                getString(R.string.driving_title),
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription( getString(R.string.driving_title));
        notificationManager.createNotificationChannel(channel);
    }

    private void toggleDrivingIcon(){
        drivingToggle.setBackgroundResource(controller.getDrivingData().isDriving() != null && controller.getDrivingData().isDriving() ? R.mipmap.driving_on : R.mipmap.driving_off);
    }

    private void destinationAlert(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(R.string.destination_label);
        final EditText destination = new EditText(this);
        destination.setInputType(InputType.TYPE_CLASS_TEXT);
        destination.setHint(R.string.destination_label);
        alert.setView(destination);


        alert.setPositiveButton(R.string.send_email, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                changeDestination(destination.getText().toString());
            }
        });
        alert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    private void changeDestination (String newDestination){
        this.destination.setText(newDestination);
        controller.updateDestination(newDestination);
        estimateDestination();
    }

    private void estimateDestination(){
        String destination = controller.getDrivingData().getDestination();
        if (destination != null && !destination.isEmpty()){
            Geocoder gc = new Geocoder(this);
            if(gc.isPresent()) {

                try {
                    List<Address> addresses = gc.getFromLocationName(destination, 1);
                    if (addresses.size() > 0){
                        Address address = addresses.get(0);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    private void takePicture(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        100);
            }else{
                startService(new Intent(DrivingActivity.this, CameraService.class));
            }
        }else{
            startService(new Intent(DrivingActivity.this, CameraService.class));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (checkPlayServices()){
                        //buildGoogleApiClient();
                    }
                }
                break;
            case 100:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
                }
            break;
        }
    }
    //region OnClick
    public void viewsHandles(View v){
        switch (v.getId()){
            case R.id.destinationInfo:
                destinationAlert();
                break;
            case R.id.parking_layout:
                controller.updateParking(controller.getDrivingData().isSearchingParking() != null ?!controller.getDrivingData().isSearchingParking() : true);
                toggleParkingIcon();
                break;
            case R.id.call_layout:
                controller.updateAcceptCalls(controller.getDrivingData().isAcceptCalls() != null ?!controller.getDrivingData().isAcceptCalls() : true);
                toggleCallIcon();
                break;
            case R.id.image:
                //takePicture();
                break;
        }
    }

    private void toggleCallIcon(){
        call_layout.setBackgroundResource(controller.getDrivingData().isAcceptCalls() != null && controller.getDrivingData().isAcceptCalls() ? R.drawable.destination_shape : R.drawable.disabled_shape);
    }

    private  void toggleParkingIcon(){
        parking_icon.setBackgroundResource(controller.getDrivingData().isSearchingParking() != null && controller.getDrivingData().isSearchingParking() ? R.mipmap.parking_icon : R.mipmap.no_parking);
    }

    private void changeStartTimeText(){
        Integer hour = controller.getDrivingData().getStartTimeHour();
        Integer min = controller.getDrivingData().getStartTimeMin();
        startTimeData.setText(hour == null && min == null ? "--:--" : String.format("%02d", hour) + ":" + String.format("%02d", min));
    }
    //endregion
    //region GPS sensor
    private void turnGPSOn(){
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(!provider.contains("gps")){ //if gps is disabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            sendBroadcast(poke);
        }
    }

    private void turnGPSOff(){
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(provider.contains("gps")){ //if gps is enabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            sendBroadcast(poke);
        }
    }
    //endregion
}
