package grodrich7.tfg.Activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ceylonlabs.imageviewpopup.ImagePopup;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import grodrich7.tfg.R;

public class ViewUserActivity extends HelperActivity implements OnMapReadyCallback {

    private MapFragment mapFragment;
    private GoogleMap googleMap;
    private ImageButton fullBtn;

    private double randomLatitudes[] = {
            41.392379,//Ronda Litoral
            41.386377,//Placa Universitat
            41.388732 //Everis
    };
    private double randomLongitudes[] = {
            2.202206, //Ronda Litoral
            2.164178,//Placa Universitat,
            2.128944 //Everis
    };

    private String randomDestinations[] = {
            "Premià de Dalt",
            "Andorra",
            "Mataró"
    };

    private LatLng currentLocation;

    private TextView drivingData;
    private TextView destinationData;
    private TextView startTimeData;
    private TextView acceptCallsData;
    private TextView parkingData;
    private RecyclerView recyclerView;

    int geoIndex = 0;
    boolean full;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        putData();
    }

    protected void getViewsByXML() {
        setContentView(R.layout.activity_view_user_activiy);
        createMapFragment();
        enableToolbar("Gabriel");
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

        drivingData = findViewById(R.id.drivingData);
        destinationData = findViewById(R.id.destinationData);
        startTimeData = findViewById(R.id.startTimeData);
        acceptCallsData = findViewById(R.id.acceptCallsData);
        parkingData = findViewById(R.id.parkingData);
        recyclerImages();
    }

    private void recyclerImages(){
        // Initialize a new String array
        final String[] animals = {
                "Aardvark",
                "Albatross",
                "Alligator",
                "Alpaca",
                "Ant",
                "Anteater",
        };

        // Intilize an array list from array
        final List<String> animalsList = new ArrayList(Arrays.asList(animals));

        recyclerView = findViewById(R.id.recyclerImages);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);


        RecyclerView.Adapter groupsAdapter = new RecyclerView.Adapter<CustomViewHolder>() {
            @Override
            public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_square
                        , viewGroup, false);
                return new CustomViewHolder(view);
            }

            @Override
            public void onBindViewHolder(CustomViewHolder viewHolder, int i) {
                viewHolder.description.setText("Imagen " + String.valueOf(i+1));
                try{
                    viewHolder.imageButton.setImageResource(R.drawable.front_image);
                    final ImagePopup imagePopup = new ImagePopup(ViewUserActivity.this);
                    imagePopup.setFullScreen(true); // Optional
                    imagePopup.setBackgroundColor(getResources().getColor(R.color.transparent));
                    imagePopup.setImageOnClickClose(true);  // Optional

                    imagePopup.initiatePopup(viewHolder.imageButton.getDrawable());
                    viewHolder.imageButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            /** Initiate Popup view **/
                            imagePopup.viewPopup();
                        }
                    });
                }catch (Exception ex){
                    Log.e("REC_IMAGES", ex.getMessage());
                }
            }

            @Override
            public int getItemCount() {
                return animalsList.size();
            }
        };
        recyclerView.setAdapter(groupsAdapter);

    }

    private class CustomViewHolder extends RecyclerView.ViewHolder {

        private TextView description;
        private ImageView imageButton;

        public CustomViewHolder(View itemView) {
            super(itemView);
            imageButton = itemView.findViewById(R.id.image);
            description = itemView.findViewById(R.id.description);
        }
    }

    private void putData(){
        boolean randomBoolean = getRandomBoolean();
        int randomColor = randomBoolean ? Color.GREEN : Color.RED;
        drivingData.setText(parseString(randomBoolean));
        drivingData.setTextColor(randomColor);

        int randomNum = randBetween(0, 2);
        destinationData.setText(randomDestinations[randomNum]);

        int hour = randBetween(0, 23);
        int min = randBetween(0, 59);
        startTimeData.setText(String.format("%02d", hour) + ":" + String.format("%02d", min));

        randomBoolean = getRandomBoolean();
        randomColor = randomBoolean ? Color.GREEN : Color.RED;
        acceptCallsData.setText(parseString(randomBoolean));
        acceptCallsData.setTextColor(randomColor);

        randomBoolean = getRandomBoolean();
        randomColor = randomBoolean ? Color.GREEN : Color.RED;
        parkingData.setText(parseString(randomBoolean));
        parkingData.setTextColor(randomColor);

    }

    public DisplayMetrics getMetrics(){
        return getApplicationContext().getResources().getDisplayMetrics();
    }

    //region Random

    public static boolean getRandomBoolean() {
        return Math.random() < 0.5;
    }

    public static int randBetween(int start, int end) {
        return start + (int)Math.round(Math.random() * (end - start));
    }
    //endregion

    //region ToolbarSettings
    @Override
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
                putData();
                break;
            case android.R.id.home:
                onBackPressed();
            default:
                break;
        }

        return true;
    }
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
        currentLocation = getRandomLocation();
        googleMap.addMarker(new MarkerOptions()
                .position(currentLocation)
                .title("Marker"));
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.moveCamera(getCameraPosition());
        googleMap.setTrafficEnabled(true);
        this.googleMap = googleMap;
    }

    public LatLng getRandomLocation(){
        double lat;
        double lon;
        try{
            lat = randomLatitudes[geoIndex];
            lon = randomLongitudes[geoIndex];
            return new LatLng(lat, lon);
        }catch(ArrayIndexOutOfBoundsException ex){
            geoIndex = 0;
            lat = randomLatitudes[geoIndex];
            lon = randomLongitudes[geoIndex];
            return new LatLng(lat, lon);
        }finally {
            geoIndex++;
        }
    }

    public void updateLocation(){
        currentLocation = getRandomLocation();
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Marker"));
        googleMap.moveCamera(getCameraPosition());
    }

    public CameraUpdate getCameraPosition(){
        return CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(currentLocation).zoom(14.0f).build());
    }
    //endregion

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
