package grodrich7.tfg.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import com.google.android.gms.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Calendar;
import java.util.Date;

import grodrich7.tfg.Manifest;
import grodrich7.tfg.Models.DrivingData;
import grodrich7.tfg.Models.LocationHelper;
import grodrich7.tfg.R;

public class DrivingActivity extends HelperActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private ImageButton drivingToggle;
    private ImageButton helpBtn;
    private boolean isDrivingActivate;

    private TextView destination;
    private TextView timeEstimated;
    private TextView startTimeData;

    private ImageView parking_icon;
    private ImageView lastImage;
    private DrivingData drivingData;

    private RelativeLayout call_layout;

    private static final int MY_PERMISSION_REQUEST_CODE = 7171;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;


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
                buildGoogleApiClient();
            }
        }

    }


    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    private Location getLastLocation(){
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) ? null :
                LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    private void startLocationUpdates(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }else{
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    protected void getViewsByXML() {
        setContentView(R.layout.activity_driving);
        enableToolbar(R.string.driving_title);
        isDrivingActivate = false;
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
        drivingData = new DrivingData();
    }

    private void showDrivingDialog(){
        int message = !isDrivingActivate ? R.string.driving_mode_on_attempt : R.string.driving_mode_off_attempt;

        new AlertDialog.Builder(DrivingActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.driving_title)
                .setMessage(message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isDrivingActivate = !isDrivingActivate;
                        toggleDrivingIcon();
                        if (isDrivingActivate) shareData();
                        else{
                            if (mGoogleApiClient != null) mGoogleApiClient.disconnect();
                        }
                    }

                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void shareData() {
        drivingData.setDriving(true);
        Date now = Calendar.getInstance().getTime();
        int hour = now.getHours();
        int min = now.getMinutes();
        drivingData.setStartTimeHour(hour);
        drivingData.setStartTimeMin(min);
        startTimeData.setText(String.format("%02d", hour) + ":" + String.format("%02d", min));
        if (mGoogleApiClient != null) mGoogleApiClient.connect();
        mLocationRequest = LocationHelper.createLocationRequest();
    }

    private void toggleDrivingIcon(){
        drivingToggle.setBackgroundResource(isDrivingActivate ? R.mipmap.driving_on : R.mipmap.driving_off);
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
        drivingData.setDestination(newDestination);
    }

    //region Location Listeners
    @Override
    public void onLocationChanged(Location location) {
        drivingData.setLat(String.valueOf(location.getLatitude()));
        drivingData.setLon(String.valueOf(location.getLongitude()));
        controller.updateLocation(drivingData);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Location location = getLastLocation();
        if (location != null){
            drivingData.setLat(String.valueOf(location.getLatitude()));
            drivingData.setLon(String.valueOf(location.getLongitude()));
        }
        controller.saveDrivingData(drivingData);
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    //endregion
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (checkPlayServices()){
                        buildGoogleApiClient();
                    }
                }
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    //region OnClick
    public void viewsHandles(View v){
        switch (v.getId()){
            case R.id.destinationInfo:
                destinationAlert();
                break;
            case R.id.parking_layout:
                drivingData.setSearchingParking(!drivingData.isSearchingParking());
                parking_icon.setBackgroundResource(drivingData.isSearchingParking() ? R.mipmap.parking_icon : R.mipmap.no_parking);
                break;
            case R.id.call_layout:
                drivingData.setAcceptCalls(!drivingData.isAcceptCalls());
                call_layout.setBackgroundResource(drivingData.isAcceptCalls() ? R.drawable.destination_shape : R.drawable.disabled_shape);
                break;
            case R.id.image:
                /*Intent intent = new Intent(DrivingActivity.this, CameraService.class);
                intent.putExtra("image", lastImage.getDrawingCache());
                startService(intent);*/
                break;
        }
    }
    //endregion
}
