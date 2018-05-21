package grodrich7.tfg.Activities;

import android.app.Notification;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ceylonlabs.imageviewpopup.ImagePopup;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import grodrich7.tfg.Activities.Services.AppService;
import grodrich7.tfg.Activities.Services.NotificationService;
import grodrich7.tfg.Models.DrivingData;
import grodrich7.tfg.Models.User;
import grodrich7.tfg.R;

public class ViewUserActivity extends HelperActivity implements OnMapReadyCallback {

    private MapFragment mapFragment;
    private GoogleMap googleMap;
    private ImageButton fullBtn;

    private TextView driving;
    private TextView destinationData;
    private TextView startTimeData;
    private TextView acceptCallsData;
    private TextView parkingData;
    private RecyclerView recyclerView;
    private DrivingData drivingData;
    boolean full;
    private String friendUid;

    public static final String VIEW_ACTION  = "VIEW";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        friendUid = (String) getIntent().getSerializableExtra("key");
        getData(friendUid);
    }

    private void getData(String friendUid){
        drivingData = new DrivingData();
        controller.dataReference.child(friendUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try{
                    for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                        for (DataSnapshot drivingDataSnapshot : groupSnapshot.getChildren()){
                            if (drivingDataSnapshot.getKey().equals(FirebaseAuth.getInstance().getUid())){
                                drivingData.merge(drivingDataSnapshot.getValue(DrivingData.class));
                            }
                          }
                    }
                }catch (Exception ex){
                    Log.e("VIEW", ex.getMessage());
                }finally {
                    putData(drivingData);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    protected void getViewsByXML() {
        setContentView(R.layout.activity_view_user_activiy);
        createMapFragment();
        String name = (String) getIntent().getSerializableExtra("name");
        enableToolbar(name);
        fullBtn = findViewById(R.id.fullBtn);
        fullBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DisplayMetrics displayMetrics = getMetrics();
                fullBtn.setBackgroundResource(!full ? R.mipmap.unfull_window : R.mipmap.full_window);
                ResizeAnimation resizeAnimation = new ResizeAnimation(
                        mapFragment.getView(),
                        !full ? displayMetrics.heightPixels : displayMetrics.heightPixels/4,
                        mapFragment.getView().getLayoutParams().height
                );
                resizeAnimation.setDuration(1000);
                mapFragment.getView().startAnimation(resizeAnimation);
                full = !full;
            }
        });

        driving = findViewById(R.id.drivingData);
        destinationData = findViewById(R.id.destinationData);
        startTimeData = findViewById(R.id.startTimeData);
        acceptCallsData = findViewById(R.id.acceptCallsData);
        parkingData = findViewById(R.id.parkingData);
    }

    private void recyclerImages(){
        recyclerView = findViewById(R.id.recyclerImages);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        RecyclerView.Adapter groupsAdapter = new RecyclerView.Adapter<ImageHolder>() {
            @Override
            public ImageHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_square
                        , viewGroup, false);
                return new ImageHolder(view);
            }

            @Override
            public void onBindViewHolder(ImageHolder viewHolder, int i) {
                viewHolder.description.setText("Imagen " + String.valueOf(i+1));
                try{
                    String url = drivingData.getImages().get(i);
                    Glide.with(ViewUserActivity.this).load(url).into(viewHolder.imageButton);
                    //viewHolder.imageButton.setImageResource(R.drawable.front_image);
                    final ImagePopup imagePopup = new ImagePopup(ViewUserActivity.this);
                    imagePopup.setFullScreen(true); // Optional
                    imagePopup.setBackgroundColor(getResources().getColor(R.color.transparent));
                    imagePopup.setImageOnClickClose(true);  // Optional

                    imagePopup.initiatePopup(viewHolder.imageButton.getDrawable());
                    viewHolder.imageButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Initiate Popup view
                            imagePopup.viewPopup();
                        }
                    });
                }catch (Exception ex){
                    Log.e("REC_IMAGES", ex.getMessage());
                }
            }

            @Override
            public int getItemCount() {
                return drivingData.getImages().size();
            }
        };
        recyclerView.setAdapter(groupsAdapter);
    }

    private class ImageHolder extends RecyclerView.ViewHolder {

        private TextView description;
        private ImageView imageButton;

        public ImageHolder(View itemView) {
            super(itemView);
            imageButton = itemView.findViewById(R.id.image);
            description = itemView.findViewById(R.id.description);
        }
    }

    private void putData(DrivingData data){
        int booleanColor = data.isDriving() != null && data.isDriving() ? Color.GREEN : Color.RED;
        driving.setText(parseString(data.isDriving()));
        driving.setTextColor(booleanColor);

        destinationData.setText(data.getDestination() != null &&
                !data.getDestination().isEmpty() ? data.getDestination() :
                getResources().getString(R.string.unknownInformation));

        Integer hour = data.getStartTimeHour();
        Integer min = data.getStartTimeMin();
        startTimeData.setText(hour == null && min == null ? "--:--" : String.format("%02d", hour) + ":" + String.format("%02d", min));

        booleanColor = data.isAcceptCalls() != null && data.isAcceptCalls() ? Color.GREEN : Color.RED;
        acceptCallsData.setText(parseString(data.isAcceptCalls()));
        acceptCallsData.setTextColor(booleanColor);

        booleanColor = data.isSearchingParking() != null && data.isSearchingParking() ? Color.GREEN : Color.RED;
        parkingData.setText(parseString(data.isSearchingParking()));
        parkingData.setTextColor(booleanColor);
        updateLocation();
        if (drivingData.getImages() != null){
            recyclerImages();
        }

    }

    public DisplayMetrics getMetrics(){
        return getApplicationContext().getResources().getDisplayMetrics();
    }

    //region ToolbarSettings
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_view_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.refresh_Action:
                updateLocation();
                putData(drivingData);
                break;
            case android.R.id.home:
                onBackPressed();
            default:
                break;
        }

        return true;
    }*/
    //endregion

    //region MapFragment
    public void createMapFragment(){
        mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();
        params.height = getMetrics().heightPixels/4; //25%
        mapFragment.getView().setLayoutParams(params);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng location = getLocation();
        googleMap.addMarker(new MarkerOptions()
                .position(location)
                .title("Marker"));
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.moveCamera(getCameraPosition(location));
        googleMap.setTrafficEnabled(true);
        this.googleMap = googleMap;
    }

    public void updateLocation(){
        LatLng location = getLocation();
        googleMap.clear();
        String title = drivingData != null && drivingData.getLocationInfo() != null ?
                drivingData.getLocationInfo().getLastLocationTime() :
                getString(R.string.unknownInformation);
        googleMap.addMarker(new MarkerOptions().position(location).title(title));
        googleMap.moveCamera(getCameraPosition(location));
    }

    public LatLng getLocation(){
        return drivingData.getLocationInfo() != null ?
                new LatLng(Double.parseDouble(drivingData.getLocationInfo().getLat()),
                        Double.parseDouble(drivingData.getLocationInfo().getLon())) :
                new LatLng(41.386377, 2.164178);
    }

    public CameraUpdate getCameraPosition(LatLng currLatLng){
        return CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(currLatLng).zoom(14.0f).build());
    }
    //endregion
    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getAction() != null){
            switch (intent.getAction()){
                case VIEW_ACTION:
                    if (isServiceRunning(NotificationService.class)) stopService(new Intent(this, NotificationService.class));
                    break;
            }
        }
        super.onNewIntent(intent);

    }

    public class ResizeAnimation extends Animation {
        final int targetHeight;
        View view;
        int startHeight;

        public ResizeAnimation(View view, int targetHeight, int startHeight) {
            this.view = view;
            this.targetHeight = targetHeight;
            this.startHeight = startHeight;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            //int newHeight = (int) (startHeight + targetHeight * interpolatedTime);
            int newHeight = (int) (startHeight+(targetHeight - startHeight) * interpolatedTime);
            view.getLayoutParams().height = newHeight;
            view.requestLayout();
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }


}
