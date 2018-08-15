package grodrich7.tfg.Activities;

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
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

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.ceylonlabs.imageviewpopup.ImagePopup;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import grodrich7.tfg.Activities.Services.AppService;
import grodrich7.tfg.Activities.Services.CameraHandler;
import grodrich7.tfg.Models.DrivingData;
import grodrich7.tfg.R;

import static grodrich7.tfg.Models.Constants.GOOGLE_MAPS_URL;
import static grodrich7.tfg.Models.Constants.WEATHER_URL;

public class DrivingActivity extends HelperActivity {

    private ImageButton drivingToggle;
    private ImageButton helpBtn;
    private TextView destination;
    private TextView timeEstimated;
    private TextView startTimeData;
    private ImageView parking_icon;
    private ImageView lastImage;
    private RelativeLayout voice_layout;
    private RelativeLayout call_layout;
    private static final int ALL_PERMISSIONS = 7171;
    private BroadcastReceiver cameraReceiver;

    public static final String FINISH_ACTION  = "FINISH";
    private String lastUrl = "";

    private RecognitionCommands recognitionCommands;
    private RequestQueue mRequestQueue;
    private boolean voiceCommands;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recognitionCommands = new RecognitionCommands(this);
        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());
        // Instantiate the RequestQueue with the cache and network.
        mRequestQueue = new RequestQueue(cache, network);
    }

    @Override
    protected void onStart() {
        super.onStart();
        permissionsSettings();
    }

    private void permissionsSettings(){
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.RECORD_AUDIO);
        permissions.add(Manifest.permission.CAMERA);

        if (permissions.size() > 0){
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[3]),ALL_PERMISSIONS);
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
        voice_layout = findViewById(R.id.voice_layout);
        this.destination.setText(controller.getDrivingData().getDestination() == null || controller.getDrivingData().getDestination().isEmpty() ? getString(R.string.unknownInformation) : controller.getDrivingData().getDestination());
        toggleDrivingIcon();
        toggleParkingIcon();
        toggleCallIcon();
        changeStartTimeText();
        putLastImage();
        if (controller.getDrivingData() != null && controller.getDrivingData().isDriving() != null && controller.getDrivingData().isDriving().booleanValue()){
            cameraReceiver = createCameraReceiver();
            IntentFilter intentFilter = new IntentFilter("grodrich7.tfg.CAMERA_ACTION");
            LocalBroadcastManager.getInstance(this).registerReceiver(cameraReceiver, intentFilter);
        }
    }

    private void showDrivingDialog(){
        int message = controller.getDrivingData().isDriving() == null || !controller.getDrivingData().isDriving() ? R.string.driving_mode_on_attempt : R.string.driving_mode_off_attempt;
        putLastImage();
        new AlertDialog.Builder(DrivingActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.driving_title)
                .setMessage(message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (controller.getCurrentUser().getGroups()== null ||  controller.getCurrentUser().getGroups().size() == 0){
                            new AlertDialog.Builder(DrivingActivity.this).setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle(R.string.no_groups_title)
                                    .setMessage(R.string.no_groups_message).show();
                        }else{
                            controller.getDrivingData().setDriving(controller.getDrivingData().isDriving() != null ? !controller.getDrivingData().isDriving() : true);
                            toggleDrivingIcon();

                            if (controller.getDrivingData().isDriving()){
                                shareData();
                            }else{
                                stopDriving();
                            }
                        }
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void stopDriving() {
        stopService(new Intent(DrivingActivity.this, AppService.class));
        recognitionCommands.stopListening();
        putLastImage();
        controller.endDriving();
        if (cameraReceiver != null){
            LocalBroadcastManager.getInstance(this).unregisterReceiver(cameraReceiver);
            //unregisterReceiver(cameraReceiver);
            cameraReceiver = null;
        }
       // stopService(new Intent(DrivingActivity.this, CameraService.class));
    }

    private void shareData() {
        statusCheck();
        controller.getDrivingData().setDriving(true);
        Date now = Calendar.getInstance().getTime();
        controller.getDrivingData().setStartTimeHour(now.getHours());
        controller.getDrivingData().setStartTimeMin(now.getMinutes());
        changeStartTimeText();
        controller.saveDrivingData();

        cameraReceiver = createCameraReceiver();
        IntentFilter intentFilter = new IntentFilter("grodrich7.tfg.CAMERA_ACTION");
        registerReceiver(cameraReceiver, intentFilter);

        startService(new Intent(this, AppService.class));
        recognitionCommands.startListening(voiceCommands);
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
        if (controller.getDrivingData() != null && controller.getDrivingData().getLocationInfo() != null){
            getEstimatedTime();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case ALL_PERMISSIONS:
                if (grantResults.length > 0 ){
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED){//Location

                    }

                    if (grantResults[1] == PackageManager.PERMISSION_GRANTED){//Location

                    }

                    if (grantResults[2] == PackageManager.PERMISSION_GRANTED){//Audio

                    }

                    if (grantResults[3] == PackageManager.PERMISSION_GRANTED){//Camera
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (!Settings.canDrawOverlays(this)) {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                                startActivityForResult(intent, 0);
                            }
                        }
                    }
                }
                break;
        }
    }

    //region OnClick
    public void viewsHandles(View v){
        pressEffect(v);
        putLastImage();
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
                putLastImage();
                final ImagePopup imagePopup = new ImagePopup(DrivingActivity.this);
                imagePopup.setFullScreen(true); // Optional
                imagePopup.setBackgroundColor(getResources().getColor(R.color.transparent));
                imagePopup.setImageOnClickClose(true);  // Optional
                imagePopup.initiatePopup(lastImage.getDrawable());
                imagePopup.viewPopup();
                break;
            case R.id.voice_layout:
                this.voiceCommands = !voiceCommands;
                voice_layout.setBackgroundResource(voiceCommands ? R.drawable.destination_shape : R.drawable.disabled_shape);
                if (controller.getDrivingData().isDriving() != null && controller.getDrivingData().isDriving() && this.voiceCommands){
                    recognitionCommands.startListening(this.voiceCommands);
                }else if (controller.getDrivingData().isDriving() != null && controller.getDrivingData().isDriving() && !this.voiceCommands){
                    recognitionCommands.stopListening();
                }
                break;
        }
    }

    private void putLastImage(){
        try{
            if (controller.getDrivingData() != null && controller.getDrivingData().getImages() != null && controller.getDrivingData().getImages().size() > 0){
                String url = controller.getDrivingData().getImages().get(controller.getDrivingData().getImages().size() -1);
                if (!url.equals(lastUrl)){
                    lastUrl = url;
                    Glide.with(this).load(url).into(lastImage);
                }
            }
        }catch (Exception ex){}

    }

    public void toggleCallIcon(){
        call_layout.setBackgroundResource(controller.getDrivingData().isAcceptCalls() != null && controller.getDrivingData().isAcceptCalls() ? R.drawable.destination_shape : R.drawable.disabled_shape);
    }

    public  void toggleParkingIcon(){
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
    protected void onPause() {
        super.onPause();
        if (cameraReceiver != null){
            LocalBroadcastManager.getInstance(this).unregisterReceiver(cameraReceiver);
            cameraReceiver = null;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getAction() != null){
            switch (intent.getAction()){
                case FINISH_ACTION:
                    stopService(new Intent(DrivingActivity.this, AppService.class));
                    controller.endDriving();
                    toggleDrivingIcon();
                    break;
            }
        }
        super.onNewIntent(intent);

    }

    private BroadcastReceiver createCameraReceiver(){
        return cameraReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try{
                    byte[] data = intent.getByteArrayExtra("image");
                    if (data != null){
                        Bitmap bitmap = CameraHandler.decodeBitmap(data);
                        lastImage.setImageBitmap(bitmap);
                    }
                    Log.d("RECEIVE", "Receive");
                }catch (Exception ex){
                    Toast.makeText(context, "RECEIVE" + ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        };
    }
    public void startVoiceToTextService(int CODE) {
        Intent intent = new Intent(CODE == 1 ? RecognizerIntent.ACTION_RECOGNIZE_SPEECH : RecognizerIntent.ACTION_VOICE_SEARCH_HANDS_FREE);
        //You can set here own local Language.
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        try {
            startActivityForResult(intent, CODE);
        }
        catch (ActivityNotFoundException a) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> resultsList = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (resultsList != null && resultsList.size() > 0){
                        changeDestination(resultsList.get(0));
                    }
                    recognitionCommands.restartListening();
                }
                break;
            }
            case 2: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> resultsList = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (resultsList != null && resultsList.size() > 0){
                       // changeDestination(resultsList.get(0));
                    }
                    recognitionCommands.restartListening();
                }
                break;
            }
            case 3: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> resultsList = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (resultsList != null && resultsList.size() > 0){
                        // changeDestination(resultsList.get(0));
                    }
                    recognitionCommands.restartListening();
                }
                break;
            }

        }
    }

    public void getEstimatedTime(){
        DrivingData drivingData = controller.getDrivingData();
        String url = GOOGLE_MAPS_URL + "units=metric" + "&origins=" +  drivingData.getLocationInfo().getLat() + "," + drivingData.getLocationInfo().getLon() +
                "&destinations=" + drivingData.getDestination() + "&departure_time=now" + "&mode=driving" + "&language=" + Locale.getDefault().getLanguage() +
                "&key=AIzaSyA89lb-rq0M168bt_w7NpAYz41QeJyZzMI";
        Log.d("ESTIMATED", url);
        mRequestQueue.start();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject elements = response.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0);
                            timeEstimated.setText(" " + elements.getJSONObject("duration").getString("text"));

                            /*description = weather.getString("description");
                            icon = "http://openweathermap.org/img/w/" + weather.getString("icon") + ".png";
                            temp = response.getJSONObject("main").getDouble("temp");*/
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        mRequestQueue.stop();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mRequestQueue.stop();
                    }
                });
        mRequestQueue.add(jsonObjectRequest);
    }
}

