package grodrich7.tfg.Activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
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
import java.util.Timer;
import java.util.TimerTask;

import grodrich7.tfg.Models.Constants;
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
    private static final int LOCATION_PERMISSION = 7171;
    private static final int CAMERA_PERMISSION = 100;
    private static final int DRAW_OVER_PERMISSION = 101;

    public static final String FINISH_ACTION  = "FINISH";

    private Timer timer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationSettings();
        cameraSettings();
    }

    private void locationSettings(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, LOCATION_PERMISSION);
        }else{
            if (checkPlayServices()){
            }
        }
    }

    private void cameraSettings(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        CAMERA_PERMISSION);
            }
            if (!Settings.canDrawOverlays(this.getApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivityForResult(intent, DRAW_OVER_PERMISSION);
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
                            stopDriving();
                        }
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void stopDriving() {
        stopService(new Intent(DrivingActivity.this, LocationService.class));
        controller.endDriving();
       // stopService(new Intent(DrivingActivity.this, CameraService.class));
        timer.cancel();
        timer = null;
    }

    private void shareData() {
        statusCheck();
        controller.getDrivingData().setDriving(true);
        Date now = Calendar.getInstance().getTime();
        controller.getDrivingData().setStartTimeHour(now.getHours());
        controller.getDrivingData().setStartTimeMin(now.getMinutes());
        changeStartTimeText();

        //createNotification();
        controller.saveDrivingData();
        startService(new Intent(this, LocationService.class));
        startCameraService();
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
            case LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (checkPlayServices()){
                        //buildGoogleApiClient();
                    }
                }
                break;
            case CAMERA_PERMISSION:
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
               takePicture();
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

    //region GPS enable
    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.gps_disabled)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
    //endregion

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getAction() != null){
            switch (intent.getAction()){
                case FINISH_ACTION:
                    stopService(new Intent(DrivingActivity.this, LocationService.class));
                    controller.endDriving();
                    toggleDrivingIcon();
                    break;
            }
        }
        super.onNewIntent(intent);

    }

    private void startCameraService(){
        String time = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getString("image_sync", "");
        long interval;
        try{
            interval = Long.parseLong(time) * 60 * 1000;//seconds
        }catch (NumberFormatException ex){
            interval = Constants.DEFAULT_TIME_LOCATION;
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("CAMERA_TASK", "Hello");
            }

        }, 0, interval);
    }



}
